package org.protege.owl.server.protegedb;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.protege.owl.server.api.RemoteOntologyRevisions;
import org.protege.owl.server.exception.RemoteOntologyCreationException;
import org.protege.owl.server.exception.RemoteOntologyException;
import org.protege.owl.server.util.AbstractServer;
import org.protege.owlapi.apibinding.ProtegeOWLManager;
import org.protege.owlapi.database.DatabaseOntology;
import org.protege.owlapi.database.DatabaseOntologyFactory;
import org.protege.owlapi.database.DatabaseSupport;
import org.protege.owlapi.database.exception.DatabaseRuntimeException;
import org.protege.owlapi.database.pool.ConnectionPool;
import org.protege.owlapi.database.serialize.Serializer;
import org.protege.owlapi.model.ProtegeOWLOntologyManager;
import org.protege.owlapi.util.SQLCallable;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyFactory;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.RemoveImport;

public class DatabaseServer extends AbstractServer {
    private Logger log = Logger.getLogger(getClass());
    private DatabaseSupport dbManagement;
    private ConnectionPool pool;
    private Map<IRI, RemoteOntologyRevisions> ontologyList = new HashMap<IRI, RemoteOntologyRevisions>();
    private Map<IRI, String> tablePrefixMap = new HashMap<IRI, String>();
    
    public DatabaseServer(String databaseUrl,
                          String userName, String password) throws IOException, SQLException, ClassNotFoundException {
        super(ProtegeOWLManager.createOWLOntologyManager());
        DatabaseSupport.useDatabaseOntologyFactory(getOntologyManager(), 
                                              databaseUrl, 
                                              userName, 
                                              password, 
                                              false, false);
        for (OWLOntologyFactory factory : getOntologyManager().getOWLOntologyFactories()) {
            if (factory instanceof DatabaseOntologyFactory) {
                dbManagement = ((DatabaseOntologyFactory) factory).getManagement();
                break;
            }
        }
        pool = dbManagement.getConnectionPool();
        initializeOntologyList();
    }
    
    private void initializeOntologyList() throws SQLException {
        Connection connection = pool.getConnection();
        try {
            Statement stmt = pool.getStatement(connection);
            for (OWLOntology ontology : getOntologyManager().getOntologies()) {
                if (ontology instanceof DatabaseOntology) {
                    DatabaseOntology dbOntology = (DatabaseOntology) ontology;
                    IRI name = dbOntology.getOntologyID().getOntologyIRI();
                    String prefix = dbOntology.getTablePrefix();
                    tablePrefixMap.put(name, prefix);
                    stmt.execute("CREATE TABLE " + DatabaseSupport.getSavedOntologiesTable(prefix) + " IF NOT EXISTS (revision INT, location VARCHAR(255))");
                    try {
                        stmt.execute("CREATE INDEX " + prefix + "AxiomsLastValidIdIndex ON " +
                                     DatabaseSupport.getAxiomsTable(prefix)  + " (lastValidId)");
                    }
                    catch (SQLException e) {
                        if (log.isDebugEnabled()) {
                            log.debug("Exception caught making lastValidId index", e);
                        }
                    }
                    PreparedStatement lookup = pool.getPreparedStatement(connection, 
                                                                         "SELECT revision FROM " + DatabaseSupport.getSavedOntologiesTable(prefix));
                    Set<Integer> revisions = new SQLCallable<Set<Integer>>() {
                        @Override
                        protected Set<Integer> call(ResultSet rs) throws SQLException {
                            Set<Integer> revisions = new HashSet<Integer>();
                            while (rs.next()) {
                                revisions.add(rs.getInt(1));
                            }
                            return revisions;                        }
                    }.executeQuery(lookup);
                    // TODO if  revisions is empty create a file...  Actually lets change org.protege.owlapi
                    ontologyList.put(name, new RemoteOntologyRevisions(name, revisions, dbOntology.getRevision()));
                }
            }
        }
        finally {
            pool.ungetConnection(connection);
        }
    }

    public ProtegeOWLOntologyManager getOntologyManager() {
        return (ProtegeOWLOntologyManager) super.getOntologyManager();
    }
    
    @Override
    protected boolean contains(IRI ontologyName, int revision, OWLAxiom axiom) {
        Connection connection = pool.getConnection();
        try {
            String prefix = tablePrefixMap.get(ontologyName);
            RemoteOntologyRevisions revisions = ontologyList.get(ontologyName);
            String query = "SELECT axiom, axiomType, hashCode FROM "
                + DatabaseSupport.getAxiomsTable(prefix)
                + " WHERE id <= ? AND (lastValidId IS NULL OR lastValidId >= ?) AND hashCode=?";
            PreparedStatement lookup = pool.getPreparedStatement(connection, query);
            lookup.setInt(1, revisions.getMaxRevision());
            lookup.setInt(2, revisions.getMaxRevision());
            lookup.setInt(3, axiom.hashCode());
            return dbManagement.getSerializer().getAxioms(lookup).contains(axiom);
        }
        catch (Exception e) {
            throw new DatabaseRuntimeException(e);
        }
        finally {
            pool.ungetConnection(connection);
        }
    }

    @Override
    protected boolean contains(IRI ontologyName, int revision, final OWLImportsDeclaration owlImport) {
        Connection connection = pool.getConnection();
        try {
            String prefix = tablePrefixMap.get(ontologyName);
            RemoteOntologyRevisions revisions = ontologyList.get(ontologyName);
            String query = "SELECT iri FROM "
                + DatabaseSupport.getOntologyImportsTable(prefix)
                + " WHERE id <= ? AND (lastValidId IS NULL OR lastValidId >= ?) AND iri=?";
            PreparedStatement lookup = pool.getPreparedStatement(connection, query);
            lookup.setInt(1, revisions.getMaxRevision());
            lookup.setInt(2, revisions.getMaxRevision());
            lookup.setString(3, owlImport.getIRI().toString());
            return new SQLCallable<Boolean>() {
                @Override
                protected Boolean call(ResultSet rs) throws SQLException {
                    while (rs.next()) {
                        if (rs.getString(1).equals(owlImport.getIRI().toString())) {
                            return true;
                        }
                    }
                    return false;
                }
            }.executeQuery(lookup);
        }
        catch (Exception e) {
            throw new DatabaseRuntimeException(e);
        }
        finally {
            pool.ungetConnection(connection);
        }
    }

    @Override
    protected boolean contains(IRI ontologyName, int revision, final OWLAnnotation annotation) {
        Connection connection = pool.getConnection();
        try {
            String prefix = tablePrefixMap.get(ontologyName);
            RemoteOntologyRevisions revisions = ontologyList.get(ontologyName);
            String query = "SELECT annotation FROM "
                + DatabaseSupport.getAxiomsTable(prefix)
                + " WHERE id <= ? AND (lastValidId IS NULL OR lastValidId >= ?) AND hashCode=?";
            PreparedStatement lookup = pool.getPreparedStatement(connection, query);
            lookup.setInt(1, revisions.getMaxRevision());
            lookup.setInt(2, revisions.getMaxRevision());
            lookup.setInt(3, annotation.hashCode());
            return new SQLCallable<Boolean>() {
                @Override
                protected Boolean call(ResultSet rs) throws SQLException {
                    while (rs.next()) {
                        try {
                            if (dbManagement.getSerializer().getAnnotations(rs.getString(1)).contains(annotation)) {
                                return true;
                            }
                        } catch (Exception e) {
                            throw new DatabaseRuntimeException(e);
                        }
                    }
                    return false;
                }
            }.executeQuery(lookup);
        }
        catch (Exception e) {
            throw new DatabaseRuntimeException(e);
        }
        finally {
            pool.ungetConnection(connection);
        }
    }

    @Override
    public List<OWLOntologyChange> getChanges(IRI ontologyName, int version1, int version2) throws RemoteOntologyException {
        OWLOntology ontology = getOntologyManager().getOntology(ontologyName);
        String prefix = tablePrefixMap.get(ontologyName);
        Connection connection = pool.getConnection();
        try {
            List<OWLOntologyChange> changes = getAxiomChanges(ontology, connection, prefix, version1, version2);
            changes.addAll(getAnnotationChanges(ontology, connection, prefix, version1, version2));
            changes.addAll(getImportChanges(ontology, connection, prefix, version1, version2));
            return changes;
        }
        catch (Exception e) {
            throw new DatabaseRuntimeException(e);
        }
        finally {
            pool.ungetConnection(connection);
        }
    }
    
    private List<OWLOntologyChange> getAnnotationChanges(final OWLOntology ontology, 
                                                          Connection connection, String prefix, 
                                                          int version1, int version2) throws SQLException {
        final Serializer serializer = dbManagement.getSerializer();
        final List<OWLOntologyChange> axiomChanges = new ArrayList<OWLOntologyChange>();
        String removeAxiomQuery = "SELECT annotation FROM "
            + DatabaseSupport.getOntologyAnnotationsTable(prefix)
            + " WHERE lastValidId >= ? AND lastValidId <= ?";
        PreparedStatement lookup = pool.getPreparedStatement(connection, removeAxiomQuery);
        lookup.setInt(1, version1);
        lookup.setInt(2, version2);
        new SQLCallable<Boolean>() {
            @Override
            protected Boolean call(ResultSet rs) throws SQLException {
                try {
                    while (rs.next()) {
                        Set<OWLAnnotation> annotations = serializer.getAnnotations(rs.getString(1));
                        axiomChanges.add(new AddOntologyAnnotation(ontology, annotations.iterator().next()));
                    }
                    return true;
                }
                catch (SQLException sqle) {
                    throw sqle;
                }
                catch (Exception e) {
                    throw new DatabaseRuntimeException(e);
                }
            }
        }.executeQuery(lookup);
        String addAxiomQuery = "SELECT annotation FROM "
            + DatabaseSupport.getOntologyAnnotationsTable(prefix)
            + " WHERE id >= ? AND (lastValidId IS NULL OR lastValidId >= ?)";
        lookup = pool.getPreparedStatement(connection, addAxiomQuery);
        lookup.setInt(1, version1);
        lookup.setInt(2, version2);
        new SQLCallable<Boolean>() {
            @Override
            protected Boolean call(ResultSet rs) throws SQLException {
                try {
                    while (rs.next()) {
                        Set<OWLAnnotation> annotations = serializer.getAnnotations(rs.getString(1));
                        axiomChanges.add(new AddOntologyAnnotation(ontology, annotations.iterator().next()));
                    }
                    return true;
                }
                catch (SQLException sqle) {
                    throw sqle;
                }
                catch (Exception e) {
                    throw new DatabaseRuntimeException(e);
                }
            }
        }.executeQuery(lookup);
        
        return axiomChanges;
    }
    
    private List<OWLOntologyChange> getImportChanges(final OWLOntology ontology, 
                                                     Connection connection, String prefix, 
                                                     int version1, int version2) throws SQLException {
        final Serializer serializer = dbManagement.getSerializer();
        final List<OWLOntologyChange> axiomChanges = new ArrayList<OWLOntologyChange>();
        final OWLDataFactory factory = getOntologyManager().getOWLDataFactory();
        String removeAxiomQuery = "SELECT iri FROM "
            + DatabaseSupport.getOntologyImportsTable(prefix)
            + " WHERE lastValidId >= ? AND lastValidId <= ?";
        PreparedStatement lookup = pool.getPreparedStatement(connection, removeAxiomQuery);
        lookup.setInt(1, version1);
        lookup.setInt(2, version2);
        new SQLCallable<Boolean>() {
            @Override
            protected Boolean call(ResultSet rs) throws SQLException {
                try {
                    while (rs.next()) {
                        OWLImportsDeclaration decl = factory.getOWLImportsDeclaration(IRI.create(rs.getString(1)));
                        axiomChanges.add(new RemoveImport(ontology, decl));
                    }
                    return true;
                }
                catch (SQLException sqle) {
                    throw sqle;
                }
                catch (Exception e) {
                    throw new DatabaseRuntimeException(e);
                }
            }
        }.executeQuery(lookup);
        String addAxiomQuery = "SELECT iri FROM "
            + DatabaseSupport.getOntologyImportsTable(prefix)
            + " WHERE id >= ? AND (lastValidId IS NULL OR lastValidId >= ?)";
        lookup = pool.getPreparedStatement(connection, addAxiomQuery);
        lookup.setInt(1, version1);
        lookup.setInt(2, version2);
        new SQLCallable<Boolean>() {
            @Override
            protected Boolean call(ResultSet rs) throws SQLException {
                try {
                    while (rs.next()) {
                        OWLImportsDeclaration decl = factory.getOWLImportsDeclaration(IRI.create(rs.getString(1)));
                        axiomChanges.add(new AddImport(ontology, decl));
                    }
                    return true;
                }
                catch (SQLException sqle) {
                    throw sqle;
                }
                catch (Exception e) {
                    throw new DatabaseRuntimeException(e);
                }
            }
        }.executeQuery(lookup);
        
        return axiomChanges;
    }
    
    private List<OWLOntologyChange> getAxiomChanges(final OWLOntology ontology, 
                                                    Connection connection, String prefix, 
                                                    int version1, int version2) throws SQLException {
        final Serializer serializer = dbManagement.getSerializer();
        final List<OWLOntologyChange> axiomChanges = new ArrayList<OWLOntologyChange>();
        String removeAxiomQuery = "SELECT axiom, axiomType, hashCode FROM "
            + DatabaseSupport.getAxiomsTable(prefix)
            + " WHERE lastValidId >= ? AND lastValidId <= ?";
        PreparedStatement lookup = pool.getPreparedStatement(connection, removeAxiomQuery);
        lookup.setInt(1, version1);
        lookup.setInt(2, version2);
        new SQLCallable<Boolean>() {
            @Override
            protected Boolean call(ResultSet rs) throws SQLException {
                try {
                    while (rs.next()) {
                        OWLAxiom axiom = serializer.getAxiom(rs.getString(1));
                        axiomChanges.add(new RemoveAxiom(ontology, axiom));
                    }
                    return true;
                }
                catch (SQLException sqle) {
                    throw sqle;
                }
                catch (Exception e) {
                    throw new DatabaseRuntimeException(e);
                }
            }
        }.executeQuery(lookup);
        String addAxiomQuery = "SELECT axiom, axiomType, hashCode FROM "
            + DatabaseSupport.getAxiomsTable(prefix)
            + " WHERE id >= ? AND (lastValidId IS NULL OR lastValidId >= ?)";
        lookup = pool.getPreparedStatement(connection, addAxiomQuery);
        lookup.setInt(1, version1);
        lookup.setInt(2, version2);
        new SQLCallable<Boolean>() {
            @Override
            protected Boolean call(ResultSet rs) throws SQLException {
                try {
                    while (rs.next()) {
                        OWLAxiom axiom = serializer.getAxiom(rs.getString(1));
                        axiomChanges.add(new AddAxiom(ontology, axiom));
                    }
                    return true;
                }
                catch (SQLException sqle) {
                    throw sqle;
                }
                catch (Exception e) {
                    throw new DatabaseRuntimeException(e);
                }
            }
        }.executeQuery(lookup);
        
        return axiomChanges;
    }

    @Override
    public synchronized Map<IRI, RemoteOntologyRevisions> getOntologyList() {
        return new HashMap<IRI, RemoteOntologyRevisions>(ontologyList);
    }

    @Override
    public InputStream getOntologyStream(IRI ontologyName, int revision) throws RemoteOntologyCreationException {
        Connection connection = pool.getConnection();
        try {
            String prefix = tablePrefixMap.get(ontologyName);
            PreparedStatement lookup = pool.getPreparedStatement(connection, 
                                                                 "SELECT location FROM " + DatabaseSupport.getSavedOntologiesTable(prefix) + " WHERE revision = ?");
           String location = new SQLCallable<String>() {
                @Override
                protected String call(ResultSet rs) throws SQLException {
                    if (rs.next()) {
                        return rs.getString(1);
                    }
                    return null;                        
                }
            }.executeQuery(lookup);
            return new FileInputStream(location);
        }
        catch (Exception e) {
            throw new RemoteOntologyCreationException(e);
        }
        finally {
            pool.ungetConnection(connection);
        }
    }

    @Override
    public Set<OWLOntology> getOntologies() {
        Set<OWLOntology> ontologies = new HashSet<OWLOntology>();
        for (OWLOntology ontology : getOntologyManager().getOntologies()) {
            if (ontology instanceof DatabaseOntology) {
                DatabaseOntology dbOntology = (DatabaseOntology) ontology;
                ontologies.add(dbOntology);
            }
        }
        return ontologies;
    }
    
    @Override
    public void save(OWLOntologyID id, int revision, File location) throws IOException, OWLOntologyStorageException {
        super.save(id, revision, location);
        Connection connection = pool.getConnection();
        try {
            String prefix = tablePrefixMap.get(id.getOntologyIRI());
            PreparedStatement lookup = pool.getPreparedStatement(connection, 
                                                                 "UPDATE " + DatabaseSupport.getSavedOntologiesTable(prefix) + " SET location = ? WHERE revision = ?");
            lookup.setString(1, location.getAbsolutePath());
            lookup.setInt(2, revision);
            lookup.execute();
        }
        catch (Exception e) {
            throw new OWLOntologyStorageException(e);
        }
        finally {
            pool.ungetConnection(connection);
        }
    }
 
}
