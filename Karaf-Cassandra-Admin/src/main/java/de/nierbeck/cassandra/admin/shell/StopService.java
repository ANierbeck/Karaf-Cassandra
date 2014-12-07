package de.nierbeck.cassandra.admin.shell;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.nierbeck.cassandra.embedded.CassandraService;

@Command(scope = "cassandra-admin", name = "stop", description = "Connect to cassandra server")
@Service
public class StopService implements Action {

	@Reference
	CassandraService cassandraServer;

	@Override
	public Object execute() throws Exception {
		if (!cassandraServer.isRunning()) {
			System.err.println("No runnung embedded Cassandra service found");
			return null;
		}
		cassandraServer.stop();
		System.out.println("Embedded Cassandra stoped!");
		return null;
	}

}
