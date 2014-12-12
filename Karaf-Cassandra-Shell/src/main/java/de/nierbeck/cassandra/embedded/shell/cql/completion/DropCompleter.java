package de.nierbeck.cassandra.embedded.shell.cql.completion;

import java.util.List;

import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.CommandLine;
import org.apache.karaf.shell.api.console.Completer;
import org.apache.karaf.shell.api.console.Session;
import org.apache.karaf.shell.support.completers.ArgumentCommandLine;
import org.apache.karaf.shell.support.completers.StringsCompleter;

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

				String[] arguments = commandLine.getArguments();
				int cursorArgumentIndex = commandLine.getCursorArgumentIndex();

				if (cursorArgumentIndex <= 1) {
					delegate.getStrings().add("KEYSPACE");
					delegate.getStrings().add("SCHEMA");
				} else if (cursorArgumentIndex == 2) {
					delegate.getStrings().clear();
					delegate.getStrings().add("IF EXISTS");
					CompleterCommons.completeKeySpace(delegate,
							cassandraSession);
				} else if (cursorArgumentIndex > 2) {
					delegate.getStrings().clear();
					CompleterCommons.completeKeySpace(delegate,
							cassandraSession);
				}


			}
		}
		return delegate.complete(session, commandLine, candidates);

	}

}
