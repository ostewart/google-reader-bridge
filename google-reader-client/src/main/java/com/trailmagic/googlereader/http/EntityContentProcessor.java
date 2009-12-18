package com.trailmagic.googlereader.http;

import java.io.Reader;

/**
 * Created by: oliver on Date: Dec 14, 2009 Time: 8:24:34 PM
 */
public interface EntityContentProcessor<T> {
    public T process(Reader content) throws Exception;
}
