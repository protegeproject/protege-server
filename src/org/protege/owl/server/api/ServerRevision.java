package org.protege.owl.server.api;

public class ServerRevision implements Comparable<ServerRevision> {
	
	public static final ServerRevision START_REVISION = new ServerRevision(0);
	
	private int revision;
	
	public ServerRevision(int revision) {
		this.revision = revision;
	}
	
	public int getRevision() {
		return revision;
	}
	
	@Override
	public int compareTo(ServerRevision o) {
		return 0;
	}

}
