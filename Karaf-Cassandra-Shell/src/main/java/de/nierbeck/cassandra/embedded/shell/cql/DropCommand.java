package de.nierbeck.cassandra.embedded.shell.cql;

import java.util.List;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.support.completers.StringsCompleter;
import org.apache.karaf.shell.support.table.ShellTable;

import com.datastax.driver.core.ExecutionInfo;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;

import de.nierbeck.cassandra.embedded.shell.CqlExecuter;
import de.nierbeck.cassandra.embedded.shell.SessionParameter;
import de.nierbeck.cassandra.embedded.shell.cql.completion.CreateCompleter;
import de.nierbeck.cassandra.embedded.shell.cql.completion.DropCompleter;
import de.nierbeck.cassandra.embedded.shell.cql.completion.KeySpaceCompleter;

@Command(scope = "cassandra:cqlsh", name = "DROP", description = "execute USE cql commands")
@Service
public class DropCommand implements Action {


	@Reference
	protected org.apache.karaf.shell.api.console.Session session;

	@Argument(name = "drop", description = "DROP keyspaces/schema", required = true, multiValued = true)
	@Completion(DropCompleter.class)
	private List<String> drop;

	public Object execute() throws Exception {
		Session session = (Session) this.session
				.get(SessionParameter.CASSANDRA_SESSION);

		if (session == null) {
			System.err
					.println("No active session found--run the connect command first");
			return null;
		}

		StringBuffer buff = new StringBuffer("DROP ");
		for (String createString : drop) {
			buff.append(createString);
			buff.append(" ");
		}
		buff.append(";");

		ResultSet execute = session.execute(buff.toString());


		if (execute.wasApplied())
			System.out.println("keyspace removed");
		else
			System.out.println("keyspace not removed");
		return null;
	}

}
