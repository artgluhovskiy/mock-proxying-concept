package org.art.concept;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * Simple presentation of a mock creation concept based
 * on CGLib proxying.
 * <p>
 * Doesn't consider the order of method invocations.
 */
public class MockCgLib {

    private static final ThreadLocal<MockCgProgress> THREAD_HOLDER = ThreadLocal.withInitial(MockCgProgress::new);

    private static final Random RND = new Random(System.currentTimeMillis());

    private static final String TO_STRING_METHOD_NAME = "toString";
    private static final String HASHCODE_METHOD_NAME = "hashCode";
    private static final String EQUALS_METHOD_NAME = "equals";

    /**
     * Creates mock based on a specified class token.
     */
    @SuppressWarnings("unchecked")
    public static <T> T mock(Class<T> clazz) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(new MockMethodInterceptor());
        return (T) enhancer.create();
    }

    /**
     * Wraps the object into a proxy (spy).
     */
    @SuppressWarnings("unchecked")
    public static <T> T spy(Object obj) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(obj.getClass());
        enhancer.setCallback(new SpyMethodInterceptor());
        return (T) enhancer.create();
    }

    public static <T> OngoingStub<T> when(T obj) {
        return new OngoingStub<>();
    }

    public static <T> T verify(T mock) {
        MockCgProgress progress = THREAD_HOLDER.get();
        //Adding into verification set (verification stage)
        progress.getVerificationSet().add(mock);
        return mock;
    }

    static class OngoingStub<T> {

        /**
         * Sets the return value for the last method call.
         */
        public OngoingStub<T> thenReturn(T retObj) {
            MockCgProgress progress = THREAD_HOLDER.get();
            AbstractMockMethodInterceptor currentInterceptor = progress.getCurrentInterceptor();
            DataHolder lastHolder = currentInterceptor.getDataHolders().getLast();
            lastHolder.retObj = retObj;
            return new OngoingStub<>();
        }
    }

    static abstract class AbstractMockMethodInterceptor implements MethodInterceptor {

        private Deque<DataHolder> dataHolders = new LinkedList<>();

        private int proxyHashcode = RND.nextInt(Integer.MAX_VALUE);      //random hashcode generation

        /**
         * Intercepts the method call and decides what value will be returned.
         */
        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            //Simulates hashCode() method invocation (allows to store mock in Set)
            if (HASHCODE_METHOD_NAME.equals(method.getName())) {
                return proxyHashcode;
            }

            //Simulates equals() method invocation
            if (EQUALS_METHOD_NAME.equals(method.getName())) {
                return this.proxyHashcode == args[0].hashCode();
            }

            //Check if mock is in the verification stage
            Set<Object> verifSet = THREAD_HOLDER.get().getVerificationSet();
            if (verifSet.contains(obj)) {
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
                verifSet.remove(obj);
                return null;
            }

            //Method interception
            //Ignore implicit toString() method invocations during the debugging mode
            if (!TO_STRING_METHOD_NAME.equals(method.getName())) {
                DataHolder holder = new DataHolder(method, args);
                dataHolders.addLast(holder);
            }
            MockCgProgress progress = THREAD_HOLDER.get();

            progress.setCurrentInterceptor(this);

            Object retObject = getVal(obj, args, proxy);
            if (retObject instanceof DataHolder) {
                return ((DataHolder) retObject).retObj;
            } else {
                return retObject;
            }
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

        protected abstract Object getVal(Object obj, Object[] args, MethodProxy proxy) throws Throwable;
    }

    static class MockMethodInterceptor extends AbstractMockMethodInterceptor {

        /**
         * In case of mocking a class or interface.
         */
        @Override
        public Object getVal(Object obj, Object[] args, MethodProxy proxy) {
            return getDataHolders().stream()
                    .filter(holder -> holder.method.getName().equals(proxy.getSignature().getName())
                            && Arrays.deepEquals(holder.args, args))
                    .findFirst()
                    .orElse(null);
        }

    }

    static class SpyMethodInterceptor extends AbstractMockMethodInterceptor {

        /**
         * In case of mocking an object the object's methods are invoked.
         */
        @Override
        public Object getVal(Object obj, Object[] args, MethodProxy proxy) throws Throwable {
            return proxy.invokeSuper(obj, args);
        }
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

/**
 * Helper class which contains the current state of "mocking flow".
 */
class MockCgProgress {

    private MockCgLib.AbstractMockMethodInterceptor currentInterceptor;

    private Set<Object> verificationSet = new HashSet<>();

    MockCgProgress() {
        //Initialization with an empty method interceptor
        currentInterceptor = new MockCgLib.MockMethodInterceptor();
    }

    public MockCgLib.AbstractMockMethodInterceptor getCurrentInterceptor() {
        return currentInterceptor;
    }

    public Set<Object> getVerificationSet() {
        return verificationSet;
    }

    public void setCurrentInterceptor(MockCgLib.AbstractMockMethodInterceptor currentInterceptor) {
        this.currentInterceptor = currentInterceptor;
    }
}
