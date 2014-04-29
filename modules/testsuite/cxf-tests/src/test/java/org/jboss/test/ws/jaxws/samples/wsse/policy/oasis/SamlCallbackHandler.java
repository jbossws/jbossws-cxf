package org.jboss.test.ws.jaxws.samples.wsse.policy.oasis;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Properties;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.wss4j.common.crypto.Crypto;
import org.apache.wss4j.common.crypto.CryptoFactory;
import org.apache.wss4j.common.crypto.CryptoType;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.wss4j.common.saml.SAMLCallback;
import org.apache.wss4j.common.saml.bean.AttributeBean;
import org.apache.wss4j.common.saml.bean.AttributeStatementBean;
import org.apache.wss4j.common.saml.bean.KeyInfoBean;
import org.apache.wss4j.common.saml.bean.KeyInfoBean.CERT_IDENTIFIER;
import org.apache.wss4j.common.saml.bean.SubjectBean;
import org.apache.wss4j.common.saml.builder.SAML1Constants;
import org.apache.wss4j.common.saml.builder.SAML2Constants;
import org.opensaml.common.SAMLVersion;

public class SamlCallbackHandler implements CallbackHandler
{
   private String confirmationMethod = SAML2Constants.CONF_BEARER;

   private boolean saml2;
   
   private boolean signed;

   public SamlCallbackHandler()
   {
   }

   public void setConfirmationMethod(String confirmationMethod)
   {
      this.confirmationMethod = confirmationMethod;
   }

   public void setSaml2(boolean isSaml2)
   {
      saml2 = isSaml2;
   }

   public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException
   {
      for (int i = 0; i < callbacks.length; i++)
      {
         if (callbacks[i] instanceof SAMLCallback)
         {
            SAMLCallback callback = (SAMLCallback) callbacks[i];
            if (saml2)
            {
               callback.setSamlVersion(SAMLVersion.VERSION_20);
            }
            callback.setIssuer("sts");
            String subjectName = "uid=sts-client,o=jbws-cxf-sts.com";
            String subjectQualifier = "www.jbws-cxf-sts.org";

            SubjectBean subjectBean = new SubjectBean(subjectName, subjectQualifier, confirmationMethod);
            if (SAML2Constants.CONF_HOLDER_KEY.equals(confirmationMethod)
                  || SAML1Constants.CONF_HOLDER_KEY.equals(confirmationMethod))
            {
               try
               {
                  KeyInfoBean keyInfo = createKeyInfo();
                  subjectBean.setKeyInfo(keyInfo);
               }
               catch (Exception ex)
               {
                  throw new IOException("Problem creating KeyInfo: " + ex.getMessage());
               }
            }

            callback.setSubject(subjectBean);

            AttributeStatementBean attrBean = new AttributeStatementBean();
            attrBean.setSubject(subjectBean);

            AttributeBean attributeBean = new AttributeBean();
            if (saml2)
            {
               attributeBean.setQualifiedName("subject-role");
            }
            else
            {
               attributeBean.setSimpleName("subject-role");
               attributeBean.setQualifiedName("http://custom-ns");
            }
            
            attributeBean.addAttributeValue("system-user");
            attrBean.setSamlAttributes(Collections.singletonList(attributeBean));
            callback.setAttributeStatementData(Collections.singletonList(attrBean));
            
            try {
                String file = "META-INF/alice.properties";
                Crypto crypto = CryptoFactory.getInstance(file);
                callback.setIssuerCrypto(crypto);
                callback.setIssuerKeyName("alice");
                callback.setIssuerKeyPassword("password");
                callback.setSignAssertion(signed);
            } catch (WSSecurityException e) {
                throw new IOException(e);
            }
         }
      }
   }

   protected KeyInfoBean createKeyInfo() throws Exception
   {
      InputStream is = Thread.currentThread().getContextClassLoader().getResource("META-INF/alice.properties").openStream();
      Properties props = new Properties();
      try
      {
         props.load(is);
      }
      finally
      {
         is.close();
      }
      Crypto crypto = CryptoFactory.getInstance(props);
      CryptoType cryptoType = new CryptoType(CryptoType.TYPE.ALIAS);
      cryptoType.setAlias("alice");
      X509Certificate[] certs = crypto.getX509Certificates(cryptoType);

      KeyInfoBean keyInfo = new KeyInfoBean();
      keyInfo.setCertificate(certs[0]);
      keyInfo.setCertIdentifer(CERT_IDENTIFIER.X509_CERT);

      return keyInfo;
   }

   public boolean isSigned()
   {
      return signed;
   }

   public void setSigned(boolean signed)
   {
      this.signed = signed;
   }
}
