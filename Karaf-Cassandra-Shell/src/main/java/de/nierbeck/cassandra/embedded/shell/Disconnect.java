package de.nierbeck.cassandra.embedded.shell;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

@Command(scope = "cassandra", name = "disconnect", description = "Disconnect from cassandra server")
@Service
public class DisConnect extends CassandraCommandSupport {

	@Override
	public Object doExecute() throws Exception {

		disconnect();

		return null;
	}

	public void disconnect() {

		Cluster cluster = (Cluster) this.session.get(CASSANDRA_CLUSTER);
		Session session = (Session) this.session.get(CASSANDRA_SESSION);

		session.close();
		cluster.close();

		this.session.put(CASSANDRA_CLUSTER, null);
		this.session.put(CASSANDRA_SESSION, null);

	}
}
