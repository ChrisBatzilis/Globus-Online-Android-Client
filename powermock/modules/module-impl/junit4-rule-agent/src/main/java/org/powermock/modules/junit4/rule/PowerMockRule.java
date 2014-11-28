/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.powermock.modules.junit4.rule;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.powermock.core.MockRepository;
import org.powermock.modules.agent.PowerMockAgent;
import org.powermock.modules.agent.support.PowerMockAgentTestInitializer;
import org.powermock.reflect.Whitebox;
import org.powermock.reflect.proxyframework.RegisterProxyFramework;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

public class PowerMockRule implements MethodRule {
    static {
        if (PowerMockRule.class.getClassLoader() != ClassLoader.getSystemClassLoader()) {
            throw new IllegalStateException("PowerMockRule can only be used with the system classloader but was loaded by " + PowerMockRule.class.getClassLoader());
        }
        PowerMockAgent.initializeIfPossible();
    }

    public Statement apply(Statement base, FrameworkMethod method, Object target) {
        PowerMockAgentTestInitializer.initialize(target.getClass());

        return new PowerMockStatement(base, target);
    }
}

class PowerMockStatement extends Statement {
    private static final String ANNOTATION_ENABLER = "org.powermock.api.extension.listener.AnnotationEnabler";
    private final Statement fNext;
    private final Object target;

    public PowerMockStatement(Statement base, Object target) {
        fNext = base;
        this.target = target;
    }

    @Override
    public void evaluate() throws Throwable {
        Object annotationEnabler = loadAnnotationEnableIfPresent();
        try {
            injectMocksUsingAnnotationEnabler(target, annotationEnabler);
            registerProxyFramework();
            fNext.evaluate();
        } finally {
            // Clear the mock repository after each test
            MockRepository.clear();
            clearMockFields(target, annotationEnabler);
        }
    }

    private Object loadAnnotationEnableIfPresent() {
        boolean hasAnnotationEnabler = hasClass(ANNOTATION_ENABLER);
        if (!hasAnnotationEnabler) {
            return null;
        }

        try {
            return Whitebox.invokeConstructor(Class.forName(ANNOTATION_ENABLER, true, Thread.currentThread().getContextClassLoader()));
        } catch (Exception e) {
            throw new RuntimeException("PowerMock internal error, failed to load annotation enabler.");
        }
    }

    private boolean hasClass(String className) {
        try {
            Class.forName(className, false, Thread.currentThread().getContextClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private void clearMockFields(Object target, Object annotationEnabler) throws Exception {
        if (annotationEnabler != null) {
            Class<? extends Annotation>[] mockAnnotations = Whitebox.invokeMethod(annotationEnabler, "getMockAnnotations");
            Set<Field> mockFields = Whitebox.getFieldsAnnotatedWith(target, mockAnnotations);
            for (Field field : mockFields) {
                field.set(target, null);
            }
        }
    }

    private void injectMocksUsingAnnotationEnabler(Object target, Object annotationEnabler) throws Exception {
        if (annotationEnabler != null) {
            Whitebox.invokeMethod(annotationEnabler, "beforeTestMethod", new Class<?>[]{Object.class, Method.class,
                    Object[].class}, target, null, null);
        }
    }

    private static void registerProxyFramework() {
        Class<?> proxyFrameworkClass;
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            proxyFrameworkClass = Class.forName("org.powermock.api.extension.proxyframework.ProxyFrameworkImpl", false, contextClassLoader);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "Extension API error: org.powermock.api.extension.proxyframework.ProxyFrameworkImpl could not be located in classpath.");
        }

        Class<?> proxyFrameworkRegistrar = null;
        try {
            proxyFrameworkRegistrar = Class.forName(RegisterProxyFramework.class.getName(), false, contextClassLoader);
        } catch (ClassNotFoundException e) {
            // Should never happen
            throw new RuntimeException(e);
        }
        try {
            Whitebox.invokeMethod(proxyFrameworkRegistrar, "registerProxyFramework", Whitebox.newInstance(proxyFrameworkClass));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
