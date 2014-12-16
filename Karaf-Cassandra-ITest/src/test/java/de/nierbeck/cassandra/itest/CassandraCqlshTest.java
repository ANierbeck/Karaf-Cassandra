package de.nierbeck.cassandra.itest;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class CassandraCqlshTest extends TestBase{

	
	@Before
	public void beforeTest() throws Exception{
		super.setUpITestBase();
		executeCommand("cassandra:connect -p 9142 localhost");
	}
	
	@After
	public void cleanUp() throws Exception {
		executeCommand("cassandra:disconnect");
		super.cleanupTest();
	}

	
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
	public void cassandraCqlshUse() throws Exception {

		assertThat(executeCommand("cassandra:cqlsh:USE system;"), containsString("now using keyspace: system"));
		
	}
	
	@Test
	public void cassandraCqlSelect() throws Exception {
		assertThat(executeCommand("SELECT * from system.schema_keyspaces;"), containsString("keyspace"));
	}
}
