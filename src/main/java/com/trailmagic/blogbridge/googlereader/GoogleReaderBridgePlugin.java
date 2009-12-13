package com.trailmagic.blogbridge.googlereader;

import com.salas.bb.core.GlobalController;
import com.salas.bb.plugins.domain.ICodePlugin;

import java.util.Map;

/**
 * Created by: oliver on Date: Dec 10, 2009 Time: 10:55:47 PM
 */
public class GoogleReaderBridgePlugin implements ICodePlugin {
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
        GrbDomainAdapter adapter = new GrbDomainAdapter();
        GlobalController.SINGLETON.addDomainListener(adapter);
    }
}
