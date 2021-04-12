package com.navercorp.pinpoint.plugin.jdbc.other;

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyMatchers;
import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;

/**
 * @author ZhangYB
 **/
public class OtherDBTypeProvider implements TraceMetadataProvider {


    public OtherDBTypeProvider() {
    }

    @Override
    public void setup(TraceMetadataSetupContext context) {
        context.addServiceType(OtherDBConstants.OTHERDB , AnnotationKeyMatchers.exact(AnnotationKey.ARGS0));
        context.addServiceType(OtherDBConstants.OTHERDB_EXECUTE_QUERY , AnnotationKeyMatchers.exact(AnnotationKey.ARGS0));
    }
}
