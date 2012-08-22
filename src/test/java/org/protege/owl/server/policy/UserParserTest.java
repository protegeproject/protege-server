package org.protege.owl.server.policy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

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
    public void policyTest01() throws FileNotFoundException, RecognitionException, IOException {
        UserDatabase db = parseUserDatabase("src/test/resources/parser/UsersAndGroups01");
        Permission perm = parsePolicy("src/test/resources/parser/Policy01");
        UserId owner = new UserId("fergerson");
        UserId tim   = new UserId("redmond");
        UserId guest = new UserId("guest");
        Assert.assertTrue(perm.isAllowed(db, owner, owner, Operation.WRITE));
        Assert.assertTrue(perm.isAllowed(db, owner, tim, Operation.WRITE));
        Assert.assertFalse(perm.isAllowed(db, owner, guest, Operation.WRITE));
        Assert.assertTrue(perm.isAllowed(db, owner, guest, Operation.READ));
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
    
    private Permission parsePolicy(String fileName) throws RecognitionException, FileNotFoundException, IOException {
        File usersAndGroups = new File(fileName);
        ANTLRInputStream input = new ANTLRInputStream(new FileInputStream(usersAndGroups));
        UsersAndGroupsLexer lexer = new UsersAndGroupsLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        UsersAndGroupsParser parser = new UsersAndGroupsParser(tokens);
        return parser.policy();
    }
}
