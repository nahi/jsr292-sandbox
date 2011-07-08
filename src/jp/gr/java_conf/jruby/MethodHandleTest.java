package jp.gr.java_conf.jruby;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: nahi
 * Date: 7/7/11
 * Time: 7:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class MethodHandleTest {
    public static void main(String[] args) throws Throwable {
        final String methodName = "target";
        int times = 100000;

        for (int idx = 0; idx < 10; ++idx) {
            System.out.println("--");
            benchmark("methodhandle", times, new Callback() {
                public void call(Object receiver) {
                    methodhandle(receiver, methodName);
                }
            });

            benchmark("reflection  ", times, new Callback() {
                public void call(Object receiver) {
                    reflection(receiver, methodName);
                }
            });

            benchmark("empty       ", times, new Callback() {
                public void call(Object receiver) {
                    empty(receiver, methodName);
                }
            });
        }
    }

    interface Callback {
        public void call(Object receiver);
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
                String.format("%s * %d: %.2f [msec], average: %.2f [nsec]",
                        title, times, elapsed, elapsed / times * 1000.0));
    }

    private static String methodhandle(Object receiver, String methodName) {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodType mt = MethodType.methodType(String.class, String.class);
            MethodHandle method = lookup.findVirtual(receiver.getClass(), methodName, mt);

            String dummy = methodName;
            dummy = dummy.replace(dummy.charAt(RANDOM.nextInt(dummy.length())), dummy.charAt(RANDOM.nextInt(dummy.length())));
            return (String) method.bindTo(receiver).invokeExact(dummy);
        } catch (Throwable t) {
            throw new RuntimeException(t.getMessage(), t);
        }
    }

    private static String reflection(Object receiver, String methodName) {
        try {
            Method method = receiver.getClass().getDeclaredMethod(methodName, new Class[]{String.class});

            String dummy = methodName;
            dummy = dummy.replace(dummy.charAt(RANDOM.nextInt(dummy.length())), dummy.charAt(RANDOM.nextInt(dummy.length())));
            return (String) method.invoke(receiver, dummy);
        } catch (Throwable t) {
            throw new RuntimeException(t.getMessage(), t);
        }
    }

    private static String empty(Object receiver, String methodName) {
        try {
            String dummy = methodName;
            dummy = dummy.replace(dummy.charAt(RANDOM.nextInt(dummy.length())), dummy.charAt(RANDOM.nextInt(dummy.length())));
            return dummy;
        } catch (Throwable t) {
            throw new RuntimeException(t.getMessage(), t);
        }
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