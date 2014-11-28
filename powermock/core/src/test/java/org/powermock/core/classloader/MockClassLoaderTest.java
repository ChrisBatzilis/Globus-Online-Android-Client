/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.powermock.core.classloader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.powermock.core.classloader.MockClassLoader.MODIFY_ALL_CLASSES;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javassist.ByteArrayClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import junit.framework.Assert;

import org.junit.Test;
import org.powermock.core.classloader.annotations.UseClassPathAdjuster;
import org.powermock.core.transformers.MockTransformer;
import org.powermock.core.transformers.impl.MainMockTransformer;
import org.powermock.reflect.Whitebox;

public class MockClassLoaderTest {
    @Test
    public void autoboxingWorks() throws Exception {
        String name = this.getClass().getPackage().getName() + ".HardToTransform";
        final MockClassLoader mockClassLoader = new MockClassLoader(new String[] { name });
        List<MockTransformer> list = new LinkedList<MockTransformer>();
        list.add(new MainMockTransformer());
        mockClassLoader.setMockTransformerChain(list);
        Class<?> c = mockClassLoader.loadClass(name);

        Object object = c.newInstance();
        Whitebox.invokeMethod(object, "run");
        Assert.assertEquals(5, Whitebox.invokeMethod(object, "testInt"));
        Assert.assertEquals(5L, Whitebox.invokeMethod(object, "testLong"));
        Assert.assertEquals(5f, Whitebox.invokeMethod(object, "testFloat"));
        Assert.assertEquals(5.0, Whitebox.invokeMethod(object, "testDouble"));
        Assert.assertEquals(new Short("5"), Whitebox.invokeMethod(object, "testShort"));
        Assert.assertEquals(new Byte("5"), Whitebox.invokeMethod(object, "testByte"));
        Assert.assertEquals(true, Whitebox.invokeMethod(object, "testBoolean"));
        Assert.assertEquals('5', Whitebox.invokeMethod(object, "testChar"));
        Assert.assertEquals("5", Whitebox.invokeMethod(object, "testString"));
    }

    @Test
    public void prepareForTestHasPrecedenceOverPowerMockIgnoreAnnotatedPackages() throws Exception {
        MockClassLoader mockClassLoader = new MockClassLoader(new String[] { "org.mytest.myclass" });
        Whitebox.setInternalState(mockClassLoader, new String[] { "*mytest*" }, DeferSupportingClassLoader.class);
        assertTrue(Whitebox.<Boolean>invokeMethod(mockClassLoader, "shouldModify", "org.mytest.myclass"));
    }

    @Test
    public void powerMockIgnoreAnnotatedPackagesAreIgnored() throws Exception {
        MockClassLoader mockClassLoader = new MockClassLoader(new String[] { "org.ikk.Jux" });
        Whitebox.setInternalState(mockClassLoader, new String[] { "*mytest*" }, DeferSupportingClassLoader.class);
        assertFalse(Whitebox.<Boolean> invokeMethod(mockClassLoader, "shouldModify", "org.mytest.myclass"));
    }

    @Test
    public void powerMockIgnoreAnnotatedPackagesHavePrecedenceOverPrepareEverythingForTest() throws Exception {
        MockClassLoader mockClassLoader = new MockClassLoader(new String[] { MODIFY_ALL_CLASSES });
        Whitebox.setInternalState(mockClassLoader, new String[] { "*mytest*" }, DeferSupportingClassLoader.class);
        assertFalse(Whitebox.<Boolean> invokeMethod(mockClassLoader, "shouldModify", "org.mytest.myclass"));
    }

    @Test
    public void prepareForTestPackagesArePrepared() throws Exception {
        MockClassLoader mockClassLoader = new MockClassLoader(new String[] { "*mytest*" });
        assertTrue(Whitebox.<Boolean> invokeMethod(mockClassLoader, "shouldModify", "org.mytest.myclass"));
    }

    @Test
    public void shouldAddIgnorePackagesToDefer() throws Exception {
        MockClassLoader mockClassLoader = new MockClassLoader(new String[0]);
        mockClassLoader.addIgnorePackage("test*");
        String[] deferPackages = Whitebox.<String[]> getInternalState(mockClassLoader, "deferPackages");
        assertTrue(deferPackages.length > 1);
        assertEquals("test*", deferPackages[deferPackages.length - 1]);
    }
    
    @Test
    public void canFindResource() throws Exception {
        final MockClassLoader mockClassLoader = new MockClassLoader(new String[0]);
        List<MockTransformer> list = new LinkedList<MockTransformer>();
        list.add(new MainMockTransformer());
        mockClassLoader.setMockTransformerChain(list);

        // Force a ClassLoader that can find 'foo/bar/baz/test.txt' into
        // mockClassLoader.deferTo.
        ResourcePrefixClassLoader resourcePrefixClassLoader = new ResourcePrefixClassLoader(
          getClass().getClassLoader(), "org/powermock/core/classloader/");
        mockClassLoader.deferTo = resourcePrefixClassLoader;
		
        // MockClassLoader will only be able to find 'foo/bar/baz/test.txt' if it
        // properly defers the resource lookup to its deferTo ClassLoader.
        URL resource = mockClassLoader.getResource("foo/bar/baz/test.txt");
        Assert.assertNotNull(resource);
        Assert.assertTrue(resource.getPath().endsWith("test.txt"));
    }

    @Test
    public void canFindResources() throws Exception {
        final MockClassLoader mockClassLoader = new MockClassLoader(new String[0]);
        List<MockTransformer> list = new LinkedList<MockTransformer>();
        list.add(new MainMockTransformer());
        mockClassLoader.setMockTransformerChain(list);

        // Force a ClassLoader that can find 'foo/bar/baz/test.txt' into
        // mockClassLoader.deferTo.
        ResourcePrefixClassLoader resourcePrefixClassLoader = new ResourcePrefixClassLoader(
            getClass().getClassLoader(), "org/powermock/core/classloader/");
        mockClassLoader.deferTo = resourcePrefixClassLoader;
		
        // MockClassLoader will only be able to find 'foo/bar/baz/test.txt' if it
        // properly defers the resources lookup to its deferTo ClassLoader.
        Enumeration<URL> resources = mockClassLoader.getResources("foo/bar/baz/test.txt");
        Assert.assertNotNull(resources);
        Assert.assertTrue(resources.hasMoreElements());
        URL resource = resources.nextElement();
        Assert.assertTrue(resource.getPath().endsWith("test.txt"));
        Assert.assertFalse(resources.hasMoreElements());
    }
    
    @Test
    public void resourcesNotDoubled() throws Exception {
        final MockClassLoader mockClassLoader = new MockClassLoader(new String[0]);
        List<MockTransformer> list = new LinkedList<MockTransformer>();
        list.add(new MainMockTransformer());
        mockClassLoader.setMockTransformerChain(list);
        
        // MockClassLoader will only be able to find 'foo/bar/baz/test.txt' if it
        // properly defers the resources lookup to its deferTo ClassLoader.
        Enumeration<URL> resources = mockClassLoader.getResources("org/powermock/core/classloader/foo/bar/baz/test.txt");
        Assert.assertNotNull(resources);
        Assert.assertTrue(resources.hasMoreElements());
        URL resource = resources.nextElement();
        Assert.assertTrue(resource.getPath().endsWith("test.txt"));
        Assert.assertFalse(resources.hasMoreElements());
    }
    
    @Test
    public void canFindDynamicClassFromAdjustedClasspath() throws Exception {
        // Construct MockClassLoader with @UseClassPathAdjuster annotation.
        // It activates our MyClassPathAdjuster class which appends our dynamic
        // class to the MockClassLoader's classpool.
        UseClassPathAdjuster useClassPathAdjuster = new UseClassPathAdjuster() {
            public Class<? extends Annotation> annotationType() {
                return UseClassPathAdjuster.class;
            }
            public Class<? extends ClassPathAdjuster> value() {
                return MyClassPathAdjuster.class;
            }
        };
        final MockClassLoader mockClassLoader = new MockClassLoader(new String[0], useClassPathAdjuster );
        List<MockTransformer> list = new LinkedList<MockTransformer>();
        list.add(new MainMockTransformer());
        mockClassLoader.setMockTransformerChain(list);

        // setup custom classloader providing our dynamic class, for MockClassLoader to defer to
        mockClassLoader.deferTo = new ClassLoader(getClass().getClassLoader()) {
            @Override
            public Class<?> loadClass(String name)
                    throws ClassNotFoundException {
                if (name.equals(DynamicClassHolder.clazz.getName())) {
                        return DynamicClassHolder.clazz;
                }
                return super.loadClass(name);
            }
        };

        // verify that MockClassLoader can successfully load the class
        Class<?> dynamicTestClass = Class.forName(DynamicClassHolder.clazz.getName(), false, mockClassLoader);
        Assert.assertNotNull(dynamicTestClass);
        // .. and that MockClassLoader really loaded the class itself rather
        // than just providing the class from the deferred classloader
        assertNotSame(DynamicClassHolder.clazz, dynamicTestClass);
    }

    @Test(expected = ClassNotFoundException.class)
    public void cannotFindDynamicClassInDeferredClassLoader() throws Exception {

        MockClassLoader mockClassLoader = new MockClassLoader(new String[0]);
        List<MockTransformer> list = new LinkedList<MockTransformer>();
        list.add(new MainMockTransformer());
        mockClassLoader.setMockTransformerChain(list);

        // setup custom classloader providing our dynamic class, for MockClassLoader to defer to
        mockClassLoader.deferTo = new ClassLoader(getClass().getClassLoader()) {

            @Override
            public Class<?> loadClass(String name)
                    throws ClassNotFoundException {
                return super.loadClass(name);
            }
        };

        //Try to locate and load a class that is not in MockClassLoader.
        Class<?> dynamicTestClass = Class.forName(DynamicClassHolder.clazz.getName(), false, mockClassLoader);

    }

    // helper class for canFindDynamicClassFromAdjustedClasspath()
    static class MyClassPathAdjuster implements ClassPathAdjuster {
        public void adjustClassPath(ClassPool classPool) {
            classPool.appendClassPath(new ByteArrayClassPath(DynamicClassHolder.clazz.getName(), DynamicClassHolder.classBytes));
        }
    }

    // helper class for canFindDynamicClassFromAdjustedClasspath()
    static class DynamicClassHolder {
        final static byte[] classBytes;
        final static Class<?> clazz;
        static {
            try {
                // construct a new class dynamically
                ClassPool cp = ClassPool.getDefault();
                final CtClass ctClass = cp.makeClass("my.ABCTestClass");
                classBytes = ctClass.toBytecode();
                clazz = ctClass.toClass();
            } catch (Exception e) {
                throw new RuntimeException("Problem constructing custom class", e);
            }
        }
    }
}
