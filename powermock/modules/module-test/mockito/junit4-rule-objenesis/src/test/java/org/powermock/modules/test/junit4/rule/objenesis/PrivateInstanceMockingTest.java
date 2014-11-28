package org.powermock.modules.test.junit4.rule.objenesis;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.exceptions.base.MockitoAssertionError;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import samples.privateandfinal.PrivateFinal;
import samples.privatemocking.PrivateMethodDemo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.never;
import static org.powermock.api.mockito.PowerMockito.*;

@PrepareForTest( { PrivateMethodDemo.class })
public class PrivateInstanceMockingTest {
    @Rule
    public PowerMockRule powerMockRule = new PowerMockRule();
    
    @Test
    public void expectationsWorkWhenSpyingOnPrivateMethods() throws Exception {
        PrivateMethodDemo tested = spy(new PrivateMethodDemo());
        assertEquals("Hello Temp, you are 50 old.", tested.sayYear("Temp", 50));

        when(tested, "doSayYear", 12, "test").thenReturn("another");

        assertEquals("Hello Johan, you are 29 old.", tested.sayYear("Johan", 29));
        assertEquals("another", tested.sayYear("test", 12));

        verifyPrivate(tested).invoke("doSayYear", 12, "test");
    }

    @Test
    public void expectationsWorkWithArgumentMatchersWhenSpyingOnPrivateMethods() throws Exception {
        PrivateMethodDemo tested = spy(new PrivateMethodDemo());
        assertEquals("Hello Temp, you are 50 old.", tested.sayYear("Temp", 50));

        when(tested, "doSayYear", Mockito.anyInt(), Mockito.anyString()).thenReturn("another");

        assertEquals("another", tested.sayYear("Johan", 29));
        assertEquals("another", tested.sayYear("test", 12));

        verifyPrivate(tested).invoke("doSayYear", 29, "Johan");
        verifyPrivate(tested).invoke("doSayYear", 12, "test");
        verifyPrivate(tested).invoke("doSayYear", 50, "Temp");
    }

    @Test
    public void errorousVerificationOnPrivateMethodGivesFilteredErrorMessage() throws Exception {
        PrivateMethodDemo tested = spy(new PrivateMethodDemo());
        assertEquals("Hello Temp, you are 50 old.", tested.sayYear("Temp", 50));

        when(tested, "doSayYear", Mockito.anyInt(), Mockito.anyString()).thenReturn("another");

        assertEquals("another", tested.sayYear("Johan", 29));
        assertEquals("another", tested.sayYear("test", 12));

        try {
            verifyPrivate(tested, never()).invoke("doSayYear", 50, "Temp");
            fail("Should throw assertion error");
        } catch (MockitoAssertionError e) {
            assertEquals("\nsamples.privatemocking.PrivateMethodDemo.doSayYear(\n    50,\n    \"Temp\"\n);\nNever wanted  but invoked .", e
                    .getMessage());
        }
    }

    @Test(expected = ArrayStoreException.class)
    public void expectationsWorkWhenSpyingOnPrivateVoidMethods() throws Exception {
        PrivateMethodDemo tested = spy(new PrivateMethodDemo());

        tested.doObjectStuff(new Object());

        when(tested, "doObjectInternal", isA(Object.class)).thenThrow(new ArrayStoreException());

        tested.doObjectStuff(new Object());
    }

    @Test
    public void answersWorkWhenSpyingOnPrivateVoidMethods() throws Exception {
        PrivateMethodDemo tested = spy(new PrivateMethodDemo());

        tested.doObjectStuff(new Object());

        when(tested, "doObjectInternal", isA(String.class)).thenAnswer(new Answer<Void>() {
            private static final long serialVersionUID = 20645008237481667L;

            public Void answer(InvocationOnMock invocation) throws Throwable {
                assertEquals("Testing", invocation.getArguments()[0]);
                return null;
            }
        });
        tested.doObjectStuff(new Object());
        tested.doObjectStuff("Testing");
    }

    @Test
    public void spyingOnPrivateFinalMethodsWorksWhenClassIsNotFinal() throws Exception {
        PrivateFinal tested = spy(new PrivateFinal());

        final String name = "test";
        tested.say(name);
        assertEquals("Hello " + name, tested.say(name));

        when(tested, "sayIt", name).thenReturn("First", "Second");

        assertEquals("First", tested.say(name));
        assertEquals("Second", tested.say(name));
    }
}
