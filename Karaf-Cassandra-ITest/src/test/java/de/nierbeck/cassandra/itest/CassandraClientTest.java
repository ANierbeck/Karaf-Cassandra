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
