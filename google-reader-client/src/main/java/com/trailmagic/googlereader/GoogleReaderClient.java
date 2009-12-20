package com.trailmagic.googlereader;

import com.trailmagic.googlereader.http.EntityContentProcessor;
import com.trailmagic.googlereader.http.HttpFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by: oliver on Date: Dec 10, 2009 Time: 11:51:22 PM
 */
@Service
public class GoogleReaderClient {
    private static final String SUBSCRIPTIONS_URL =
            "http://www.google.com/reader/api/0/subscription/list?output=json";
    private static Logger log = LoggerFactory.getLogger(GoogleReaderClient.class);
    private static final String READ_TAG = "user/-/state/com.google/read";
    private static final String RECENTLY_READ_URL =
            "http://www.google.com/reader/atom/user/-/state/com.google/read";
    private static final String EDIT_READ_TAG_URL =
            "http://www.google.com/reader/api/0/edit-tag?client=greader-blogbridge-bridge/0.0.1-SNAPSHOT";
    private static final String FEED_BASE = "http://www.google.com/reader/atom/feed/";
    private HttpClient httpClient;
    private GoogleClientLogin clientLogin;
    private Map<String, String> originalToGoogleIdMap = new HashMap<String, String>();
    private Map<String, Boolean> googleIdToReadStatusMap = new HashMap<String, Boolean>();
    private String readerToken;
    private HttpFactory httpFactory;
    private GoogleReaderTokenClient tokenClient;
    private Map<String, Date> seenFeeds = new HashMap<String, Date>();

    @Autowired
    public GoogleReaderClient(HttpClient httpClient, GoogleClientLogin clientLogin, HttpFactory httpFactory, GoogleReaderTokenClient tokenClient) {
        this.httpClient = httpClient;
        this.clientLogin = clientLogin;
        this.httpFactory = httpFactory;
        this.tokenClient = tokenClient;
    }

    @PostConstruct
    public void init() throws IOException {
        try {
            addGoogleCookie("SID", clientLogin.getSidToken());
            readerToken = tokenClient.getReaderToken();
            addGoogleCookie("T", readerToken);
        } catch (UnsuccessfulLoginException e) {
            throw new IllegalStateException("Couldn't get an auth token");
        }

    }

    private void addGoogleCookie(String name, String value) {
        BasicClientCookie authCookie = new BasicClientCookie(name, value);
        authCookie.setDomain(".google.com");
        authCookie.setPath("/");
        setCookieWhenNotTesting(authCookie);
    }

    private void setCookieWhenNotTesting(BasicClientCookie authCookie) {
        if (httpClient instanceof DefaultHttpClient) {
            ((DefaultHttpClient) httpClient).getCookieStore().addCookie(authCookie);
        } else {
            log.warn("Not setting cookie {}; httpClient is not a DefaultHttpClient", authCookie);
        }
    }

    public void loadSubscriptions() throws IOException {
        HttpGet get = new HttpGet(SUBSCRIPTIONS_URL);
        HttpResponse response = httpClient.execute(get);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        HttpEntity httpEntity = response.getEntity();
        SubscriptionsResponse subscriptionsResponse = mapper.readValue(httpEntity.getContent(), SubscriptionsResponse.class);
        consumeContent(response);

        for (Subscription subscription : subscriptionsResponse.getSubscriptions()) {
            log.info(subscription.getTitle());
        }
    }

    public void markArticleAsRead(String originalId) {
        if (!originalToGoogleIdMap.containsKey(originalId)) {
            log.debug("Ignoring feed unknown to google: {}", originalId);
            return;
        }

        String googleId = originalToGoogleIdMap.get(originalId);
        HttpPost post = httpFactory.post(EDIT_READ_TAG_URL);
        BasicNameValuePair[] bodyParams = new BasicNameValuePair[]{
                new BasicNameValuePair("i", googleId),
                new BasicNameValuePair("ac", "edit"),
                new BasicNameValuePair("a", READ_TAG),
                new BasicNameValuePair("T", readerToken)
        };

        post.setEntity(httpFactory.urlEncodedFormEntity(Arrays.asList(bodyParams)));

        try {
            HttpResponse response = httpClient.execute(post);
            consumeContent(response);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                if (log.isDebugEnabled()) {
                    log.debug("Failed to set read status on article: {} (google id: {}); response body said: {}",
                              new Object[]{originalId, googleId, EntityUtils.toString(response.getEntity())});
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Set read status on article: {} (google id: {})", originalId, googleId);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void consumeContent(HttpResponse response) throws IOException {
        HttpEntity httpEntity = response.getEntity();
        if (httpEntity != null) {
            httpEntity.consumeContent();
        }
    }

    @SuppressWarnings({"unchecked"})
    public void loadReadStatuses(int numArticles) throws GoogleReaderCommunicationException {
        int newUnreads = 0;
        try {
            HttpGet get = httpFactory.get(RECENTLY_READ_URL + "?n=" + numArticles);
            HttpResponse response = httpClient.execute(get);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                SAXBuilder builder = new SAXBuilder();
                Document doc = builder.build(response.getEntity().getContent());
                XPath xPath = atomXPath("//a:feed/a:entry");
                for (Element entry : (List<Element>) xPath.selectNodes(doc)) {
                    Text googleId = (Text) atomXPath("a:id/text()").selectSingleNode(entry);
                    if (googleId != null && !googleIdToReadStatusMap.containsKey(googleId.getValue())) {
                        googleIdToReadStatusMap.put(googleId.getValue(), Boolean.TRUE);
                        newUnreads++;
                    }
                }
            } else if (log.isWarnEnabled()) {
                log.warn("Failed to load read statuses. Response body said: {}",
                         EntityUtils.toString(response.getEntity()));
            }
            consumeContent(response);
            log.info("Loaded {} read articles from Google Reader", newUnreads);
        } catch (IOException e) {
            throw new GoogleReaderCommunicationException(e);
        } catch (JDOMException e) {
            throw new GoogleReaderCommunicationException(e);
        }
    }

    public boolean isRead(String originalId) {
        String googleId = originalToGoogleIdMap.get(originalId);
        return googleIdToReadStatusMap.containsKey(googleId) && googleIdToReadStatusMap.get(googleId);
    }

    public Map<String, String> getFeedArticleIds(final String feedUrl) throws IOException, JDOMException {
        return processFeed(feedUrl, new EntityContentProcessor<Map<String, String>>() {
            @Override
            public Map<String, String> process(Reader content) throws Exception {
                Map<String, String> mappings = extractIdMapping(content);
                log.debug("Found {} mappings for feed: {}", mappings.size(), feedUrl);
                return mappings;
            }
        });
    }

    public Map<String, String> fetchFeedArticleLinks(final String feedUrl) {
        Map<String, String> mappings = processFeed(feedUrl, new GoogleReaderClient.FeedArticleLinksProcessor());
        if (log.isDebugEnabled()) {
            for (String key : mappings.keySet()) {
                log.debug("Adding mapping: {} -> {}", key, mappings.get(key));
            }
        }
        originalToGoogleIdMap.putAll(mappings);
        seenFeeds.put(feedUrl, new Date());
        return mappings;
    }

    public <T> T processFeed(String feedUrl, EntityContentProcessor<T> processor) {
        HttpGet get = new HttpGet(FEED_BASE + feedUrl);
        log.debug("Fetching Google feed: {}", feedUrl);
        try {
            HttpResponse response = httpClient.execute(get);

            log.debug("Got response for feed: {}, processing content", feedUrl);
            HttpEntity httpEntity = response.getEntity();
            InputStream content = httpEntity.getContent();
            try {
                InputStreamReader contentReader = new InputStreamReader(content);
                try {
                    T result = processor.process(contentReader);
                    log.debug("Finished processing content for feed: {}", feedUrl);
                    return result;
                } finally {
                    contentReader.close();
                }
            } finally {
                content.close();
                consumeContent(response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new FeedProcessingFailedException(feedUrl, e);
        }
    }

    void addFeedMapping(String originalId, String googleId) {
        originalToGoogleIdMap.put(originalId, googleId);
    }

    @SuppressWarnings({"unchecked"})
    Map<String, String> extractIdMapping(Reader content) throws JDOMException, IOException {
        Map<String, String> mappings = new HashMap<String, String>();
        SAXBuilder builder = new SAXBuilder();
        builder.setValidation(false);
        Document doc = builder.build(content);

        XPath xPath = atomXPath("//a:feed/a:entry");
        for (Element entry : (List<Element>) xPath.selectNodes(doc)) {
            Attribute originalId = (Attribute) atomXPath("a:id/@gr:original-id").selectSingleNode(entry);
            Text googleId = (Text) atomXPath("a:id/text()").selectSingleNode(entry);
            if (originalId != null && googleId != null) {
                mappings.put(originalId.getValue(), googleId.getText());
            }
        }
        return mappings;
    }

    private static XPath atomXPath(String path) throws JDOMException {
        XPath xPath = XPath.newInstance(path);
        xPath.addNamespace("a", "http://www.w3.org/2005/Atom");
        xPath.addNamespace("gr", "http://www.google.com/schemas/reader/atom/");
        return xPath;
    }

    public static void main(String[] args) {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:com/trailmagic/googlereader/applicationContext.xml");
        GoogleReaderClient client = ctx.getBean("googleReaderClient", GoogleReaderClient.class);

        try {
            client.init();
            client.loadSubscriptions();
            Map<String, String> map = client.getFeedArticleIds("http://daringfireball.net/index.xml");
            for (String orig : map.keySet()) {
                log.info("Found Mapping: {} : {}", orig, map.get(orig));
            }

            client.loadReadStatuses(100);

            log.info("Found {} read statuses", client.googleIdToReadStatusMap.size());

            Map<String, String> linkMap = client.fetchFeedArticleLinks("http://daringfireball.net/index.xml");
            for (String orig : linkMap.keySet()) {
                log.info("Found Mapping: {} : {}", orig, linkMap.get(orig));
            }


        } catch (IOException e) {
            e.printStackTrace();
        } catch (JDOMException e) {
            e.printStackTrace();
        }
    }

    public void processFeedIfNecessary(String feedUrl) {
        if (!seenFeeds.containsKey(feedUrl)) {
            fetchFeedArticleLinks(feedUrl);
        }
    }

    static class FeedArticleLinksProcessor implements EntityContentProcessor<Map<String, String>> {
        @SuppressWarnings({"unchecked"})
        @Override
        public Map<String, String> process(Reader content) throws Exception {
            Map<String, String> mappings = new HashMap<String, String>();
            SAXBuilder builder = new SAXBuilder();
            builder.setValidation(false);
            Document doc = builder.build(content);

            XPath xPath = atomXPath("//a:feed/a:entry");
            for (Element entry : (List<Element>) xPath.selectNodes(doc)) {
                Attribute linkAttr = (Attribute) atomXPath("a:link[@rel='alternate']/@href").selectSingleNode(entry);
                Text googleId = (Text) atomXPath("a:id/text()").selectSingleNode(entry);
                if (linkAttr != null && googleId != null) {
                    mappings.put(linkAttr.getValue(), googleId.getText());
                }
            }
            return mappings;
        }
    }
}
