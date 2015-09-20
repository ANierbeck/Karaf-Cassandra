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

import de.nierbeck.cassandra.embedded.shell.SessionParameter;

@Service
public class CreateCompleter implements Completer {

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
				 * CREATE ( KEYSPACE | SCHEMA ) IF NOT EXISTS keyspace_name WITH
				 * REPLICATION = map AND DURABLE_WRITES = ( true | false )
				 */

				/*
				 * CREATE TABLE IF NOT EXISTS keyspace_name.table_name
				 * (column_definition, column_definition, ...) WITH property AND
				 * property ...
				 */

				List<String> arguments = Arrays.asList(commandLine
						.getArguments());
				int cursorArgumentIndex = commandLine.getCursorArgumentIndex();

				if (cursorArgumentIndex <= 1) {
					delegate.getStrings().add("KEYSPACE");
					delegate.getStrings().add("TABLE");
				}

				if (cursorArgumentIndex >= 2) {

					String prevArg = commandLine.getArguments()[1];
					switch (prevArg) {
					case "TABLE":
						int index = arguments.indexOf("TABLE");
						if (arguments.contains("TABLE")
								&& arguments.size() > index + 1) {
							delegate.getStrings().add(arguments.get(index + 1));
						}
						delegate.getStrings().add("TABLE");
						delegate.getStrings().remove("KEYSPACE");
						delegate.getStrings().add("(");
						delegate.getStrings().add(")");
						delegate.getStrings().add("PRIMARY KEY");
						break;
					case "KEYSPACE":
					default:
						if (cursorArgumentIndex == 2) {
							delegate.getStrings().clear();
							delegate.getStrings().add("IF NOT EXISTS");
						} else if (cursorArgumentIndex > 2) {
							delegate.getStrings().clear();
							index = arguments.indexOf("KEYSPACE");
							if (arguments.contains("KEYSPACE")
									&& arguments.size() > index + 1) {
								delegate.getStrings().add(
										arguments.get(index + 1));
							}
							delegate.getStrings().add("WITH REPLICATION = {");
							delegate.getStrings().add("'class' :");
							delegate.getStrings().add("'SimpleStrategy'");
							delegate.getStrings().add(",");
							delegate.getStrings().add("'replication_factor' :");
							delegate.getStrings().add("}");
						}
						break;
					}
				}
			}
		}
		return delegate.complete(session, commandLine, candidates);

	}

}
