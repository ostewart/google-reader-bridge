package com.trailmagic.googlereader;

import org.apache.http.HttpStatus;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by: oliver on Date: Dec 10, 2009 Time: 11:51:22 PM
 */
@Service
public class GoogleReaderClient {
    private static final String FEED_BASE = "http://www.google.com/reader/atom/feed/";
    private static final String READ_TAG = "user/-/state/com.google/read";
    private static final String RECENTLY_READ_URL =
            "http://www.google.com/reader/atom/user/-/state/com.google/read";
    private static final String EDIT_READ_TAG_URL =
            "http://www.google.com/reader/api/0/edit-tag?client=greader-blogbridge-bridge/0.0.1-SNAPSHOT";
    private static Logger log = LoggerFactory.getLogger(GoogleReaderClient.class);

    private GoogleClientLogin clientLogin;
    private Map<String, String> linkToGoogleIdMap = new HashMap<String, String>();
    private Map<String, Boolean> googleIdToReadStatusMap = new HashMap<String, Boolean>();
    private String readerToken;
    private GoogleReaderTokenClient tokenClient;
    private GoogleFeedArticleLinksProcessor feedArticleLinksProcessor;
    private Map<String, Date> seenFeeds = new HashMap<String, Date>();
    private GoogleFeedProcessor feedProcessor;
    private GoogleReadStatusProcessor readStatusProcessor;

    @Autowired
    public GoogleReaderClient(GoogleClientLogin clientLogin, GoogleReaderTokenClient tokenClient,
                              GoogleFeedArticleLinksProcessor feedArticleLinksProcessor,
                              GoogleFeedProcessor feedProcessor, GoogleReadStatusProcessor readStatusProcessor) {
        this.clientLogin = clientLogin;
        this.tokenClient = tokenClient;
        this.feedArticleLinksProcessor = feedArticleLinksProcessor;
        this.feedProcessor = feedProcessor;
        this.readStatusProcessor = readStatusProcessor;
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
        feedProcessor.setCookieWhenNotTesting(authCookie);
    }


    @SuppressWarnings({"unchecked"})
    public void loadReadStatuses(int numArticles) throws GoogleReaderCommunicationException {
        try {
            Map<String, Boolean> newStatuses =
                    feedProcessor.processFeed(RECENTLY_READ_URL + "?n=" + numArticles, readStatusProcessor);
            for (String googleId : newStatuses.keySet()) {
                googleIdToReadStatusMap.put(googleId, newStatuses.get(googleId));
            }
            log.info("Loaded {} read articles from Google Reader", newStatuses.size());
        } catch (FeedProcessingFailedException e) {
            log.warn("Loading read articles failed: {}", e.getMessage());
        }
    }

    public void loadFeedArticleLinksIfNecessary(String feedUrl, int numArticles) {
        if (!seenFeeds.containsKey(feedUrl)) {
            loadFeedArticleLinks(feedUrl, numArticles);
        }
    }

    public Map<String, String> loadFeedArticleLinks(final String feedUrl, int articlesCount) {
        Map<String, String> mappings =
                feedProcessor.processFeed(FEED_BASE + urlEncode(feedUrl) + "?n=" + articlesCount,
                                          feedArticleLinksProcessor);
        logMappings(mappings);
        linkToGoogleIdMap.putAll(mappings);
        seenFeeds.put(feedUrl, new Date());
        return mappings;
    }

    private String urlEncode(String feedUrl) {
        try {
            return URLEncoder.encode(feedUrl, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Unsupported encoding (UTF-8)");
        }
    }

    private void logMappings(Map<String, String> mappings) {
        if (log.isDebugEnabled()) {
            for (String key : mappings.keySet()) {
                log.debug("Adding mapping: {} -> {}", key, mappings.get(key));
            }
        }
    }

    String googleUrl(String feedUrl) {
        return linkToGoogleIdMap.get(feedUrl);
    }

    public boolean isRead(String link) {
        String googleId = linkToGoogleIdMap.get(link);
        return googleIdToReadStatusMap.containsKey(googleId) && googleIdToReadStatusMap.get(googleId);
    }

    public void markArticleAsRead(String originalId) {
        if (!linkToGoogleIdMap.containsKey(originalId)) {
            log.debug("Ignoring feed unknown to google: {}", originalId);
            return;
        }

        String googleId = linkToGoogleIdMap.get(originalId);
        Map<String, String> params = new HashMap<String, String>();
        params.put("i", googleId);
        params.put("ac", "edit");
        params.put("a", READ_TAG);
        params.put("T", readerToken);


        int statusCode = feedProcessor.post(EDIT_READ_TAG_URL, params);
        if (statusCode != HttpStatus.SC_OK && log.isDebugEnabled()) {
            log.debug("Failed to set read status on article: {} (google id: {}); status code was : {}",
                      new Object[]{originalId, googleId, statusCode});
        } else {
            log.debug("Set read status on article: {} (google id: {})", originalId, googleId);
        }
    }

    void addFeedMapping(String link, String googleId) {
        linkToGoogleIdMap.put(link, googleId);
    }

}
