package com.trailmagic.googlereader;

/**
 * Created by: oliver on Date: Dec 19, 2009 Time: 1:33:26 PM
 */
public class GoogleReaderCommunicationException extends RuntimeException {

    public GoogleReaderCommunicationException(Throwable e) {
        super("Error accessing Google Reader", e);
    }
}
