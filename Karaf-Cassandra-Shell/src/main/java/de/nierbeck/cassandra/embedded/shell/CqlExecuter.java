package de.nierbeck.cassandra.embedded.shell;

import java.awt.List;
import java.util.Collections;
import java.util.UUID;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.support.table.ShellTable;

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

		Session session = (Session) this.session
				.get(SessionParameter.CASSANDRA_SESSION);

		if (session == null) {
			System.err
					.println("No active session found--run the connect command first");
			return null;
		}

		ShellTable table = new ShellTable();

		ResultSet execute = session.execute(cql);

		cassandraRowFormater(table, execute);

		table.print(System.out);
		return null;
	}

	public static void cassandraRowFormater(ShellTable table, ResultSet execute) {

		ColumnDefinitions columnDefinitions = execute.getColumnDefinitions();
		for (Definition definition : columnDefinitions) {
			table.column(definition.getName());
		}

		for (Row row : execute) {
			org.apache.karaf.shell.support.table.Row shellRow = table.addRow();

			for (Definition definition : columnDefinitions) {
				Class<?> asJavaClass = definition.getType().asJavaClass();

				if (String.class == asJavaClass) {
					shellRow.addContent(row.getString(definition.getName()));
				} else if (Integer.class == asJavaClass) {
					shellRow.addContent(row.getInt(definition.getName()));
				} else if (Double.class == asJavaClass) {
					shellRow.addContent(row.getDouble(definition.getName()));
				} else if (UUID.class == asJavaClass) {
					shellRow.addContent(row.getUUID(definition.getName())
							.toString());
				} else if (List.class == asJavaClass) {
					shellRow.addContent("List:" + definition.getType());

				} else {
					shellRow.addContent(definition.getType());
				}

			}
		}
	}

}
