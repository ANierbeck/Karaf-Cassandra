package de.nierbeck.cassandra.embedded.shell.cql.completion;

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
public class SelectCompleter implements Completer {

	public int complete(Session session, CommandLine commandLine,
			List<String> candidates) {
		StringsCompleter delegate = new StringsCompleter();

		delegate.getStrings().add("*");
		delegate.getStrings().add("FROM");

		com.datastax.driver.core.Session cassandraSession = (com.datastax.driver.core.Session) session
				.get(SessionParameter.CASSANDRA_SESSION);

		ResultSet resultSet = cassandraSession
				.execute("SELECT keyspace_name FROM system.schema_keyspaces;");
		for (Row row : resultSet) {
			delegate.getStrings().add(row.getString("keyspace_name"));
		}

		resultSet = cassandraSession.execute("DESCRIBE TABLES");

		for (Row row : resultSet) {
			delegate.getStrings().add(row.getString("name"));
		}

		return delegate.complete(session, commandLine, candidates);
	}

}
