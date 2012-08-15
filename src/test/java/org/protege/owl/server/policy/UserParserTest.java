package org.protege.owl.server.policy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.protege.owl.server.api.User;
import org.protege.owl.server.policy.generated.UsersAndGroupsLexer;
import org.protege.owl.server.policy.generated.UsersAndGroupsParser;
import org.testng.Assert;
import org.testng.annotations.Test;

public class UserParserTest {

    @Test
    public void parserTest() throws FileNotFoundException, IOException, RecognitionException {
        File usersAndGroups = new File("etc/standalone/configuration/UsersAndGroups");
        ANTLRInputStream input = new ANTLRInputStream(new FileInputStream(usersAndGroups));
        UsersAndGroupsLexer lexer = new UsersAndGroupsLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        UsersAndGroupsParser parser = new UsersAndGroupsParser(tokens);
        parser.top();
        User redmond = null;
        for (User someone : parser.getUsers()) {
            if (someone.getUserName().equals("redmond")) {
                redmond = someone;
            }
        }
        Assert.assertNotNull(redmond);
        Assert.assertTrue(redmond instanceof UserImpl);
        Assert.assertEquals(((UserImpl) redmond).getSecret(), "troglodyte");
    }
}
