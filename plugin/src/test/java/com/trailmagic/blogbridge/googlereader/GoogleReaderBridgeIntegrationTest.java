package com.trailmagic.blogbridge.googlereader;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by: oliver on Date: Dec 19, 2009 Time: 12:25:25 AM
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:com/trailmagic/blogbridge/googlereader/applicationContext.xml")
public class GoogleReaderBridgeIntegrationTest {
    @Autowired
    private GoogleReaderBridge bridge;

    @Test @Ignore
    public void testWiring() {
        bridge.loadReadStatuses();
    }
}
