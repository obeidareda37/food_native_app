package com.obeidareda37.foodapp.eventbus;

public class CounterCartEven {
    private boolean success;

    public CounterCartEven(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
