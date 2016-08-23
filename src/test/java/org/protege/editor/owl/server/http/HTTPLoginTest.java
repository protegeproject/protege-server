package org.protege.editor.owl.server.http;

import edu.stanford.protege.metaproject.api.Serializer;
import edu.stanford.protege.metaproject.api.ServerConfiguration;
import edu.stanford.protege.metaproject.serialization.DefaultJsonSerializer;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.protege.editor.owl.server.api.exception.ServerServiceException;
import org.protege.editor.owl.server.http.messages.HttpAuthResponse;
import org.protege.editor.owl.server.http.messages.LoginCreds;
import org.testng.annotations.AfterClass;

import java.io.InputStreamReader;

@RunWith(MockitoJUnitRunner.class)
public class HTTPLoginTest {

    private static HTTPServer httpServer;
    
    private OkHttpClient client;

    @Mock private ServerConfiguration configuration;
    
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
    public void loginClientTest() throws ServerServiceException {

    	final MediaType JSON  = MediaType.parse("application/json; charset=utf-8");

    	String url = "http://localhost:8080/nci_protege/login";

    	LoginCreds creds = new LoginCreds("guest","guestpwd");
    	Serializer serl = new DefaultJsonSerializer();

    	RequestBody body = RequestBody.create(JSON, serl.write(creds, LoginCreds.class));

    	Request request = new Request.Builder()
    			.url(url)
    			.post(body)
    			.build();
    	
    	okhttp3.Response response;
    	try {
    		response = client.newCall(request).execute();
    		    		
    		HttpAuthResponse toke = 
    				(HttpAuthResponse) serl.parse(new InputStreamReader(response.body().byteStream()), HttpAuthResponse.class);

    		org.junit.Assert.assertTrue(!toke.getToken().equals(""));
    	} catch (Exception e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}


    } 
    
    @AfterClass
    public static void stopServer() throws Exception {
    	httpServer.stop();
    	
    }
}
