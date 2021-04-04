package com.navercorp.pinpoint.bootstrap.plugin.http;

import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.request.HttpHeadersRequestAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestAdaptor;
import com.navercorp.pinpoint.common.trace.AnnotationKey;

/**
 * @author ZhangYB
 **/
public class HttpHeadersRecorder<T> {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    private final HttpHeadersRequestAdaptor<T> requestAdaptor;

    public HttpHeadersRecorder(RequestAdaptor<T> requestAdaptor){
        if(requestAdaptor instanceof HttpHeadersRequestAdaptor){
            this.requestAdaptor = (HttpHeadersRequestAdaptor) requestAdaptor;
        }else{
            this.requestAdaptor = null;
        }
    }

    public void record(SpanRecorder spanRecorder, T request){
        if(spanRecorder != null){
            if(this.requestAdaptor != null){
                String headers = this.requestAdaptor.getHeaders(request);
                if(isDebug){
                    logger.info(headers);
                }
                spanRecorder.recordAttribute(AnnotationKey.REQUEST_HEADER,headers);
            }
        }
    }
}
