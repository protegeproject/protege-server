package org.protege.owl.server.changes;

import java.util.List;

import org.protege.owl.server.changes.api.DocumentFactory;
import org.protege.owl.server.changes.api.SingletonChangeHistory;

import org.semanticweb.owlapi.model.OWLOntologyChange;

public class SingletonChangeHistoryImpl extends ChangeHistoryImpl implements SingletonChangeHistory {
    private static final long serialVersionUID = -2672158554044081216L;

    public SingletonChangeHistoryImpl(DocumentFactory documentFactory, 
                                      OntologyDocumentRevision startRevision, 
                                      List<OWLOntologyChange> changes, 
                                      ChangeMetaData metaData) {
        super(documentFactory, startRevision, changes, metaData);
    }
}
