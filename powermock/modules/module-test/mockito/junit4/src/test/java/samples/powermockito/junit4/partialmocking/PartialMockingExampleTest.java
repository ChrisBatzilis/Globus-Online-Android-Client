/*
 * Copyright 2009 the original author or authors.
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
package samples.powermockito.junit4.partialmocking;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import samples.partialmocking.PartialMockingExample;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.spy;

/**
 * Asserts that partial mocking (spying) with PowerMockito works for non-final
 * methods.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(PartialMockingExample.class)
public class PartialMockingExampleTest {

	@Test
	public void validatingSpiedObjectGivesCorrectNumberOfExpectedInvocations() throws Exception {
		final String expected = "TEST VALUE";
		PartialMockingExample underTest = spy(new PartialMockingExample());
		doReturn(expected).when(underTest).methodToMock();

		assertEquals(expected, underTest.methodToTest());

		verify(underTest).methodToMock();
	}
}
