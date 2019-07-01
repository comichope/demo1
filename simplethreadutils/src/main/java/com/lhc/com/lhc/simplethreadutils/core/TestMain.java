package com.lhc.com.lhc.simplethreadutils.core;

public class TestMain {
    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            SimpleCallable sr = new TestThread();
            SimpleThreadUtils.submit(sr);
        }
    }

    static class TestThread implements SimpleCallable<String> {


        @Override
        public String call() throws Exception {
            Thread.sleep(200);
            return null;
        }
    }
}
