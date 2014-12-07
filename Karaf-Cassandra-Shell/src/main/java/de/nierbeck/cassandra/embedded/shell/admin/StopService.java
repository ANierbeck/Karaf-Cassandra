package de.nierbeck.cassandra.embedded.shell.admin;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.util.tracker.RequireService;

import de.nierbeck.cassandra.embedded.CassandraService;
import de.nierbeck.cassandra.embedded.shell.CassandraCommandSupport;

@Command(scope = "cassandra/admin", name = "stop", description = "Connect to cassandra server")
@Service
public class StopService extends CassandraCommandSupport {

	@RequireService(CassandraService.class)
	CassandraService cassandraServer;

	@Override
	public Object doExecute() throws Exception {
		if (!cassandraServer.isRunning()) {
			System.err.println("No runnung embedded Cassandra service found");
			return null;
		}
		cassandraServer.stop();
		System.out.println("Embedded Cassandra stoped!");
		return null;
	}

}
