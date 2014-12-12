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
