package de.nierbeck.cassandra.embedded.shell.cql;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;

import de.nierbeck.cassandra.embedded.shell.SessionParameter;
import de.nierbeck.cassandra.embedded.shell.cql.completion.KeySpaceCompleter;

@Command(scope = "cassandra:cqlsh", name = "USE", description = "execute USE cql commands")
@Service
public class UseCommand implements Action {


	@Reference
	protected org.apache.karaf.shell.api.console.Session session;

	@Argument(name = "keyspace", description = "keyspace to USE", required = true, multiValued = false)
	@Completion(caseSensitive = false, value = KeySpaceCompleter.class)
	private String keyspace;

	public Object execute() throws Exception {
		Session session = (Session) this.session
				.get(SessionParameter.CASSANDRA_SESSION);

		if (session == null) {
			System.err
					.println("No active session found--run the connect command first");
			return null;
		}


		ResultSet execute = session.execute("USE " + keyspace);
		if (execute.wasApplied())
			System.out.printf("now using keyspace: %s \n", keyspace);
		else
			System.err.println("can't use keyspace");

		return null;
	}

}
