package com.trailmagic.googlereader;

/**
 * Created by: oliver on Date: Dec 14, 2009 Time: 8:34:01 PM
 */
public class FeedProcessingFailedException extends RuntimeException {
    public FeedProcessingFailedException(String feedUrl, Exception e) {
        super(feedUrl, e);
    }
}
