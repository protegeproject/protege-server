package org.protege.owl.server.configuration;

import static org.protege.owl.server.configuration.MetaprojectVocabulary.HAS_SERVER_FILTER;

import java.util.Collection;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.protege.owl.server.api.server.Server;
import org.protege.owl.server.api.server.ServerComponentFactory;
import org.protege.owl.server.api.server.ServerFilter;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.search.EntitySearcher;

@Deprecated
public class FilterConstraint {
    public static final Logger logger = LoggerFactory.getLogger(FilterConstraint.class.getCanonicalName());
    private OWLIndividual i;
    private FilterConstraint containingFilterConstraint;
    
    public static FilterConstraint getDelegateConstraint(OWLOntology configuration, OWLIndividual i) {
        Collection<OWLIndividual> subFilters = EntitySearcher.getObjectPropertyValues(i, HAS_SERVER_FILTER, configuration);
        if (subFilters == null || subFilters.size() == 0) {
            return null;
        }
        if (subFilters.size() > 1) {
            logger.warn("Filter specification " + i + " specifies more than one delegate constraint.  Some of the specification may be lost.");
        }
        return new FilterConstraint(configuration, subFilters.iterator().next());
    }
    
    public FilterConstraint(OWLOntology configuration, OWLIndividual i) {
        containingFilterConstraint = getDelegateConstraint(configuration, i);
        this.i = i;
    }
    
    public boolean satisfied(Set<ServerComponentFactory> factories) {
        if (containingFilterConstraint != null && !containingFilterConstraint.satisfied(factories)) {
            return false;
        }
        for (ServerComponentFactory factory : factories) {
            if (factory.hasSuitableServerFilter(i)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Using " + factory + " to satisfy constraint: " + i);
                }
                return true;
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Could not find factory to satisfy constraint: " + i);
        }
        return false;
    }
    
    public ServerFilter build(Server server, Set<ServerComponentFactory> factories) {
        ServerFilter me = null;
        for (ServerComponentFactory factory : factories) {
            if (factory.hasSuitableServerFilter(i)) {
                me = factory.createServerFilter(i, server);
            }
        }
        if (containingFilterConstraint != null) {
            return containingFilterConstraint.build(me, factories);
        }
        else {
            return me;
        }
    }

}
