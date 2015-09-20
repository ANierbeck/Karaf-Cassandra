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
package de.nierbeck.cassandra.embedded.shell.cql.completion;

import java.util.List;

import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.CommandLine;
import org.apache.karaf.shell.api.console.Completer;
import org.apache.karaf.shell.api.console.Session;
import org.apache.karaf.shell.support.completers.StringsCompleter;

import de.nierbeck.cassandra.embedded.shell.SessionParameter;

@Service
public class KeySpaceCompleter implements Completer {


	public int complete(Session session, CommandLine commandLine,
			List<String> candidates) {
		StringsCompleter delegate = new StringsCompleter();

		com.datastax.driver.core.Session cassandraSession = (com.datastax.driver.core.Session) session
				.get(SessionParameter.CASSANDRA_SESSION);

		if (cassandraSession == null) {
			System.err
					.println("No active session found--run the connect command first");
			return 0;
			// return delegate.complete(session, commandLine, candidates);
		}

		CompleterCommons.completeKeySpace(delegate, cassandraSession);

		return delegate.complete(session, commandLine, candidates);
	}

}
