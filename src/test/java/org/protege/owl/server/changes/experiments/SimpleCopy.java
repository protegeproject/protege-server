package org.protege.owl.server.changes.experiments;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SimpleCopy {
	public static final File ONTOLOGY_LOCATION = new File("/Users/tredmond/work/Shared/ontologies/NCI/Thesaurus-11.01e-fixed-annotations.owl");
	
	public static void main(String[] args) throws IOException {
		long startTime = System.currentTimeMillis();
		InputStream is = new BufferedInputStream(new FileInputStream(ONTOLOGY_LOCATION));
		File tmpFile = File.createTempFile("Performance", ".test");
		OutputStream os = new BufferedOutputStream(new FileOutputStream(tmpFile));
		int c;
		while ((c = is.read()) >= 0) {
			os.write(c);
		}
		os.flush();
		is.close();
		os.close();
		System.out.println("Took " + (System.currentTimeMillis() - startTime) + "ms.");
	}

}
