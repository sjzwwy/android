package com.example.jniDemo2;

/**
 * Created by yp-tc-m-2781 on 16/5/25.
 */
public class MyJniTest {
    static {
        System.loadLibrary("JniTest2");
    }
    public static native String getKey();
}