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
import org.protege.owl.server.api.UserId;
import org.protege.owl.server.policy.generated.UsersAndGroupsLexer;
import org.protege.owl.server.policy.generated.UsersAndGroupsParser;
import org.testng.Assert;
import org.testng.annotations.Test;

public class UserParserTest {

    @Test
    public void basicUserParseTest() throws FileNotFoundException, IOException, RecognitionException {
        UserDatabase db = parseUserDatabase("src/test/resources/parser/UsersAndGroups01");
        UserId redmond = new UserId("redmond");
        Assert.assertTrue(db.checkPassword(redmond, "troglodyte"));
        Assert.assertEquals(2, db.getGroups(redmond).size());
    }
   
    
    @Test
    public void roundTripTest() throws FileNotFoundException, RecognitionException, IOException {
        roundTripTest("src/test/resources/parser/UsersAndGroups01");
    }
    
    private void roundTripTest(String fileName) throws FileNotFoundException, RecognitionException, IOException {
        UserDatabase p = parseUserDatabase(fileName);
        File tmp = File.createTempFile("UserDb", ".policy");
        Writer tmpWriter = new FileWriter(tmp);
        try {
            p.write(tmpWriter);
        }
        finally {
            tmpWriter.flush();
            tmpWriter.close();
        }
        UserDatabase p2 = parseUserDatabase(tmp.getAbsolutePath());
        Assert.assertEquals(p2, p);
    }
    
    
    private UserDatabase parseUserDatabase(String fileName) throws RecognitionException, FileNotFoundException, IOException {
        File usersAndGroups = new File(fileName);
        ANTLRInputStream input = new ANTLRInputStream(new FileInputStream(usersAndGroups));
        UsersAndGroupsLexer lexer = new UsersAndGroupsLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        UsersAndGroupsParser parser = new UsersAndGroupsParser(tokens);
        parser.top();
        return parser.getUserDatabase();
    }
}
