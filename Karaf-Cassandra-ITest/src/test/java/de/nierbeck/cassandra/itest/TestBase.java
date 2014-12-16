package de.nierbeck.cassandra.itest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.MavenUtils.asInProject;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.configureConsole;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.logLevel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.karaf.features.BootFinished;
import org.apache.karaf.features.FeaturesService;
import org.apache.karaf.shell.api.console.Session;
import org.apache.karaf.shell.api.console.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.karaf.options.LogLevelOption.LogLevel;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.options.MavenUrlReference;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nierbeck.cassandra.embedded.CassandraService;

public class TestBase {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Inject
	protected BundleContext bc;
	
	@Inject
	protected FeaturesService featuresService;

	/**
	 * To make sure the tests run only when the boot features are fully
	 * installed
	 */
	@Inject
	protected BootFinished bootFinished;
	
	@Inject
	protected SessionFactory sessionFactory;
	
	@Inject
	protected CassandraService cassandraService;
	
	private ExecutorService executor = Executors.newCachedThreadPool();

	private ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	private PrintStream printStream = new PrintStream(byteArrayOutputStream);
	private PrintStream errStream = new PrintStream(byteArrayOutputStream);
	private Session session;
	
	
	@Configuration
	public Option[] config() {
		return options(
				karafDistributionConfiguration().frameworkUrl(mvnKarafDist())
						.unpackDirectory(new File("target/paxexam/unpack/"))
						.useDeployFolder(false),
				logLevel(LogLevel.INFO),
				keepRuntimeFolder(),
				features(karafStandardFeature(), "wrap"),
				features(
						maven().groupId("de.nierbeck.cassandra")
								.artifactId("Karaf-Cassandra-Feature")
								.type("xml").classifier("features")
								.versionAsInProject(),
						"Karaf-Cassandra-Embedded", "Karaf-Cassandra-Shell"),
				configureConsole().ignoreLocalConsole(),
				junitBundles());
	}

	private MavenArtifactUrlReference mvnKarafDist() {
		return maven().groupId("org.apache.karaf").artifactId("apache-karaf")
				.type("tar.gz").version(asInProject());
	}

	private MavenUrlReference karafStandardFeature() {
		return maven()
				.groupId("org.apache.karaf.features").artifactId("standard")
				.type("xml").classifier("features").version(asInProject());
	}

	@Before
	public void setUpITestBase() throws Exception {
		int count = 0;
		while (!cassandraService.isRunning() && count < 10) {
			Thread.sleep(500);
		}
		logger.info("Waited for Cassandra service to appear: " + Integer.toString(count*500));
		
		session = sessionFactory.create(System.in, printStream, errStream);
	}

	@After
	public void cleanupTest() throws Exception {
		if (!cassandraService.isRunning()) {
			cassandraService.start();
		}
		session = null;
	}

	protected String executeCommand(final String command) throws IOException {
		byteArrayOutputStream.flush();
		byteArrayOutputStream.reset();
		
		String response;
		FutureTask<String> commandFuture = new FutureTask<String>(
				new Callable<String>() {
					public String call() {
						try {
							System.err.println(command);
							session.execute(command);
						} catch (Exception e) {
							e.printStackTrace(System.err);
						}
						printStream.flush();
						errStream.flush();
						return byteArrayOutputStream.toString();
					}
				});
	
		try {
			executor.submit(commandFuture);
			response = commandFuture.get(10000L, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			response = "SHELL COMMAND TIMED OUT: ";
		}
	
		System.err.println(response);
		
		return response;
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
}
