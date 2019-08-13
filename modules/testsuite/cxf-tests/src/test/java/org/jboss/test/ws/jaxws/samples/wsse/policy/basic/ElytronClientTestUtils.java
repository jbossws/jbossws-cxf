package org.jboss.test.ws.jaxws.samples.wsse.policy.basic;

import org.wildfly.security.auth.client.AuthenticationContext;
import org.wildfly.security.auth.client.ElytronXmlParser;
import org.wildfly.security.auth.client.InvalidAuthenticationConfigurationException;

import java.security.PrivilegedAction;

import static java.security.AccessController.doPrivileged;

/**
 * Util class for working with Elytron client configuration
 * @author dvilkola@redhat.com
 * @since 2019
 */
public class ElytronClientTestUtils {

   // When Arquillian creates a deployment, it checks server-state using wildfly-controller-client. This action sets authentication context for JVM.
   // To test different client configurations, this method sets new authentication context (parsed from given file) to the context manager.
   public static void setElytronClientConfig(String configurationFilePath) {
      try {
         System.setProperty("wildfly.config.url", configurationFilePath);
         AuthenticationContext context = doPrivileged((PrivilegedAction<AuthenticationContext>) () -> {
            try {
               return ElytronXmlParser.parseAuthenticationClientConfiguration().create();
            } catch (Throwable t) {
               throw new InvalidAuthenticationConfigurationException(t);
            }
         });

         AuthenticationContext.getContextManager().setGlobalDefault(context);
      } finally {
         System.clearProperty("wildfly.config.url");
      }
   }
}
