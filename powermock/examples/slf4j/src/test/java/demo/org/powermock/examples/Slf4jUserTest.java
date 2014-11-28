package demo.org.powermock.examples;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.mockpolicies.Slf4jMockPolicy;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;

import java.lang.reflect.Proxy;

import static org.junit.Assert.assertTrue;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

/**
 * Unit tests that assert that the {@link Slf4jMockPolicy} works.
 */
@RunWith(PowerMockRunner.class)
@MockPolicy(Slf4jMockPolicy.class)
public class Slf4jUserTest {

    @Test
    public void assertSlf4jMockPolicyWorks() throws Exception {
        final Slf4jUser tested = new Slf4jUser();

        replayAll();

        tested.getMessage();

        assertTrue(Proxy.isProxyClass(Whitebox.getInternalState(Slf4jUser.class, Logger.class).getClass()));
        verifyAll();
    }
}
