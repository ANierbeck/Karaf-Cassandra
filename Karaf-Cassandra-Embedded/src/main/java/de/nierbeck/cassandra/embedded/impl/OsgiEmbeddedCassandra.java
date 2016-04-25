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
package de.nierbeck.cassandra.embedded.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Dictionary;
import java.util.HashMap;

import org.apache.cassandra.service.CassandraDaemon.Server;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nierbeck.cassandra.embedded.CassandraRunner;
import de.nierbeck.cassandra.embedded.CassandraService;
import jline.internal.Log;

public class OsgiEmbeddedCassandra implements Server, CassandraService, ManagedService {

    private static final String NATIVE_TRANSPORT_PORT = "native_transport_port";

    private static final String CASSANDRA_YAML = "cassandra.yaml";

    private static final String JMX_PORT = "jmx_port";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String cassandraConfig;

    private CassandraRunner runner;

    private BundleContext context;

    private String native_transport_port;

    private String jmx_port;
    
    private static String default_native_transport_port = "9142";

    public OsgiEmbeddedCassandra(BundleContext context) {
        this.context = context;
    }

    @Override
    public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
        logger.info("Updating Service with new Properties");
        if (isRunning()) {
            logger.info("stopping running instance");
            stop();
        }
        if (properties != null) {
            cassandraConfig = (String) properties.get(CASSANDRA_YAML);
            native_transport_port = (String) properties.get(NATIVE_TRANSPORT_PORT);
            jmx_port = (String) properties.get(JMX_PORT);
        }
        logger.info("starting cassandra service");
        start();
    }

    @Override
    public boolean isRunning() {
        if (runner == null)
            return false;
        
        try {
            return CassandraRunner.waitForPortOpen(InetAddress.getByName("localhost"), Integer.parseInt(default_native_transport_port), 2000);
        } catch (UnknownHostException e) {
            Log.error("Cassandra failed to start: ", e);
            return false;
        }
    }

    @Override
    public void start() {
        logger.info("starting Cassandra in Embedded mode");
        HashMap<String, String> properties = new HashMap<>();
        properties.put(NATIVE_TRANSPORT_PORT, getNativePort());
        properties.put("rpc_address", "localhost");
        if (jmx_port != null && !jmx_port.isEmpty())
            properties.put(JMX_PORT, jmx_port);

        try {
            InputStream configFile = null;
            if (cassandraConfig != null) {
                URI uri = URI.create(cassandraConfig);
                configFile = uri.toURL().openStream();
            } else {
                if (context != null)
                    configFile = context.getBundle().getEntry(CASSANDRA_YAML).openStream();
                else
                    configFile = getClass().getClassLoader().getResourceAsStream(CASSANDRA_YAML);
            }
            runner = new CassandraRunner(configFile, properties, true);
            logger.info("cassandra up and running");
        } catch (IOException e) {
            logger.error("can't start Cassandra", e);
        }

    }

    private String getNativePort() {
        return native_transport_port != null ? native_transport_port : default_native_transport_port;
    }

    public Integer getPort() {
        return Integer.valueOf(getNativePort());
    }

    @Override
    public void cleanUp() {
    }

    @Override
    public void stop() {
        logger.info("stopping Cassandra process");
        runner.destroy();
        runner = null;
    }

}
