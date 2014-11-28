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

import org.powermock.classloading.ClassloaderExecutor;
import org.powermock.core.classloader.MockClassLoader;
import org.powermock.core.transformers.MockTransformer;
import org.powermock.core.transformers.impl.MainMockTransformer;
import org.powermock.reflect.Whitebox;
import org.powermock.reflect.proxyframework.RegisterProxyFramework;
import org.powermock.tests.utils.impl.MockPolicyInitializerImpl;
import org.powermock.tests.utils.impl.PowerMockIgnorePackagesExtractorImpl;
import org.powermock.tests.utils.impl.PrepareForTestExtractorImpl;
import org.powermock.tests.utils.impl.StaticConstructorSuppressExtractorImpl;

import java.util.ArrayList;
import java.util.List;

public class PowerMockClassloaderExecutor {

    public static ClassloaderExecutor forClass(Class<?> testClass) {
        List<MockTransformer> mockTransformerChain = new ArrayList<MockTransformer>();
        final MainMockTransformer mainMockTransformer = new MainMockTransformer();
        mockTransformerChain.add(mainMockTransformer);
    
        String[] classesToLoadByMockClassloader = new String[0];
        String[] packagesToIgnore = new String[0];
        MockClassLoader mockLoader = new MockClassLoader(classesToLoadByMockClassloader, packagesToIgnore);
        mockLoader.setMockTransformerChain(mockTransformerChain);
        PrepareForTestExtractorImpl testClassesExtractor = new PrepareForTestExtractorImpl();
        StaticConstructorSuppressExtractorImpl staticInitializationExtractor = new StaticConstructorSuppressExtractorImpl();
        PowerMockIgnorePackagesExtractorImpl ignorePackagesExtractor = new PowerMockIgnorePackagesExtractorImpl();
    
        mockLoader.addIgnorePackage(ignorePackagesExtractor.getPackagesToIgnore(testClass));
        mockLoader.addClassesToModify(testClassesExtractor.getTestClasses(testClass));
        mockLoader.addClassesToModify(staticInitializationExtractor.getTestClasses(testClass));
        registerProxyframework(mockLoader);
        new MockPolicyInitializerImpl(testClass).initialize(mockLoader);
        return new ClassloaderExecutor(mockLoader);
    }

    private static void registerProxyframework(ClassLoader classLoader) {
        Class<?> proxyFrameworkClass = null;
        try {
            proxyFrameworkClass = Class.forName("org.powermock.api.extension.proxyframework.ProxyFrameworkImpl", false, classLoader);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "Extension API internal error: org.powermock.api.extension.proxyframework.ProxyFrameworkImpl could not be located in classpath.");
        }

        Class<?> proxyFrameworkRegistrar = null;
        try {
            proxyFrameworkRegistrar = Class.forName(RegisterProxyFramework.class.getName(), false, classLoader);
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
