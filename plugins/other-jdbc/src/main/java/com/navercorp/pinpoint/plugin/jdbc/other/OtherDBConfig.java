package com.navercorp.pinpoint.plugin.jdbc.other;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

import java.util.List;

/**
 * @author ZhangYB
 **/
public class OtherDBConfig {
    private final boolean enable;
    private final List<String> drivers;
    private final List<String> connections;
    private final List<String> statements;
    private final List<String> preparedStatements;

    public OtherDBConfig(ProfilerConfig config){
        this.enable = config.readBoolean("profiler.jdbc.othres.enable",true);
        this.drivers = config.readList("profiler.jdbc.othres.driver");
        this.statements = config.readList("profiler.jdbc.othres.statements");
        this.connections = config.readList("profiler.jdbc.othres.statement");
        this.preparedStatements = config.readList("profiler.jdbc.othres.preparedStatement");
    }

    public boolean isEnable() {
        return enable;
    }

    public List<String> getDrivers() {
        return drivers;
    }

    public List<String> getConnections() {
        return connections;
    }

    public List<String> getStatements() {
        return statements;
    }

    public List<String> getPreparedStatements() {
        return preparedStatements;
    }
}
