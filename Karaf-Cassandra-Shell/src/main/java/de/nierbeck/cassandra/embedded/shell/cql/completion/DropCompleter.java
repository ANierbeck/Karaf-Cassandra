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

import java.util.Arrays;
import java.util.List;

import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.CommandLine;
import org.apache.karaf.shell.api.console.Completer;
import org.apache.karaf.shell.api.console.Session;
import org.apache.karaf.shell.support.completers.ArgumentCommandLine;
import org.apache.karaf.shell.support.completers.StringsCompleter;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;

import de.nierbeck.cassandra.embedded.shell.SessionParameter;

@Service
public class DropCompleter implements Completer {

	StringsCompleter delegate = new StringsCompleter(false);

	public int complete(Session session, CommandLine commandLine,
			List<String> candidates) {
		if (session != null) {

			com.datastax.driver.core.Session cassandraSession = (com.datastax.driver.core.Session) session
					.get(SessionParameter.CASSANDRA_SESSION);

			if (cassandraSession == null) {
				System.err
						.println("No active session found--run the connect command first");
				return 0;
			}

			if (commandLine instanceof ArgumentCommandLine) {
				delegate.getStrings().add(commandLine.getCursorArgument());

			} else {
				/*
				 * DROP ( KEYSPACE | SCHEMA ) IF EXISTS keyspace_name
				 */

				/*
				 * DROP TABLE IF EXISTS keyspace_name.table_name
				 */

				List<String> arguments = Arrays.asList(commandLine
						.getArguments());
				int cursorArgumentIndex = commandLine.getCursorArgumentIndex();
				String validArgument; 
				if (cursorArgumentIndex > 1)
					validArgument = arguments.get(cursorArgumentIndex -1);
				else
					validArgument = arguments.get(0);
				
				if (cursorArgumentIndex <= 1) {
					delegate.getStrings().add("KEYSPACE");
					delegate.getStrings().add("SCHEMA");
					// alternative drop a table
					delegate.getStrings().add("TABLE");
				} else if (cursorArgumentIndex >= 2) {
					delegate.getStrings().clear();
					if (cursorArgumentIndex == 2)
						delegate.getStrings().add("IF EXISTS");

					// tables are needed.
					if (validArgument.equalsIgnoreCase("KEYSPACE")
							|| validArgument.equalsIgnoreCase("SCHEMA")
							|| cassandraSession.getLoggedKeyspace() == null) {
						CompleterCommons.completeKeySpace(delegate,
								cassandraSession);
					} else {
						String keyspace = cassandraSession.getLoggedKeyspace();
						ResultSet execute = cassandraSession
								.execute(String
										.format("select columnfamily_name from system.schema_columnfamilies where keyspace_name = '%s';",
												keyspace));
						for (Row row : execute) {
							String table = row.getString("columnfamily_name");
							delegate.getStrings().add(table);
						}
					}
				} 
			}
		}
		return delegate.complete(session, commandLine, candidates);

	}

}
