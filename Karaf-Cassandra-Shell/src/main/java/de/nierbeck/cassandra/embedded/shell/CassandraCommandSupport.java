package de.nierbeck.cassandra.embedded.shell;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.console.Session;

public abstract class CassandraCommandSupport implements Action {

	@Reference
	protected Session session;

	public Object execute() throws Exception {
		return doExecute();
	}

	protected abstract Object doExecute() throws Exception;

	public void setSession(Session session) {
		this.session = session;
	}

}
