package org.protege.owl.server.policy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.protege.owl.server.api.ServerPath;
import org.protege.owl.server.policy.generated.PolicyLexer;
import org.protege.owl.server.policy.generated.PolicyParser;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PolicyParserTest {
    public static final ServerPath FERGERSON_PIZZA = new ServerPath("/pizza-fergerson.history");
    public static final ServerPath OTHER_PIZZA     = new ServerPath("/pizza.history");
    
    @Test
    public void testPolicy0() throws FileNotFoundException, RecognitionException, IOException {
        Policy policy = parsePolicy("src/test/resources/parser/Policy01");
        UserDatabase userDb = UserParserTest.parseUserDatabase("src/test/resources/parser/UsersAndGroups01");
        
        Assert.assertTrue(policy.checkPermission(userDb, UserParserTest.TANIA, FERGERSON_PIZZA, Operation.READ));
        Assert.assertTrue(policy.checkPermission(userDb, UserParserTest.TANIA, FERGERSON_PIZZA, Operation.WRITE));
        
        Assert.assertTrue(policy.checkPermission(userDb, UserParserTest.GUEST, FERGERSON_PIZZA, Operation.READ));
        Assert.assertFalse(policy.checkPermission(userDb, UserParserTest.GUEST, FERGERSON_PIZZA, Operation.WRITE));
        
        Assert.assertTrue(policy.checkPermission(userDb, UserParserTest.FERGERSON, FERGERSON_PIZZA, Operation.WRITE));
    }

    
    @Test
    public void roundTripTest() throws FileNotFoundException, RecognitionException, IOException {
        roundTripTest("src/test/resources/parser/Policy01");
    }
    
    private void roundTripTest(String fileName) throws FileNotFoundException, RecognitionException, IOException {
        Policy p = parsePolicy(fileName);
        File tmp = File.createTempFile("Policy", ".policy");
        Writer tmpWriter = new FileWriter(tmp);
        try {
            p.write(tmpWriter);
        }
        finally {
            tmpWriter.flush();
            tmpWriter.close();
        }
        Policy p2 = parsePolicy(tmp.getAbsolutePath());
        Assert.assertEquals(p2, p);
        tmp.delete();
    }
    
    private Policy parsePolicy(String fileName) throws RecognitionException, FileNotFoundException, IOException {
        File usersAndGroups = new File(fileName);
        ANTLRInputStream input = new ANTLRInputStream(new FileInputStream(usersAndGroups));
        PolicyLexer lexer = new PolicyLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        PolicyParser parser = new PolicyParser(tokens);
        parser.top();
        return parser.getPolicy();
    }
}
