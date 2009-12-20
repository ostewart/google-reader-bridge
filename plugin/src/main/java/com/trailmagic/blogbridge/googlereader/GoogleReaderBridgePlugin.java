package com.trailmagic.blogbridge.googlereader;

import com.salas.bb.core.GlobalController;
import com.salas.bb.plugins.domain.ICodePlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Map;

/**
 * Created by: oliver on Date: Dec 10, 2009 Time: 10:55:47 PM
 */
public class GoogleReaderBridgePlugin implements ICodePlugin {
    private static final Logger log = LoggerFactory.getLogger(GoogleReaderBridgePlugin.class);
    private String googleUser;

    public void setPackageLoader(ClassLoader classLoader) {
    }

    public void setParameters(Map<String, String> stringStringMap) {
        googleUser = stringStringMap.get("googlereader.user");
    }

    public String getTypeName() {
        return null;
    }

    public void initialize() {
        log.info("Google Reader Bridge Plugin initializing");
        ApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:com/trailmagic/blogbridge/googlereader/applicationContext.xml");

        try {
            GlobalController globalController = GlobalController.SINGLETON;
            globalController.addDomainListener(ctx.getBean("grbDomainAdapter", GrbDomainAdapter.class));
            globalController.addControllerListener(ctx.getBean("grbControllerAdapter", GrbControllerAdapter.class));

        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("Google Reader Bridge Plugin finished initialization");
    }
}
