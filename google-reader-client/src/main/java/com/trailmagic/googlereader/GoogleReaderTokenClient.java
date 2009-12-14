package com.trailmagic.googlereader;

import com.trailmagic.googlereader.http.HttpFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Created by: oliver on Date: Dec 13, 2009 Time: 7:49:44 PM
 */
@Service
public class GoogleReaderTokenClient {
    private static final Logger log = LoggerFactory.getLogger(GoogleReaderTokenClient.class);
    private HttpClient httpClient;
    private HttpFactory httpFactory;

    @Autowired
    public GoogleReaderTokenClient(HttpClient httpClient, HttpFactory httpFactory) {
        this.httpClient = httpClient;
        this.httpFactory = httpFactory;
    }

    public String getReaderToken() throws IOException {
        HttpGet tokenGet = httpFactory.get("http://www.google.com/reader/api/0/token");
        HttpResponse response = httpClient.execute(tokenGet);
        String token = EntityUtils.toString(response.getEntity());
        log.debug("Got token: " + token);
        return token;
    }

}
