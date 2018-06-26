package org.art.concept;

import org.mockito.Mockito;

/**
 * Mock tests (creation, arrangement, action, verification stages):
 * - custom mocks;
 * - CGLib mocks;
 * - Mockito framework mocks.
 *
 * @author ahlukhouski
 */
public class ProxyTest {

    public static void main(String[] args) {

        /* *** java.lang.reflect.Proxy test *** */
        System.out.println("*** java.lang.reflect.Proxy test ***");

        /* Mock creation stage */
        TargetInterface proxyMock = MockProxy.mock(TargetInterface.class);

        /* Mock arrangement stage */
        MockProxy.when(proxyMock.invokeHello()).thenReturn("invokeHello() method invocation!");

        MockProxy.when(proxyMock.invokeHelloWithParam("Invocation 1")).thenReturn("Return string 1");
        MockProxy.when(proxyMock.invokeHelloWithParam("Invocation 2")).thenReturn("Return string 2");

        /* Mock action stage */
        System.out.println("Method invokeHello() invocation result: " + proxyMock.invokeHello());

        System.out.println("Method invokeHelloWithParam(\"Invocation 1\") invocation result: " + proxyMock.invokeHelloWithParam("Invocation 1"));
        System.out.println("Method invokeHelloWithParam(\"Invocation 2\") invocation result: " + proxyMock.invokeHelloWithParam("Invocation 2"));

        /* Mock verification stage */
        MockProxy.verify(proxyMock).invokeHelloWithParam("Invocation 1");       //verification passed
        MockProxy.verify(proxyMock).invokeHelloWithParam("Invocation 3");       //verification failed
        MockProxy.verify(proxyMock).invokeHelloWithParam("Invocation 2");       //verification passed

        System.out.println();

        /* *** CGLib proxy test *** */
        System.out.println("*** CGLib proxy test ***");

        /* Mock creation stage */
        TargetInterface cgLibMock = MockCgLib.mock(TargetInterface.class);

        /* Spy creation stage */
        TargetClass targetClass = new TargetClass();
        TargetClass cgLibSpy = MockCgLib.spy(targetClass);

        /* Mock arrangement stage */
        MockCgLib.when(cgLibMock.invokeHello()).thenReturn("invokeHello() method invocation!");

        MockCgLib.when(cgLibMock.invokeHelloWithParam("Invocation 1")).thenReturn("Return string 1");
        MockCgLib.when(cgLibMock.invokeHelloWithParam("Invocation 2")).thenReturn("Return string 2");

        /* Mock/spy action stage */
        System.out.println("Mock: method invokeHello() invocation result: " + cgLibMock.invokeHello());

        System.out.println("Mock: method invokeHelloWithParam(\"Invocation 1\") invocation result: " + cgLibMock.invokeHelloWithParam("Invocation 1"));
        System.out.println("Mock: method invokeHelloWithParam(\"Invocation 2\") invocation result: " + cgLibMock.invokeHelloWithParam("Invocation 2"));

        System.out.println("Spy: method invokeHello() invocation result: " + cgLibSpy.invokeHello());
        System.out.println("Spy: method invokeHelloWithParam(\"Spy Param\") invocation result: " + cgLibSpy.invokeHelloWithParam("Spy Param"));

        /* Mock verification stage */
        MockCgLib.verify(cgLibMock).invokeHelloWithParam("Invocation 1");       //verification passed
        MockCgLib.verify(cgLibMock).invokeHelloWithParam("Invocation 3");       //verification failed
        MockCgLib.verify(cgLibMock).invokeHelloWithParam("Invocation 2");       //verification passed

        System.out.println();

        /* *** Mockito Framework test *** */
        System.out.println("*** Mockito Framework test ***");

        /* Mock creation */
        TargetInterface mockitoMock = Mockito.mock(TargetInterface.class);

        /* Mock arrangement stage */
        Mockito.when(mockitoMock.invokeHello()).thenReturn("invokeHello() method invocation!");

        Mockito.when(mockitoMock.invokeHelloWithParam("Invocation 1")).thenReturn("Return string 1");
        Mockito.when(mockitoMock.invokeHelloWithParam("Invocation 2")).thenReturn("Return string 2");

        /* Mock action stage */
        System.out.println("Method invokeHello() invocation result: " + mockitoMock.invokeHello());

        System.out.println("Method invokeHelloWithParam(\"Invocation 1\") invocation result: " + mockitoMock.invokeHelloWithParam("Invocation 1"));
        System.out.println("Method invokeHelloWithParam(\"Invocation 2\") invocation result: " + mockitoMock.invokeHelloWithParam("Invocation 2"));

        /* Mock verification stage */
        Mockito.verify(mockitoMock).invokeHelloWithParam("Invocation 1");       //verification passed
        //Mockito.verify(mockitoMock).invokeHelloWithParam("Invocation 3");     //verification failed
        Mockito.verify(mockitoMock).invokeHelloWithParam("Invocation 2");       //verification passed
    }
}
