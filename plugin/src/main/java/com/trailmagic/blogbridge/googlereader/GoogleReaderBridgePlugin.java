package com.trailmagic.blogbridge.googlereader;

import com.salas.bb.core.GlobalController;
import com.salas.bb.plugins.domain.ICodePlugin;
import com.trailmagic.googlereader.GoogleReaderClient;
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
        ApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:com/trailmagic/googlereader/applicationContext.xml");
        GoogleReaderClient client = ctx.getBean("googleReaderClient", GoogleReaderClient.class);

        try {
            client.init();
            client.loadReadStatuses(1000);

            Map<String, String> linkMap = client.fetchFeedArticleLinks("http://daringfireball.net/index.xml");
            for (String orig : linkMap.keySet()) {
                log.info("Found Mapping: {} : {}", orig, linkMap.get(orig));
            }


            GrbDomainAdapter adapter = new GrbDomainAdapter(client);
            GrbControllerAdapter controllerAdapter = new GrbControllerAdapter(client);
            GlobalController globalController = GlobalController.SINGLETON;
            globalController.addDomainListener(adapter);
            globalController.addControllerListener(controllerAdapter);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
