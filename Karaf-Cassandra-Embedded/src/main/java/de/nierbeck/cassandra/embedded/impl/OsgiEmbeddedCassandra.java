package de.nierbeck.cassandra.embedded.impl;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Dictionary;
import java.util.List;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.factory.HFactory;

import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.config.Schema;
import org.apache.cassandra.service.CassandraDaemon;
import org.apache.cassandra.service.CassandraDaemon.Server;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nierbeck.cassandra.embedded.CassandraService;

public class OsgiEmbeddedCassandra implements Server, CassandraService,
		ManagedService {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private static final String INTERNAL_CASSANDRA_KEYSPACE = "system";
	private static final String INTERNAL_CASSANDRA_AUTH_KEYSPACE = "system_auth";
	private static final String INTERNAL_CASSANDRA_TRACES_KEYSPACE = "system_traces";

	private CassandraDaemon cassandraDaemon;

	private String cassandraConfig;
	
	@Override
	public void updated(Dictionary<String, ?> properties)
			throws ConfigurationException {
		if (isRunning())
			stop();
		if (properties != null) {
			cassandraConfig = (String) properties.get("cassandra.yaml");
		}
		start();
	}

	@Override
	public boolean isRunning() {
		if (cassandraDaemon == null)
			return false;

		return (cassandraDaemon.nativeServer != null && cassandraDaemon.nativeServer
				.isRunning())
				|| (cassandraDaemon.thriftServer != null && cassandraDaemon.thriftServer
						.isRunning());
	}

	@Override
	public void start() {
		logger.info("starting Cassandra in Embedded mode");

		if (cassandraConfig != null) {
			System.setProperty("cassandra.config", "file://" + cassandraConfig);
		}
		System.setProperty("cassandra-foreground", "false");

		cassandraDaemon = new CassandraDaemon();
		try {
			logger.info("initializing cassandra deamon");
			cassandraDaemon.init(null);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		logger.info("starting cassandra deamon");
		cassandraDaemon.start();

		logger.info("cassandra up and runnign");
	}

	@Override
	public void stop() {
		logger.info("Stopping cassandra deamon");
		logger.info("cleaning up the Schema keys");
		Schema.instance.clear();
		logger.info("stopping cassandra");
		cassandraDaemon.stop();
		logger.info("destroying the cassandra deamon");
		cassandraDaemon.destroy();
		logger.info("cassandra is removed");
		cassandraDaemon = null;

		logger.info("removing MBean");
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		try {
			mbs.unregisterMBean(new ObjectName(
					"org.apache.cassandra.db:type=DynamicEndpointSnitch"));
		} catch (MBeanRegistrationException | InstanceNotFoundException
				| MalformedObjectNameException e) {
			logger.warn("Couldn't remove MBean");
		}

	}

	@Override
	public void cleanUp() {
		if (isRunning()) {
			dropKeyspaces();
		}
	}

	private void dropKeyspaces() {
		String host = DatabaseDescriptor.getRpcAddress().getHostName();
		int port = DatabaseDescriptor.getRpcPort();
		logger.debug("Cleaning cassandra keyspaces on " + host + ":" + port);
		Cluster cluster = HFactory.getOrCreateCluster("TestCluster",
				new CassandraHostConfigurator(host + ":" + port));
		/* get all keyspace */
		List<KeyspaceDefinition> keyspaces = cluster.describeKeyspaces();

		/* drop all keyspace except internal cassandra keyspace */
		for (KeyspaceDefinition keyspaceDefinition : keyspaces) {
			String keyspaceName = keyspaceDefinition.getName();

			if (!INTERNAL_CASSANDRA_KEYSPACE.equals(keyspaceName)
					&& !INTERNAL_CASSANDRA_AUTH_KEYSPACE.equals(keyspaceName)
					&& !INTERNAL_CASSANDRA_TRACES_KEYSPACE.equals(keyspaceName)) {
				cluster.dropKeyspace(keyspaceName);
			}
		}
	}

	@Override
	public Integer getPort() {
		return DatabaseDescriptor.getRpcPort();
	}

}
