grammar UsersAndGroups;

@header{
package org.protege.owl.server.policy.generated;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.protege.owl.server.policy.Group;
import org.protege.owl.server.policy.SimpleAuthToken;
import org.protege.owl.server.policy.UserDatabase;
}

@lexer::header{
package org.protege.owl.server.policy.generated;
}


@members{
	private UserDatabase db = new UserDatabase();
	
	public UserDatabase getUserDatabase() {
	    return db;
	}
}

top:  ( user ) * ;

user : 'User:' username=ID 'Password:' password=ID { 
           SimpleAuthToken u = new SimpleAuthToken($username.getText(), $password.getText());
		   db.addUser(u);
       } 
       'Groups:'
          ( group[ u ] ) *
       ;
       
group[SimpleAuthToken user ]: groupToken = ID {
          String groupName = $groupToken.getText();
          Group group = new Group(groupName);
		  db.addGroup(user, group);
       }
       ;


ID  :   ('a'..'z'|'A'..'Z'|'.'|'/')  ('a'..'z'|'A'..'Z'|'.'|'/'|'_'|'-'|'0'..'9')* ;

NUM :   ('0'..'9') ('0' .. '9')* ;

WS  : (' '|'\r'|'\t'|'\u000C'|'\n') {$channel=HIDDEN;}
    ;
COMMENT
    : '/*' .* '*/' {$channel=HIDDEN;}
    ;
LINE_COMMENT
    : '//' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;}
    ;
