package de.nierbeck.cassandra.embedded.shell.cql.completion;

import java.util.Arrays;
import java.util.List;

import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.CommandLine;
import org.apache.karaf.shell.api.console.Completer;
import org.apache.karaf.shell.api.console.Session;
import org.apache.karaf.shell.support.completers.StringsCompleter;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;

import de.nierbeck.cassandra.embedded.shell.SessionParameter;

@Service
public class SelectsCompleter implements Completer {

	public int complete(Session session, CommandLine commandLine,
			List<String> candidates) {
		StringsCompleter delegate = new StringsCompleter();

		com.datastax.driver.core.Session cassandraSession = (com.datastax.driver.core.Session) session
				.get(SessionParameter.CASSANDRA_SESSION);

		if (cassandraSession == null) {
			System.err
					.println("No active session found--run the connect command first");
			return 0;
		}

		boolean foundStar = false;
		boolean foundFrom = false;
		String foundDot = null;

		List<String> commandLineArgs = Arrays
				.asList(commandLine.getArguments());

		delegate.getStrings().add("*");
		delegate.getStrings().add("FROM");

		if (!commandLineArgs.isEmpty()) {
			foundStar = commandLineArgs.contains("*");
			foundFrom = commandLineArgs.contains("FROM")
					|| commandLineArgs.contains("from");
			for (String string : commandLineArgs) {
				if (string.contains("."))
					foundDot = string;
			}
		}

		String loggedKeyspace = cassandraSession.getLoggedKeyspace();
		if (loggedKeyspace == null && foundStar && foundFrom) {
			CompleterCommons.completeKeySpace(delegate, cassandraSession);
		}

		if ((loggedKeyspace != null || foundDot != null) && foundStar && foundFrom) {
			delegate.getStrings().remove("*");
			delegate.getStrings().remove("FROM");

			String keyspace = loggedKeyspace;
			if (foundDot != null) {
				keyspace = foundDot;
				keyspace = keyspace.substring(0, keyspace.indexOf("."));
			}
			ResultSet execute = cassandraSession
					.execute(String
							.format("select columnfamily_name from system.schema_columnfamilies where keyspace_name = '%s';",
									keyspace));
			if (foundDot != null)
				delegate.getStrings().remove(foundDot);
			for (Row row : execute) {
				String table = row.getString("columnfamily_name");
				if (foundDot != null)
					delegate.getStrings().add(keyspace + "." + table);
				else
					delegate.getStrings().add(table);
			}
		}


		return delegate.complete(session, commandLine, candidates);

	}

}
