package org.protege.editor.owl.server.http;

import com.google.gson.Gson;
import edu.stanford.protege.metaproject.api.Serializer;
import edu.stanford.protege.metaproject.api.ServerConfiguration;
import edu.stanford.protege.metaproject.serialization.DefaultJsonSerializer;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.protege.editor.owl.server.api.CommitBundle;
import org.protege.editor.owl.server.http.messages.HttpAuthResponse;
import org.protege.editor.owl.server.http.messages.LoginCreds;
import org.protege.editor.owl.server.policy.CommitBundleImpl;
import org.protege.editor.owl.server.util.GetUncommittedChangesVisitor;
import org.protege.editor.owl.server.versioning.Commit;
import org.protege.editor.owl.server.versioning.api.ChangeHistory;
import org.protege.editor.owl.server.versioning.api.DocumentRevision;
import org.protege.editor.owl.server.versioning.api.RevisionMetadata;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLRuntimeException;

import java.io.*;
import java.net.URISyntaxException;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class HTTPCommitTest {

	private static HTTPServer httpServer;

	private OkHttpClient client;

	@Mock private ServerConfiguration configuration;

	protected static final File pizzaOntology() {
		try {
			return new File(HTTPCommitTest.class.getResource("/pizza.owl").toURI());
		}
		catch (URISyntaxException e) {
			throw new OWLRuntimeException("File not found", e);
		}
	}

	@BeforeClass
	public static void startServer() throws Exception {
		httpServer = new HTTPServer();
		httpServer.start();

	}

	@Before
	public void setUp() throws Exception {
		client = new OkHttpClient();
	}

	@Test
	public void commitTest() throws Exception {

		final MediaType JSON  = MediaType.parse("application/json; charset=utf-8");

		String url = "http://localhost:8080/nci_protege/login";

		LoginCreds creds = new LoginCreds("guest","guestpwd");
		Serializer<Gson> serl = new DefaultJsonSerializer();

		RequestBody body = RequestBody.create(JSON, serl.write(creds, LoginCreds.class));

		Request request = new Request.Builder()
				.url(url)
				.post(body)
				.build();

		okhttp3.Response response;

		response = client.newCall(request).execute();

		HttpAuthResponse toke = 
				(HttpAuthResponse) serl.parse(new InputStreamReader(response.body().byteStream()), HttpAuthResponse.class);

		org.junit.Assert.assertTrue(!toke.getToken().equals(""));

		String token = toke.getToken();

		String toenc = "guest:" + token;

		String auths = "Basic " + new String(Base64.encodeBase64(toenc.getBytes())); 

		// Now we have a login



		OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(pizzaOntology());
		GetUncommittedChangesVisitor visitor = new GetUncommittedChangesVisitor(ontology);
		List<OWLOntologyChange> changes = visitor.getChanges();
		RevisionMetadata metadata = new RevisionMetadata(
				"guest",
				"Guest User",
				"",
				"First commit");
		CommitBundle commitBundle = new CommitBundleImpl(DocumentRevision.START_REVISION, new Commit(metadata, changes));

		ByteArrayOutputStream b = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(b);

		os.writeObject(commitBundle);

		RequestBody req2 = RequestBody.create(MediaType.parse("application"), b.toByteArray());

		String commiturl = "http://localhost:8080/nci_protege/commit";

		Request request2 = new Request.Builder()
				.url(commiturl)
				.addHeader("Authorization", auths)
				.post(req2)
				.build();

		okhttp3.Response response2;


		response2 = client.newCall(request2).execute();

		org.junit.Assert.assertTrue(response2.code() == 200);

		ObjectInputStream is = new ObjectInputStream(response2.body().byteStream());

		ChangeHistory history = (ChangeHistory) is.readObject();

		org.junit.Assert.assertTrue(history.getRevisions().size() == 1);






	}  


}
