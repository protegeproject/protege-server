package org.protege.owl.server.connection.servlet;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerOntologyInfo;
import org.protege.owl.server.connection.servlet.serialize.Serializer;
import org.protege.owl.server.connection.servlet.serialize.SerializerFactory;
import org.protege.owl.server.exception.OntologyConflictException;
import org.protege.owl.server.exception.RemoteOntologyException;
import org.protege.owl.server.exception.RemoteQueryException;
import org.protege.owl.server.util.AxiomToChangeConverter;
import org.protege.owl.server.util.ChangeToAxiomConverter;
import org.protege.owl.server.util.Utilities;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

public class OntologyCommitServlet extends HttpServlet {
    private static final long serialVersionUID = -2089045681752989817L;
    public static final String PATH="/ontology/commit";
    
    private Server server;
    private Serializer serializer;
    
    public OntologyCommitServlet(Server server) {
        this.server = server;
        this.serializer = new SerializerFactory().createSerializer();
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ChangeAndRevisionSummary changes = getChanges(request, response);
        Map<IRI,Integer> revisionsBeforeCommit = getCurrentServerRevisions(changes);
        if (applyClientChanges(response, changes)) {
            try {
                List<OWLOntologyChange> changesForClient = getChangesForClient(changes, revisionsBeforeCommit);
                sendChangesToClient(response, changesForClient);
            }
            catch (Exception e) {
                throw new ServletException("Changes committed on server but client is not synchronized", e);
            }
        }
    }
    
    private ChangeAndRevisionSummary getChanges(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        OWLOntologyManager otherManager = OWLManager.createOWLOntologyManager();
        OWLOntology changeOntology;
        try {
            changeOntology = serializer.deserialize(otherManager, new StreamDocumentSource(request.getInputStream()));
        }
        catch (RemoteQueryException re) {
            response.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
            throw new ServletException(re);
        }
        return ChangeAndRevisionSummary.getChanges(server.getOntologies(), changeOntology);
    }
    
    private Map<IRI, Integer> getCurrentServerRevisions(ChangeAndRevisionSummary changes) {
        Map<IRI, Integer> revisions = new HashMap<IRI, Integer>();
        Map<IRI, ServerOntologyInfo> ontologyInfoMap = server.getOntologyInfoByIRI();
        for (OWLOntologyChange change : changes.getChanges()) {
            OWLOntology ontology = change.getOntology();
            IRI ontologyName = ontology.getOntologyID().getOntologyIRI();
            revisions.put(ontologyName, ontologyInfoMap.get(ontologyName).getMaxRevision());
        }
        return revisions;
    }
    
    private boolean applyClientChanges(HttpServletResponse response, ChangeAndRevisionSummary changes) throws ServletException {
        try {
            server.applyChanges(changes.getRevisions(), changes.getChanges());
            return true;
        } catch (OntologyConflictException e) {
            handleRejectedChanges(response, e);
            return false;
        }
        catch (RemoteOntologyException e) {
            throw new ServletException(e);
        }
    }
    
    private void handleRejectedChanges(HttpServletResponse response, OntologyConflictException e) throws ServletException {
        response.setStatus(HttpURLConnection.HTTP_CONFLICT);
        try {
            ChangeToAxiomConverter converter = new ChangeToAxiomConverter();
            for (OWLOntologyChange change : e.getRejectedChanges()) {
                change.accept(converter);
            }
            serializer.serialize(converter.getMetaOntology(), response.getOutputStream());
        }
        catch (Exception ooce) {
            throw new ServletException(ooce);
        }
    }
    
    private List<OWLOntologyChange> getChangesForClient(ChangeAndRevisionSummary clientInfo, Map<IRI, Integer> revisionsBeforeCommit) throws RemoteOntologyException {
        List<OWLOntologyChange> totalChanges = new ArrayList<OWLOntologyChange>();
        for (IRI ontologyName : revisionsBeforeCommit.keySet()) {
            List<OWLOntologyChange> changes = server.getChanges(ontologyName, 
                                                                clientInfo.getRevisions().get(ontologyName), 
                                                                revisionsBeforeCommit.get(ontologyName));
            List<OWLOntologyChange> redundant = new ArrayList<OWLOntologyChange>();
            for (OWLOntologyChange change : changes) {
                for (OWLOntologyChange clientChange : clientInfo.getChanges()) {
                    if (Utilities.overlappingChange(change, clientChange)) {
                        redundant.add(change);
                    }
                }
            }
            changes.removeAll(redundant);
        }
        return totalChanges;
    }
    
    private void sendChangesToClient(HttpServletResponse response, List<OWLOntologyChange> changesForClient) throws OWLOntologyCreationException, OWLOntologyStorageException, IOException {
        ChangeToAxiomConverter visitor = new ChangeToAxiomConverter();
        Set<OWLOntology> changedOntologies = new HashSet<OWLOntology>();
        for (OWLOntologyChange change : changesForClient) {
            change.accept(visitor);
            changedOntologies.add(change.getOntology());
        }
        for (OWLOntology ontology : changedOntologies) {
            IRI ontologyName = ontology.getOntologyID().getOntologyIRI();
            ServerOntologyInfo info = server.getOntologyInfoByIRI().get(ontologyName);
            visitor.addRevisionInfo(ontology, info.getMaxRevision());
        }
        OWLOntology summary = visitor.getMetaOntology();
        serializer.serialize(summary, response.getOutputStream());
    }

}
