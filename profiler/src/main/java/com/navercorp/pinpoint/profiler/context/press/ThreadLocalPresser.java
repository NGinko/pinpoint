package com.navercorp.pinpoint.profiler.context.press;


/**
 * @author ZhangYB
 **/
import com.google.inject.Inject;
import com.navercorp.pinpoint.bootstrap.context.PressDetail;
import com.navercorp.pinpoint.bootstrap.context.Presser;

public class ThreadLocalPresser implements Presser {

    private final ThreadLocal<PressDetail> threadLocal = new ThreadLocal<PressDetail>();

    @Inject
    public ThreadLocalPresser(){}

    @Override
    public void put(PressDetail value) {
        threadLocal.set(value);
    }

    public boolean isPressSource() {
        PressDetail currentPressDetail = this.get();
        if(currentPressDetail!=null){
            return Boolean.TRUE.equals(currentPressDetail.getPressFlag());
        }
        return false;
    }

    @Override
    public PressDetail get() {
        return threadLocal.get();
    }

    @Override
    public void remove() {
        this.threadLocal.remove();
    }



}
