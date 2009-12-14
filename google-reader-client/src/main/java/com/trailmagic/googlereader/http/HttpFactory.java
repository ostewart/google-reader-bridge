package com.trailmagic.googlereader.http;

import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by: oliver on Date: Dec 13, 2009 Time: 7:41:26 PM
 */
@Service
public class HttpFactory {
    public HttpPost post(String url) {
        return new HttpPost(url);
    }

    public HttpGet get(String url) {
        return new HttpGet(url);
    }

    public HttpEntity urlEncodedFormEntity(List<BasicNameValuePair> basicNameValuePairs) {
        try {
            return new UrlEncodedFormEntity(basicNameValuePairs, HTTP.UTF_8);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Couldn't initialize HTTP Client", e);
        }
    }
}
