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
package powermock.examples.staticmocking;

import java.util.HashMap;
import java.util.Map;

public class ServiceRegistrator {

	/**
	 * Holds all services that has been registered to this service registry.
	 */
	private final Map<Long, Object> serviceRegistry = new HashMap<Long, Object>();

	public long registerService(Object service) {
		final long id = IdGenerator.generateNewId();
		serviceRegistry.put(id, service);
		return id;
	}

}
