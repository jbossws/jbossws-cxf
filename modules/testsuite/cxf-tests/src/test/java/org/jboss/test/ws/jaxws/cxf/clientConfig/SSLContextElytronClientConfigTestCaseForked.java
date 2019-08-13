package org.jboss.test.ws.jaxws.cxf.clientConfig;

import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.ws.jaxws.cxf.httpauth.Hello;
import org.jboss.test.ws.jaxws.samples.wsse.policy.basic.ElytronClientTestUtils;
import org.jboss.wsf.stack.cxf.client.configuration.CXFClientConfigurer;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.security.auth.client.AuthenticationContext;
import sun.security.ssl.SSLContextImpl;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import java.io.File;
import java.net.URL;

/**
 * Class testing functionality of WS client when Elytron is on classpath and SSLContext from Elytron client configuration is used
 *
 * @author dvilkola@redhat.com
 * @since August-2019
 */
@RunWith(Arquillian.class)
public class SSLContextElytronClientConfigTestCaseForked extends JBossWSTest {

   private static final String DEPLOYMENT = "sslMutualAuthContextTestCase";
   private static final String SSL_MUTUAL_AUTH_SERVER = "ssl-mutual-auth";
   private final String serviceURLHttps = "https://" + getServerHost() + ":" + (getServerPort(CXF_TESTS_GROUP_QUALIFIER, SSL_MUTUAL_AUTH_SERVER) + 363) + "/ssl-mutual-auth/";

   @ArquillianResource
   private Deployer deployer;

   @ArquillianResource
   private ContainerController containerController;


   @Deployment(name = DEPLOYMENT, testable = false, managed = false)
   @TargetsContainer(SSL_MUTUAL_AUTH_SERVER)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "ssl-mutual-auth.war");
      archive.addClass(org.jboss.test.ws.jaxws.cxf.httpauth.Hello.class)
              .addClass(org.jboss.test.ws.jaxws.cxf.httpauth.HelloImpl.class)
              .addClass(org.jboss.test.ws.jaxws.cxf.httpauth.HelloRequest.class)
              .addClass(org.jboss.test.ws.jaxws.cxf.httpauth.HelloResponse.class)
              .addClass(org.jboss.test.ws.jaxws.cxf.httpauth.ObjectFactory.class)
              .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/httpauth/WEB-INF/wsdl/helloMutualSSLport.wsdl"), "wsdl/hello.wsdl");
      return archive;
   }

   @Before
   public void startContainerAndDeploy() throws Exception {
      if (!containerController.isStarted(SSL_MUTUAL_AUTH_SERVER)) {
         containerController.start(SSL_MUTUAL_AUTH_SERVER);
         try {
            deployer.deploy(DEPLOYMENT);
         } catch (Exception e) {
            System.err.println(e);
         }
      }
   }

   @Test
   @RunAsClient
   public void testConfiguredSSLContext() throws Exception {
      AuthenticationContext previousAuthContext = AuthenticationContext.getContextManager().getGlobalDefault();
      SSLContext previousDefaultSSLContext = SSLContext.getDefault();
      try {
         ElytronClientTestUtils.setElytronClientConfig(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/clientConfig/META-INF/wildfly-config-ssl-context.xml");
         // for this test we must remove default SSL context because it accepts existing certificate without additional configuration
         SSLContext emptySSLContext = new SSLContext(new SSLContextImpl.TLS10Context(), null, null) {};
         emptySSLContext.init(null, new TrustManager[] { null }, null);
         SSLContext.setDefault(emptySSLContext);
         QName serviceName = new QName("http://jboss.org/http/security", "HelloService");
         URL wsdlURL = getResourceURL("jaxws/cxf/httpauth/WEB-INF/wsdl/hello.wsdl");
         Service service = Service.create(wsdlURL, serviceName);
         Hello proxy = service.getPort(Hello.class);
         ((BindingProvider) proxy).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, serviceURLHttps + "HelloService");
         CXFClientConfigurer cxfClientConfigurer = new CXFClientConfigurer();
         cxfClientConfigurer.setConfigProperties(proxy, null, null);
         assertEquals(100, proxy.helloRequest("number"));
      } finally {
         AuthenticationContext.getContextManager().setGlobalDefault(previousAuthContext);
         SSLContext.setDefault(previousDefaultSSLContext);
      }
   }

   @Test
   @RunAsClient
   public void testNotConfiguredSSLContext() throws Exception {
      AuthenticationContext previousAuthContext = AuthenticationContext.getContextManager().getGlobalDefault();
      SSLContext previousDefaultSSLContext = SSLContext.getDefault();
      try {
         ElytronClientTestUtils.setElytronClientConfig(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/clientConfig/META-INF/wildfly-config-default-auth.xml"); // file does not contain SSLContext configuration
         // for this test we must remove default SSL context because it accepts existing certificate without additional configuration
         SSLContext emptySSLContext = new SSLContext(new SSLContextImpl.TLS10Context(), null, null) {};
         emptySSLContext.init(null, new TrustManager[] { null }, null);
         SSLContext.setDefault(emptySSLContext);
         QName serviceName = new QName("http://jboss.org/http/security", "HelloService");
         URL wsdlURL = getResourceURL("jaxws/cxf/httpauth/WEB-INF/wsdl/hello.wsdl");
         Service service = Service.create(wsdlURL, serviceName);
         Hello proxy = service.getPort(Hello.class);
         ((BindingProvider) proxy).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, serviceURLHttps + "HelloService");
         CXFClientConfigurer cxfClientConfigurer = new CXFClientConfigurer();
         cxfClientConfigurer.setConfigProperties(proxy, null, null);
         try {
            assertEquals(100, proxy.helloRequest("number"));
            fail("SSL handshake should fail.");
         } catch (Exception e) {
            assertTrue(e.getCause().getMessage().contains("SSLHandshakeException"));
         }
      } finally {
         AuthenticationContext.getContextManager().setGlobalDefault(previousAuthContext);
         SSLContext.setDefault(previousDefaultSSLContext);
      }
   }
}
