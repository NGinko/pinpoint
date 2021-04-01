package com.navercorp.pinpoint.plugin.jdk.http;
/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.util.InstrumentUtils;
import com.navercorp.pinpoint.plugin.jdk.http.interceptor.HttpURLConnectionInterceptor;

/**
 * 
 * @author Jongho Moon
 *
 */
public class JdkHttpPlugin implements ProfilerPlugin, TransformTemplateAware {

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        //第一个参数为目标类，第二个实际上是注册了一个修改字节码的类到transformContext里
        transformTemplate.transform("sun.net.www.protocol.http.HttpURLConnection", HttpURLConnectionTransform.class);
    }

    /**
     * 插件会给指定类注册TransformCallbak类。类加载器在加载类时，如果该类名上有注册TransformCallBack，那么就会调用到TransformCallback的doInTansform方法，对类进行字节码注入。
     *然后会将修改后的的字节码返回给类加载器。需要做的就是在指定类的方法中，记录tx，spanId等参数
     */
    public static class HttpURLConnectionTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            target.addGetter(ConnectedGetter.class, "connected");

            if (target.hasField("connecting", "boolean")) {
                target.addGetter(ConnectingGetter.class, "connecting");
            }

            //找到具体对应的方法名 添加拦截器
            final InstrumentMethod connectMethod = InstrumentUtils.findMethod(target, "connect");
            connectMethod.addScopedInterceptor(HttpURLConnectionInterceptor.class, "HttpURLConnection");

            final InstrumentMethod getInputStreamMethod = InstrumentUtils.findMethod(target, "getInputStream");
            getInputStreamMethod.addScopedInterceptor(HttpURLConnectionInterceptor.class, "HttpURLConnection");

            final InstrumentMethod getOutputStreamMethod = InstrumentUtils.findMethod(target, "getOutputStream");
            getOutputStreamMethod.addScopedInterceptor(HttpURLConnectionInterceptor.class, "HttpURLConnection");

            return target.toBytecode();
        }
    }

    // pinpoint会检测到该实现，并自动出入TransformTemplateAware
    // template是pinpoint在进行类加载是未开发者提供的Jvm字节码增强处理的入口。
    // 具体还是通过transform方法并实现
    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
