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
package org.jboss.test.ws.jaxws.cxf.nls;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Test case for web services with National Language Symbols (NLS) in service names using system property configuration.
 * Tests the global system property {@code org.jboss.ws.cxf.decodeUrlPath} set at WildFly startup.
 *
 * @author fburzigo@ibm.com
 * @since 2026-04-16
 */
@ExtendWith(ArquillianExtension.class)
public class NLSSystemPropertyTestCase extends JBossWSTest
{
   private static final String CONTAINER_NAME = "jboss-sysprop";
   private static final String DEPLOYMENT_NAME = "jaxws-cxf-nls-sysprop";
   private static final String TARGET_NS = "http://org.jboss.ws.jaxws.cxf/nls";
   private static final Charset ENCODED_URL_CHARSET = StandardCharsets.UTF_8;
   // The service name is "Caffè" which should be URL-encoded as "Caff%C3%A8"
   private final String encodedServiceName = URLEncoder.encode("Caffè", ENCODED_URL_CHARSET);

   @ArquillianResource
   private ContainerController containerController;

   @ArquillianResource
   private Deployer deployer;

   /**
    * Creates a deployment for testing with system property enabled.
    * This deployment does NOT include jboss-webservices.xml.
    *
    * @return A {@link WebArchive} instance
    */
   @Deployment(name = DEPLOYMENT_NAME, managed = false, testable = false)
   @TargetsContainer(CONTAINER_NAME)
   public static WebArchive createDeployment()
   {
      return ShrinkWrap.create(WebArchive.class, DEPLOYMENT_NAME + ".war")
              .addClasses(NLSEndpoint.class, NLSEndpointImpl.class);
   }

   @BeforeEach
   public void startContainer() {
      System.out.println("=== DEBUG: Starting container: " + CONTAINER_NAME);
      System.out.println("=== DEBUG: Container started status before start: " + containerController.isStarted(CONTAINER_NAME));

      if (!containerController.isStarted(CONTAINER_NAME)) {
         try {
            System.out.println("=== DEBUG: Calling containerController.start()...");
            containerController.start(CONTAINER_NAME);
            System.out.println("=== DEBUG: containerController.start() completed successfully");
         } catch (Exception e) {
            System.err.println("=== DEBUG: Exception during container start: " + e.getMessage());
            dumpServerLog();
            dumpRunningProcesses();
            throw e;
         }
      }

      System.out.println("=== DEBUG: Container started status after start: " + containerController.isStarted(CONTAINER_NAME));
      System.out.println("=== DEBUG: Deploying: " + DEPLOYMENT_NAME);

      try {
         deployer.deploy(DEPLOYMENT_NAME);
         System.out.println("=== DEBUG: Deployment completed successfully");
      } catch (Exception e) {
         System.err.println("=== DEBUG: Exception during deployment: " + e.getMessage());
         throw e;
      }
   }

   private void dumpServerLog() {
      try {
         String jbossHome = System.getProperty("jboss.home");
         if (jbossHome == null) {
            jbossHome = System.getenv("JBOSS_HOME");
         }
         System.out.println("=== DEBUG: jboss.home = " + jbossHome);
         if (jbossHome == null) {
            System.out.println("=== DEBUG: Cannot determine jboss.home, skipping server.log dump");
            return;
         }
         Path serverLog = Paths.get(jbossHome, "standalone", "log", "server.log");
         System.out.println("=== DEBUG: server.log path: " + serverLog);
         System.out.println("=== DEBUG: server.log exists: " + Files.exists(serverLog));
         if (Files.exists(serverLog)) {
            System.out.println("=== DEBUG: server.log size: " + Files.size(serverLog) + " bytes");
            System.out.println("=== DEBUG: server.log last modified: " + Files.getLastModifiedTime(serverLog));
            List<String> lines = Files.readAllLines(serverLog);
            int start = Math.max(0, lines.size() - 50);
            System.out.println("=== DEBUG: server.log last " + (lines.size() - start) + " lines (of " + lines.size() + " total):");
            for (int i = start; i < lines.size(); i++) {
               System.out.println("=== SERVER.LOG [" + (i + 1) + "]: " + lines.get(i));
            }
         }
      } catch (Exception ex) {
         System.err.println("=== DEBUG: Failed to dump server.log: " + ex.getMessage());
      }
   }

   private void dumpRunningProcesses() {
      try {
         boolean isWindows = System.getProperty("os.name", "").toLowerCase().contains("win");
         System.out.println("=== DEBUG: OS = " + System.getProperty("os.name") + ", listing Java processes...");
         ProcessBuilder pb;
         if (isWindows) {
            pb = new ProcessBuilder("cmd", "/c", "wmic process where \"name='java.exe'\" get ProcessId,CommandLine /FORMAT:LIST");
         } else {
            pb = new ProcessBuilder("sh", "-c", "ps aux | grep java | grep -v grep");
         }
         pb.redirectErrorStream(true);
         Process p = pb.start();
         try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
               System.out.println("=== PROCESS: " + line);
            }
         }
         p.waitFor();
      } catch (Exception ex) {
         System.err.println("=== DEBUG: Failed to list processes: " + ex.getMessage());
      }
      dumpPortState();
   }

   private void dumpPortState() {
      int[] ports = {48787, 49990};
      boolean isWindows = System.getProperty("os.name", "").toLowerCase().contains("win");
      for (int port : ports) {
         System.out.println("=== DEBUG: Checking port " + port + " state...");
         try {
            ProcessBuilder pb;
            if (isWindows) {
               pb = new ProcessBuilder("cmd", "/c", "netstat -ano | findstr " + port);
            } else {
               pb = new ProcessBuilder("sh", "-c",
                  "ss -tlnp 'sport = " + port + "' 2>/dev/null; ss -ulnp 'sport = " + port + "' 2>/dev/null");
            }
            pb.redirectErrorStream(true);
            Process p = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
               String line;
               while ((line = reader.readLine()) != null) {
                  System.out.println("=== PORT " + port + ": " + line);
               }
            }
            p.waitFor();
         } catch (Exception ex) {
            System.err.println("=== DEBUG: Failed to check port " + port + ": " + ex.getMessage());
         }
         try (java.net.ServerSocket ss = new java.net.ServerSocket(port)) {
            System.out.println("=== PORT " + port + ": TCP bind SUCCESS (port is now free)");
         } catch (IOException e) {
            System.out.println("=== PORT " + port + ": TCP bind FAILED: " + e.getMessage());
         }
         try (java.net.DatagramSocket ds = new java.net.DatagramSocket(port)) {
            System.out.println("=== PORT " + port + ": UDP bind SUCCESS (port is now free)");
         } catch (Exception e) {
            System.out.println("=== PORT " + port + ": UDP bind FAILED: " + e.getMessage());
         }
      }
   }

   @AfterEach
   public void stopContainer() {
      try {
         deployer.undeploy(DEPLOYMENT_NAME);
      } catch (Exception e) {
         // ignore
      }
      if (containerController.isStarted(CONTAINER_NAME)) {
         containerController.stop(CONTAINER_NAME);
      }
   }

   /**
    * Verify that the WSDL for the service which name contains NLS chars is accessible when the
    * {@code org.jboss.ws.cxf.decodeUrlPath} system property is set to {@code true} at server startup.
    * @throws IOException If the WSDL URL generation fails, or if opening a connection to the WSDL fails.
    */
   @Test
   @RunAsClient
   public void testWsdlAvailableWithSystemProperty() throws IOException {
      // Build the URL with encoded service name
      final int port = getServerPort("cxf-tests", CONTAINER_NAME);
      final URL baseURL = new URL("http://" + getServerHost() + ":" + port + "/" + DEPLOYMENT_NAME + "/");
      final String endpointURL = baseURL + encodedServiceName;
      // Verify WSDL is accessible with encoded URL
      final URL wsdlURL = new URL(endpointURL + "?wsdl");

      NLSTestUtils.verifyWsdlServiceName(wsdlURL, ENCODED_URL_CHARSET);
   }

   /**
    * Verifies that a Web Service which {@code name} and {@code serviceName} contain NLS chars
    * can be accessed via an encoded URL when the {@code org.jboss.ws.cxf.decodeUrlPath} system property
    * is set to {@code true} at server startup.
    *
    * @throws MalformedURLException If the WSDL URL generation fails
    */
   @Test
   @RunAsClient
   public void testNLSServiceWithEncodedURLAvailableViaSystemProperty() throws MalformedURLException {
      // Build the URL with encoded service name
      final int port = getServerPort("cxf-tests", CONTAINER_NAME);
      final URL baseURL = new URL("http://" + getServerHost() + ":" + port + "/" + DEPLOYMENT_NAME + "/");
      final String endpointURL = baseURL + encodedServiceName;
      final URL wsdlURL = new URL(endpointURL + "?wsdl");

      NLSTestUtils.verifyNLSService(TARGET_NS, wsdlURL, endpointURL);
   }
}
