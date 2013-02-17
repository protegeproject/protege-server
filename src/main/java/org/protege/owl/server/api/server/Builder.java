package org.protege.owl.server.api.server;



public interface Builder {
	void initialise(ServerConfiguration configuration);
	
	void addServerComponentFactory(ServerComponentFactory factory);

	void removeServerComponentFactory(ServerComponentFactory factory);
	
	void deactivate();
	
	boolean isUp();
}
