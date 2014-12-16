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
public class SelectsCompleter implements Completer {

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
				 *   SELECT select_expression
				 *	  FROM keyspace_name.table_name
				 *	  WHERE relation AND relation ... 
				 *	  ORDER BY ( clustering_column ( ASC | DESC )...)
				 *	  LIMIT n
				 *	  ALLOW FILTERING
				 *	select expression is:
				 *	
				 *	selection_list
				 *	| DISTINCT selection_list
				 *	| ( COUNT ( * | 1 ) )
				 *	selection_list is one of:
				 *	A list of partition keys (used with DISTINCT)
				 *	selector AS alias, selector AS alias, ...| * 
				 *	alias is an alias for a column name.
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
					delegate.getStrings().add("*");
					delegate.getStrings().add("DISTINCT");
					delegate.getStrings().add("COUNT");
				} else  if (cursorArgumentIndex == 2) {
					//first input, see if it is a star, distrinct, count or a selection_list
					delegate.getStrings().clear();
					if ("COUNT".equalsIgnoreCase(validArgument)) {
						//count completion needed
						delegate.getStrings().add("(");
						delegate.getStrings().add("*");
						delegate.getStrings().add("1");
						delegate.getStrings().add(")");
						session.put("isCount", true);
					} else if("*".equalsIgnoreCase(validArgument)) {
						delegate.getStrings().add("FROM");
					} else if("DISTINCT".equalsIgnoreCase(validArgument)) {
						//nothing to add, it's specific to the selection_list
					} else {
						delegate.getStrings().add("FROM");
						delegate.getStrings().add("AS");
						delegate.getStrings().add(",");
					}
				} else if (cursorArgumentIndex > 2) {
					//now it's either FROM or a list or somewhere in the Count ...
					delegate.getStrings().clear();
					Boolean isCount = (Boolean) session.get("isCount");
					if (isCount != null && isCount) {
						switch (validArgument) {
						case "'('":
							delegate.getStrings().add("*");
							delegate.getStrings().add("1");
							delegate.getStrings().add(")");
							break;
						case "*":
						case "1":
							delegate.getStrings().add(")");
							break;
						case "')'":
						default:
							delegate.getStrings().add("FROM");
							session.put("isCount", false);
							break;
						}
					} else if ("FROM".equalsIgnoreCase(validArgument) && loggedKeyspace == null && (currentArgument == null || !currentArgument.contains("."))) {
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
					} else if ("FROM".equalsIgnoreCase(validArgument) && loggedKeyspace != null) {
						//keyspace selected add Tables to completion
						delegate.getStrings().clear();
						ResultSet execute = cassandraSession.execute(String.format(
								"select columnfamily_name from system.schema_columnfamilies where keyspace_name = '%s';",
								loggedKeyspace));
						for (Row row : execute) {
							String table = row.getString("columnfamily_name");
							delegate.getStrings().add(table);
						}
					} else if ("FROM".equalsIgnoreCase(validArgument) && currentArgument != null && currentArgument.contains(".")) {
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
					} else if (!arguments.contains("FROM")) {
						delegate.getStrings().add("FROM");	
					} else if ("WHERE".equalsIgnoreCase(validArgument)) {
						delegate.getStrings().clear();
						//select columns from selected table
						String buffer = commandLine.getBuffer();
						int fromIndx = buffer.indexOf("FROM") + 4;
						int whereIndx = buffer.indexOf("WHERE");
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
					} else if (commandLine.getBuffer().contains("WHERE")) {
						//we are done with where and we are actually after it, so it might very well be that the column name has been selected. Add '=' to it. 
						delegate.getStrings().clear();
						//CONTAINS, CONTAINS KEY, IN, =, >, >=, <, or <=
						delegate.getStrings().add("=");
						delegate.getStrings().add("IN");
						delegate.getStrings().add("CONTAINS");
						delegate.getStrings().add("KEY");
						delegate.getStrings().add(">");
						delegate.getStrings().add(">=");
						delegate.getStrings().add("<");
						delegate.getStrings().add("<=");
					} else {
						//nothing matched so far so we might be at the end of the select, so add a WHERE as posibility
						delegate.getStrings().clear();
						delegate.getStrings().add("WHERE");
					}
					
				}
				
			}
		}
		return delegate.complete(session, commandLine, candidates);

	}
}
