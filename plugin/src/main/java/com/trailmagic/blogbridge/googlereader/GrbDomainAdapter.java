package com.trailmagic.blogbridge.googlereader;

import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.NetworkFeed;
import com.salas.bb.domain.utils.DomainAdapter;
import com.trailmagic.googlereader.GoogleReaderClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by: oliver on Date: Dec 10, 2009 Time: 11:30:33 PM
 */
@Service
public class GrbDomainAdapter extends DomainAdapter {
    private static final Logger log = LoggerFactory.getLogger(GrbDomainAdapter.class);
    private GoogleReaderClient client;

    @Autowired
    public GrbDomainAdapter(GoogleReaderClient client) {
        this.client = client;
    }

    @Override
    public void articleAdded(IFeed feed, IArticle article) {
        if (feed instanceof NetworkFeed) {
            client.fetchFeedArticleLinks(feedLink(feed));
        }
        if (client.isRead(article.getLink().toString())) {
            if (log.isDebugEnabled()) {
                log.debug("Processing read article: {}" + article.getLink().toString());
            }
            article.setRead(true);
        }
    }

    private String feedLink(IFeed feed) {
        return ((NetworkFeed) feed).getXmlURL().toString();
    }

    @Override
    public void propertyChanged(IArticle iArticle, String property, Object oldValue, Object newValue) {
        super.propertyChanged(iArticle, property, oldValue, newValue);

        client.processFeedIfNecessary(feedLink(iArticle.getFeed()));
        

        String link = iArticle.getLink().toString();
        if (IArticle.PROP_READ.equals(property) && !client.isRead(link)) {
            if (log.isDebugEnabled()) {
                log.debug("Processing read property change on article {}, old value: {}, new value: {}",
                          new Object[] {link, oldValue, newValue});
            }
            client.markArticleAsRead(link);
        }
    }
}
