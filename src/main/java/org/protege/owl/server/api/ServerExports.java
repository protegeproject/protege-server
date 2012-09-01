package org.protege.owl.server.api;

import java.util.Collection;
import java.util.Map;

import org.protege.owl.server.api.exception.OWLServerException;

public interface ServerExports {
    
    OntologyDocumentRevision evaluateRevisionPointer(AuthToken u, ServerOntologyDocument doc, RevisionPointer pointer) throws OWLServerException;
    
    ServerDocument getServerDocument(AuthToken u, ServerPath serverIRI) throws OWLServerException;
    
    Collection<ServerDocument> list(AuthToken u, ServerDirectory dir) throws OWLServerException;
        
    ServerDirectory createDirectory(AuthToken u, ServerPath serverIRI) throws OWLServerException;

    ServerOntologyDocument createOntologyDocument(AuthToken u, ServerPath serverIRI, Map<String, Object> settings) throws OWLServerException;
        
    ChangeHistory getChanges(AuthToken u, ServerOntologyDocument doc, OntologyDocumentRevision start, OntologyDocumentRevision end) throws OWLServerException;

    /**
     * The call to commit changes.  The revision of the change history
     * passed in is the last revision that has been seen by the
     * client.  
     * <p/>
     * The commit operation is a bit complicated.  We will describe
     * its correct operation incrementally; we will first describe an
     * approximation to its correct behavior and then we will describe
     * a complication.  
     * <p/>
     * Suppose for a start that a user has some
     * change <i>uh<sub>1</sub></i> that he wants to commit.  The user
     * is at some revision, <i>r</i>, and on the server there is some change
     * history <i>sh<sub>1</sub></i> that represents all the changes
     * up to that revision.  After the revision <i>r</i> there is some
     * more (possibly empty) change history <i>sh<sub>2</sub></i> that
     * goes from revision <r>r</i> to the head revision.  To make the
     * commit, we must find a change history <i>uh<sub>1</sub>'</i>
     * such that 
     * <center>
     *     <i>sh<sub>1</sub>&#8226;uh<sub>1</sub>&#8226;sh<sub>2</sub>
     *      = sh<sub>1</sub>&#8226;sh<sub>2</sub>&#8226;uh<sub>1</sub>'</i>
     * </center>
     * Then the server can append the change <i>uh<sub>1</sub>'</i> to
     * his list of changes.  There are some utilities in the
     * {@link org.protege.owl.server.util.ChangeUtilities ChangeUtilities} class that
     * can calculate <i>uh<sub>1</sub>'</i>.
     * <vertical size=100/>
     * <p/>
     * The complication involves the case where the user makes
     * several commits in sequence.  The server needs to take some
     * measures to ensure that the user does not have a conflict with himself.
     * Thus as an example suppose that the server history is
     * <center>
     *      [rev. 0 - {AddAxiom <i>ax<sub>2</sub></i>}]
     * </center>
     * and the user makes two commits in sequence.  We will also
     * assume that the user is at revision 0; he has not yet seen <i>ax<sub>2</sub></i> . 
     * In the first commit
     * the user adds the axiom <i>ax<sub>1</sub></i>  (which we assume
     * is different from <i>ax<sub>2</sub></i>) and in the second
     * commit the user removes the same axiom.  After the first commit
     * the server will have the change history
     * <center>
     *      [rev. 0 - {AddAxiom <i>ax<sub>2</sub></i>}]
     *      [rev. 1 - {AddAxiom <i>ax<sub>1</sub></i>}]
     * </center>
     * The client is still at revision 0; he did not choose to update
     * and accept <i>ax<sub>2</sub></i> into his local copy of the
     * ontology.  Thus when the client tries to commit the remove
     * axiom <i>ax<sub>1</sub></i>, the server finds that the change
     * is redundant because the axiom is added after revision 0 (in
     * the future.)  The reason this happened is because this version
     * of the commit algorithm allows a user commit to come into
     * conflict with his previous commits.  This is easily repaired by
     * slightly modifying the algorithm used to handle commits.
     * </p>
     * Suppose that a user at revision <i>r</i> makes two commits in
     * sequence <i>uh<sub>1</sub></i> and <i>uh<sub>2</sub></i>.  The
     * server history from the start revision to 
     * revision <i>r</i> is 
     * <i>sh<sub>1</sub></i>.  At the time of the first commit the
     * server history after revision <i>r</i> is <i>sh<sub>2</sub></i>
     * and at the time o fthe second commit the server history
     * after revision <i>r</i> is
     * <center>
     *     <i>sh<sub>2</sub></i>&#8226;<i>sh<sub>3</sub></i>
     * </center>
     * We would like the final history of server changes to look like this
     * <center>
     *     <i>sh<sub>1</sub></i>&#8226;
     *       <i>sh<sub>2</sub></i> &#8226;<i>uh<sub>1</sub></i>'&#8226;
     *       <i>sh<sub>3</sub></i>&#8226;<i>uh<sub>2</sub></i>'
     * </center>
     * where <i>uh<sub>1</sub></i>' and <i>uh<sub>2</sub></i>' are the
     * minimal change histories with 
     * <center>
     *    <i>sh<sub>1</sub></i>&#8226;<i>uh<sub>1</sub></i>&#8226; <i>sh<sub>2</sub></i>&#8226;
     *               = 
     *     <i>sh<sub>1</sub></i>&#8226;
     *       <i>sh<sub>2</sub></i> &#8226;<i>uh<sub>1</sub></i>'
     * </center>
     * and
     * <center>
     *    <i>sh<sub>1</sub></i>&#8226;<i>uh<sub>1</sub></i>&#8226;<i>uh<sub>2</sub></i>
     *                  &#8226; <i>sh<sub>2</sub></i>&#8226;
     *               = 
     *     <i>sh<sub>1</sub></i>&#8226;<i>uh<sub>1</sub></i>
     *       <i>sh<sub>2</sub></i> &#8226;<i>sh<sub>3</sub></i>&#8226;<i>uh<sub>2</sub></i>'
     * </center>
     * </p>
     * This computation can be made simpler by using routines that given histories 
     * <i>uh<sub>1</sub></i> and <i>sh<sub>2</sub></i> will calculate a history 
     * <i>uh<sub>1</sub>'</i>  such that 
     * <center>
     *    <i>sh<sub>1</sub></i>&#8226;<i>uh<sub>1</sub></i>&#8226;<i>sh<sub>2</sub></i>
     *       = <i>sh<sub>1</sub></i>&#8226;<i>sh<sub>2</sub></i>&#8226;<i>uh<sub>1</sub>'</i>
     * </center>
     * holds for any history <i>sh<sub>1</sub></i>.  After using this routine the histories
     * <i>uh<sub>1</sub>'</i> and <i>uh<sub>2</sub>'</i> can be made minimal with respect with respect 
     * to the change histories that they are getting added to:
     * <center>
     *       <i>sh<sub>1</sub></i> &#8226;<i>sh<sub>2</sub></i>
     * </center>
     * and 
     * <center>
     *       <i>sh<sub>1</sub></i> &#8226;<i>sh<sub>2</sub></i>
     *         &#8226;<i>uh<sub>1</sub>'</i>&#8226;<i>sh<sub>3</sub></i>
     * </center>
     * respectively.
     *
     * @param u
     * @param doc
     * @param changes
     * @param myCommits
     * @param option
     * @return
     * @throws OWLServerException
     */
    void commit(AuthToken u, ServerOntologyDocument doc, SingletonChangeHistory changes) throws OWLServerException;

}
