package com.trailmagic.googlereader;

import com.trailmagic.googlereader.http.HttpFactory;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicNameValuePair;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Created by: oliver on Date: Dec 13, 2009 Time: 2:58:03 PM
 */
public class GoogleReaderClientTest {
    private GoogleReaderClient client;
    private Reader content;
    private InputStream contentInputStream;
    @Mock private HttpClient httpClient;
    @Mock private GoogleClientLogin clientLogin;
    @Mock private HttpFactory httpFactory;
    @Mock private GoogleReaderTokenClient tokenClient;
    @Mock private HttpResponse response;
    @Mock private HttpPost post;
    @Mock private StatusLine statusLine;
    @Mock private HttpGet get;
    @Mock private HttpEntity entity;
    private static final String ARTICLE_GOOGLE_ID = "tag:google.com,2005:reader/item/e5636a3f87610bd2";
    private static final String READ_ARTICLE_GOOGLE_ID = "tag:google.com,2005:reader/item/ebed01cf7178605b";
    private static final String READ_ARTICLE_ORIGINAL_ID = "tag:daringfireball.net,2009:/linked//6.18520";
    public static final String READER_TOKEN = "a3NqiiUBAAA.nd2QOvjmYxMXL1rd_t5LAw.kvfdwaucRm3nrVuWM7Ddyg";
    private static final String ARTICLE_ORIGINAL_ID = "tag:daringfireball.net,2009:/linked//6.18543";
    private static final String TEST_FEED = "classpath:com/trailmagic/googlereader/testfeed.xml";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);


        when(tokenClient.getReaderToken()).thenReturn(READER_TOKEN);
        client = new GoogleReaderClient(httpClient, clientLogin, httpFactory, tokenClient);
    }

    private String loadContentFromResource(String location) throws IOException {
        DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource(location);
        contentInputStream = resource.getInputStream();
        contentInputStream.mark(Integer.MAX_VALUE);
        String stringContent = IOUtils.toString(contentInputStream);
        contentInputStream.reset();
        content = new StringReader(stringContent);
        return stringContent;
    }

    @Test
    public void testExtractsIdsFromGoogleFeed() throws JDOMException, IOException {
        String stringContent = loadContentFromResource(TEST_FEED);

        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build(new StringReader(stringContent));

        Map<String, String> mappings = client.extractIdMapping(content);
        XPath xPath = XPath.newInstance("//a:feed/a:entry");
        xPath.addNamespace("a", "http://www.w3.org/2005/Atom");
        int numEntries = xPath.selectNodes(document).size();
        assertEquals(numEntries, mappings.size());
    }

    @Test
    public void testComposesMarkReadRequest() throws IOException {
        when(httpFactory.post(Mockito.anyString())).thenReturn(post);
        when(httpClient.execute(Mockito.<HttpUriRequest>anyObject())).thenReturn(response);
        when(response.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode()).thenReturn(200);

        client.addFeedMapping(ARTICLE_ORIGINAL_ID, ARTICLE_GOOGLE_ID);

        client.init();
        client.markArticleAsRead(ARTICLE_ORIGINAL_ID);

        BasicNameValuePair[] bodyParams = new BasicNameValuePair[]{
                new BasicNameValuePair("i", ARTICLE_GOOGLE_ID),
                new BasicNameValuePair("ac", "edit"),
                new BasicNameValuePair("a", "user/-/state/com.google/read"),
                new BasicNameValuePair("T", READER_TOKEN)
        };

        Mockito.verify(httpFactory).urlEncodedFormEntity(Arrays.asList(bodyParams));
        Mockito.verify(httpClient, Mockito.times(1)).execute(post);
        Mockito.verify(statusLine, Mockito.times(1)).getStatusCode();
    }

    @Test
    public void testLoadsAndReturnsReadStatus() throws IOException, JDOMException {
        when(httpFactory.get(Mockito.anyString())).thenReturn(get);
        when(httpClient.execute(Mockito.<HttpUriRequest>anyObject())).thenReturn(response);
        when(response.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode()).thenReturn(200);
        when(response.getEntity()).thenReturn(entity);

        loadContentFromResource("classpath:com/trailmagic/googlereader/googlereader-read.xml");


        when(entity.getContent()).thenReturn(contentInputStream);
        client.addFeedMapping(READ_ARTICLE_ORIGINAL_ID, READ_ARTICLE_GOOGLE_ID);
        client.loadReadStatuses(10);

        assertTrue(client.isRead(READ_ARTICLE_ORIGINAL_ID));
    }

    @Test
    public void testFeedArticleLinksProcessor() throws Exception {
        GoogleReaderClient.FeedArticleLinksProcessor processor = new GoogleReaderClient.FeedArticleLinksProcessor();
        Map<String, String> map = processor.process(new StringReader(loadContentFromResource(TEST_FEED)));
        System.out.println("map size: " + map.size());
        assertEquals("tag:google.com,2005:reader/item/e994f511d3c6e533", map.get("http://rogerebert.suntimes.com/apps/pbcs.dll/article?AID=/20091211/REVIEWS/912119998"));
    }

    public void testLoadsFeedStatusesOnce() {
        
    }
}
