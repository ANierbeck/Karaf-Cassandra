package de.nierbeck.cassandra.embedded.shell;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ColumnDefinitions.Definition;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

@Command(scope = "cassandra", name = "cql", description = "execute cql commands")
@Service
public class CqlExecuter extends CassandraCommandSupport {

	@Argument(name = "cql", description = "CQL to execute", required = true, multiValued = false)
	private String cql;

	@Override
	public Object doExecute() throws Exception {

		Session session = (Session) this.session.get(CASSANDRA_SESSION);

		if (session == null) {
			System.err.println("No active session found--run the connect command first");
		}

		ResultSet execute = session.execute(cql);

		boolean isFirst = true;
		for (Row row : execute) {
			StringBuffer headRow = new StringBuffer();
			StringBuffer rowPrint = new StringBuffer();
			ColumnDefinitions columnDefinitions = row.getColumnDefinitions();

			for (Definition definition : columnDefinitions) {
				if (isFirst) {
					headRow.append(definition.getName());
					headRow.append(" ");
				}
				Class<?> asJavaClass = definition.getType().asJavaClass();
				if (String.class == asJavaClass)
					rowPrint.append(row.getString(definition.getName()));
				if (Integer.class == asJavaClass)
					rowPrint.append(row.getInt(definition.getName()));
				rowPrint.append(" ");
			}
			if (isFirst)
				System.out.println(headRow.toString());

			System.out.println(rowPrint.toString());
			isFirst = false;
		}

		return null;
	}

}
