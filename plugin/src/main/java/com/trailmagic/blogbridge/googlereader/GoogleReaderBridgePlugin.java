package com.trailmagic.blogbridge.googlereader;

import com.salas.bb.core.GlobalController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by: oliver on Date: Dec 10, 2009 Time: 10:55:47 PM
 */
public class GoogleReaderBridgePlugin {
    private static final Logger log = LoggerFactory.getLogger(GoogleReaderBridgePlugin.class);


    @SuppressWarnings({"UnusedDeclaration"})
    public void loadPlugin() {
        log.info("Loading Google Reader plugin...");
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/com/trailmagic/blogbridge/googlereader/applicationContext.xml", getClass());
        GlobalController globalController = GlobalController.SINGLETON;
        globalController.addDomainListener(ctx.getBean("grbDomainAdapter", GrbDomainAdapter.class));
        globalController.addControllerListener(ctx.getBean("grbControllerAdapter", GrbControllerAdapter.class));
        log.info("Finished loading Google Reader plugin");


    }
}
