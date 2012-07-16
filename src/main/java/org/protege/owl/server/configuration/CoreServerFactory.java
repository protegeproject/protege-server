package org.protege.owl.server.configuration;

import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerComponentFactory;
import org.protege.owl.server.api.ServerFilter;
import org.protege.owl.server.api.ServerTransport;
import org.semanticweb.owlapi.model.OWLIndividual;

public class CoreServerFactory implements ServerComponentFactory {

	@Override
	public boolean hasSuitableServer(OWLIndividual i) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Server createServer(OWLIndividual i) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasSuitableServerFilter(OWLIndividual i) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ServerFilter createServerFilter(OWLIndividual i, Server server) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServerTransport hasSuitableServerTransport(OWLIndividual i) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServerTransport createServerTransport(OWLIndividual i) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String toString() {
		return "<<Core Server Components>>";
	}

}
