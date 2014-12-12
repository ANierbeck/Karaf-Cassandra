package de.nierbeck.cassandra.embedded.shell.cql;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.support.completers.StringsCompleter;
import org.apache.karaf.shell.support.table.ShellTable;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;

import de.nierbeck.cassandra.embedded.shell.CqlExecuter;
import de.nierbeck.cassandra.embedded.shell.SessionParameter;
import de.nierbeck.cassandra.embedded.shell.cql.completion.KeySpaceCompleter;

@Command(scope = "cassandra:cqlsh", name = "DESCRIBE", description = "execute USE cql commands")
@Service
public class DescribeCommand implements Action {


	@Reference
	protected org.apache.karaf.shell.api.console.Session session;

	@Argument(name = "value", description = "DESCRIBE ?", required = true, multiValued = false)
	@Completion(caseSensitive = false, values = { "keyspaces", "TABLES" }, value = StringsCompleter.class)
	private String value;

	public Object execute() throws Exception {
		Session session = (Session) this.session
				.get(SessionParameter.CASSANDRA_SESSION);

		if (session == null) {
			System.err
					.println("No active session found--run the connect command first");
			return null;
		}

		ShellTable table = new ShellTable();

		ResultSet execute = null;

		switch (value) {
		case "TABLES":
		case "tables":
		case "table":
		case "TABLE":
			execute = session
					.execute("select keyspace_name, columnfamily_name, key_validator from schema_columnfamilies;");
			break;
		case "keyspaces":
		default:
			execute = session.execute("select * from  system.schema_keyspaces;");
			break;
		}

		if (execute != null) {
			CqlExecuter.cassandraRowFormater(table, execute);
			table.print(System.out);
		}

		return null;
	}

}
