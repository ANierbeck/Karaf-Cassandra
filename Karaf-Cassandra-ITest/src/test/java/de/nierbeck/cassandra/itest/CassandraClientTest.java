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
package de.nierbeck.cassandra.itest;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class CassandraClientTest extends TestBase{
	

	
	@Test
	public void shouldHaveBundleContext() {
		assertThat(bc, is(notNullValue()));
	}

	@Test
	public void featuresShouldBeInstalled() throws Exception {
		assertThat(featuresService.isInstalled(featuresService
				.getFeature("Karaf-Cassandra-Embedded")), is(true));
		assertThat(featuresService.isInstalled(featuresService
				.getFeature("Karaf-Cassandra-Client")), is(true));
		assertThat(featuresService.isInstalled(featuresService
				.getFeature("Karaf-Cassandra-Shell")), is(true));
	}
	
	@Test
	public void cassandraConnect() throws Exception {

		assertThat(executeCommand("cassandra:connect -p 9142 localhost"), containsString("Connected to cluster:"));
		
		executeCommand("cassandra:disconnect");
	}

	
	@Test
	public void cassandraDisconnect() throws Exception {
		executeCommand("cassandra:connect -p 9142 localhost");
		assertThat(executeCommand("cassandra:disconnect"), containsString("disconnected"));
	}
	
	@Test
	public void cassandraIsConnected() throws Exception {
		executeCommand("cassandra:connect -p 9142 localhost");
		assertThat(executeCommand("cassandra:isConnected"), containsString("true"));
		executeCommand("cassandra:disconnect");
	}
	
	@Test
	public void cassandraCqlArgument() throws Exception {
		executeCommand("cassandra:connect -p 9142 localhost");
		assertThat(executeCommand("cassandra:cql \"SELECT keyspace_name FROM system.schema_keyspaces;\""), containsString("keyspace_name"));
		executeCommand("cassandra:disconnect");
	}
	
	@Test
	public void cassandraCqlOption() throws Exception {
		executeCommand("cassandra:connect -p 9142 localhost");
		assertThat(executeCommand("cassandra:cql -f file:"+new File("../../../../src/test/resources/test.cql").getAbsolutePath()), containsString("keyspace_name"));
		executeCommand("cassandra:disconnect");
	}
	
}
