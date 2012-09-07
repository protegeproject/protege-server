package org.protege.owl.server.configuration.factories;

import static org.protege.owl.server.configuration.MetaprojectVocabulary.HAS_CONFIGURATION_PATH;
import static org.protege.owl.server.configuration.MetaprojectVocabulary.HAS_ROOT_PATH;
import static org.protege.owl.server.configuration.MetaprojectVocabulary.STANDARD_SERVER;

import java.io.File;
import java.util.logging.Logger;

import org.protege.owl.server.api.Server;
import org.protege.owl.server.core.ServerImpl;
import org.protege.owl.server.util.ServerComponentFactoryAdapter;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;

public class CoreServerFactory extends ServerComponentFactoryAdapter {
	public static Logger logger = Logger.getLogger(CoreServerFactory.class.getCanonicalName());
	private OWLOntology ontology;
	private OWLDataFactory factory;
	
	@Override
	public void setConfiguration(OWLOntology ontology) {
		this.ontology = ontology;
		factory = ontology.getOWLOntologyManager().getOWLDataFactory();
	}

	@Override
	public boolean hasSuitableServer(OWLIndividual i) {
		OWLAxiom rightType = factory.getOWLClassAssertionAxiom(STANDARD_SERVER, i);
		boolean hasRightType = ontology.containsAxiom(rightType);
		return hasRightType;
	}

	@Override
	public Server createServer(OWLIndividual i) {
        return new ServerImpl(getRootPath(i), getConfigurationPath(i));
	}
	
	private File getRootPath(OWLIndividual i) {
	    for (OWLLiteral rootPathLiteral : i.getDataPropertyValues(HAS_ROOT_PATH, ontology)) {
	        String rootPath = rootPathLiteral.getLiteral();
	        return new File(rootPath);
	    }
	    return new File("root");
	}
	
	private File getConfigurationPath(OWLIndividual i) {
	       for (OWLLiteral rootPathLiteral : i.getDataPropertyValues(HAS_CONFIGURATION_PATH, ontology)) {
	            String configurationPath = rootPathLiteral.getLiteral();
	            return new File(configurationPath);
	        }
	        return new File("configuration");
	}

    @Override
    public String toString() {
    	return "Core Server Factory";
    }

}
