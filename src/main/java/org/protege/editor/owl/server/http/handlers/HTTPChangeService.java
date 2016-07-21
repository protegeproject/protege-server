package org.protege.editor.owl.server.http.handlers;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.protege.editor.owl.server.api.ChangeService;
import org.protege.editor.owl.server.api.CommitBundle;
import org.protege.editor.owl.server.api.exception.OutOfSyncException;
import org.protege.editor.owl.server.base.ProtegeServer;
import org.protege.editor.owl.server.change.ChangeDocumentPool;
import org.protege.editor.owl.server.change.ChangeManagementFilter;
import org.protege.editor.owl.server.change.DefaultChangeService;
import org.protege.editor.owl.server.conflict.ConflictDetectionFilter;
import org.protege.editor.owl.server.http.HTTPServer;
import org.protege.editor.owl.server.http.exception.ServerException;
import org.protege.editor.owl.server.policy.AccessControlFilter;
import org.protege.editor.owl.server.versioning.api.ChangeHistory;
import org.protege.editor.owl.server.versioning.api.DocumentRevision;
import org.protege.editor.owl.server.versioning.api.HistoryFile;

import edu.stanford.protege.metaproject.api.ProjectId;
import io.undertow.server.HttpServerExchange;

public class HTTPChangeService extends BaseRoutingHandler {
	
	private ChangeService changeService;
	private ConflictDetectionFilter cf;
	private AccessControlFilter acf;

    public HTTPChangeService(ProtegeServer s) {
    	super();
    	cf = new ConflictDetectionFilter(new ChangeManagementFilter(s));
    	acf = new AccessControlFilter(cf);
    	changeService = new DefaultChangeService(acf.getChangePool());    	
    }

	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		if (exchange.getRequestPath().equalsIgnoreCase(HTTPServer.COMMIT)) {

			ObjectInputStream ois = new ObjectInputStream(exchange.getInputStream());
			ProjectId pid = (ProjectId) ois.readObject();
			CommitBundle bundle = (CommitBundle) ois.readObject();
			
			ChangeHistory hist = null;
			
			ObjectOutputStream os = new ObjectOutputStream(exchange.getOutputStream());
			
			try {
				hist = acf.commit(getAuthToken(exchange), pid, bundle);
				os.writeObject(hist);
			} catch (Exception e) {
				os.writeObject(new ServerException(500, "Server processing error", e));
				exchange.setStatusCode(500);

			}		
			
			exchange.endExchange();
		} else if (exchange.getRequestPath().equalsIgnoreCase(HTTPServer.ALL_CHANGES)) {
			long beg = System.currentTimeMillis();
			ObjectInputStream ois = new ObjectInputStream(exchange.getInputStream());
			HistoryFile file = (HistoryFile) ois.readObject();
			DocumentRevision headRevision = changeService.getHeadRevision(file);			
			ChangeHistory history = changeService.getChanges(file, DocumentRevision.START_REVISION, headRevision);			
			ObjectOutputStream os = new ObjectOutputStream(exchange.getOutputStream());
			os.writeObject(history);
			exchange.endExchange();
			System.out.println("Time execute all changes " + (System.currentTimeMillis() - beg)/1000);

		} else if (exchange.getRequestPath().equalsIgnoreCase(HTTPServer.LATEST_CHANGES)) {
			ObjectInputStream ois = new ObjectInputStream(exchange.getInputStream());
			HistoryFile file = (HistoryFile) ois.readObject();
			DocumentRevision start = (DocumentRevision) ois.readObject();			
			DocumentRevision headRevision = changeService.getHeadRevision(file);
			ChangeHistory history = changeService.getChanges(file, start, headRevision);
			ObjectOutputStream os = new ObjectOutputStream(exchange.getOutputStream());
			os.writeObject(history);
			exchange.endExchange();

		} else if (exchange.getRequestPath().equalsIgnoreCase(HTTPServer.HEAD)) {
			ObjectInputStream ois = new ObjectInputStream(exchange.getInputStream());
			HistoryFile file = (HistoryFile) ois.readObject();
			//DocumentRevision start = (DocumentRevision) ois.readObject();			
			DocumentRevision headRevision = changeService.getHeadRevision(file);
			ObjectOutputStream os = new ObjectOutputStream(exchange.getOutputStream());
			os.writeObject(headRevision);
			exchange.endExchange();

		}
	}	
}
