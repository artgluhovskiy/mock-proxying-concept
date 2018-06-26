package org.art.concept;

/**
 * Simple target class for proxying.
 */
public class TargetClass {

    public String invokeHello() {
        return "Hello from the origin class!";
    }

    public String invokeHelloWithParam(String param) {
        return param;
    }
}
