grammar Policy;

@header{
package org.protege.owl.server.policy.generated;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.antlr.runtime.BitSet;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.Parser;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;
import org.protege.owl.server.api.UserId;
import org.protege.owl.server.policy.Group;
import org.protege.owl.server.policy.Operation;
import org.protege.owl.server.policy.Permission;
import org.protege.owl.server.policy.Policy;
import org.protege.owl.server.policy.UserContainer;
import org.protege.owl.server.policy.UserContainerImpl;
}

@lexer::header{
package org.protege.owl.server.policy.generated;
}

@members{
	private Policy policy = new Policy();
	
	public Policy getPolicy() {
	    return policy;
	}
}

top:  ( policy ) * ;


policy: 'Policy' '('
       { Permission perm = null;
         Map<Operation, UserContainer> allowedOpMap = new TreeMap<Operation, UserContainer>(); }
     ( 
      'Allow' '['
          container=usercontainer
       ']' 'to' operation=ID ';'
       { allowedOpMap.put(new Operation($operation.getText()), container); }
        )*  
       { perm = new Permission(allowedOpMap); }
   ')' 'on' object=ID { policy.addPolicyEntry($object.getText(), perm); } ';'
  ;

usercontainer returns [UserContainer container]:
       ( 
            { 
              Set<Group> groups = new TreeSet<Group>();
              Set<UserId> users = new TreeSet<UserId>();
             }
            ( ('User'  (userName=ID { users.add(new UserId($userName.getText())); })* ';' ) |
              ('Group'  (groupName=ID { groups.add(new Group($groupName.getText())); })* ';' )
              )* 
                  { container = new UserContainerImpl(users, groups); }
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
