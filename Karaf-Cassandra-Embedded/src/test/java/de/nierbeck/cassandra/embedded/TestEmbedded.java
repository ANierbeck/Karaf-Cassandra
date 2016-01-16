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
package de.nierbeck.cassandra.embedded;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.service.cm.ConfigurationException;

import de.nierbeck.cassandra.embedded.impl.OsgiEmbeddedCassandra;

public class TestEmbedded {

	private static OsgiEmbeddedCassandra cassandra;

	public static void main(String[] args) throws IOException, ConfigurationException {
		cassandra = new OsgiEmbeddedCassandra();
		cassandra.start();

		System.out.println("started");

		cassandra.stop();

		System.out.println("stopped");

		System.out.println("going for restaret");

		cassandra.start();

		System.out.println("restarted");

		System.exit(1);

	}

}
