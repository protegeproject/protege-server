package org.protege.editor.owl.server.util;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.semanticweb.binaryowl.BinaryOWLOntologyDocumentSerializer;
import org.semanticweb.binaryowl.owlapi.BinaryOWLOntologyBuildingHandler;
import org.semanticweb.binaryowl.owlapi.OWLOntologyWrapper;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class SnapShot implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5394391545733189187L;
	
	private OWLOntology ont;
	
	public SnapShot(OWLOntology o) {
		ont = o;
	}
	
	public OWLOntology getOntology() {
		return ont;
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
        try {
        	BinaryOWLOntologyDocumentSerializer serializer = new BinaryOWLOntologyDocumentSerializer();
			
			serializer.write(new OWLOntologyWrapper(ont), new DataOutputStream(out));
            
        }
        catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
        finally {
            out.flush();
//            out.close(); // Do not close the stream! The other end point might not get the whole data stream.
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        OWLOntologyManager owlManager = OWLManager.createOWLOntologyManager();
        try {
        	BinaryOWLOntologyDocumentSerializer serializer = new BinaryOWLOntologyDocumentSerializer();
			//OWLOntologyManager manIn = OWLManager.createOWLOntologyManager();
	        OWLOntology ontIn = owlManager.createOntology();
	        BufferedInputStream inputStream = new BufferedInputStream(in);
	        serializer.read(inputStream, new BinaryOWLOntologyBuildingHandler(ontIn), owlManager.getOWLDataFactory());
            
            ont = ontIn;
        }
        catch (OWLOntologyCreationException e) {
            throw new IOException("Internal error while reading commit object", e);
        }
        finally {
//            in.close();
        }
    }

}
