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
package de.nierbeck.cassandra.embedded;

/**
 * This service interface is for communicating with the embedded Cassandra
 * instance. This instance is registered as service.
 * 
 * @author anierbeck
 *
 */
public interface CassandraService {

	/**
	 * @return - true or false depending on the state of the embedded Cassandra
	 *         instance.
	 */
	boolean isRunning();
	
	/**
	 * @return - the port the embedded Cassandra is using. 
	 */
	Integer getPort();

	/**
	 * stops the embedded Cassandra instance if running.
	 */
	void stop();

	/**
	 * starts the embedded Cassndra instance if it isn't running.
	 */
	void start();

	/**
	 * cleans the keyspaces available to the embedded instance. Beware data is
	 * not available after cleaning the keyspaces.
	 */
	void cleanUp();

}
