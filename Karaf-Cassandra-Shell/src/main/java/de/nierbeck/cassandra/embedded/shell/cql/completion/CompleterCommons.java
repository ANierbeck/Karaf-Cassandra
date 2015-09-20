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
