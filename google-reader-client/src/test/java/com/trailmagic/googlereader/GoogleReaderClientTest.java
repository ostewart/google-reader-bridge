package com.trailmagic.googlereader;

import com.trailmagic.googlereader.http.HttpFactory;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Created by: oliver on Date: Dec 13, 2009 Time: 2:58:03 PM
 */
public class GoogleReaderClientTest {
    private GoogleReaderClient client;
    @Mock private HttpClient httpClient;
    @Mock private GoogleClientLogin clientLogin;
    @Mock private HttpFactory httpFactory;
    @Mock private GoogleReaderTokenClient tokenClient;
    @Mock private HttpResponse response;
    @Mock private HttpPost post;
    @Mock private StatusLine statusLine;
    @Mock private HttpGet get;
    @Mock private HttpEntity entity;
    @Mock private GoogleFeedArticleLinksProcessor feedArticleLinksProcessor;
    @Mock private GoogleFeedProcessor feedProcessor;
    @Mock private GoogleReadStatusProcessor readStatusProcessor;
    private static final String ARTICLE_GOOGLE_ID = "tag:google.com,2005:reader/item/e5636a3f87610bd2";
    private static final String READ_ARTICLE_GOOGLE_ID = "tag:google.com,2005:reader/item/ebed01cf7178605b";
    public static final String READER_TOKEN = "a3NqiiUBAAA.nd2QOvjmYxMXL1rd_t5LAw.kvfdwaucRm3nrVuWM7Ddyg";
    private static final String ARTICLE_ORIGINAL_ID = "tag:daringfireball.net,2009:/linked//6.18543";
    private static final String TEST_FEED = "classpath:com/trailmagic/googlereader/testfeed.xml";
    private static final String ARTICLE_LINK = "http://online.wsj.com/article/SB10001424052748703757404574592530591075444.html";
    private static final String FEED_URL = "http://example.com/stuff.xml";
    private static final String FEED_URL_ENCODED = "http%3A%2F%2Fexample.com%2Fstuff.xml";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);


        when(tokenClient.getReaderToken()).thenReturn(READER_TOKEN);
        client = new GoogleReaderClient(clientLogin, tokenClient, feedArticleLinksProcessor, feedProcessor, readStatusProcessor);
    }

    private String loadContentFromResource(String location) throws IOException {
        DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource(location);
        InputStream contentInputStream = resource.getInputStream();
        contentInputStream.mark(Integer.MAX_VALUE);
        String stringContent = IOUtils.toString(contentInputStream);
        contentInputStream.reset();
        return stringContent;
    }

    @Test
    public void testLoadsFeedUrlMappings() throws Exception {
        Map<String, String> testMappings = new HashMap<String, String>();
        testMappings.put(ARTICLE_LINK, ARTICLE_GOOGLE_ID);
        when(feedProcessor.processFeed(Mockito.anyString(), Mockito.any(GoogleFeedArticleLinksProcessor.class)))
                .thenReturn(testMappings);

        Map<String, String> mappings = client.loadFeedArticleLinks(FEED_URL, 100);

        Mockito.verify(feedProcessor).processFeed(Mockito.endsWith(FEED_URL_ENCODED + "?n=100"), Mockito.same(feedArticleLinksProcessor));

        assertEquals(ARTICLE_GOOGLE_ID, mappings.get(ARTICLE_LINK));
        assertEquals(ARTICLE_GOOGLE_ID, client.googleUrl(ARTICLE_LINK));

        assertEquals(1, mappings.size());
    }


    @Test
    public void testComposesMarkReadRequest() throws IOException, UnsuccessfulLoginException {
        client.addFeedMapping(ARTICLE_ORIGINAL_ID, ARTICLE_GOOGLE_ID);

        when(tokenClient.getReaderToken()).thenReturn(READER_TOKEN);
        client.init();

        client.markArticleAsRead(ARTICLE_ORIGINAL_ID);


        Map<String, String> params = new HashMap<String, String>();
        params.put("i", ARTICLE_GOOGLE_ID);
        params.put("ac", "edit");
        params.put("a", "user/-/state/com.google/read");
        params.put("T", READER_TOKEN);

        Mockito.verify(feedProcessor, Mockito.times(1)).post(Mockito.anyString(), Mockito.eq(params));
    }

    @Test
    public void testDoesntMarkReadInGoogleWhenNoKeyMapping() throws IOException, UnsuccessfulLoginException {
        client.markArticleAsRead(ARTICLE_ORIGINAL_ID);

        Mockito.verify(feedProcessor, Mockito.never()).post(Mockito.anyString(),
                                                            Mockito.<Map<String, String>>anyObject());
    }

    @Test
    public void testLoadsAndReturnsReadStatus() throws IOException, JDOMException {
        Map<String, Boolean> mappings = new HashMap<String, Boolean>();
        mappings.put(READ_ARTICLE_GOOGLE_ID, Boolean.TRUE);

        client.addFeedMapping(ARTICLE_LINK, READ_ARTICLE_GOOGLE_ID);

        when(feedProcessor.processFeed(Mockito.endsWith("?n=10"), Mockito.eq(readStatusProcessor))).thenReturn(mappings);
        client.loadReadStatuses(10);

        assertTrue(client.isRead(ARTICLE_LINK));
        assertFalse(client.isRead("some nonexistent key"));
    }

    @Test
    public void testFeedArticleLinksProcessor() throws Exception {
        GoogleFeedArticleLinksProcessor processor = new GoogleFeedArticleLinksProcessor();
        Map<String, String> map = processor.process(new StringReader(loadContentFromResource(TEST_FEED)));
        System.out.println("map size: " + map.size());
        assertEquals("tag:google.com,2005:reader/item/e994f511d3c6e533", map.get("http://rogerebert.suntimes.com/apps/pbcs.dll/article?AID=/20091211/REVIEWS/912119998"));
    }

    public void testLoadsFeedStatusesOnce() {

    }
}
