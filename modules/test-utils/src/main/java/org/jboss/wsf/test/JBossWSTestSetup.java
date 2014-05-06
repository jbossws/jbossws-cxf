/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.management.MBeanServerConnection;
import javax.naming.NamingException;
import javax.security.sasl.SaslException;

import junit.extensions.TestSetup;
import junit.framework.Protectable;
import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.jboss.logging.Logger;

/**
 * A test setup that deploys/undeploys archives
 *
 * @author Thomas.Diesler@jboss.org
 * @author alessio.soldano@jboss.com
 * @since 14-Oct-2004
 */
public class JBossWSTestSetup extends TestSetup
{
   // provide logging
   private static Logger log = Logger.getLogger(JBossWSTestSetup.class);
   
   private static final String JBOSSWS_SEC_DOMAIN = "JBossWS";
   private static final String SYSPROP_HTTPS_CONNECTION_REUSE_TIMEOUT = "test.https.connection.reuse.timeout";
   private static final int HTTPS_CONNECTION_REUSE_TIMEOUT = Integer.getInteger(SYSPROP_HTTPS_CONNECTION_REUSE_TIMEOUT, 5000);

   private String[] archives = new String[0];
   private OutputStream appclientOutputStream;
   private String appclientArg;
   private ClassLoader originalClassLoader;
   private Map<String, Map<String, String>> securityDomains;
   private boolean defaultSecurityDomainRequirement = false;
   private Map<String, String> httpsConnOptions;
   private CleanupOperation cleanupOp;
   
   private static volatile long lastHttpsConnectorRemoval = 0;

   public JBossWSTestSetup(Class<?> testClass, String archiveList)
   {
      super(new TestSuite(testClass));
      getArchiveArray(archiveList);
   }
   
   public JBossWSTestSetup(Class<?> testClass, String archiveList, CleanupOperation cleanupOp)
   {
      this(testClass, archiveList);
      this.cleanupOp = cleanupOp;
   }
   
   public JBossWSTestSetup(Class<?> testClass, String archiveList, OutputStream appclientOutputStream)
   {
      super(new TestSuite(testClass));
      getArchiveArray(archiveList);
      this.appclientOutputStream = appclientOutputStream;
   }
   
   public JBossWSTestSetup(Class<?> testClass, String archiveList, boolean requiresDefaultSecurityDomain)
   {
      this(testClass, archiveList);
      setDefaultSecurityDomainRequirement(requiresDefaultSecurityDomain);
   }

   public JBossWSTestSetup(Class<?> testClass, String archiveList, boolean requiresDefaultSecurityDomain, CleanupOperation cleanupOp)
   {
      this(testClass, archiveList, requiresDefaultSecurityDomain);
      this.cleanupOp = cleanupOp;
   }

   public JBossWSTestSetup(Test test, String archiveList)
   {
      super(test);
      getArchiveArray(archiveList);
   }
   
   public JBossWSTestSetup(Test test, String archiveList, CleanupOperation cleanupOp)
   {
      this(test, archiveList);
      this.cleanupOp = cleanupOp;
   }
   
   public JBossWSTestSetup(Test test, String archiveList, boolean requiresDefaultSecurityDomain)
   {
      this(test, archiveList);
      setDefaultSecurityDomainRequirement(requiresDefaultSecurityDomain);
   }

   public JBossWSTestSetup(Test test)
   {
      super(test);
   }
   
   /**
    * Override junit.extensions.TestSetup:run(TestResult result) to call cleanup operation
    * before tearing down the whole test setup. Required for allowing tests to perform
    * final cleanup of static references.
    */
   @Override
   public void run(final TestResult result)
   {
      Protectable p = new Protectable()
      {
         public void protect() throws Exception
         {
            setUp();
            basicRun(result);
            if (cleanupOp != null) {
               cleanupOp.cleanUp();
            }
            tearDown();
         }
      };
      result.runProtected(this, p);
   }

   public File getArchiveFile(String archive)
   {
      return JBossWSTestHelper.getArchiveFile(archive);
   }

   public URL getArchiveURL(String archive) throws MalformedURLException
   {
      return JBossWSTestHelper.getArchiveFile(archive).toURI().toURL();
   }

   public File getResourceFile(String resource)
   {
      return JBossWSTestHelper.getResourceFile(resource);
   }

   public URL getResourceURL(String resource) throws MalformedURLException
   {
      return JBossWSTestHelper.getResourceFile(resource).toURI().toURL();
   }

   private void getArchiveArray(String archiveList)
   {
      if (archiveList != null)
      {
         StringTokenizer st = new StringTokenizer(archiveList, ", ");
         archives = new String[st.countTokens()];

         for (int i = 0; i < archives.length; i++)
            archives[i] = st.nextToken();
      }
   }

   protected void setUp() throws Exception
   {
      // verify integration target
      String integrationTarget = JBossWSTestHelper.getIntegrationTarget();
      log.debug("Integration target: " + integrationTarget);
      
      if (securityDomains != null && !securityDomains.isEmpty())
      {
         for (String key : securityDomains.keySet())
         {
            JBossWSTestHelper.addSecurityDomain(key, securityDomains.get(key));
         }
      }
      if (defaultSecurityDomainRequirement)
      {
         String usersPropFile = System.getProperty("org.jboss.ws.testsuite.securityDomain.users.propfile");
         String rolesPropFile = System.getProperty("org.jboss.ws.testsuite.securityDomain.roles.propfile");
         Map<String, String> authenticationOptions = new HashMap<String, String>();
         if (usersPropFile != null) {
             authenticationOptions.put("usersProperties", usersPropFile);
         }
         if (rolesPropFile != null) {
             authenticationOptions.put("rolesProperties", rolesPropFile);
         }
         authenticationOptions.put("unauthenticatedIdentity", "anonymous");
         try {
            JBossWSTestHelper.addSecurityDomain(JBOSSWS_SEC_DOMAIN, authenticationOptions);
         } catch (Exception e) {
            //be lenient here, the default jbossws security domain might be there because of
            //a previously prematurely interrupted testsuite run, so go ahead with that, it
            //will removed and re-installed at next test requiring it in any case
            e.printStackTrace();
            log.warn("Could not add default security domain!", e);
         }
      }
      if (httpsConnOptions != null) {
         JBossWSTestHelper.addHttpsConnector(httpsConnOptions);
      }

      List<URL> clientJars = new ArrayList<URL>();
      for (int i = 0; i < archives.length; i++)
      {
         String archive = archives[i];
         if (archive.endsWith("-appclient.jar"))
         {
            URL archiveURL = getArchiveURL(archive.substring(archive.indexOf('#') + 1));
            clientJars.add(archiveURL);
            JBossWSTestHelper.deployAppclient(archive, appclientOutputStream, appclientArg);
         }
         else if (archive.endsWith("-client.jar"))
         {
            URL archiveURL = getArchiveURL(archive);
            clientJars.add(archiveURL);
         }
         else
         {
            performDeploy(archive);
         }
      }

      ClassLoader parent = Thread.currentThread().getContextClassLoader();
      originalClassLoader = parent;
      // add client jars to the class loader
      if (!clientJars.isEmpty())
      {
         URL[] urls = new URL[clientJars.size()];
         for (int i = 0; i < clientJars.size(); i++)
         {
            urls[i] = clientJars.get(i);
         }
         URLClassLoader cl = new URLClassLoader(urls, parent);
         Thread.currentThread().setContextClassLoader(cl);
      }
      if (httpsConnOptions != null) {
         final long lr = lastHttpsConnectorRemoval;
         if (lr != 0) {
            final long wait = HTTPS_CONNECTION_REUSE_TIMEOUT - (System.currentTimeMillis() - lr);
            if (wait > 0) {
               log.info("Will sleep for " + wait + " ms...");
               Thread.sleep(wait);
               log.debug("Going on!");
            }
         }
      }
   }
   
   private static void performDeploy(String archive) throws Exception
   {
      try
      {
         JBossWSTestHelper.deploy(archive);
      }
      catch (Throwable ex)
      {
         ex.printStackTrace();
         Throwable cause = ex.getCause();
         boolean foundSecurityCause = false;
         while (!foundSecurityCause && cause != null && cause != ex) {
            foundSecurityCause = cause instanceof SaslException;
            ex = cause;
            cause = cause.getCause();
         }
         if (foundSecurityCause) {
            System.out.println("\n** Please make sure you properly setup authentication credentials to the application server management console **\n\n" +
            		"In order for running the JBossWS testsuite against a protected application server (username/password different from 'admin' / 'admin'),\n" +
            		"use '-Djbossws.deployer.authentication.username=username -Djbossws.deployer.authentication.password=password'\n");
         }
         JBossWSTestHelper.undeploy(archive);
      }
   }

   protected void tearDown() throws Exception
   {
      try
      {
         for (int i = 0; i < archives.length; i++)
         {
            String archive = archives[archives.length - i - 1];
            if (archive.endsWith("-appclient.jar"))
            {
                JBossWSTestHelper.undeployAppclient(archive, true);
            }
            else if (!archive.endsWith("-client.jar"))
            {
                JBossWSTestHelper.undeploy(archive);
            }
         }
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(originalClassLoader);
         
         if (securityDomains != null && !securityDomains.isEmpty())
         {
            for (String key : securityDomains.keySet())
            {
               JBossWSTestHelper.removeSecurityDomain(key);
            }
         }
         if (defaultSecurityDomainRequirement)
         {
            JBossWSTestHelper.removeSecurityDomain(JBOSSWS_SEC_DOMAIN);
         }
         if (httpsConnOptions != null)
         {
            JBossWSTestHelper.removeHttpsConnector();
            lastHttpsConnectorRemoval = System.currentTimeMillis();
         }
      }
   }
   
   protected ClassLoader getOriginalClassLoader()
   {
      return originalClassLoader;
   }

   public MBeanServerConnection getServer() throws NamingException
   {
      return JBossWSTestHelper.getServer();
   }
   
   public void addSecurityDomainRequirement(String securityDomainName, Map<String, String> authenticationOptions)
   {
      if (securityDomains == null) {
         securityDomains = new HashMap<String, Map<String,String>>();
      }
      securityDomains.put(securityDomainName, authenticationOptions);
   }
   
   public void setDefaultSecurityDomainRequirement(boolean defaultSecurityDomainRequirement)
   {
      this.defaultSecurityDomainRequirement = defaultSecurityDomainRequirement;
   }
   
   public void setHttpsConnectorRequirement(Map<String, String> options) {
      httpsConnOptions = options;
   }
}
