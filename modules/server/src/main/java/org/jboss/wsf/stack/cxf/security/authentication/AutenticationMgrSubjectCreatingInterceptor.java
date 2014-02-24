package org.jboss.wsf.stack.cxf.security.authentication;

import java.security.Principal;

import javax.security.auth.Subject;

import org.apache.cxf.common.security.SecurityToken;
import org.apache.cxf.common.security.TokenType;
import org.apache.cxf.common.security.UsernameToken;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.security.SecurityContext;
import org.apache.ws.security.WSUsernameTokenPrincipal;
import org.jboss.security.plugins.JBossAuthenticationManager;
import org.jboss.wsf.stack.cxf.Loggers;
import org.jboss.wsf.stack.cxf.Messages;

/* 
 * Interceptor to authenticate principal with provided jaspi JBossAuthenticationManager
 * @author <a href="mailto:ema@redhat.com">Jim Ma</a>
 */
public class AutenticationMgrSubjectCreatingInterceptor extends SubjectCreatingPolicyInterceptor
{
   
   public AutenticationMgrSubjectCreatingInterceptor() {
      super();        
   }
  

   @Override
   public void handleMessage(Message message) throws Fault
   {
	  JBossAuthenticationManager authenticationManger = message.get(JBossAuthenticationManager.class);
	  if (authenticationManger == null) {
		  return;
	  }
      SecurityContext context = message.get(SecurityContext.class);
      if (context == null || context.getUserPrincipal() == null)
      {
         Loggers.SECURITY_LOGGER.userPrincipalNotAvailableOnCurrentMessage();
         return;
      }

      SecurityToken token = message.get(SecurityToken.class);
      Subject subject = null;
      if (token != null)
      {
         //Try authenticating using SecurityToken info
         if (token.getTokenType() != TokenType.UsernameToken)
         {
            throw Messages.MESSAGES.unsupportedTokenType(token.getTokenType());
         }
         UsernameToken ut = (UsernameToken) token;
         subject = helper.createSubject(authenticationManger, ut.getName(), ut.getPassword(), ut.isHashed(), ut.getNonce(), ut.getCreatedTime());

      }
      else
      {
         //Try authenticating using WSS4J internal info (previously set into SecurityContext by WSS4JInInterceptor)
         Principal p = context.getUserPrincipal();
         if (!(p instanceof WSUsernameTokenPrincipal)) {
            throw Messages.MESSAGES.couldNotGetSubjectInfo();
         }
         WSUsernameTokenPrincipal up = (WSUsernameTokenPrincipal) p;
         subject = helper.createSubject(authenticationManger, up.getName(), up.getPassword(), up.isPasswordDigest(), up.getNonce(), up.getCreatedTime());
      }

      Principal principal = getPrincipal(context.getUserPrincipal(), subject);
      message.put(SecurityContext.class, createSecurityContext(principal, subject));
   }

  
}
