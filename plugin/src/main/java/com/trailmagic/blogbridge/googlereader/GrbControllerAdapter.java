package com.trailmagic.blogbridge.googlereader;

import com.salas.bb.core.ControllerAdapter;
import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.NetworkFeed;
import com.trailmagic.googlereader.GoogleReaderClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URL;

/**
 * Created by: oliver on Date: Dec 18, 2009 Time: 1:49:07 AM
 */
@Service
public class GrbControllerAdapter extends ControllerAdapter {
    private static final Logger log = LoggerFactory.getLogger(GrbControllerAdapter.class);
    private GoogleReaderClient client;

    @Autowired
    public GrbControllerAdapter(GoogleReaderClient client) {
        this.client = client;
    }

    @Override
    public void feedSelected(IFeed feed) {
        if (feed == null) return;
        URL xmlURL = ((NetworkFeed) feed).getXmlURL();
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
    }
}
