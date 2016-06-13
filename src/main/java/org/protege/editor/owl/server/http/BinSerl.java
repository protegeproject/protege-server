package org.protege.editor.owl.server.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.semanticweb.binaryowl.BinaryOWLOntologyDocumentSerializer;
import org.semanticweb.binaryowl.owlapi.BinaryOWLOntologyBuildingHandler;
import org.semanticweb.binaryowl.owlapi.OWLOntologyWrapper;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class BinSerl {
	// A simple smoke test to measure the speed of binary serializer
	
	public static void main(String[] args) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        
        OWLOntology ont;
		try {
			
			ont = manager.loadOntologyFromOntologyDocument(new File("thesaurus.owl"));
			long beg = System.currentTimeMillis();
			BinaryOWLOntologyDocumentSerializer serializer = new BinaryOWLOntologyDocumentSerializer();
	        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(new File("thes")));
	        serializer.write(new OWLOntologyWrapper(ont), new DataOutputStream(outputStream));
	        outputStream.close();
	        System.out.println("Time to serialize out " + (System.currentTimeMillis() - beg)/1000);
	        
	        OWLOntologyManager manIn = OWLManager.createOWLOntologyManager();
	        OWLOntology ontIn = manIn.createOntology();
	        BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(new File("thes")));
	        serializer.read(inputStream, new BinaryOWLOntologyBuildingHandler(ontIn), manager.getOWLDataFactory());
	        System.out.println("Time to serialize in " + (System.currentTimeMillis() - beg)/1000);
	        
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		// TODO Auto-generated method stub

	}

}
