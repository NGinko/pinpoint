package com.navercorp.pinpoint.plugin.jdbc.other;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.*;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.*;
import com.navercorp.pinpoint.bootstrap.plugin.util.InstrumentUtils;
import com.navercorp.pinpoint.common.util.VarArgs;


import java.security.ProtectionDomain;
import java.util.List;

/**
 * @author ZhangYB
 **/
public class OtherDBPlugin implements ProfilerPlugin, TransformTemplateAware {
    /**
     * profile主要是将TransformCallBack添加到Pinpoint
     * TransformCallBack transform a target class by adding interceptors,getters and/or fields.
     */
    //ProfilerPlugin 主要是提供ServiceType,AnnotationKey

    private static final String OTHERDB_SCOPE = OtherDBConstants.OTHERDB_SCOPE;

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final JdbcUrlParserV2 jdbcUrlParser = new OtherDBJdbcUrlParser();
    private TransformTemplate transformTemplate;

    public OtherDBPlugin() {

    }

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        //利用参数对象获得插件配置
        OtherDBConfig config = new OtherDBConfig(context.getConfig());
        if (!config.isEnable()) {
            this.logger.info("{} disabled", this.getClass().getSimpleName());
        } else {
            this.logger.info("{} config:{}", this.getClass().getSimpleName());
            context.addJdbcUrlParser(jdbcUrlParser);
            this.addConnectionTransformer(config.getConnections());
            this.addDriverTransformer(config.getDrivers());
            this.addPreparedStatementTransformer(config.getPreparedStatements());
            this.addStatementTransformer(config.getStatements());
        }

    }

    private void addConnectionTransformer(List<String> connections) {
        for (String connection : connections) {
            this.transformTemplate.transform(connection, OtherDBPlugin.OtherDbConnectionTransform.class);
        }
    }

    private void addDriverTransformer(List<String> drivers) {
        for (String driver : drivers) {
            this.transformTemplate.transform(driver, OtherDBPlugin.DriverTransformer.class);
        }
    }

    private void addPreparedStatementTransformer(List<String> statements) {
        for (String statement : statements) {
            this.transformTemplate.transform(statement, OtherDBPlugin.PreparedStatementTransform.class);
        }
    }

    private void addStatementTransformer(List<String> statements) {
        for (String statement : statements) {
            this.transformTemplate.transform(statement, OtherDBPlugin.OtherDbStatementTransform.class);
        }

    }


    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }


    public static class OtherDbConnectionTransform implements TransformCallback {

        public OtherDbConnectionTransform() {
        }

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            if (!target.isInterceptable()) {
                return null;
            } else {
                target.addField(DatabaseInfoAccessor.class);
                InstrumentUtils.findMethod(target, "close").addScopedInterceptor(ConnectionCloseInterceptor.class, OTHERDB_SCOPE);
                String methodNameCreateStatement = "createStatement";
                Class<? extends Interceptor> statementCreate = StatementCreateInterceptor.class;
                InstrumentUtils.findMethod(target, methodNameCreateStatement).addScopedInterceptor(statementCreate, OTHERDB_SCOPE);
                InstrumentUtils.findMethod(target, methodNameCreateStatement, "int", "int").addScopedInterceptor(statementCreate, OTHERDB_SCOPE);
                InstrumentUtils.findMethod(target, methodNameCreateStatement, "int", "int", "int").addScopedInterceptor(statementCreate, OTHERDB_SCOPE);
                String methodNamePrepareStatement = "prepareStatement";
                Class<? extends Interceptor> preparedStatementCreate = PreparedStatementCreateInterceptor.class;
                InstrumentUtils.findMethod(target, methodNamePrepareStatement, "java.lang.String").addScopedInterceptor(preparedStatementCreate, OTHERDB_SCOPE);
                InstrumentUtils.findMethod(target, methodNamePrepareStatement, "java.lang.String", "int").addScopedInterceptor(preparedStatementCreate, OTHERDB_SCOPE);
                InstrumentUtils.findMethod(target, methodNamePrepareStatement, "java.lang.String", "java.lang.String[]").addScopedInterceptor(preparedStatementCreate, OTHERDB_SCOPE);
                InstrumentUtils.findMethod(target, methodNamePrepareStatement, "java.lang.String", "int[]").addScopedInterceptor(preparedStatementCreate, OTHERDB_SCOPE);
                InstrumentUtils.findMethod(target, methodNamePrepareStatement, "java.lang.String", "int", "int").addScopedInterceptor(preparedStatementCreate, OTHERDB_SCOPE);
                InstrumentUtils.findMethod(target, methodNamePrepareStatement, "java.lang.String", "int", "int", "int").addScopedInterceptor(preparedStatementCreate, OTHERDB_SCOPE);
                String methodNamePrepareCall = "prepareCall";
                InstrumentUtils.findMethod(target, methodNamePrepareCall, "java.lang.String").addScopedInterceptor(preparedStatementCreate, OTHERDB_SCOPE);
                InstrumentUtils.findMethod(target, methodNamePrepareCall, "java.lang.String", "int", "int").addScopedInterceptor(preparedStatementCreate, OTHERDB_SCOPE);
                InstrumentUtils.findMethod(target, methodNamePrepareCall, "java.lang.String", "int", "int", "int").addScopedInterceptor(preparedStatementCreate, OTHERDB_SCOPE);
                return target.toBytecode();
            }
        }
    }

    public static class DriverTransformer implements TransformCallback {

        public DriverTransformer() {
        }

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(DatabaseInfoAccessor.class);
            InstrumentUtils.findMethod(target, "connect", "java.lang.String", "java.lang.String").addScopedInterceptor(DriverConnectInterceptorV2.class, VarArgs.va(OtherDBConstants.OTHERDB, true), OTHERDB_SCOPE, ExecutionPolicy.ALWAYS);
            return target.toBytecode();

        }
    }

    public static class PreparedStatementTransform implements TransformCallback {
        public PreparedStatementTransform() {

        }

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(DatabaseInfoAccessor.class);
            target.addField(ParsingResultAccessor.class);
            target.addField(BindValueAccessor.class);
            int maxBindValueSize = 1024;
            Class<? extends Interceptor> preparedStatementInterceptor = PreparedStatementExecuteQueryInterceptor.class;
            InstrumentUtils.findMethod(target, "execute").addScopedInterceptor(preparedStatementInterceptor, VarArgs.va(maxBindValueSize), OTHERDB_SCOPE);
            InstrumentUtils.findMethod(target, "executeQuery").addScopedInterceptor(preparedStatementInterceptor, VarArgs.va(maxBindValueSize), OTHERDB_SCOPE);
            InstrumentUtils.findMethod(target, "executeUpdate").addScopedInterceptor(preparedStatementInterceptor, VarArgs.va(maxBindValueSize), OTHERDB_SCOPE);
            PreparedStatementBindingMethodFilter excludes = PreparedStatementBindingMethodFilter.excludes("setRowId", "setNClod", "setSQLXML");
            List<InstrumentMethod> declaredMethods = target.getDeclaredMethods(excludes);
            for (InstrumentMethod declaredMethod : declaredMethods) {
                if (declaredMethod.getName().startsWith("set")) {
                    declaredMethod.addScopedInterceptor(PreparedStatementBindVariableInterceptor.class, OTHERDB_SCOPE, ExecutionPolicy.BOUNDARY);
                }
            }
            return target.toBytecode();

        }
    }

    public static class OtherDbStatementTransform implements TransformCallback {
        public OtherDbStatementTransform() {
        }


        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            if (!target.isInterceptable()) {
                return null;
            } else {
                target.addField(DatabaseInfoAccessor.class);
                Class<? extends Interceptor> executeQueryInterceptor = StatementExecuteQueryInterceptor.class;
                InstrumentUtils.findMethod(target, "executeQuery", "java.lang.String").addScopedInterceptor(executeQueryInterceptor, OTHERDB_SCOPE);
                InstrumentUtils.findMethod(target, "executeUpdate", "java.lang.String").addScopedInterceptor(executeQueryInterceptor, OTHERDB_SCOPE);
                InstrumentUtils.findMethod(target, "executeUpdate", "java.lang.String", "int").addScopedInterceptor(executeQueryInterceptor, OTHERDB_SCOPE);
                InstrumentUtils.findMethod(target, "execute", "java.lang.String").addScopedInterceptor(executeQueryInterceptor, OTHERDB_SCOPE);
                InstrumentUtils.findMethod(target, "execute", "java.lang.String", "int").addScopedInterceptor(executeQueryInterceptor, OTHERDB_SCOPE);
                return target.toBytecode();

            }
        }
    }
}


