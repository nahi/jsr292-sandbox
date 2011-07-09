package jp.gr.java_conf.jruby;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Random;

public class MethodHandleTest {
    public static void main(String[] args) throws Throwable {
        final String methodName = "target";
        int times = 100000;

        for (int idx = 0; idx < 10; ++idx) {
            System.out.println("-- " + idx);
            benchmark("methodhandle lookup       ", times, new Callback() {
                public void call(Object receiver) throws Throwable {
                    methodhandleLookup(receiver, methodName);
                }
            });

            benchmark("methodhandle lookup+invoke", times, new Callback() {
                public void call(Object receiver) throws Throwable {
                    MethodHandle method = methodhandleLookup(receiver, methodName);
                    methodhandleInvoke(receiver, method);
                }
            });

            benchmark("reflection   lookup       ", times, new Callback() {
                public void call(Object receiver) throws Throwable {
                    reflectionLookup(receiver, methodName);
                }
            });

            benchmark("reflection   lookup+invoke", times, new Callback() {
                public void call(Object receiver) throws Throwable {
                    Method method = reflectionLookup(receiver, methodName);
                    reflectionInvoke(receiver, method);
                }
            });
        }
    }

    interface Callback {
        public void call(Object receiver) throws Throwable;
    }

    private static Random RANDOM = new Random();

    private static void benchmark(String title, int times, Callback cb) throws Throwable {
        Class[] classes = new Class[]{Foo.class, Bar.class, Baz.class, Qux.class};
        long start = System.nanoTime();
        for (int idx = 0; idx < times; ++idx) {
            Object receiver = classes[RANDOM.nextInt(classes.length)].newInstance();
            cb.call(receiver);
        }
        double elapsed = (System.nanoTime() - start) / 1000000.0;
        System.out.println(
                String.format("%s * %d: %6.2f [msec], average: %6.2f [nsec]",
                        title, times, elapsed, elapsed / times * 1000.0));
    }

    private static MethodHandle methodhandleLookup(Object receiver, String methodName) throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodType mt = MethodType.methodType(String.class, String.class);
        return lookup.findVirtual(receiver.getClass(), methodName, mt);
    }
    
    private static String methodhandleInvoke(Object receiver, MethodHandle method) throws Throwable {
        return (String) method.bindTo(receiver).invokeExact("methodhandle");
    }

    private static Method reflectionLookup(Object receiver, String methodName) throws Throwable {
        return receiver.getClass().getDeclaredMethod(methodName, new Class[]{String.class});
    }

    private static String reflectionInvoke(Object receiver, Method method) throws Throwable {
        return (String) method.invoke(receiver, "reflection");
    }
}

class Foo {
    public String target(String name) {
        return "Hello " + name + " from Foo#target";
    }
}

class Bar {
    public String target(String name) {
        return "Hello " + name + " from Bar#target";
    }
}

class Baz {
    public String target(String name) {
        return "Hello " + name + " from Baz#target";
    }
}

class Qux {
    public String target(String name) {
        return "Hello " + name + " from Qux#target";
    }
}