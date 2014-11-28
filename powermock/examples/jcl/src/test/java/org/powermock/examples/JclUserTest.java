package org.powermock.examples;

import org.apache.commons.logging.Log;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.mockpolicies.JclMockPolicy;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.lang.reflect.Proxy;

import static org.junit.Assert.assertTrue;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

/**
 * Unit tests that assert that the {@link JclMockPolicy} works.
 */
@RunWith(PowerMockRunner.class)
@MockPolicy(JclMockPolicy.class)
public class JclUserTest {

	@Test
	public void assertJclMockPolicyWorks() throws Exception {
		final JclUser tested = new JclUser();

		assertTrue(Proxy.isProxyClass(Whitebox.getInternalState(JclUser.class, Log.class).getClass()));

		replayAll();

		tested.getMessage();

		verifyAll();
	}
}
