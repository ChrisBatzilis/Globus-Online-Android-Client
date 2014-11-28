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
package samples.suppressconstructor;

public class SuppressConstructorHierarchy extends SuppressConstructorHierarchyParent {

	public SuppressConstructorHierarchy(String message) {
		super(message);
	}

	/**
	 * This method is just here to check if it works to execute several tests
	 * with the same test suite class loader.
	 * 
	 * @return 42.
	 */
	public int getNumber() {
		return 42;
	}

}
