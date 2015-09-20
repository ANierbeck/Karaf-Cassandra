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
package de.nierbeck.cassandra.admin.shell;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.nierbeck.cassandra.embedded.CassandraService;

@Command(scope = "cassandra-admin", name = "cleanup", description = "Cleans the keyspace of the embedded Cassandra server.")
@Service
public class CleanupKeyspace implements Action {

	@Reference
	CassandraService cassandraServer;

	@Override
	public Object execute() throws Exception {
		if (!cassandraServer.isRunning()) {
			System.err.println("To clean the keyspace it is necessary to have the embedded server running.");
			return null;
		}
		cassandraServer.cleanUp();
		
		System.out
				.println("All keyspaces of the embedded server have been cleaned.");
		return null;
	}

}
