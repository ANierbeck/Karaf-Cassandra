package de.nierbeck.cassandra.embedded.shell.admin;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.util.tracker.RequireService;

import de.nierbeck.cassandra.embedded.CassandraService;
import de.nierbeck.cassandra.embedded.shell.CassandraCommandSupport;

@Command(scope = "cassandra/admin", name = "isRunning", description = "Connect to cassandra server")
@Service
public class IsServiceRunning extends CassandraCommandSupport {

	@RequireService(CassandraService.class)
	CassandraService cassandraServer;

	@Override
	public Object doExecute() throws Exception {
		if (cassandraServer.isRunning())
			System.out.println("Embedded Cassandra is available.");
		else
			System.out.println("Embedded Cassandra is stoped.");
		return null;
	}

}
