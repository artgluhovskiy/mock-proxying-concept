package org.art.concept;

/**
 * Simple target interface for proxying.
 */
public interface TargetInterface {

    String invokeHello();

    String invokeHelloWithParam(String param);
}
