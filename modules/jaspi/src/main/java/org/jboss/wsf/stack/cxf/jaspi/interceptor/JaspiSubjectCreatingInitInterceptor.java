package org.jboss.wsf.stack.cxf.jaspi.interceptor;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.jboss.security.auth.callback.JBossCallbackHandler;
import org.jboss.security.plugins.JBossAuthenticationManager;

/* 
 * CXF interceptor to set jaspi JBossAuthenticationManager in message
 * @author <a href="mailto:ema@redhat.com">Jim Ma</a>
 */
public class JaspiSubjectCreatingInitInterceptor extends AbstractPhaseInterceptor<Message>
{
   private final JBossAuthenticationManager authenticationManger;
   
   public JaspiSubjectCreatingInitInterceptor(String securityDomain) {
	  super(Phase.PRE_INVOKE);
	  this.addBefore("org.jboss.wsf.stack.cxf.security.authentication.JaspiSubjectCreatingInterceptor");
      authenticationManger = new JBossAuthenticationManager(securityDomain, new JBossCallbackHandler());    
   }
  

   @Override
   public void handleMessage(Message message) throws Fault
   {
	  message.put(JBossAuthenticationManager.class, authenticationManger);
   }

  
}
