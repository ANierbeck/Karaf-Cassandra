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

			List<String> arguments = Arrays.asList(commandLine.getArguments());

			int argPos = 0;
			if (commandLine.getArguments()[0].equalsIgnoreCase("CREATE"))
				argPos++;

			if (commandLine.getArgumentPosition() <= argPos) {
				delegate.getStrings().add("KEYSPACE");
				delegate.getStrings().add("TABLE");
			}

			if (commandLine.getArgumentPosition() >= argPos + 1) {

				String prevArg = commandLine.getArguments()[argPos];
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
					index = arguments.indexOf("KEYSPACE");
					if (arguments.contains("KEYSPACE")
							&& arguments.size() > index + 1) {
						delegate.getStrings().add(arguments.get(index + 1));
					}
					delegate.getStrings().add("KEYSPACE");
					delegate.getStrings().remove("TABLE");
					delegate.getStrings().add("WITH REPLICATION = {");
					delegate.getStrings().add("'class' :");
					delegate.getStrings().add("'SimpleStrategy'");
					delegate.getStrings().add(",");
					delegate.getStrings().add("'replication_factor' :");
					delegate.getStrings().add("}");
					break;
				}

			}
			if (commandLine instanceof ArgumentCommandLine) {
				// sometimes it completes on single arguments, those should be
				// added cause otherwise the completion doesn't work at all.
				delegate.getStrings().add(commandLine.getCursorArgument());
			}
		}
		return delegate.complete(session, commandLine, candidates);

	}

}
