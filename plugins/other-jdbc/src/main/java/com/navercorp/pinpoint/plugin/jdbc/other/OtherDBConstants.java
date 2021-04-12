package com.navercorp.pinpoint.plugin.jdbc.other;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;
import com.navercorp.pinpoint.common.trace.ServiceTypeProperty;


import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.*;

/**
 * @author ZhangYB
 **/
public class OtherDBConstants {

    public static final String OTHERDB_SCOPE = "OTHERDB_JDBC";
    public static final ServiceType OTHERDB = ServiceTypeFactory.of(2901, "OTHERDB", ServiceTypeProperty.TERMINAL, ServiceTypeProperty.INCLUDE_DESTINATION_ID);
    public static final ServiceType OTHERDB_EXECUTE_QUERY = ServiceTypeFactory.of(2902, "OTHERDB_EXECUTE_QUERY", "OTHERDB", TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION_ID);

    private OtherDBConstants() {
    }


}
