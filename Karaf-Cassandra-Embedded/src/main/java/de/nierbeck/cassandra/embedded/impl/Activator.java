/*
 *    Copyright 2015 Achim Nierbeck
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package de.nierbeck.cassandra.embedded.impl;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nierbeck.cassandra.embedded.CassandraService;

public class Activator implements BundleActivator{

	static Logger log = LoggerFactory.getLogger(Activator.class);

	private OsgiEmbeddedCassandra cassandra;

	private ServiceRegistration<?> cassandraService;

	@SuppressWarnings("unchecked")
	@Override
	public void start(BundleContext context) throws Exception {
		cassandra = new OsgiEmbeddedCassandra();

		@SuppressWarnings("rawtypes")
		Dictionary props = new Hashtable();
		props.put("service.pid", "de.nierbeck.cassandra,embedded");
		cassandraService = context.registerService(new String[] {
						ManagedService.class.getName(),
						CassandraService.class.getName() },
				cassandra, props);

	}

	@Override
	public void stop(BundleContext context) throws Exception {
		if (cassandraService != null) {
			cassandraService.unregister();
			cassandraService = null;
			if (cassandra.isRunning())
				cassandra.stop();
		}
	}
}
