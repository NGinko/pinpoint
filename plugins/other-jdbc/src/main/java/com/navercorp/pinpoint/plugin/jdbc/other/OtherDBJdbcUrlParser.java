package com.navercorp.pinpoint.plugin.jdbc.other;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DefaultDatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParserV2;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.StringMaker;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.UnKnownDatabaseInfo;
import com.navercorp.pinpoint.common.trace.ServiceType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author ZhangYB
 **/
public class OtherDBJdbcUrlParser implements JdbcUrlParserV2 {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    @Override
    public DatabaseInfo parse(String url) {
        if (url == null) {
            this.logger.info("jdbcUrl");
            return UnKnownDatabaseInfo.INSTANCE;
        } else {
            try {
                return this.parseNormal(url);
            } catch (Exception e) {
                this.logger.info("OtherDBUrl parse error. url {},Caused {}", url, e.getMessage(), e);
                return UnKnownDatabaseInfo.createUnknownDataBase(OtherDBConstants.OTHERDB, OtherDBConstants.OTHERDB_EXECUTE_QUERY, url);
            }
        }
    }

    private DatabaseInfo parseNormal(String url) {
        StringMaker maker = new StringMaker(url);
        String dbType = maker.after("jdbc:").before(":").value().toUpperCase();
        maker.clear();
        String host = maker.next().after("//").before('/').value();
        List<String> hostList = this.splitHost(host);
        String databaseId = maker.clear().after('/').before('?').value();
        String normalizedUrl = maker.clear().before("?").value();
        return new DefaultDatabaseInfo(OtherDBConstants.OTHERDB, OtherDBConstants.OTHERDB_EXECUTE_QUERY, url, normalizedUrl, hostList, dbType + ":" + databaseId);
    }

    private List<String> splitHost(String host) {
        int multiHost = host.indexOf(",");
        if (multiHost == -1) {
            return Collections.singletonList(host);
        } else {
            String[] hostArr = host.split(",");
            return Arrays.asList(hostArr);
        }
    }


    @Override
    public ServiceType getServiceType() {
        return OtherDBConstants.OTHERDB;
    }
}
