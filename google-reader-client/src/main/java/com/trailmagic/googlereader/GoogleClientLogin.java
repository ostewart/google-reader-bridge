package com.trailmagic.googlereader;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by: oliver on Date: Dec 12, 2009 Time: 11:15:27 PM
 */
@Service
public class GoogleClientLogin {
    private DefaultHttpClient httpClient;
    private String googleEmail;
    private String googlePassword;
    private static Logger log = LoggerFactory.getLogger(GoogleClientLogin.class);

    @SuppressWarnings({"SpringJavaAutowiringInspection"})
    @Autowired
    public GoogleClientLogin(DefaultHttpClient httpClient,
                             @Value("#{grprops['google.email']}") String googleEmail,
                             @Value("#{grprops['google.password']}") String googlePassword) {
        this.httpClient = httpClient;
        this.googleEmail = googleEmail;
        this.googlePassword = googlePassword;
    }

    public String getAuthToken() throws UnsuccessfulLoginException {
        HttpPost post = new HttpPost("https://www.google.com/accounts/ClientLogin");

        Map<String, String> variables = new HashMap<String, String>();
        variables.put("Email", googleEmail);
        variables.put("Passwd", googlePassword);
        variables.put("source", "trailmagic-BBReaderBridge-1.0");
        variables.put("accountType", "HOSTED_OR_GOOGLE");
        variables.put("service", "reader");

        try {
            post.setEntity(new UrlEncodedFormEntity(mapToNameValuePairs(variables), HTTP.UTF_8));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Couldn't initialize HTTP Client", e);
        }

        try {
            HttpResponse response = httpClient.execute(post);
            String responseBody = EntityUtils.toString(response.getEntity());
            String authString = getAuthString(responseBody);
            log.debug("Got auth token: {}", authString);
            return authString;
        } catch (IOException e) {
            throw new UnsuccessfulLoginException(e);
        }
    }

    private String getAuthString(String responseBody) {
        Pattern pattern = Pattern.compile("^SID=(.+?)$", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(responseBody);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1);
    }

    private List<NameValuePair> mapToNameValuePairs(Map<String, String> input) {
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        for (String key : input.keySet()) {
            nameValuePairs.add(new BasicNameValuePair(key, input.get(key)));
        }
        return nameValuePairs;
    }
}
