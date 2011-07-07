package jp.gr.java_conf.jruby;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by IntelliJ IDEA.
 * User: nahi
 * Date: 7/7/11
 * Time: 7:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class MethodHandleTest {
    public static void main(String[] args) {
        final Object receiver = new MethodHandleTest();
        final String methodName = "target";
        int times = 500000;

        for (int idx = 0; idx < 10; ++idx) {
            System.out.println("--");
            benchmark("methodhandle", times, new Callback() {
                MethodHandle method;

                public void before() {
                    MethodHandles.Lookup lookup = MethodHandles.lookup();
                    MethodType mt = MethodType.methodType(String.class, String.class);
                    try {
                        method = lookup.findVirtual(receiver.getClass(), methodName, mt);
                    } catch (IllegalAccessException | NoSuchMethodException excn) {
                        throw new RuntimeException(excn.getMessage(), excn);
                    }
                }

                public void call() {
                    methodhandle(receiver, method);
                }
            });

            benchmark("reflection  ", times, new Callback() {
                Method method;

                public void before() {
                    try {
                        method = receiver.getClass().getDeclaredMethod(methodName, new Class[]{String.class});
                    } catch (NoSuchMethodException excn) {
                        throw new RuntimeException(excn.getMessage(), excn);
                    }
                }

                public void call() {
                    reflection(receiver, method);
                }
            });
        }
    }

    interface Callback {
        public void before();

        public void call();
    }

    private static void benchmark(String title, int times, Callback cb) {
        cb.before();
        long start = System.nanoTime();
        for (int idx = 0; idx < times; ++idx) {
            cb.call();
            // cb.before(); for benchmarking method lookup time
        }
        double elapsed = (System.nanoTime() - start) / 1000000.0;
        System.out.println(
                String.format("%s * %d: %3.2f [msec], average: %3.2f [nsec]",
                        title, times, elapsed, elapsed / times * 1000.0));
    }

    private static String methodhandle(Object receiver, MethodHandle method) {
        try {
            return (String) method.bindTo(receiver).invokeExact("methodhandle");
        } catch (Throwable t) {
            throw new RuntimeException(t.getMessage(), t);
        }
    }

    private static String reflection(Object receiver, Method method) {
        try {
            return (String) method.invoke(receiver, "reflection");
        } catch (Throwable t) {
            throw new RuntimeException(t.getMessage(), t);
        }
    }

    public String target(String name) {
        return "Hello " + name;
    }
}