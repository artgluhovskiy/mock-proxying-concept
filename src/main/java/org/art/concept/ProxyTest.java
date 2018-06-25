package org.art.concept;

public class ProxyTest {

    public static void main(String[] args) {

        // java.lang.reflect.Proxy
        System.out.println("*** java.lang.reflect.Proxy test ***");
        TargetInterface mock = MockProxy.mock(TargetInterface.class);
        MockProxy.when(mock.invokeHello()).thenReturn("Hello from proxy!");
        System.out.println("invokeHello(): " + mock.invokeHello());
//        MockProxy.when(mock.invokeHelloWithParam("echo")).thenReturn("echo");
//        System.out.println(mock.invokeHelloWithParam("echo"));
//        MockProxy.when(mock.invokeHelloWithParam("hello")).thenReturn("world");
//        System.out.println(mock.invokeHelloWithParam("hello"));
        System.out.println();
    }
}
