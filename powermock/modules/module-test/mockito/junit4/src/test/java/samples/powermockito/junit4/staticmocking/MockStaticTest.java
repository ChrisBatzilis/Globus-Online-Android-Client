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
package samples.powermockito.junit4.staticmocking;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.exceptions.base.MockitoAssertionError;
import org.mockito.exceptions.verification.TooLittleActualInvocations;
import org.mockito.exceptions.verification.junit.ArgumentsAreDifferent;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import samples.singleton.SimpleStaticService;
import samples.singleton.StaticService;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.doThrow;
import static org.powermock.api.mockito.PowerMockito.*;
import static org.powermock.api.mockito.PowerMockito.spy;

/**
 * Test class to demonstrate static mocking with PowerMockito.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest( { StaticService.class, SimpleStaticService.class })
public class MockStaticTest {

	@Test
	public void testMockStaticNoExpectations() throws Exception {
		mockStatic(StaticService.class);
		assertNull(StaticService.say("hello"));

		// Verification is done in two steps using static methods.
		verifyStatic();
		StaticService.say("hello");
	}

	@Test
	public void testMockStaticWithExpectations() throws Exception {
		final String expected = "Hello world";
		final String argument = "hello";

		mockStatic(StaticService.class);

		when(StaticService.say(argument)).thenReturn(expected);

		assertEquals(expected, StaticService.say(argument));

		// Verification is done in two steps using static methods.
		verifyStatic();
		StaticService.say(argument);
	}

	@Test
	public void errorousVerificationOfStaticMethodsGivesANonMockitoStandardMessage() throws Exception {
		final String expected = "Hello world";
		final String argument = "hello";

		mockStatic(StaticService.class);

		when(StaticService.say(argument)).thenReturn(expected);

		assertEquals(expected, StaticService.say(argument));

		// Verification is done in two steps using static methods.
		verifyStatic(times(2));
		try {
			StaticService.say(argument);
			fail("Should throw assertion error");
		} catch (MockitoAssertionError e) {
			assertEquals("\nsamples.singleton.StaticService.say(\"hello\");\nWanted 2 times but was 1 time.", e.getMessage());
		}
	}

	@Test(expected = IllegalStateException.class)
	public void testMockStaticThatThrowsException() throws Exception {
		final String argument = "hello";

		mockStatic(StaticService.class);

		when(StaticService.say(argument)).thenThrow(new IllegalStateException());

		StaticService.say(argument);
	}

	@Test(expected = ArgumentsAreDifferent.class)
    // TODO Fix error message!!
	public void testMockStaticVerificationFails() throws Exception {
		mockStatic(StaticService.class);
		assertNull(StaticService.say("hello"));

		// Verification is done in two steps using static methods.
		verifyStatic();
		StaticService.say("Hello");
	}

	@Test
	public void testMockStaticAtLeastOnce() throws Exception {
		mockStatic(StaticService.class);
		assertNull(StaticService.say("hello"));
		assertNull(StaticService.say("hello"));

		// Verification is done in two steps using static methods.
		verifyStatic(atLeastOnce());
		StaticService.say("hello");
	}

	@Test
	public void testMockStaticCorrectTimes() throws Exception {
		mockStatic(StaticService.class);
		assertNull(StaticService.say("hello"));
		assertNull(StaticService.say("hello"));

		// Verification is done in two steps using static methods.
		verifyStatic(times(2));
		StaticService.say("hello");
	}

	@Test(expected = TooLittleActualInvocations.class)
	public void testMockStaticIncorrectTimes() throws Exception {
		mockStatic(StaticService.class);
		assertNull(StaticService.say("hello"));
		assertNull(StaticService.say("hello"));

		// Verification is done in two steps using static methods.
		verifyStatic(times(3));
		StaticService.say("hello");
	}

	@Test
	public void testMockStaticVoidWithNoExpectations() throws Exception {
		mockStatic(StaticService.class);

		StaticService.sayHello();

		verifyStatic();
		StaticService.sayHello();
	}

	@Test(expected = ArrayStoreException.class)
	public void testMockStaticVoidWhenThrowingException() throws Exception {
		mockStatic(StaticService.class);

		// Expectations
		doThrow(new ArrayStoreException("Mock error")).when(StaticService.class);
		StaticService.sayHello();

		// Test
		StaticService.sayHello();
	}

	@Test
	public void testSpyOnStaticMethods() throws Exception {
		spy(StaticService.class);

		String expectedMockValue = "expected";
		when(StaticService.say("world")).thenReturn(expectedMockValue);

		assertEquals(expectedMockValue, StaticService.say("world"));
		assertEquals("Hello world2", StaticService.say("world2"));
	}

    @Test
	public void mockStatic_uses_var_args_to_create_multiple_static_mocks() throws Exception {
        mockStatic(StaticService.class, SimpleStaticService.class);

        when(SimpleStaticService.say("Something")).thenReturn("other");

        StaticService.sayHello();
        final String said = SimpleStaticService.say("Something");

        verifyStatic();
        StaticService.sayHello();
        verifyStatic();
        SimpleStaticService.say("Something");

        assertEquals(said, "other");
	}

    @Test
    public void spyingUsingArgumentCaptor() throws Exception {
        // Given
        mockStatic(StaticService.class);
        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        StaticService.say("something");

        verifyStatic();
        StaticService.say(captor.capture());

        assertEquals("something", captor.getValue());
    }
}
