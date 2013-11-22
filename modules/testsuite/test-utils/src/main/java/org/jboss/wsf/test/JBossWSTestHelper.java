/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.wsf.test;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.soap.SOAPBinding;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.deployer.Deployer;

/**
 * A JBossWS test helper that deals with test deployment/undeployment, etc.
 *
 * @author Thomas.Diesler@jboss.org
 * @author ropalka@redhat.com
 * @author alessio.soldano@jboss.com
 */
public class JBossWSTestHelper
{
   private static final String SYSPROP_JBOSSWS_INTEGRATION_TARGET = "jbossws.integration.target";
   private static final String SYSPROP_JBOSS_BIND_ADDRESS = "jboss.bind.address";
   private static final String SYSPROP_TEST_ARCHIVE_DIRECTORY = "test.archive.directory";
   private static final String SYSPROP_TEST_RESOURCES_DIRECTORY = "test.resources.directory";
   private static final String SYSPROP_AS_SERVER_CONN_RETRIEVAL_ATTEMPTS = "test.as.server.connection.retrieval.attempts";
   private static final String TEST_USERNAME = "test.username";
   private static final String TEST_PASSWORD = "test.password";
   private static final boolean DEPLOY_PROCESS_ENABLED = !Boolean.getBoolean("disable.test.archive.deployment");
   private static final int AS_SERVER_CONN_RETRIEVAL_ATTEMPTS = Integer.getInteger(SYSPROP_AS_SERVER_CONN_RETRIEVAL_ATTEMPTS, 5);
   private static final String testArchiveDir = System.getProperty(SYSPROP_TEST_ARCHIVE_DIRECTORY);
   private static final String testResourcesDir = System.getProperty(SYSPROP_TEST_RESOURCES_DIRECTORY);
   private static final String integrationTarget;
   private static final String implInfo;
   
   private static volatile Deployer deployer;
   private static volatile MBeanServerConnection server;
   
   static {
      integrationTarget = System.getProperty(SYSPROP_JBOSSWS_INTEGRATION_TARGET);
      if (integrationTarget == null)
         throw new IllegalStateException("Cannot obtain system property: " + SYSPROP_JBOSSWS_INTEGRATION_TARGET);
      Object obj = getImplementationObject();
      implInfo = obj.getClass().getPackage().getName();
   }

   private static Deployer getDeployer()
   {
      //lazy loading of deployer
      if (deployer == null)
      {
         synchronized (JBossWSTestHelper.class)
         {
            if (deployer == null)
            {
               SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
               deployer = spiProvider.getSPI(Deployer.class);
            }
         }
      }
      return deployer;
   }

   /** Deploy the given archive to the server
    */
   public static void deploy(final String archive) throws Exception
   {
      if (DEPLOY_PROCESS_ENABLED)
      {
         URL archiveURL = getArchiveFile(archive).toURI().toURL();
         getDeployer().deploy(archiveURL);
      }
   }

   /** Undeploy the given archive from the server
    */
   public static void undeploy(final String archive) throws Exception
   {
      if (DEPLOY_PROCESS_ENABLED)
      {
         URL archiveURL = getArchiveFile(archive).toURI().toURL();
         getDeployer().undeploy(archiveURL);
      }
   }

   /** Deploy the given archive to the appclient.
    * Archive name is always in form archive.ear#appclient.jar
    */
   public static Process deployAppclient(final String archive, final OutputStream appclientOS, final String... appclientArgs) throws Exception
   {
      if (DEPLOY_PROCESS_ENABLED)
      {
         return AppclientHelper.deployAppclient(archive, appclientOS, appclientArgs);
      }
      return null;
   }

   /** Undeploy the given archive from the appclient
    * Archive name is always in form archive.ear#appclient.jar
    */
   public static void undeployAppclient(final String archive, boolean awaitShutdown) throws Exception
   {
      if (DEPLOY_PROCESS_ENABLED)
      {
         AppclientHelper.undeployAppclient(archive, awaitShutdown);
      }
   }

   public static boolean isTargetJBoss7()
   {
       String target = getIntegrationTarget();
       return target.startsWith("jboss7");
   }

   public static boolean isTargetJBoss71()
   {
       String target = getIntegrationTarget();
       return target.startsWith("jboss71");
   }

   public static boolean isTargetJBoss72()
   {
       String target = getIntegrationTarget();
       return target.startsWith("jboss72");
   }

   @Deprecated
   public static boolean isTargetJBoss8()
   {
      return isTargetJBoss8();
   }
   
   public static boolean isTargetWildFly8()
   {
       String target = getIntegrationTarget();
       return target.startsWith("wildfly8");
   }

   @Deprecated
   public static boolean isTargetJBoss80()
   {
       return isTargetWildFly80();
   }

   public static boolean isTargetWildFly80()
   {
       String target = getIntegrationTarget();
       return target.startsWith("wildfly80");
   }

   public static boolean isIntegrationNative()
   {
      String vendor = getImplementationInfo();
      return vendor.toLowerCase().indexOf("jboss") != -1;
   }

   public static boolean isIntegrationCXF()
   {
      String vendor = getImplementationInfo();
      return vendor.toLowerCase().indexOf("apache") != -1;
   }

   private static String getImplementationInfo()
   {
      return implInfo;
   }

   private static Object getImplementationObject()
   {
      Service service = Service.create(new QName("dummyService"));
      Object obj = service.getHandlerResolver();
      if (obj == null)
      {
         service.addPort(new QName("dummyPort"), SOAPBinding.SOAP11HTTP_BINDING, "http://dummy-address");
         obj = service.createDispatch(new QName("dummyPort"), Source.class, Mode.PAYLOAD);
      }
      return obj;
   }

   /**
    * Get the JBoss server host from system property "jboss.bind.address"
    * This defaults to "localhost"
    */
   public static String getServerHost()
   {
      final String host = System.getProperty(SYSPROP_JBOSS_BIND_ADDRESS, "localhost"); 
      return toIPv6URLFormat(host);
   }
   
   private static String toIPv6URLFormat(final String host)
   {
      try
      {
         if (host.startsWith(":"))
         {
            throw new IllegalArgumentException("JBossWS test suite requires IPv6 addresses to be wrapped with [] brackets. Expected format is: [" + host + "]");
         }
         if (host.startsWith("["))
         {
            if (System.getProperty("java.net.preferIPv4Stack") == null)
            {
               throw new IllegalStateException("always provide java.net.preferIPv4Stack JVM property when using IPv6 address format");
            }
            if (System.getProperty("java.net.preferIPv6Addresses") == null)
            {
               throw new IllegalStateException("always provide java.net.preferIPv6Addresses JVM property when using IPv6 address format");
            }
         }
         final boolean isIPv6Address = InetAddress.getByName(host) instanceof Inet6Address;
         final boolean isIPv6Formatted = isIPv6Address && host.startsWith("[");
         return isIPv6Address && !isIPv6Formatted ? "[" + host + "]" : host;
      }
      catch (final UnknownHostException e)
      {
         throw new RuntimeException(e);
      }
   }

   public static MBeanServerConnection getServer()
   {
      if (server == null)
      {
         synchronized (JBossWSTestHelper.class)
         {
            if (server == null)
            {
               String integrationTarget = getIntegrationTarget();
               if (integrationTarget.startsWith("jboss7") || integrationTarget.startsWith("wildfly8") || integrationTarget.startsWith("jboss8"))
               {
                  server = getAS7ServerConnection(integrationTarget);
               }
               else
               {
                  throw new IllegalStateException("Unsupported target container: " + integrationTarget);
               }
            }
         }
      }
      return server;
   }
   
   private static MBeanServerConnection getAS7ServerConnection(String integrationTarget)
   {
      final String urlString = System.getProperty("jmx.service.url", "service:jmx:remoting-jmx://" + getServerHost() + ":" + 9999);
      JMXServiceURL serviceURL = null;
      JMXConnector connector = null;
      try
      {
         serviceURL = new JMXServiceURL(urlString);
      }
      catch (MalformedURLException e1)
      {
         throw new IllegalStateException(e1);
      }
      //add more tries to get the connection. Workaround to fix some test failures caused by connection is not established in 5 seconds
      for (int i = 0; i < AS_SERVER_CONN_RETRIEVAL_ATTEMPTS && connector == null; i++)
      {
         try
         {
            connector = JMXConnectorFactory.connect(serviceURL, null);
         }
         catch (IOException ex)
         {
            throw new IllegalStateException("Cannot obtain MBeanServerConnection to: " + urlString, ex);
         }
         catch (RuntimeException e)
         {
            if (e.getMessage().contains("WAITING") && i < AS_SERVER_CONN_RETRIEVAL_ATTEMPTS - 1)
            {
               continue;
            }
            else
            {
               throw e;
            }
         }
      }

      try
      {
         return connector.getMBeanServerConnection();
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Cannot obtain MBeanServerConnection to: " + urlString, e);
      }
   }
   
   public static String getIntegrationTarget()
   {
      return integrationTarget;
   }

   /** Try to discover the URL for the deployment archive */
   public static URL getArchiveURL(String archive) throws MalformedURLException
   {
      return getArchiveFile(archive).toURI().toURL();
   }

   /** Try to discover the File for the deployment archive */
   public static File getArchiveFile(String archive)
   {
      File file = new File(archive);
      if (file.exists())
         return file;

      file = new File(getTestArchiveDir() + "/" + archive);
      if (file.exists())
         return file;

      String notSet = (getTestArchiveDir() == null ? " System property '" + SYSPROP_TEST_ARCHIVE_DIRECTORY + "' not set." : "");
      throw new IllegalArgumentException("Cannot obtain '" + getTestArchiveDir() + "/" + archive + "'." + notSet);
   }

   /** Try to discover the URL for the test resource */
   public static URL getResourceURL(String resource) throws MalformedURLException
   {
      return getResourceFile(resource).toURI().toURL();
   }

   /** Try to discover the File for the test resource */
   public static File getResourceFile(String resource)
   {
      File file = new File(resource);
      if (file.exists())
         return file;

      file = new File(getTestResourcesDir() + "/" + resource);
      if (file.exists())
         return file;

      String notSet = (getTestResourcesDir() == null ? " System property '" + SYSPROP_TEST_RESOURCES_DIRECTORY + "' not set." : "");
      throw new IllegalArgumentException("Cannot obtain '" + getTestResourcesDir() + "/" + resource + "'." + notSet);
   }

   public static String getTestArchiveDir()
   {
      return testArchiveDir;
   }

   public static String getTestResourcesDir()
   {
      return testResourcesDir;
   }
   
   public static String getTestUsername() {
      String prop = System.getProperty(TEST_USERNAME);
      if (prop == null || "".equals(prop) || ("${" + TEST_USERNAME + "}").equals(prop)) {
         prop = "kermit";
      }
      return prop; 
   }

   public static String getTestPassword() {
      String prop = System.getProperty(TEST_PASSWORD);
      if (prop == null || "".equals(prop) || ("${" + TEST_PASSWORD + "}").equals(prop)) {
         prop = "thefrog";
      }
      return prop; 
   }

   public static void addSecurityDomain(String name, Map<String,String> authenticationOptions) throws Exception
   {
      getDeployer().addSecurityDomain(name, authenticationOptions);
   }
   
   public static void addJaspiSecurityDomain(String name, String loginModuleStackName, Map<String, String> loginModuleOptions, String authModuleName,
         Map<String, String> authModuleOptions) throws Exception
   {
      getDeployer().addJaspiSecurityDomain(name, loginModuleStackName, loginModuleOptions, authModuleName, authModuleOptions);
   }
   
   
   public static void removeSecurityDomain(String name) throws Exception
   {
      getDeployer().removeSecurityDomain(name);
   }
   
   public static void addHttpsConnector(Map<String, String> options) throws Exception
   {
      getDeployer().addHttpsConnector(options);
   }
   
   public static void removeHttpsConnector() throws Exception
   {
      getDeployer().removeHttpsConnector();
   }

   public static abstract class BaseDeployment<T extends org.jboss.shrinkwrap.api.Archive<T>>
   {
      protected T archive;

      public BaseDeployment(Class<T> clazz, String name)
      {
         archive = ShrinkWrap.create(clazz, name);
      }

      public T create()
      {
         return archive;
      }

      public T writeToFile()
      {
         File archiveDir = assertArchiveDirExists(JBossWSTestHelper.getTestArchiveDir());
         File file = new File(archiveDir, archive.getName());
         archive.as(ZipExporter.class).exportTo(file, true);
         return archive;
      }

      private File assertArchiveDirExists(String testArchiveDir)
      {
         File archiveDir = new File(testArchiveDir);
         if (!archiveDir.exists())
         {
            if (testArchiveDir == null)
               throw new IllegalArgumentException("Cannot create archive - system property '"
                     + JBossWSTestHelper.SYSPROP_TEST_ARCHIVE_DIRECTORY + "' not set.");
            if (!archiveDir.mkdirs())
               ;
            throw new IllegalArgumentException("Cannot create archive - can not create test archive directory '"
                  + archiveDir.getAbsolutePath() + "' not set.");
         }
         return archiveDir;
      }
   }

   public static abstract class WarDeployment extends BaseDeployment<WebArchive>
   {
      public WarDeployment(String name)
      {
         super(WebArchive.class, name);
      }
   }

   public static abstract class JarDeployment extends BaseDeployment<JavaArchive>
   {
      public JarDeployment(String name)
      {
         super(JavaArchive.class, name);
      }
   }
}
