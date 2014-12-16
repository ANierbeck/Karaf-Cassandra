package de.nierbeck.cassandra.embedded.shell.cql.completion;

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

				String[] arguments = commandLine.getArguments();
				int cursorArgumentIndex = commandLine.getCursorArgumentIndex();

				if (cursorArgumentIndex <= 1) {
					delegate.getStrings().add("KEYSPACE");
					delegate.getStrings().add("SCHEMA");

					// alternative drop a table
					delegate.getStrings().add("TABLE");
				} else if (cursorArgumentIndex == 2) {
					delegate.getStrings().clear();
					delegate.getStrings().add("IF EXISTS");
					// TODO: special handling to differ between keyspaces and
					// tables are needed.
					if (arguments[cursorArgumentIndex - 1]
							.equalsIgnoreCase("KEYSPACE")
							|| arguments[cursorArgumentIndex - 1]
									.equalsIgnoreCase("SCHEMA")
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
//							if (foundDot != null)
//								delegate.getStrings().add(keyspace + "." + table);
//							else
								delegate.getStrings().add(table);
						}
					}
				} else if (cursorArgumentIndex > 2) {
					delegate.getStrings().clear();
					if (arguments[cursorArgumentIndex - 1]
							.equalsIgnoreCase("KEYSPACE")
							|| arguments[cursorArgumentIndex - 1]
									.equalsIgnoreCase("SCHEMA")) {
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
//							if (foundDot != null)
//								delegate.getStrings().add(keyspace + "." + table);
//							else
								delegate.getStrings().add(table);
						}
					}
				}
			}
		}
		return delegate.complete(session, commandLine, candidates);

	}

}
