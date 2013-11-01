package org.jboss.wsf.stack.cxf.jaspi;

import java.util.Properties;

import javax.security.auth.Subject;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.config.ClientAuthConfig;
import javax.security.auth.message.config.ClientAuthContext;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;

import org.apache.cxf.binding.soap.Soap12;
import org.apache.cxf.binding.soap.SoapBinding;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.message.Message;
import org.jboss.security.auth.login.JASPIAuthenticationInfo;
import org.jboss.security.auth.message.GenericMessageInfo;
/** 
 * @author <a href="ema@redhat.com">Jim Ma</a>
 */
public class JaspiClientAuthenticator
{
   private ClientAuthConfig clientConfig;
   private String securityDomain;
   private JASPIAuthenticationInfo jpi;

   public JaspiClientAuthenticator(ClientAuthConfig clientConfig, String securityDomain, JASPIAuthenticationInfo jpi)
   {

      this.clientConfig = clientConfig;
      this.securityDomain = securityDomain;
      this.jpi = jpi;
   }

   public void secureRequest(SoapMessage message)
   {
      SOAPMessage soapMessage = message.getContent(SOAPMessage.class);
      MessageInfo messageInfo = new GenericMessageInfo(soapMessage, null);
      String authContextID = clientConfig.getAuthContextID(messageInfo);

      Properties serverContextProperties = new Properties();
      serverContextProperties.put("security-domain", securityDomain);
      serverContextProperties.put("jaspi-policy", jpi);
      Subject clientSubject = new Subject();
      AuthStatus authStatus = null;
      try
      {
         ClientAuthContext cctx = clientConfig.getAuthContext(authContextID, clientSubject, serverContextProperties);
         authStatus = cctx.secureRequest(messageInfo, clientSubject);
      }
      catch (AuthException e)
      {
         if (isSOAP12(message))
         {
            SoapFault soap12Fault = new SoapFault(e.getMessage(), Soap12.getInstance().getSender());
            throw soap12Fault;
         }
         else
         {
            throw new SoapFault(e.getMessage(), new QName("", "japsi AuthException"));
         }
      }
     //TODO:look at how to handle AuthStatus

   }

   public void validateResponse(SoapMessage message)
   {
      SOAPMessage request = message.getExchange().getInMessage().get(SOAPMessage.class);
      SOAPMessage response = message.getContent(SOAPMessage.class);
      MessageInfo messageInfo = new GenericMessageInfo(request, response);
      String authContextID = clientConfig.getAuthContextID(messageInfo);

      Properties serverContextProperties = new Properties();
      serverContextProperties.put("security-domain", securityDomain);
      serverContextProperties.put("jaspi-policy", jpi);
      Subject clientSubject = new Subject();
      AuthStatus authStatus = null;
      try
      {
         ClientAuthContext sctx = clientConfig.getAuthContext(authContextID, clientSubject, serverContextProperties);
         authStatus = sctx.validateResponse(messageInfo, new Subject(), new Subject());
      }
      catch (AuthException e)
      {
         if (isSOAP12(message))
         {
            SoapFault soap12Fault = new SoapFault(e.getMessage(), Soap12.getInstance().getSender());
            throw soap12Fault;
         }
         else
         {
            throw new SoapFault(e.getMessage(), new QName("", "japsi AuthException"));
         }
      }
      //TODO:handle AuthStatus

   }

   private boolean isSOAP12(Message message)
   {
      if (message.getExchange().getBinding() instanceof SoapBinding)
      {
         SoapBinding binding = (SoapBinding)message.getExchange().getBinding();
         if (binding.getSoapVersion() == Soap12.getInstance())
         {
            return true;
         }
      }
      return false;
   }

}

