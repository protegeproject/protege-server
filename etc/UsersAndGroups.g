grammar UsersAndGroups;

@header{
package org.protege.owl.server.policy.generated;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.protege.owl.server.api.UserId;
import org.protege.owl.server.policy.Group;
import org.protege.owl.server.policy.Operation;
import org.protege.owl.server.policy.Permission;
import org.protege.owl.server.policy.SimpleAuthToken;
import org.protege.owl.server.policy.UserContainer;
import org.protege.owl.server.policy.UserContainerImpl;
import org.protege.owl.server.policy.UserDatabase;
}

@lexer::header{
package org.protege.owl.server.policy.generated;
}


@members{
	private UserDatabase db = new UserDatabase();
    private Map<String, Permission> namedPolicyMap = new TreeMap<String, Permission>();
	
	public UserDatabase getUserDatabase() {
	    return db;
	}
}

top:  ( defaultDocPolicy )? ( namedPolicy |  user ) * ;

defaultDocPolicy: 'Default' 'Document' p=policy ';' {
      db.setDefaultDocPolicy(p);
   };

namedPolicy: 'Set' name=ID '=' p=policy ';' {
            namedPolicyMap.put(name.getText(), p);
        };

user : 'User:' username=ID 'Password:' password=ID { 
           UserId u = db.addUser($username.getText(), $password.getText());
       } 
        ('Groups:'
          ( group[ u ] ) *
          ';' )?
        ( 'Use' 'Document' 'Policy' p=ID ';' {
                db.setDefaultDocPolicy(u, namedPolicyMap.get($p.getText()));
            } )?
       ;
       
group[UserId user ]: groupToken = ID {
          String groupName = $groupToken.getText();
          Group group = new Group(groupName);
		  db.addGroup(user, group);
       }
       ;
       
policy returns [Permission perm ]: 'Policy' '('
       { Map<Operation, UserContainer> allowedOpMap = new TreeMap<Operation, UserContainer>(); }
     ( 
      'Allow' '['
          container=usercontainer
       ']' 'to' operation=ID ';'
       { allowedOpMap.put(new Operation($operation.getText()), container); }
        )*  
       { perm = new Permission(allowedOpMap); }
   ')'
  ;

usercontainer returns [UserContainer container]:
       ( 
            { 
              boolean allowOwner = false; 
              Set<Group> groups = new TreeSet<Group>();
             }
            ( 'Owner' { allowOwner = true; }  )?
            ( 'Group' ( groupName=ID { groups.add(new Group($groupName.getText())); } )*  )? 
            { container = new UserContainerImpl(allowOwner, groups); }
        ) 
     |
        ( 'All' { container = UserContainer.EVERYONE; } ) 
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
