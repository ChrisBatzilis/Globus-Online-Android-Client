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
package demo.org.powermock.examples.tutorial.domainmocking.impl;

import demo.org.powermock.examples.tutorial.domainmocking.EventService;
import demo.org.powermock.examples.tutorial.domainmocking.PersonService;
import demo.org.powermock.examples.tutorial.domainmocking.domain.SampleServiceException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The purpose of this test is to get 100% coverage of the
 * {@link SampleServiceImpl} class without any code changes to that class. To
 * achieve this you need learn how to mock instantiation of domain objects.
 * <p>
 * While doing this tutorial please refer to the documentation on how to mock
 * construction of new objects at the PowerMock web site.
 */
// TODO Specify the PowerMock runner
// TODO Specify which classes that must be prepared for test
public class SampleServiceImplTest_Tutorial {

	private SampleServiceImpl tested;
	private PersonService personServiceMock;
	private EventService eventService;

	@Before
	public void setUp() {
		// TODO Create a mock object of the PersonService and Event service class
		// TODO Create a new instance of SampleServiceImpl and pass in the created mock objects to the constructor
	}

	@After
	public void tearDown() {
		// TODO Set all references to null or use the FieldDefaulter test listener
	}

	@Test
	public void testCreatePerson() throws Exception {
        // TODO	Create a mock object of the BusinessMessages class and mock the a construction of "new BusinessMessages" and instead return the mock
		// TODO Create a mock object of the Person class and mock the a construction of "new Person" and instead return the mock
		// TODO Expect the call to personService.createPerson(..)
		// TODO Expect the call to businessMessages.hasErrors(..) and make it return false
		// TODO Replay all mock objects used and also replay the classes whose constructions were mocked
		// TODO Perform the actual test and assert that the result is false
		// TODO Verify all mock objects used and also verify the classes whose constructions were mocked
	}

	@Test
	public void testCreatePerson_error() throws Exception {
		// TODO	Create a mock object of the BusinessMessages class and mock the a construction of "new BusinessMessages" and instead return the mock
		// TODO Create a mock object of the Person class and mock the a construction of "new Person" and instead return the mock
		// TODO Expect the call to personService.createPerson(..)
		// TODO Expect the call to businessMessages.hasErrors(..) and make it return true
		// TODO Expect the all to eventService.sendErrorEvent(..)
		// TODO Replay all mock objects used and also replay the classes whose constructions were mocked
		// TODO Perform the actual test and assert that the result is false
		// TODO Verify all mock objects used and also verify the classes whose constructions were mocked
	}

	@Test(expected = SampleServiceException.class)
	public void testCreatePerson_illegalName() throws Exception {
		// TODO Create a mock object of the Person class and mock the a construction of "new Person" but instead throw an IllegalArgumentException
		// TODO Replay all mock objects used and also replay the classes whose constructions were mocked
		// TODO Perform the actual test and assert that the result is false
		// TODO Verify all mock objects used and also verify the classes whose constructions were mocked
	}
}
