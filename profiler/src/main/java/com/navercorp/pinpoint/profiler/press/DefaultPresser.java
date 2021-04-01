package com.navercorp.pinpoint.profiler.press;

import com.navercorp.pinpoint.bootstrap.press.Presser;

public class DefaultPresser implements Presser {
    @Override
    public boolean isPressTest() {
        /**
         * 从线程变量中获取判断是否是压测的标记
         *
         * example中是，如果tl中存在test的标记，就需要将test添加到对应的参数Map中。
         */
        return false;
    }
}
