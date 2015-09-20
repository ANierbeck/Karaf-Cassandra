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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class CassandraAdminTest extends TestBase{

	
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
	public void embeddedCassandraServiceShouldBeAvailable() throws Exception {
		assertThat(cassandraService, notNullValue());
		assertThat(cassandraService.isRunning(), is(true));
	}

	@Test
	public void cassandraAdminIsRunning() throws Exception {

		assertThat(executeCommand("cassandra-admin:isRunning"),
				containsString("Embedded Cassandra is available"));
	}

	
	@Test
	public void cassandraAdminStop() throws Exception {
		assertThat(executeCommand("cassandra-admin:stop"), containsString("Embedded Cassandra stoped!"));
		
		assertThat(cassandraService.isRunning(), is(false));
		
		cassandraService.start();
	}
	
	
	@Test
	public void cassandraAdminStart() throws Exception {
		cassandraService.stop();
		
		assertThat(executeCommand("cassandra-admin:start"), containsString("Embedded Cassandra started."));
		assertThat(cassandraService.isRunning(), is(true));
	}
	
	@Test
	public void cassandraAdminStopOnStopedInstance() throws Exception {
		cassandraService.stop();
		
		assertThat(executeCommand("cassandra-admin:stop"), containsString("No runnung embedded Cassandra service found"));
		cassandraService.start();
	}
	
	@Test
	public void cassandraAdminStartOnStartedInstance() throws Exception {
		assertThat(executeCommand("cassandra-admin:start"), containsString("Embedded Cassandra is already started"));
	}

}
