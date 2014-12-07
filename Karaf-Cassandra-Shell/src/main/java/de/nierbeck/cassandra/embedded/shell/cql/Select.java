package de.nierbeck.cassandra.embedded.shell.cql;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import com.datastax.driver.core.Session;

import de.nierbeck.cassandra.embedded.shell.CassandraCommandSupport;
import de.nierbeck.cassandra.embedded.shell.SessionParameter;
import de.nierbeck.cassandra.embedded.shell.cql.completion.SelectCompleter;

@Command(scope = "cassandra:cqlsh", name = "SELECT", description = "SELECT")
@Service
public class Select extends CassandraCommandSupport {

	@Argument(name = "statement", description = "CQL SELECT", required = true, multiValued = true)
	@Completion(SelectCompleter.class)
	private String[] statement;

	@Override
	public Object doExecute() throws Exception {

		Session session = (Session) this.session
				.get(SessionParameter.CASSANDRA_SESSION);

		if (session == null) {
			System.err.println("No active session found--run the connect command first");
			return null;
		}

		StringBuffer buf = new StringBuffer();

		for (String string : statement) {
			buf.append(string);
			buf.append(" ");
		}

		System.out.println("Command: " + buf.toString());

		// ResultSet execute = session.execute(statement);


		return null;
	}

}
