package de.nierbeck.cassandra.embedded.shell;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import com.datastax.driver.core.Session;

@Command(scope = "cassandra", name = "isConnected", description = "Checks if there is a connected session available")
@Service
public class IsConnected extends CassandraCommandSupport {

	@Override
	public Object doExecute() throws Exception {

		Session session = (Session) this.session
				.get(SessionParameter.CASSANDRA_SESSION);

		boolean isActive = (session != null && !session.isClosed());

		String loggedKeyspace = "";

		if (isActive)
			loggedKeyspace = session.getLoggedKeyspace();

		if (loggedKeyspace == null || loggedKeyspace.isEmpty())
			System.out.println(isActive);
		else
			System.out.println(isActive + ": " + loggedKeyspace);


		return null;
	}

}
