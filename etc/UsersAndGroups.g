grammar UsersAndGroups;

@header{
package org.protege.owl.server.policy.generated;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.protege.owl.server.api.User;

import org.protege.owl.server.policy.Group;
import org.protege.owl.server.policy.UserImpl;
}

@lexer::header{
package org.protege.owl.server.policy.generated;
}


@members{
     private Map<String, User> userMap = new HashMap<String, User>();
     private Map<User, Collection<Group>> userToGroups = new HashMap<User, Collection<Group>>();
     private Map<String, Group> groupMap = new TreeMap<String, Group>();

	 public Collection<User> getUsers() {
	     return Collections.unmodifiableCollection(userMap.values());
	 }
	 
	 public Collection<Group> getGroups() {
	     return Collections.unmodifiableCollection(groupMap.values());
	 }
	 
	 public Collection<Group> getGroups(User u) {
	      return Collections.unmodifiableCollection(userToGroups.get(u));
	 }
}

top:  ( user ) * ;

user : 'User:' username=ID 'Password:' password=ID { 
           UserImpl u = new UserImpl($username.getText(), $password.getText());
           Collection<Group> userGroups = new TreeSet<Group>();
           userMap.put($username.getText(), u);
           userToGroups.put(u, userGroups);
       } 
       'Groups:'
          ( group[ u, userGroups ] ) *
       ;
       
group[UserImpl user, Collection<Group> userGroups ]: groupToken = ID {
          String groupName = $groupToken.getText();
          Group group = new Group(groupName);
          groupMap.put(groupName, group);
          userGroups.add(group);
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
