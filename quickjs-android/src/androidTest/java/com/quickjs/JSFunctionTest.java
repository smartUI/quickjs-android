package com.quickjs;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JSFunctionTest {

    private JSContext context;
    private QuickJS quickJS;

    @Before
    public void setUp() {
        quickJS = QuickJS.createRuntime();
        context = quickJS.createContext();
    }

    @After
    public void tearDown() {
        context.close();
        quickJS.close();
    }

    @Test
    public void testJavaCallback1() {
        context.set("intFunction", new JSFunction(context, (JavaCallback) (receiver, array) -> Integer.MAX_VALUE));
        context.set("doubleFunction", new JSFunction(context, (JavaCallback) (receiver, args) -> Double.MAX_VALUE));
        context.set("boolFunction", new JSFunction(context, (JavaCallback) (receiver, args) -> true));
        context.set("stringFunction", new JSFunction(context, (JavaCallback) (receiver, args) -> "Hello"));

        assertEquals(Integer.MAX_VALUE, context.executeIntegerFunction("intFunction", null));
        assertEquals(Double.MAX_VALUE, context.executeDoubleFunction("doubleFunction", null), 1);
        assertTrue(context.executeBooleanFunction("boolFunction", null));
        assertEquals("Hello", context.executeStringFunction("stringFunction", null));
    }

    @Test
    public void testJavaCallback2() {
        context.set("intFunction", new JSFunction(context, (JavaCallback) (receiver, array) -> Integer.MAX_VALUE));
        context.set("doubleFunction", new JSFunction(context, (JavaCallback) (receiver, args) -> Double.MAX_VALUE));
        context.set("boolFunction", new JSFunction(context, (JavaCallback) (receiver, args) -> true));
        context.set("stringFunction", new JSFunction(context, (JavaCallback) (receiver, args) -> "Hello"));
        assertEquals(Integer.MAX_VALUE, context.executeIntegerScript("intFunction()", "file.js"));
        assertEquals(Double.MAX_VALUE, context.executeDoubleScript("doubleFunction()", "file.js"), 1);
        assertTrue(context.executeBooleanScript("boolFunction()", null));
        assertEquals("Hello", context.executeStringScript("stringFunction()", "file.js"));
    }

    @Test
    public void testJavaCallback3() {
        context.set("intFunction", new JSFunction(context, (JavaCallback) (receiver, array) -> Integer.MAX_VALUE));
        context.set("doubleFunction", new JSFunction(context, (JavaCallback) (receiver, args) -> Double.MAX_VALUE));
        context.set("boolFunction", new JSFunction(context, (JavaCallback) (receiver, args) -> true));
        context.set("stringFunction", new JSFunction(context, (JavaCallback) (receiver, args) -> "Hello"));

        context.executeVoidFunction("intFunction", new JSArray(context).push(new JSArray(context)));
        JSFunction function = (JSFunction) context.getObject("intFunction");
        function.call(JSValue.TYPE.INTEGER, null, null);
    }


    @Test
    public void testJavaCallback4() {
        context.set("test", new JSFunction(context, (receiver, args) -> {
            assertEquals(1, args.getInteger(0));
            assertEquals(3.14, args.getDouble(1), 0);
            assertTrue(args.getBoolean(2));
            assertEquals("Hello", args.getString(3));
        }));
        context.executeVoidScript("test(1, 3.14, true, 'Hello')", "file.js");
    }

    @Test
    public void call0() {
        context.executeVoidScript("function test(data){return 'Hello'}", "file.js");
        JSFunction function = (JSFunction) context.getObject("test");
        function.call(JSValue.TYPE.STRING, context, null);
        function.call(JSValue.TYPE.STRING, context, null);
    }

    @Test
    public void call1() {
        context.executeVoidScript("function test(data){return 'Hello'}", "file.js");
        assertEquals("Hello", context.executeStringFunction("test", null));
        assertEquals("Hello", context.executeStringFunction("test", null));
    }


    @Test
    public void call2() {
        context.registerJavaMethod(new JavaVoidCallback() {
            @Override
            public void invoke(JSObject receiver, JSArray array) {
                assertEquals("Hello", array.getString(0));
                assertEquals(3.14, array.getDouble(1), 0);
            }
        }, "test");
        JSFunction function = (JSFunction) context.getObject("test");
        JSArray args = new JSArray(context).push("Hello").push(3.14);
        function.call(JSValue.TYPE.STRING, context, args);
    }

    @Test
    public void call3() {
        context.registerJavaMethod(new JavaCallback() {
            @Override
            public Object invoke(JSObject receiver, JSArray array) {
                return array.getDouble(1);
            }
        }, "test");
        JSFunction function = (JSFunction) context.getObject("test");
        JSArray parameters = new JSArray(context).push("Hello").push(3.14);
        assertEquals(3.14, function.call(JSValue.TYPE.DOUBLE, context, parameters));
    }

    @Test
    public void call4() {
        JSFunction function = new JSFunction(context, new JavaCallback() {
            @Override
            public Object invoke(JSObject receiver, JSArray args) {
                return args.getArray(0);
            }
        });
        context.set("test", function);
        JSArray array = new JSArray(context).push("Hello");
        JSArray result = (JSArray) function.call(null, new JSArray(context).push(array));
        assertEquals("Hello", result.getString(0));
        JSArray result1 = context.executeArrayScript("test(['Hello'])", null);
        assertEquals("Hello", result1.getString(0));
        JSFunction function2 = (JSFunction) context.getObject("test");
        JSArray result2 = (JSArray) function2.call(null, new JSArray(context).push(array));
        assertEquals("Hello", result2.getString(0));
    }

    @Test
    public void call5() throws InterruptedException {
        Object[] ans = new Object[1];
        context.registerJavaMethod(new JavaVoidCallback() {
            @Override
            public void invoke(JSObject receiver, JSArray args) {
                JSFunction callback = (JSFunction) args.getObject(0);
//                callback.released = true;
//                callback.call(null, new JSArray(context).push("Hello"));
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        callback.call(null, new JSArray(context).push("Hello"));
                    }
                }).start();
            }
        }, "fetch");
        context.registerJavaMethod(new JavaVoidCallback() {
            @Override
            public void invoke(JSObject receiver, JSArray args) {
                ans[0] = args.getString(0);
            }
        }, "log");
        context.executeVoidScript("fetch(function(data){log(data)})", null);
        Thread.sleep(5000);
        assertEquals("Hello", ans[0]);
    }


    @Test
    public void call6() {
        Object[] ans = new Object[1];
        context.registerJavaMethod(new JavaVoidCallback() {
            @Override
            public void invoke(JSObject receiver, JSArray args) {
                JSArray obj = args.getArray(0);
                obj.released = true;
                ans[0] = obj.getString(0);
            }
        }, "log");
        context.executeVoidScript("log(['Hello'])", null);
        assertEquals("Hello", ans[0]);
    }
}