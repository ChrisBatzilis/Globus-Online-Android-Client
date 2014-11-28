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
package demo.org.powermock.examples.tutorial.partialmocking.service.impl;

import demo.org.powermock.examples.tutorial.partialmocking.dao.ProviderDao;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The purpose of this test is to get 100% coverage of the
 * {@link ProviderServiceImpl} class without any code changes to that class. To
 * achieve this you need learn how to create partial mocks, modify internal
 * state, invoke and expect private methods.
 * <p>
 * While doing this tutorial please refer to the documentation on how to expect
 * private methods and bypass encapsulation at the PowerMock web site.
 */
// TODO Specify the PowerMock runner
// TODO Specify which classes that must be prepared for test
public class ProviderServiceImplTest_Tutorial {
	
	private ProviderServiceImpl tested;
	private ProviderDao providerDaoMock;


	@Before
	public void setUp() {
		// TODO Create a mock object of the ProviderDao class
		// TODO Create a new instance of ProviderServiceImpl
		// TODO Set the providerDao mock to the providerDao field in the tested instance
	}

	@After
	public void tearDown() {
		// TODO Set all references to null
	}

	@Test
	public void testGetAllServiceProviders() throws Exception {
		// TODO Create a partial mock of the ProviderServiceImpl mocking only the getAllServiceProducers method
		// TODO Create a new HashSet of ServiceProducer's and add a ServiceProducer to the set
		// TODO Expect the private method call to getAllServiceProducers and return the created HashSet
		// TODO Replay all mock objects used
		// TODO Perform the actual test and assert that the result matches the expectations  
		// TODO Verify all mock objects used
	}

	@Test
	public void testGetAllServiceProviders_noServiceProvidersFound() throws Exception {
		// TODO Create a partial mock of the ProviderServiceImpl mocking only the getAllServiceProducers method
		// TODO Expect the private method call to getAllServiceProducers and return null
		// TODO Replay all mock objects used
		// TODO Perform the actual test and assert that the result matches the expectations 
		// TODO Verify all mock objects used 
	}

	@Test
	public void testServiceProvider_found() throws Exception {
		// TODO Create a partial mock of the ProviderServiceImpl mocking only the getAllServiceProducers method
		// TODO Create a new HashSet of ServiceProducer's and add a ServiceProducer to the set
		// TODO Expect the private method call to getAllServiceProducers and return the created HashSet
		// TODO Replay all mock objects used
		// TODO Perform the actual test and assert that the result matches the expectations  
		// TODO Verify all mock objects used
	}

	@Test
	public void testServiceProvider_notFound() throws Exception {
		// TODO Create a partial mock of the ProviderServiceImpl mocking only the getAllServiceProducers method
		// TODO Expect the private method call to getAllServiceProducers and return null
		// TODO Replay all mock objects used
		// TODO Perform the actual test and assert that the result matches the expectations 
		// TODO Verify all mock objects used 
	}

	@Test
	public void getAllServiceProducers() throws Exception {
		// TODO Create a new ServiceArtifact and a new HashSet place the created ServiceArtifact in this set
		// TODO Expect the call to the providerDao.getAllServiceProducers(..) and return the HashSet
		// TODO Replay all mock objects used
		// TODO Perform the actual test by invoking the private "getAllServiceProducers" method. Assert that the result matches the expectations.
		// TODO Verify all mock objects used
	}

	@Test
	public void getAllServiceProducers_empty() throws Exception {
		// TODO Create a new HashSet of ServiceArtifacts
		// TODO Replay all mock objects used
		// TODO Perform the actual test by invoking the private "getAllServiceProducers" method. Assert that the result matches the expectations.
		// TODO Verify all mock objects used
	}
}
