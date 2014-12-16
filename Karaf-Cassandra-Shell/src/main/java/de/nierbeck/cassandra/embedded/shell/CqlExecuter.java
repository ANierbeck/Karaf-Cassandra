package de.nierbeck.cassandra.embedded.shell;

import java.awt.List;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.UUID;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
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

	@Argument(name = "cql", description = "CQL to execute", required = false, multiValued = false)
	private String cql;

	@Option(name = "-f", aliases = {"--file"}, description = "A URI pointing to a CQL file to be executed by the command, must start with a URI schema like file:", required = false, multiValued = false)
	private URI fileLocation;

	@Override
	public Object doExecute() throws Exception {

		Session session = (Session) this.session
				.get(SessionParameter.CASSANDRA_SESSION);
		Boolean oldExpansion = (Boolean) this.session.get("org.apache.felix.gogo.expansion");
		this.session.put("org.apache.felix.gogo.expansion", false);

		if (session == null) {
			System.err
					.println("No active session found--run the connect command first");
			return null;
		}

		if (cql == null && fileLocation == null) {
			System.err
					.println("Either cql skript or a filename must be given.");
			return null;
		}

		if (cql == null && fileLocation != null) {
			byte[] encoded;
			InputStream is = fileLocation.toURL().openStream ();
			try (ByteArrayOutputStream os = new ByteArrayOutputStream();) {
		        byte[] buffer = new byte[0xFFFF];

		        for (int len; (len = is.read(buffer)) != -1;)
		            os.write(buffer, 0, len);

		        os.flush();

		        encoded = os.toByteArray();
		    } catch (IOException e) {
		    	System.err.println("Can't read fileinput");
		        return null;
		    }

			cql = new String(encoded, Charset.defaultCharset());
		}

		ShellTable table = new ShellTable();

		ResultSet execute = session.execute(cql);

		cassandraRowFormater(table, execute);

		table.print(System.out);
		
		this.session.put("org.apache.felix.gogo.expansion", oldExpansion);
		
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
				} else if (BigInteger.class == asJavaClass) {
					shellRow.addContent(row.getLong(definition.getName()));
				} else if (Long.class == asJavaClass) {
					shellRow.addContent(row.getLong(definition.getName()));
				} else {
					shellRow.addContent(definition.getType());
				}

			}
		}
	}

}
