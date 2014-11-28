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
package samples.privatemocking;

import javax.activation.FileDataSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.Reader;

/**
 * A class used to test the functionality to mock private methods.
 *
 * @author Johan Haleby
 */
public class PrivateMethodDemo {
    public String say(String name) {
        return sayIt(name);
    }

    public String enhancedSay(String firstName, String lastName) {
        return sayIt(firstName, " ", lastName);
    }

    public String sayYear(String name, int years) {
        return doSayYear(years, name);
    }

    private String doSayYear(int years, String name) {
        return "Hello " + name + ", you are " + years + " old.";
    }

    private String sayIt(String firstName, String spacing, String lastName) {
        return "Hello" + firstName + spacing + lastName;
    }

    private String sayIt(String name) {
        return "Hello " + name;
    }

    @SuppressWarnings("unused")
    private String sayIt() {
        return "Hello world";
    }

    public int methodCallingPrimitiveTestMethod() {
        return aTestMethod(10);
    }

    public int methodCallingWrappedTestMethod() {
        return aTestMethod(new Integer(15));
    }

    private int aTestMethod(int aValue) {
        return aValue;
    }

    private Integer aTestMethod(Integer aValue) {
        return aValue;
    }

    public void doArrayStuff(String v) {
        doArrayInternal(new String[]{v});
    }

    private void doArrayInternal(String[] strings) {
    }

    public void doObjectStuff(Object o) {
        doObjectInternal(o);
    }

    private void doObjectInternal(Object o) {
    }

    public int invokeVarArgsMethod(int a, int b) {
        return varArgsMethod(a, b);
    }

    private int varArgsMethod(int... ints) {
        int sum = 0;
        for (int i : ints) {
            sum += i;
        }
        return sum;
    }

    private Reader createReader(File folder, FileDataSource fileDataSource) throws FileNotFoundException {
        return null;
    }
}
