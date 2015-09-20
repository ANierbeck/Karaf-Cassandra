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
package de.nierbeck.cassandra.embedded.shell.cql;

import java.util.List;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.Parsing;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;

import de.nierbeck.cassandra.embedded.shell.SessionParameter;
import de.nierbeck.cassandra.embedded.shell.cql.completion.CreateCompleter;

@Command(scope = "cassandra:cqlsh", name = "CREATE", description = "execute USE cql commands")
@Parsing(CqlParser.class)
@Service
public class CreateCommand implements Action {


	@Reference
	protected org.apache.karaf.shell.api.console.Session session;

	@Argument(name = "create", description = "CREATE table or keyspaces", required = true, multiValued = true)
	@Completion(CreateCompleter.class)
	private List<String> create;

	public Object execute() throws Exception {
		Session session = (Session) this.session
				.get(SessionParameter.CASSANDRA_SESSION);

		if (session == null) {
			System.err
					.println("No active session found--run the connect command first");
			return null;
		}

		StringBuffer buff = new StringBuffer("CREATE ");
		for (String createString : create) {
			buff.append(createString);
			buff.append(" ");
		}
		buff.append(";");

		ResultSet execute = session.execute(buff.toString());

		if (execute.wasApplied())
			System.out.println("created");
		else
			System.out.println("not-created");
		return null;
	}

}
