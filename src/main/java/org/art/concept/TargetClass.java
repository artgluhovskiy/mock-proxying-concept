package org.art.concept;

/**
 * Simple target class for proxying.
 */
public class TargetClass {

    public String invokeHello() {
        return "Hello!";
    }

    public String invokeHelloWithParam(String param) {
        return "Hello " + param + "!";
    }
}
