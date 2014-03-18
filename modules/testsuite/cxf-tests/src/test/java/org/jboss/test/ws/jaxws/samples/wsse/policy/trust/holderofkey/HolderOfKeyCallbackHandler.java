package org.jboss.test.ws.jaxws.samples.wsse.policy.trust.holderofkey;

import org.jboss.wsf.stack.cxf.extensions.security.PasswordCallbackHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * User: rsearls
 * Date: 3/14/14
 */
public class HolderOfKeyCallbackHandler extends PasswordCallbackHandler {

   public HolderOfKeyCallbackHandler()
   {
      super(getInitMap());
   }

   private static Map<String, String> getInitMap()
   {
      Map<String, String> passwords = new HashMap<String, String>();
      passwords.put("myservicekey", "skpass");
      passwords.put("alice", "clarinet");
      passwords.put("mystskey", "stskpass");
      passwords.put("myclientkey", "ckpass");
      return passwords;
   }
}

