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
package org.powermock.classloading;

import com.thoughtworks.xstream.XStream;
import org.powermock.classloading.spi.DeepClonerSPI;

/**
 * The purpose of the deep cloner is to create a deep clone of an object. An
 * object can also be cloned to a different class-loader.
 * <p>
 */
public class DeepCloner implements DeepClonerSPI {
    private final XStream xStream;

	/**
	 * Clone using the supplied ClassLoader.
	 */
	public DeepCloner(ClassLoader classLoader) {
        xStream = new XStream();
        xStream.omitField(ClassloaderExecutor.class, "classloader");
        xStream.setClassLoader(classLoader);
	}

	/**
	 * Clone using the current ContextClassLoader.
	 */
	public DeepCloner() {
		this(Thread.currentThread().getContextClassLoader());
	}



	/**
	 * Clones an object.
	 *
	 * @return A deep clone of the object to clone.
	 */
	public <T> T clone(T objectToClone) {
        final String serialized = xStream.toXML(objectToClone);
        return (T) xStream.fromXML(serialized);
    }
}
