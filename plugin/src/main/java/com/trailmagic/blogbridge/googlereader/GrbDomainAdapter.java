package com.trailmagic.blogbridge.googlereader;

import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.utils.DomainAdapter;
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
    private GoogleReaderBridge bridge;

    @Autowired
    public GrbDomainAdapter(GoogleReaderBridge bridge) {
        this.bridge = bridge;
    }

    @Override
    public void articleAdded(IFeed feed, IArticle article) {
        try {
            bridge.retrieveAndUpdateArticleReadStatus(feed, article);
        } catch (Throwable e) {
            log.warn("Error handling article added event", e);
        }
    }

    @Override
    public void propertyChanged(IArticle iArticle, String property, Object oldValue, Object newValue) {
        try {
            if (IArticle.PROP_READ.equals(property)) {
                bridge.updateGoogleReadStatus(iArticle, (Boolean) newValue);
            }
        } catch (Throwable e) {
            log.warn("Error handling article property changed event", e);
        }
    }
}
