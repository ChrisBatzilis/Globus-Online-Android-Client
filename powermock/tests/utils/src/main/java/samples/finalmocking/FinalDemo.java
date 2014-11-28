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
package samples.finalmocking;

import samples.simplemix.SimpleMix;
import samples.simplereturn.SimpleReturnExample;

public final class FinalDemo {

	public final String say(String string) {
		return "Hello " + string;
	}

	public final void finalVoidCaller() {
		finalVoidCallee();
	}

	public final void finalVoidCallee() {
		System.err.println("void method");
	}

	public final native String sayFinalNative(String string);

    public final SimpleReturnExample simpleReturnExample() {
        return new SimpleReturnExample();
    }

    public final SimpleMix simpleMix() {
        return new SimpleMix();
    }
}
