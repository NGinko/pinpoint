package com.navercorp.pinpoint.bootstrap.plugin.request;

/**
 * @author ZhangYB
 **/
public interface HttpHeadersRequestAdaptor<T> {
    String getHeaders(T var1);
}
