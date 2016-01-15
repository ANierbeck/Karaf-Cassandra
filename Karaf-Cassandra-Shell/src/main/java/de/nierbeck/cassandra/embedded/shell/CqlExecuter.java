/*
 *    Copyright 2015 Achim Nierbeck
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
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
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.DataType.Name;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

@Command(scope = "cassandra", name = "cql", description = "execute cql commands")
@Service
public class CqlExecuter extends CassandraCommandSupport {

	@Argument(name = "cql", description = "CQL to execute, must be quoted", required = false, multiValued = false)
	private String cql;

	@Option(name = "-f", aliases = {"--file"}, description = "A URI pointing to a CQL file to be executed by the command, must start with a URI schema like file:", required = false, multiValued = false)
	private URI fileLocation;

	@Override
	public Object doExecute() throws Exception {

		Session session = (Session) this.session
				.get(SessionParameter.CASSANDRA_SESSION);

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
		} else {
			int start = 0;
			int end = 0;
			if (cql.startsWith("\"")) {
				//need to remove quotes first
				start = 1;
				if (cql.endsWith("\"")) {
					end = cql.lastIndexOf("\"");
				}
				cql = cql.substring(start, end);
			}
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
			    boolean isCollection = definition.getType().isCollection();
				
				Name definitionName = definition.getType().getName();
				
                if (DataType.Name.TEXT.isCompatibleWith(definitionName)) {
					shellRow.addContent(row.getString(definition.getName()));
				} else if (DataType.Name.INT.isCompatibleWith(definitionName)) {
					shellRow.addContent(row.getInt(definition.getName()));
				} else if (DataType.Name.DOUBLE.isCompatibleWith(definitionName)) {
					shellRow.addContent(row.getDouble(definition.getName()));
				} else if (DataType.Name.UUID.isCompatibleWith(definitionName)) {
					shellRow.addContent(row.getUUID(definition.getName())
							.toString());
				} else if (isCollection) {
					shellRow.addContent("List:" + definitionName);
				} else if (DataType.Name.BIGINT.isCompatibleWith(definitionName)) {
					shellRow.addContent(row.getLong(definition.getName()));
				} else if (DataType.Name.FLOAT.isCompatibleWith(definitionName)) {
					shellRow.addContent(row.getFloat(definition.getName()));
				} else if (DataType.Name.BOOLEAN.isCompatibleWith(definitionName)) {
				    shellRow.addContent(row.getBool(definition.getName()));
				} else {
					shellRow.addContent(definition.getType());
				}

			}
		}
	}

}
