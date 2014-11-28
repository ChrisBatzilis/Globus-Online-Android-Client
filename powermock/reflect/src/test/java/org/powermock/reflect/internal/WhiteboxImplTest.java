/*
 * Copyright 2008 the original author or authors.
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
package org.powermock.reflect.internal;

import org.junit.Test;
import org.junit.matchers.JUnitMatchers;
import org.powermock.reflect.testclasses.Child;
import org.powermock.reflect.testclasses.ClassWithOverloadedMethods;
import org.powermock.reflect.testclasses.ClassWithStandardMethod;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;
import static org.junit.matchers.JUnitMatchers.hasItem;

/**
 * Unit tests specific to the WhiteboxImpl.
 */
public class WhiteboxImplTest {

	/**
	 * Asserts that a previous bug was fixed.
	 */
	@Test
	public void assertThatClassAndNotStringIsNotSameWhenInvokingCheckIfTypesAreSame() throws Exception {
		Method method = WhiteboxImpl.getMethod(WhiteboxImpl.class, "checkIfParameterTypesAreSame", boolean.class,
				Class[].class, Class[].class);
		boolean invokeMethod = (Boolean) method.invoke(WhiteboxImpl.class, false, new Class<?>[] { Class.class },
				new Class<?>[] { String.class });
		assertFalse(invokeMethod);
	}

	@Test
	public void assertThatClassAndClassIsSameWhenInvokingCheckIfTypesAreSame() throws Exception {
		Method method = WhiteboxImpl.getMethod(WhiteboxImpl.class, "checkIfParameterTypesAreSame", boolean.class,
				Class[].class, Class[].class);
		boolean invokeMethod = (Boolean) method.invoke(WhiteboxImpl.class, false, new Class<?>[] { Class.class },
				new Class<?>[] { Class.class });
		assertTrue(invokeMethod);
	}

	@Test
	public void getBestCandidateMethodReturnsMatchingMethodWhenNoOverloading() throws Exception {
		final Method expectedMethod = ClassWithStandardMethod.class.getDeclaredMethod("myMethod", double.class);
		final Method actualMethod = WhiteboxImpl.getBestMethodCandidate(ClassWithStandardMethod.class, "myMethod",
				new Class<?>[] { double.class }, false);
		assertEquals(expectedMethod, actualMethod);
	}

	@Test
	public void getBestCandidateMethodReturnsMatchingMethodWhenOverloading() throws Exception {
		final Method expectedMethod = ClassWithOverloadedMethods.class.getDeclaredMethod("overloaded", double.class,
				Child.class);
		final Method actualMethod = WhiteboxImpl.getBestMethodCandidate(ClassWithOverloadedMethods.class, "overloaded",
				new Class<?>[] { double.class, Child.class }, false);
		assertEquals(expectedMethod, actualMethod);
	}

    @Test
    public void defaultMethodsAreFound() throws Exception {
        assumeTrue(Float.valueOf(System.getProperty("java.specification.version")) >= 1.8f);

        Method[] methods = WhiteboxImpl.getAllMethods(Collection.class);
        List<String> methodNames = new ArrayList<String>();
        for (Method method : methods) {
            methodNames.add(method.getName());
        }

        assertThat(methodNames, hasItem("stream"));
    }
}
