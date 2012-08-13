package org.protege.owl.server.api;


public interface Builder {
	void initialise(ServerConfiguration configuration);
	
	void addServerComponentFactory(ServerComponentFactory factory);

	void removeServerComponentFactory(ServerComponentFactory factory);
	
	void deactivate();
	
	boolean isUp();
}
