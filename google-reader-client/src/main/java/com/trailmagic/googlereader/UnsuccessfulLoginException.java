package com.trailmagic.googlereader;


/**
 * Created by: oliver on Date: Dec 13, 2009 Time: 12:09:14 AM
 */
public class UnsuccessfulLoginException extends Exception {
    public UnsuccessfulLoginException(Exception e) {
        super(e);
    }

    public UnsuccessfulLoginException(String message) {
        super(message);
    }
}
