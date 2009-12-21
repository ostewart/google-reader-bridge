package com.trailmagic.blogbridge.googlereader;

import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.NetworkFeed;
import com.trailmagic.googlereader.GoogleReaderClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.URL;

/**
 * Created by: oliver on Date: Dec 18, 2009 Time: 10:57:43 PM
 */
@Service
public class GoogleReaderBridge {
    private static final Logger log = LoggerFactory.getLogger(GoogleReaderBridge.class);
    private GoogleReaderClient client;

    protected GoogleReaderBridge() {
        // for cglib
    }

    @Autowired
    public GoogleReaderBridge(GoogleReaderClient client) {
        this.client = client;
    }

    @PostConstruct
    public void loadInitialReadStatuses() {
        try {
            client.loadReadStatuses(1000);
        } catch (Exception e) {
            log.warn("Failed to load google read statuses", e);
        }
    }

    @Scheduled(fixedRate = 300000)
    public void loadReadStatuses() {
        try {
            client.loadReadStatuses(100);
        } catch (Exception e) {
            log.warn("Failed to load google read statuses", e);
        }
    }

    @Async
    public void retrieveAndUpdateArticleReadStatus(IFeed feed, IArticle article) {
        try {
            if (feed instanceof NetworkFeed) {
                client.loadFeedArticleLinks(feedLink(feed));
            }
            if (client.isRead(article.getLink().toString())) {
                logSetReadStatus(article);
                article.setRead(true);
            }
        } catch (Exception e) {
            log.warn("Error updating read status for feed", e);
        }
    }

    private void logSetReadStatus(IArticle article) {
        if (log.isDebugEnabled()) {
            log.debug("Setting read status from google: {}" + article.getLink().toString());
        }
    }

    @Async
    public void updateGoogleReadStatus(IArticle article, Boolean newValue) {
        try {
            client.processFeedIfNecessary(feedLink(article.getFeed()));


            String link = article.getLink().toString();
            if (!client.isRead(link)) {
                logPropertyChange(newValue, link);
                client.markArticleAsRead(link);
            }
        } catch (Exception e) {
            log.warn("Error updating google read status", e);
        }

    }

    private void logPropertyChange(Boolean newValue, String link) {
        if (log.isDebugEnabled()) {
            log.debug("Processing read property change on article {}, new value: {}",
                      new Object[]{link, newValue});
        }
    }

    @Async
    public void loadFeedReadStatuses(NetworkFeed feed) {
        try {
            URL xmlURL = feed.getXmlURL();
            if (xmlURL == null) return;
            client.processFeedIfNecessary(xmlURL.toString());

            for (IArticle article : feed.getArticles()) {
                if (client.isRead(article.getLink().toString())) {
                    if (log.isDebugEnabled()) {
                        log.debug("Marking article read in blogbridge: {}" + article.getLink().toString());
                    }
                    article.setRead(true);
                }
            }
        } catch (Exception e) {
            log.warn("Error loading google read statuses", e);
        }
    }


    private String feedLink(IFeed feed) {
        return ((NetworkFeed) feed).getXmlURL().toString();
    }
}
