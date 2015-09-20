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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;

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
public class InsertCompleter implements Completer {

	StringsCompleter delegate = new StringsCompleter(false);

	public int complete(Session session, CommandLine commandLine, List<String> candidates) {
		if (session != null) {

			com.datastax.driver.core.Session cassandraSession = (com.datastax.driver.core.Session) session
					.get(SessionParameter.CASSANDRA_SESSION);

			if (cassandraSession == null) {
				System.err.println("No active session found--run the connect command first");
				return 0;
			}

			if (commandLine instanceof ArgumentCommandLine) {
				delegate.getStrings().add(commandLine.getCursorArgument());

			} else {

				/*
				 * INSERT INTO keyspace_name.table_name
				 *	  ( column_name, column_name...)
				 *	  VALUES ( value, value ... ) IF NOT EXISTS
				 *	  USING option AND option
				 *	
				 */
				
				//match select expression till we find FROM
				List<String> arguments = Arrays.asList(commandLine
						.getArguments());
				int cursorArgumentIndex = commandLine.getCursorArgumentIndex();
				String validArgument; 
				if (cursorArgumentIndex > 1)
					validArgument = arguments.get(cursorArgumentIndex -1);
				else
					validArgument = arguments.get(0);
				
				String currentArgument = null;
				if(cursorArgumentIndex < arguments.size()) {
					currentArgument = arguments.get(cursorArgumentIndex);
				}
				
				String loggedKeyspace = cassandraSession.getLoggedKeyspace();

				if (cursorArgumentIndex <= 1) {
					delegate.getStrings().add("INTO");
				} else if (cursorArgumentIndex > 1) {
					//now it's either FROM or a list or somewhere in the Count ...
					delegate.getStrings().clear();
					if ("INTO".equalsIgnoreCase(validArgument) && loggedKeyspace == null && (currentArgument == null || !currentArgument.contains("."))) {
						delegate.getStrings().clear();
						CompleterCommons.completeKeySpace(delegate, cassandraSession);
						
						SortedSet<String> strings = delegate.getStrings();
						List<String> toDelete = new ArrayList<>();
						List<String> toAdd = new ArrayList<>();
						for (String string : strings) {
							toDelete.add(string);
							toAdd.add(string+".");
						}
						delegate.getStrings().removeAll(toDelete);
						delegate.getStrings().addAll(toAdd);
					} else if ("INTO".equalsIgnoreCase(validArgument) && loggedKeyspace != null) {
						//keyspace selected add Tables to completion
						delegate.getStrings().clear();
						ResultSet execute = cassandraSession.execute(String.format(
								"select columnfamily_name from system.schema_columnfamilies where keyspace_name = '%s';",
								loggedKeyspace));
						for (Row row : execute) {
							String table = row.getString("columnfamily_name");
							delegate.getStrings().add(table);
						}
					} else if ("INTO".equalsIgnoreCase(validArgument) && currentArgument != null && currentArgument.contains(".")) {
						//keyspace not selected need to complete keyspace with tables
						delegate.getStrings().clear();
						String keyspace = currentArgument.substring(0, currentArgument.indexOf("."));
						ResultSet execute = cassandraSession.execute(String.format(
								"select columnfamily_name from system.schema_columnfamilies where keyspace_name = '%s';",
								keyspace));
						for (Row row : execute) {
							String table = row.getString("columnfamily_name");
							delegate.getStrings().add(keyspace + "." + table);
						}
					} else if (!arguments.contains("INTO")) {
						delegate.getStrings().add("INTO");	
					} else if (commandLine.getBuffer().contains("(") && !commandLine.getBuffer().contains("VALUES") && !commandLine.getBuffer().contains(")")) {
						delegate.getStrings().clear();
						//select columns from selected table
						String buffer = commandLine.getBuffer();
						int fromIndx = buffer.indexOf("INTO") + 4;
						int whereIndx = buffer.indexOf("(");
						String tableSelection = buffer.substring(fromIndx, whereIndx);
						String keyspace = loggedKeyspace;
						String table = tableSelection.trim();
						if (tableSelection.contains(".")) {
							String[] split = tableSelection.split("\\.");
							keyspace = split[0].trim();
							table = split[1].trim();
						}
						String select = String.format("SELECT columnfamily_name, column_name FROM system.schema_columns where keyspace_name = '%s';", keyspace);
						ResultSet execute = cassandraSession.execute(select);
						for (Row row : execute) {
							String tableName = row.getString("columnfamily_name");
							if (table.equalsIgnoreCase(tableName))
								delegate.getStrings().add(row.getString("column_name"));
						}
						delegate.getStrings().add(",");
					} else if (commandLine.getBuffer().contains(")") && !commandLine.getBuffer().contains("VALUES")) {
						// found closing bracket but no VALUES, therefore add VALUES to it
						delegate.getStrings().clear();
						delegate.getStrings().add("VALUES");
					} else if ("VALUES".equalsIgnoreCase(validArgument)) {
						delegate.getStrings().clear();
						delegate.getStrings().add("(");
						delegate.getStrings().add(")");
					} else if (commandLine.getBuffer().contains("INTO")) {
						//we are done with into and we are actually after it, so it might very well be that the column name has been selected. ( ) to it. 
						delegate.getStrings().clear();
						delegate.getStrings().add("(");
						delegate.getStrings().add(")");
					} else {
						//nothing matched so far so we might be at the end of the select, so add a WHERE as posibility
						delegate.getStrings().clear();
						if (!commandLine.getBuffer().contains("IF NOT EXISTS"))
							delegate.getStrings().add("IF NOT EXISTS");
						if (!commandLine.getBuffer().contains("USING"))
							delegate.getStrings().add("USING");
						delegate.getStrings().add("AND");
						delegate.getStrings().add("TTL");
					}
					
				}
				
			}
		}
		return delegate.complete(session, commandLine, candidates);

	}
}
