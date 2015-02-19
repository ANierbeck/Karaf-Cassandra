package de.nierbeck.cassandra.embedded.shell.cql;

import java.util.List;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.Parsing;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.support.table.ShellTable;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;

import de.nierbeck.cassandra.embedded.shell.CqlExecuter;
import de.nierbeck.cassandra.embedded.shell.SessionParameter;
import de.nierbeck.cassandra.embedded.shell.cql.completion.SelectsCompleter;

@Command(scope = "cassandra:cqlsh", name = "SELECT", description = "execute SELECT cql commands, completion helps with simple commands, though all SELECTS can be issued")
@Parsing(CqlParser.class)
@Service
public class SelectCommand implements Action {


	@Reference
	protected org.apache.karaf.shell.api.console.Session session;

	@Argument(name = "select", description = "SELECT to execute", required = true, multiValued = true)
	@Completion(caseSensitive = false, value = SelectsCompleter.class)
	private List<String> select;

	public Object execute() throws Exception {
		Session session = (Session) this.session
				.get(SessionParameter.CASSANDRA_SESSION);

		if (session == null) {
			System.err
					.println("No active session found--run the connect command first");
			return null;
		}

		StringBuffer buff = new StringBuffer("SELECT ");
		for (String selectString : select) {
			buff.append(selectString);
			buff.append(" ");
		}
		buff.append(";");

		ShellTable shellTable = new ShellTable();
		ResultSet execute = session.execute(buff.toString());

		CqlExecuter.cassandraRowFormater(shellTable, execute);
		shellTable.print(System.out);

		return null;
	}

}
