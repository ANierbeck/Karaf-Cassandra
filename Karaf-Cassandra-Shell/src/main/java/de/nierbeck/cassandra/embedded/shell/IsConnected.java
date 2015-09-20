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
package de.nierbeck.cassandra.embedded.shell;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import com.datastax.driver.core.Session;

@Command(scope = "cassandra", name = "isConnected", description = "Checks if there is a connected session available")
@Service
public class IsConnected extends CassandraCommandSupport {

	@Override
	public Object doExecute() throws Exception {

		Session session = (Session) this.session
				.get(SessionParameter.CASSANDRA_SESSION);

		boolean isActive = (session != null && !session.isClosed());

		String loggedKeyspace = "";

		if (isActive)
			loggedKeyspace = session.getLoggedKeyspace();

		if (loggedKeyspace == null || loggedKeyspace.isEmpty())
			System.out.println(isActive);
		else
			System.out.println(isActive + ": " + loggedKeyspace);


		return null;
	}

}
