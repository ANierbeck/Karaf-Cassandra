package de.nierbeck.cassandra.embedded.shell.admin;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.util.tracker.RequireService;

import de.nierbeck.cassandra.embedded.CassandraService;
import de.nierbeck.cassandra.embedded.shell.CassandraCommandSupport;

@Command(scope = "cassandra/admin", name = "start", description = "Connect to cassandra server")
@Service
public class StartService extends CassandraCommandSupport {

	@RequireService(CassandraService.class)
	CassandraService cassandraServer;

	@Override
	public Object doExecute() throws Exception {
		if (cassandraServer.isRunning()) {
			System.err.println("Embedded Cassandra is already started");
			return null;
		}

		cassandraServer.start();
		System.out.println("Embedded Cassandra started.");
		return null;
	}

}
