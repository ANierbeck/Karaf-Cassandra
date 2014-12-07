package de.nierbeck.cassandra.admin.shell;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.nierbeck.cassandra.embedded.CassandraService;

@Command(scope = "cassandra-admin", name = "isRunning", description = "Connect to cassandra server")
@Service
public class IsServiceRunning implements Action {

	@Reference
	CassandraService cassandraServer;

	@Override
	public Object execute() throws Exception {
		if (cassandraServer.isRunning())
			System.out.println("Embedded Cassandra is available.");
		else
			System.out.println("Embedded Cassandra is stoped.");
		return null;
	}

}
