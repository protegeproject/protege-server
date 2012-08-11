grammar Authorization;

@header{
package org.protege.owl.server.policy.authorize.generated

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.protege.owl.server.policy.authorize.User
import org.protege.owl.server.policy.authorize.Group
}
@lexer::header{
package org.protege.owl.server.policy.authorize.generated
}


@members{
     private Collection<User> users = new ArrayList<User>();
     private Collection<Group> groups = new ArrayList<Group>();

     public Collection<User> getUsers() {
         return Collections.unmodifiableSet(users);
     }

     public Collection<Group> getGroups() {
         return Collections.unmodifiableSet(groups);
     }
}

top:  ( user | group ) * ;

user : 'User: ' username=ID 'Password:' password=ID; { users.add(classname.getText(), password.getText()); } ;

filedecl : 'file:' ID ';'  { 
   if (artifact.getProjectLocation() == null) {
      artifact.setProjectLocation(new File($ID.getText())); 
   } else {
      throw new IllegalStateException("location of ontology can only be set once");
   }
 };

moduledecl : 'module:' ID ';' { 
    if (module == null) {
        module = $ID.getText(); 
     } else {
         throw new IllegalStateException("plugin can only have one module");
     }
  };

tab : 'tab:' { TabBean tab = new TabBean(); }  ID ';' { 
   tab.setClassname($ID.getText());
   tabs.add(tab);
};

portlet : 'portlet:' { PortletBean portlet = new PortletBean(); } ID ';' {
    portlet.setClassname($ID.getText());
    portlets.add(portlet);
};


form : 'form:' { 
     boolean customCreatorSet = false;
     FormBean form = new FormBean(); 
  } classname=ID {form.setClassname($classname.getText());} 
  (('shortname' '=' shortname =ID {
        if (form.getShortname() == null) {
          form.setShortname($shortname.getText());
        } else {
           throw new IllegalStateException("Only one short name allowed for " + form);
        }
      }) |
   ('componentType' '=' componentType = ID { 
        if (form.getComponentType() == null) {
            form.setComponentType($componentType.getText());
         } else {
             throw new IllegalStateException("Only one component type allowed for " + form);
         }
     }) |
   ('customCreator' '=' custom = ID { 
       if (!customCreatorSet) {
           form.setCustomCreator($custom.getText()); 
           customCreatorSet = true;
       } else {
           throw new IllegalStateException("Only one custom creator allowed for " + form);
       }
   }))* ';'
   { forms.add(form); } ;
   

servlet : 'servlet:' { ServletBean servlet = new ServletBean(); }
          name = ID { servlet.setName($name.getText()); }
          classname = ID { servlet.setClassname($classname.getText()); }
          ( 'loadOnStartup' '=' loadOnStartup = NUM { 
               int i = Integer.parseInt($loadOnStartup.getText());
               servlet.setLoadOnStartup(i);
             })?
           ( url = ID { servlet.addUrl($url.getText()); } )* ';' 
           { servlets.add(servlet); } ;
          

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
