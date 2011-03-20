package org.protege.owl.server.configuration;

import org.protege.owl.server.metaproject.Vocabulary;
import org.semanticweb.owlapi.model.OWLIndividual;

public class StrictConflictConfiguration {

    public static boolean isSuitable(ServerConfiguration configuration) {
        OWLIndividual i = configuration.getConflictManagerDeclaration();
        return i != null && i.getTypes(configuration.getOntology()).contains(Vocabulary.STRICT_CONFLICT_MANAGER_CLASS);
    }
}
