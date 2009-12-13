package com.trailmagic.googlereader;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;


/**
 * Created by: oliver on Date: Dec 10, 2009 Time: 11:51:22 PM
 */
@Service
public class GoogleReaderClient {
    private static final String SUBSCRIPTIONS_URL =
            "http://www.google.com/reader/api/0/subscription/list?output=json";
    private Logger log = LoggerFactory.getLogger(GoogleReaderClient.class);
    private static final String READ_TAG = "user/-/state/com.google/read";
    private static final String RECENTLY_READ_URL =
            "http://www.google.com/reader/atom/user/-/state/com.google/read";
    private static final String FEED_BASE = "http://www.google.com/reader/atom/feed/";
    private DefaultHttpClient httpClient;
    private GoogleClientLogin clientLogin;

    @Autowired
    public GoogleReaderClient(DefaultHttpClient httpClient, GoogleClientLogin clientLogin) {
        this.httpClient = httpClient;
        this.clientLogin = clientLogin;
    }

    public void init() throws IOException {
        try {
            addGoogleCookie("SID", clientLogin.getAuthToken());
            addGoogleCookie("T", getReaderToken());
        } catch (UnsuccessfulLoginException e) {
            throw new IllegalStateException("Couldn't get an auth token");
        }

    }

    private void addGoogleCookie(String name, String value) {
        BasicClientCookie authCookie = new BasicClientCookie(name, value);
        authCookie.setDomain(".google.com");
        authCookie.setPath("/");
        httpClient.getCookieStore().addCookie(authCookie);
    }

    public void loadSubscriptions() throws IOException {
        HttpGet get = new HttpGet(SUBSCRIPTIONS_URL);
        HttpResponse response = httpClient.execute(get);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        SubscriptionsResponse subscriptionsResponse = mapper.readValue(response.getEntity().getContent(), SubscriptionsResponse.class);

        for (Subscription subscription : subscriptionsResponse.getSubscriptions()) {
            log.info(subscription.getTitle());
        }
    }

    public Map<String,String> getFeedArticleIds(String feedUrl) throws IOException {
        HttpGet get = new HttpGet(FEED_BASE + feedUrl);
        get.getParams().setParameter("output", "json");
        HttpResponse response = httpClient.execute(get);
        log.debug("Feed response: {}", EntityUtils.toString(response.getEntity()));
        return null;
    }

    public String getReaderToken() throws IOException {
        HttpGet tokenGet = new HttpGet("http://www.google.com/reader/api/0/token");
        HttpResponse response = httpClient.execute(tokenGet);
        String token = EntityUtils.toString(response.getEntity());
        log.debug("Got token: " + token);
        return token;
    }


    public static void main(String[] args) {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:com/trailmagic/googlereader/applicationContext.xml");
        GoogleReaderClient client = ctx.getBean("googleReaderClient", GoogleReaderClient.class);

        try {
            client.init();
            client.loadSubscriptions();
            client.getFeedArticleIds("http://daringfireball.net/index.xml");


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
