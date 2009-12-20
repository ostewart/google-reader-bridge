package com.trailmagic.blogbridge.googlereader;

import com.salas.bb.core.ControllerAdapter;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.NetworkFeed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by: oliver on Date: Dec 18, 2009 Time: 1:49:07 AM
 */
@Service
public class GrbControllerAdapter extends ControllerAdapter {
    private static final Logger log = LoggerFactory.getLogger(GrbControllerAdapter.class);
    private GoogleReaderBridge bridge;

    @Autowired
    public GrbControllerAdapter(GoogleReaderBridge bridge) {
        this.bridge = bridge;
    }

    @Override
    public void feedSelected(IFeed feed) {
        try {
            if (feed == null || !(feed instanceof NetworkFeed)) return;

            bridge.loadFeedReadStatuses((NetworkFeed) feed);
        } catch (Throwable e) {
            log.warn("Error handling feed selected event", e);
        }
    }

}
