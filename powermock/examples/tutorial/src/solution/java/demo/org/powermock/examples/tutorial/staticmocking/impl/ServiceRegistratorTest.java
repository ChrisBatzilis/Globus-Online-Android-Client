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
package demo.org.powermock.examples.tutorial.staticmocking.impl;

import demo.org.powermock.examples.tutorial.staticmocking.osgi.BundleContext;
import demo.org.powermock.examples.tutorial.staticmocking.osgi.ServiceRegistration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.junit.Assert.*;
import static org.powermock.api.easymock.PowerMock.*;
import static org.powermock.reflect.Whitebox.getInternalState;
import static org.powermock.reflect.Whitebox.setInternalState;

/**
 * Unit test for the {@link ServiceRegistrator} class.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(IdGenerator.class)
public class ServiceRegistratorTest {
	private ServiceRegistrator tested;

	private BundleContext bundleContextMock;

	private ServiceRegistration serviceRegistrationMock;

	/**
	 * {@inheritDoc}
	 */
	@Before
	public void setUp() {
		tested = new ServiceRegistrator();
		bundleContextMock = createMock(BundleContext.class);
		serviceRegistrationMock = createMock(ServiceRegistration.class);
		mockStatic(IdGenerator.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@After
	public void tearDown() {
		tested = null;
		bundleContextMock = null;
		serviceRegistrationMock = null;
	}

	/**
	 * Test for the {@link ServiceRegistrator#registerService(String, Object)}
	 * method.
	 * 
	 * @throws Exception
	 *             If an error occurs.
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testRegisterService() throws Exception {
		final String name = "a name";
		final Object object = new Object();
		final long expectedId = 42;

		setInternalState(tested, bundleContextMock);

		expect(bundleContextMock.registerService(name, object, null)).andReturn(serviceRegistrationMock);
		expect(IdGenerator.generateNewId()).andReturn(expectedId);

		replayAll();

		final long actualId = tested.registerService(name, object);

		verifyAll();

		Map<Long, ServiceRegistration> map = getInternalState(tested, Map.class);

		assertEquals(1, map.size());
		assertTrue("The id " + actualId + " was not found in the mServiceRegistrations map.", map.containsKey(actualId));
		assertTrue("The service " + serviceRegistrationMock + " was not found in the mServiceRegistrations map.", map
				.containsValue(serviceRegistrationMock));
		assertNotNull(map.get(expectedId));
	}

	/**
	 * Test for the {@link ServiceRegistrator#unregisterService(long)} method.
	 * 
	 * @throws Exception
	 *             If an error occurs.
	 */
	@Test
	public void testUnregisterService() throws Exception {
		Map<Long, ServiceRegistration> map = new HashMap<Long, ServiceRegistration>();
		final long id = 1L;
		map.put(id, serviceRegistrationMock);

		setInternalState(tested, map);

		serviceRegistrationMock.unregister();
		expectLastCall().times(1);

		replayAll();

		tested.unregisterService(id);

		verifyAll();

		assertTrue("Map should be empty", map.isEmpty());

	}

	/**
	 * Test for the {@link ServiceRegistrator#unregisterService(long)} method
	 * when the ID doesn't exist.
	 * 
	 * @throws Exception
	 *             If an error occurs.
	 */
	@Test
	public void testUnregisterService_idDoesntExist() throws Exception {
		Map<Long, ServiceRegistration> map = new HashMap<Long, ServiceRegistration>();
		final long id = 1L;

		setInternalState(tested, map);

		replayAll();

		try {
			tested.unregisterService(id);
			fail("Should throw IllegalStateException");
		} catch (IllegalStateException e) {
			assertEquals("Registration with id " + id + " has already been removed or has never been registered", e.getMessage());
		}

		verifyAll();

		assertTrue("Map should be empty", map.isEmpty());

	}
}
