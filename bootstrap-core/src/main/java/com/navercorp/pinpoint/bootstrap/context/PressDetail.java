package com.navercorp.pinpoint.bootstrap.context;


/**
 * @author ZhangYB
 **/
public class PressDetail {

    private final Boolean pressFlag;

    public Boolean getPressFlag() {
        return pressFlag;
    }

    public PressDetail(Builder builder) {
        this.pressFlag = builder.pressFlag;
    }

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder {
        private Boolean pressFlag;

        public Builder() {
        }

        public Builder setPressFlag(Boolean pressFlag) {
            this.pressFlag = pressFlag;
            return this;
        }

        public PressDetail build() {
            return new PressDetail(this);
        }
    }

}
