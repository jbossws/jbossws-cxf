/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.wsf.test;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.WeakHashMap;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.Service.Mode;
import jakarta.xml.ws.soap.SOAPBinding;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.apache.commons.validator.routines.InetAddressValidator;

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
   private static final String SYSPROP_JBOSS_REMOTING_PROTOCOL = "jboss.remoting.protocol";
   private static final String SYSPROP_INITIAL_CONTEXT_FACTORY = "jboss.initial.context.factory";
   private static final String SYSPROP_TEST_ARCHIVE_DIRECTORY = "test.archive.directory";
   private static final String SYSPROP_TEST_RESOURCES_DIRECTORY = "test.resources.directory";
   private static final String SYSPROP_DEFAULT_CONTAINER_QUALIFIER = "default.container.qualifier";
   private static final String SYSPROP_DEFAULT_CONTAINER_GROUP_QUALIFIER = "default.container.group.qualifier";
   private static final String SYSPROP_CONTAINER_PORT_OFFSET_PREFIX = "port-offset.";
   private static final String SYSPROP_AS_SERVER_CONN_RETRIEVAL_ATTEMPTS = "test.as.server.connection.retrieval.attempts";
   private static final String TEST_USERNAME = "test.username";
   private static final String TEST_PASSWORD = "test.password";
   private static final int AS_SERVER_CONN_RETRIEVAL_ATTEMPTS = Integer.getInteger(SYSPROP_AS_SERVER_CONN_RETRIEVAL_ATTEMPTS, 5);
   private static final String testArchiveDir = System.getProperty(SYSPROP_TEST_ARCHIVE_DIRECTORY);
   private static final String testResourcesDir = System.getProperty(SYSPROP_TEST_RESOURCES_DIRECTORY);
   //TODO: Look at remove this completely
   private static final String integrationTarget = "wildfly";
   private static final String implInfo;
   private static final String serverHost = System.getProperty(SYSPROP_JBOSS_BIND_ADDRESS, "localhost");
   private static final String remotingProtocol = System.getProperty(SYSPROP_JBOSS_REMOTING_PROTOCOL);
   private static final String initialContextFactory = System.getProperty(SYSPROP_INITIAL_CONTEXT_FACTORY);

   private static WeakHashMap<ClassLoader, Hashtable<String, String>> containerEnvs = new WeakHashMap<ClassLoader, Hashtable<String,String>>();

   static {
      Object obj = getImplementationObject();
      implInfo = obj.getClass().getPackage().getName();
   }

   /** Deploy the given archive to the appclient.
    * Archive name is always in form archive.ear#appclient.jar
    */
   public static Process deployAppclient(final String archive, final OutputStream appclientOS, final String... appclientArgs) throws Exception
   {
      return AppclientHelper.deployAppclient(archive, appclientOS, appclientArgs);
   }

   /** Undeploy the given archive from the appclient
    * Archive name is always in form archive.ear#appclient.jar
    */
   public static void undeployAppclient(final String archive, boolean awaitShutdown) throws Exception
   {
      AppclientHelper.undeployAppclient(archive, awaitShutdown);
   }

   public static boolean isTargetWildFly9()
   {
       String target = getIntegrationTarget();
       return target.startsWith("wildfly9");
   }

   public static boolean isTargetWildFly10()
   {
       String target = getIntegrationTarget();
       return target.startsWith("wildfly10");
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
   
   public static String getRemotingProtocol()
   {
      return remotingProtocol;
   }
   
   public static String getInitialContextFactory()
   {
      return initialContextFactory;
   }

   /**
    * Get the JBoss server host from system property "jboss.bind.address"
    * This defaults to "localhost"
    */
   public static String getServerHost()
   {
      return toIPv6URLFormat(serverHost);
   }
   
   public static int getServerPort()
   {
      return getServerPort(null, null);
   }
   
   public static int getServerPort(String groupQualifier, String containerQualifier)
   {
      return 8080 + getContainerPortOffset(groupQualifier, containerQualifier);
   }
   
   public static int getSecureServerPort(String groupQualifier, String containerQualifier)
   {
      return 8443 + getContainerPortOffset(groupQualifier, containerQualifier);
   }
   
   protected static int getContainerPortOffset(String groupQualifier, String containerQualifier)
   {
      Hashtable<String, String> env = getContainerEnvironment();
      
      if (groupQualifier == null) {
         groupQualifier = env.get(SYSPROP_DEFAULT_CONTAINER_GROUP_QUALIFIER);
      }
      if (containerQualifier == null) {
         containerQualifier = env.get(SYSPROP_DEFAULT_CONTAINER_QUALIFIER);
      }
      String offset = env.get(SYSPROP_CONTAINER_PORT_OFFSET_PREFIX + groupQualifier + "." + containerQualifier);
      return offset != null ? Integer.valueOf(offset) : 0;
   }
   
   private static Hashtable<String, String> getContainerEnvironment() {
      Hashtable<String, String> env;
      ClassLoader tccl = Thread.currentThread().getContextClassLoader();
      synchronized (containerEnvs)
      {
         env = containerEnvs.get(tccl);
         if (env == null) {
            env = new Hashtable<String, String>();
            final InputStream is = tccl.getResourceAsStream("container.properties");
            try {
               if (is != null) {
                   final Properties props = new Properties();
                   props.load(is);
                   Entry<Object, Object> entry;
                   final Iterator<Entry<Object, Object>> entries = props.entrySet().iterator();
                   while (entries.hasNext()) {
                       entry = entries.next();
                       env.put((String)entry.getKey(), (String)entry.getValue());
                   }
               }
            } catch (IOException e) {
               throw new RuntimeException(e);
            }
            containerEnvs.put(tccl, env);
         }
         return env;
      }
  }
   
   public static String toIPv6URLFormat(final String host)
   {
      try
      {
         if (host.startsWith("[") || host.startsWith(":"))
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
         final boolean isIPv6Literal = isIPv6Address && InetAddressValidator.getInstance().isValidInet6Address(host.replaceAll("^\\[(.*)\\]$","$1"));
         final boolean isIPv6LiteralFormattedForURI = isIPv6Literal && host.startsWith("[");
         return isIPv6Literal && !isIPv6LiteralFormattedForURI ? "[" + host + "]" : host;
      }
      catch (final UnknownHostException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   public static MBeanServerConnection getServer()
   {
      return getServer(null, null);
   }

   public static MBeanServerConnection getServer(String groupQualifier, String containerQualifier)
   {
      int portOffset = getContainerPortOffset(groupQualifier, containerQualifier);
      MBeanServerConnection server;
      server = getServerConnection("service:jmx:http-remoting-jmx://" + getServerHost() + ":" + (9990 + portOffset));
      return server;
   }

   private static MBeanServerConnection getServerConnection(String jmxServiceUrl)
   {
      final String urlString = System.getProperty("jmx.service.url", jmxServiceUrl);
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

   @SuppressWarnings("rawtypes")
   public static void writeToFile(Archive archive)
   {
      File archiveDir = assertArchiveDirExists();
      File file = new File(archiveDir, archive.getName());
      archive.as(ZipExporter.class).exportTo(file, true);
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
         File archiveDir = assertArchiveDirExists();
         File file = new File(archiveDir, archive.getName());
         archive.as(ZipExporter.class).exportTo(file, true);
         return archive;
      }
      
      public String getName()
      {
         return archive.getName();
      }
   }
   
   public static File assertArchiveDirExists()
   {
      File archiveDir = new File(testArchiveDir);
      if (!archiveDir.exists())
      {
         if (testArchiveDir == null)
            throw new IllegalArgumentException("Cannot create archive - system property '"
                  + JBossWSTestHelper.SYSPROP_TEST_ARCHIVE_DIRECTORY + "' not set.");
         if (!archiveDir.mkdirs() && !archiveDir.exists())
            throw new IllegalArgumentException("Cannot create archive - can not create test archive directory '"
               + archiveDir.getAbsolutePath() + "'");
      }
      return archiveDir;
   }
   
   public static String writeToFile(BaseDeployment<?>... deps) {
      if (deps == null) {
         return "";
      }
      StringBuilder sb = new StringBuilder();
      for (BaseDeployment<?> dep : deps) {
         sb.append(dep.writeToFile().getName());
         sb.append(" ");
      }
      return sb.toString().trim();
   }

   public static abstract class JarDeployment extends BaseDeployment<JavaArchive>
   {
      public JarDeployment(String name)
      {
         super(JavaArchive.class, name);
      }
   }
   public static void addLibrary(File dir, WebArchive archive)
   {
      archive.addAsLibraries(dir.listFiles(new FilenameFilter() {

         @Override
         public boolean accept(File dir, String name)
         {
            if (name.endsWith(".jar"))
            {
               return true;
            }
            return false;
         }
      }));
   }
}
