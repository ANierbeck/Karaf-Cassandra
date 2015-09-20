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
package de.nierbeck.cassandra.embedded.shell;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Cluster.Builder;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.Session;

@Command(scope = "cassandra", name = "connect", description = "Connect to cassandra server")
@Service
public class Connect extends CassandraCommandSupport {

	@Option(name = "-p", aliases = { "--port" }, description = "Port to Connect to", required = false, multiValued = false)
	private Integer port;

	@Argument(name = "node", description = "Node to Connect to", required = true, multiValued = false)
	private String node;

	@Override
	public Object doExecute() throws Exception {
		System.out.println("Executing command Connect");
		System.out.println("Port: " + port);
		System.out.println("Node: " + node);

		connect(node, port);

		return null;
	}

	public void connect(String node, Integer port) {
		this.session.put("org.apache.felix.gogo.expansion", false);
		Builder clusterBuilder = Cluster.builder().addContactPoint(node);
		if (port != null) {
			clusterBuilder.withPort(port);
		}
		Cluster cluster = clusterBuilder.build();
		Metadata metadata = cluster.getMetadata();
		System.out.printf("Connected to cluster: %s\n",
				metadata.getClusterName());
		for (Host host : metadata.getAllHosts()) {
			System.out.printf("Datatacenter: %s; Host: %s; Rack: %s\n",
					host.getDatacenter(), host.getAddress(), host.getRack());
		}
		Session session = cluster.connect();

		this.session.put(SessionParameter.CASSANDRA_CLUSTER, cluster);
		this.session.put(SessionParameter.CASSANDRA_SESSION, session);
	}
}
