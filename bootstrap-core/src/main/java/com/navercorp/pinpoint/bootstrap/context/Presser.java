package com.navercorp.pinpoint.bootstrap.context;


/**
 * @author ZhangYB
 **/
public interface Presser {
    void put(PressDetail value);

    PressDetail get();

    void remove();
}
