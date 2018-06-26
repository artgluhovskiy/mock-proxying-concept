package org.art.concept;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * Simple presentation of a mock creation concept based on
 * {@link java.lang.reflect.Proxy}.
 * This mock class is limited to mock interfaces because of
 * the limitation of the Proxy class.
 * Doesn't consider the order of method invocations.
 */
public class MockProxy {

    private static final ThreadLocal<MockProgress> THREAD_HOLDER = ThreadLocal.withInitial(MockProgress::new);

    private static final Random RND = new Random(System.currentTimeMillis());

    private static final String TO_STRING_METHOD = "toString";
    private static final String HASHCODE_METHOD = "hashCode";
    private static final String EQUALS_METHOD = "equals";

    /**
     * Creates mock based on a specified class token.
     */
    @SuppressWarnings("unchecked")
    public static <T> T mock(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(MockProxy.class.getClassLoader(),
                new Class[]{clazz}, new MockInvocationHandler());
    }

    public static <T> OngoingStub<T> when(T obj) {
        return new OngoingStub<>();
    }

    public static <T> T verify(T mock) {
        MockProgress progress = THREAD_HOLDER.get();
        //Adding into verification set (verification stage)
        progress.getVerificationSet().add(mock);
        return mock;
    }

    public static class OngoingStub<T> {

        /**
         * Sets the return value for the last method call.
         */
        public OngoingStub<T> thenReturn(T retObj) {
            MockProgress progress = THREAD_HOLDER.get();
            MockInvocationHandler currentHandler = progress.getCurrentHandler();
            MockInvocationHandler.DataHolder lastHolder = currentHandler.getDataHolders().getLast();
            lastHolder.retObj = retObj;
            return new OngoingStub<>();
        }
    }

    public static class MockInvocationHandler implements InvocationHandler {

        private Deque<DataHolder> dataHolders = new LinkedList<>();

        private int proxyHashcode = RND.nextInt();

        /**
         * Intercepts the method call and decides what mock stage should be triggered.
         */
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {

            //Simulates hashCode() method invocation (allows to store mock in Set)
            if (HASHCODE_METHOD.equals(method.getName())) {
                return proxyHashcode;
            }

            //Simulates equals() method invocation
            if (EQUALS_METHOD.equals(method.getName())) {
                return this.proxyHashcode == args[0].hashCode();
            }

            //Check if mock is in the verification stage
            Set<Object> verifSet = THREAD_HOLDER.get().getVerificationSet();
            if (verifSet.contains(proxy)) {
                DataHolder requestedData = new DataHolder(method, args);
                boolean verifResult = dataHolders.stream()
                        .anyMatch(holder -> holder.containsInvocation(requestedData));
                String argsString = Stream.of(args)
                        .map(String::valueOf)
                        .collect(joining(","));
                if (!verifResult) {
                    //Print error message...
                    System.out.printf("Verification failed! Method %s(%s) wasn't invoked.%n", method.getName(), argsString);
                    //throw new AssertionError();
                } else {
                    //Print success message...
                    System.out.printf("Verification passed! Method %s(%s) was invoked.%n", method.getName(), argsString);
                }
                verifSet.remove(proxy);
                return null;
            }

            //Method interception
            //Ignore implicit toString() method invocations during the debugging mode
            if (!TO_STRING_METHOD.equals(method.getName())) {
                DataHolder holder = new DataHolder(method, args);
                dataHolders.addLast(holder);
            }
            MockProgress progress = THREAD_HOLDER.get();

            progress.setCurrentHandler(this);

            DataHolder invocation = findInvocation(method, args);
            if (invocation != null) {
                return invocation.retObj;
            }
            return null;
        }

        private DataHolder findInvocation(Method method, Object[] args) {
            return this.dataHolders.stream()
                    .filter(holder -> holder.method.equals(method) && Arrays.deepEquals(holder.args, args))
                    .findFirst()
                    .orElse(null);
        }

        public Deque<DataHolder> getDataHolders() {
            return dataHolders;
        }

        /**
         * Stores the method invocation data with
         * its name, arguments and return value.
         */
        private static class DataHolder {

            private Object[] args;
            private Method method;
            private Object retObj;

            private DataHolder(Method method, Object[] args) {
                this.args = args;
                this.method = method;
            }

            private boolean containsInvocation(DataHolder holder) {
                return Arrays.deepEquals(this.args, holder.args)
                        && this.method.getName().equals(holder.method.getName());
            }
        }
    }
}

/**
 * Helper class which contains the current state of "mocking flow".
 */
class MockProgress {

    private MockProxy.MockInvocationHandler currentHandler;

    private Set<Object> verificationSet = new HashSet<>();

    MockProgress() {
        //Initialization with an empty handler
        currentHandler = new MockProxy.MockInvocationHandler();
    }

    public MockProxy.MockInvocationHandler getCurrentHandler() {
        return currentHandler;
    }

    public Set<Object> getVerificationSet() {
        return verificationSet;
    }

    public void setCurrentHandler(MockProxy.MockInvocationHandler currentHandler) {
        this.currentHandler = currentHandler;
    }
}
