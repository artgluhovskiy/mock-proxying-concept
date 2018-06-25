package org.art.concept;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Set;

/**
 * Simple presentation of a mock creation concept based on
 * {@link java.lang.reflect.Proxy}.
 * This mock class is limited to mock interfaces because of
 * the limitation of the Proxy class.
 */
public class MockProxy {

    private static final ThreadLocal<OngoingStub> THREAD_HOLDER = new ThreadLocal<>();

    @SuppressWarnings("unchecked")
    public static <T> T mock(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(MockProxy.class.getClassLoader(),
                new Class[]{clazz}, new MockInvocationHandler());
    }

    public static OngoingStub when(Objects obj) {
        return THREAD_HOLDER.get();
    }

    public static class OngoingStub {

        private Set<MockInvocationHandler> handlers;

        public OngoingStub thenReturn(Object retObj) {
            OngoingStub stub = THREAD_HOLDER.get();
            stub.getHandlers()
            return null;
        }

        public Set<MockInvocationHandler> getHandlers() {
            return handlers;
        }
    }

    private static class MockInvocationHandler implements InvocationHandler {

        private Deque<DataHolder> dataHolders = new LinkedList<>();

        /**
         * Intercepts the method call and decides what value will be returned.
         */
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {

            OngoingStub stub = THREAD_HOLDER.get();
            if (stub == null) {
                stub = new OngoingStub();
                THREAD_HOLDER.set(stub);
            }



            DataHolder holder = new DataHolder(method, args);
            dataHolders.add(holder);

            stub.getHandlers().add(this);

            return null;
        }

        public Deque<DataHolder> getDataHolders() {
            return dataHolders;
        }

        /**
         * Stores the method with it's arguments and the return value.
         */
        private class DataHolder {

            private Object[] args;
            private Method method;
            private Object retObj;

            private DataHolder(Method method, Object[] args) {
                this.args = args;
                this.method = method;
            }

            private Object[] getArgs() {
                return args;
            }

            private Method getMethod() {
                return method;
            }

            private Object getRetObj() {
                return retObj;
            }
        }
    }

    public static void main(String[] args) {

        TargetInterface interfProxy = MockProxy.mock(TargetInterface.class);
        System.out.println(interfProxy.hashCode());

//        when(interfProxy.invokeHelloWithParam("World")).thenReturn("Hello");


    }

//    //Arrange stage
//    String userLogin = "user_login_1";
//    User user = new User(3L, "Harry", "Potter", userLogin, "22.03.18", Role.USER);
//
//    when(userDao.getUserByLogin(userLogin)).thenReturn(user);
//
//    //Act stage
//        userService.getUserByLogin(userLogin);
//
//    //Assert stage
//    verify(userDao, times(1)).getUserByLogin(userLogin);
//    verifyNoMoreInteractions(userDao);

}
