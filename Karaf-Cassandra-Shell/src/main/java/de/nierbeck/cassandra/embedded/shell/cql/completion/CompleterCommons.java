package de.nierbeck.cassandra.embedded.shell.cql.completion;

import org.apache.karaf.shell.support.completers.StringsCompleter;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;

public class CompleterCommons {

	public static void completeKeySpace(StringsCompleter delegate,
			com.datastax.driver.core.Session cassandraSession) {
		try {
			ResultSet execute = cassandraSession
					.execute("SELECT keyspace_name FROM system.schema_keyspaces;");
			for (Row row : execute) {
				String keySpace = row.getString("keyspace_name");
				delegate.getStrings().add(keySpace);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
