package com.trailmagic.googlereader;

import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by: oliver on Date: Dec 18, 2009 Time: 1:37:05 AM
 */
@Service
public class SchemeRegistryConfigurer {
    @Autowired
    public SchemeRegistryConfigurer(SchemeRegistry schemeRegistry) {
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
    }
}
