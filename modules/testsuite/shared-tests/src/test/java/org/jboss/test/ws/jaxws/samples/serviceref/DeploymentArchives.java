/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.samples.serviceref;

import java.io.File;

import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.wsf.test.JBossWSTestHelper;

public final class DeploymentArchives
{
   private final String server;
   
   private final String appclient;
   
   private final String appclientEar;
   
   private final String ejbClient;
   
   private final String servletClient;
   
   
   private static final DeploymentArchives me = new DeploymentArchives();
   
   public static String getServerArchiveFilename() {
      return me.server;
   }
   
   public static String getAppclientJarArchiveFilename() {
      return me.appclient;
   }
   
   public static String getAppclientEarArchiveFilename() {
      return me.appclientEar;
   }
   
   public static String getEJBClientArchiveFilename() {
      return me.ejbClient;
   }
   
   public static String getServletClientArchiveFilename() {
      return me.servletClient;
   }
   
   private DeploymentArchives()
   {
      server = JBossWSTestHelper.writeToFile(new JBossWSTestHelper.WarDeployment("jaxws-samples-serviceref.war")
      {
         {
            archive
                  .addManifest()
                  .addClass(org.jboss.test.ws.jaxws.samples.serviceref.Endpoint.class)
                  .addClass(org.jboss.test.ws.jaxws.samples.serviceref.EndpointImpl.class)
                  .addClass(org.jboss.test.ws.jaxws.samples.serviceref.EndpointService.class)
                  .setWebXML(
                        new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/serviceref/WEB-INF/web.xml"));
         }
      });
      appclient = JBossWSTestHelper.writeToFile(new JBossWSTestHelper.JarDeployment("jaxws-samples-serviceref-appclient.jar")
      {
         {
            archive
                  .setManifest(
                        new StringAsset("Manifest-Version: 1.0\n"
                              + "main-class: org.jboss.test.ws.jaxws.samples.serviceref.ApplicationClient\n"))
                  .addClass(org.jboss.test.ws.jaxws.samples.serviceref.ApplicationClient.class)
                  .addClass(org.jboss.test.ws.jaxws.samples.serviceref.Endpoint.class)
                  .addClass(org.jboss.test.ws.jaxws.samples.serviceref.EndpointService.class)
                  .addAsManifestResource(
                        new File(JBossWSTestHelper.getTestResourcesDir()
                              + "/jaxws/samples/serviceref/META-INF/application-client.xml"), "application-client.xml")
                  .addAsManifestResource(
                        new File(JBossWSTestHelper.getTestResourcesDir()
                              + "/jaxws/samples/serviceref/META-INF/wsdl/Endpoint.wsdl"), "wsdl/Endpoint.wsdl");
         }
      });
      appclientEar = JBossWSTestHelper.writeToFile(new JBossWSTestHelper.JarDeployment("jaxws-samples-serviceref-appclient.ear")
      {
         {
            archive.addManifest().addAsResource(new File(JBossWSTestHelper.getTestArchiveDir(), "jaxws-samples-serviceref-appclient.jar"));
         }
      });
      ejbClient = JBossWSTestHelper.writeToFile(new JBossWSTestHelper.JarDeployment("jaxws-samples-serviceref-ejbclient.jar")
      {
         {
            archive
                  .addManifest()
                  .addClass(org.jboss.test.ws.jaxws.samples.serviceref.EJBClient.class)
                  .addClass(org.jboss.test.ws.jaxws.samples.serviceref.EJBRemote.class)
                  .addClass(org.jboss.test.ws.jaxws.samples.serviceref.Endpoint.class)
                  .addClass(org.jboss.test.ws.jaxws.samples.serviceref.EndpointService.class)
                  .addAsManifestResource(
                        new File(JBossWSTestHelper.getTestResourcesDir()
                              + "/jaxws/samples/serviceref/META-INF/ejb-jar.xml"), "ejb-jar.xml")
                  .addAsManifestResource(
                        new File(JBossWSTestHelper.getTestResourcesDir()
                              + "/jaxws/samples/serviceref/META-INF/permissions.xml"), "permissions.xml")
                  .addAsManifestResource(
                        new File(JBossWSTestHelper.getTestResourcesDir()
                              + "/jaxws/samples/serviceref/META-INF/wsdl/Endpoint.wsdl"), "wsdl/Endpoint.wsdl");
         }
      });
      servletClient = JBossWSTestHelper.writeToFile(new JBossWSTestHelper.WarDeployment("jaxws-samples-serviceref-servlet-client.war")
      {
         {
            archive
                  .addManifest()
                  .addClass(org.jboss.test.ws.jaxws.samples.serviceref.Endpoint.class)
                  .addClass(org.jboss.test.ws.jaxws.samples.serviceref.EndpointService.class)
                  .addClass(org.jboss.test.ws.jaxws.samples.serviceref.ServletClient.class)
                  .addAsWebInfResource(
                        new File(JBossWSTestHelper.getTestResourcesDir()
                              + "/jaxws/samples/serviceref/META-INF/wsdl/Endpoint.wsdl"), "wsdl/Endpoint.wsdl")
                  .addAsWebInfResource(
                        new File(JBossWSTestHelper.getTestResourcesDir()
                              + "/jaxws/samples/serviceref/servlet-client/WEB-INF/jboss-web.xml"), "jboss-web.xml")
                  .setWebXML(
                        new File(JBossWSTestHelper.getTestResourcesDir()
                              + "/jaxws/samples/serviceref/servlet-client/WEB-INF/web.xml"));
         }
      });
   }
}
