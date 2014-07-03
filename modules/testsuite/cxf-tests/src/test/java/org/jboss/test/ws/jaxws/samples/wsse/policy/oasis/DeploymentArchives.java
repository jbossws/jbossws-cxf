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
package org.jboss.test.ws.jaxws.samples.wsse.policy.oasis;

import java.io.File;

import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.wsf.test.JBossWSTestHelper;

public final class DeploymentArchives
{
   public static final String CLIENT_JAR = JBossWSTestHelper.writeToFile(new JBossWSTestHelper.JarDeployment("jaxws-samples-wsse-policy-oasis-client.jar") { {
         archive
            .addManifest()
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/oasis/META-INF/alice.jks"), "alice.jks")
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/oasis/META-INF/alice.properties"), "alice.properties");
         }
   });
   
   public static final String SERVER_22X_WAR = JBossWSTestHelper.writeToFile(new JBossWSTestHelper.WarDeployment("jaxws-samples-wsse-policy-oasis-22x.war") { {
         archive
            .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                  + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client\n"))
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHello.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHelloResponse.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.KeystorePasswordCallback.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.Service221Impl.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.Service222Impl.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.Service223Impl.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.Service224Impl.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.ServiceIface.class)
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/oasis/WEB-INF/bob.jks"), "classes/bob.jks")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/oasis/WEB-INF/bob.properties"), "classes/bob.properties")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/oasis/WEB-INF/wsdl/SecurityService.wsdl"), "wsdl/SecurityService.wsdl")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/oasis/WEB-INF/wsdl/SecurityService_schema1.xsd"), "wsdl/SecurityService_schema1.xsd");
      }
   });
   
   public static final String SERVER_21X_WAR = JBossWSTestHelper.writeToFile(new JBossWSTestHelper.WarDeployment("jaxws-samples-wsse-policy-oasis-21x.war") { {
         archive
            .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                  + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client\n"))
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHello.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHelloResponse.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.ServerUsernamePasswordCallback.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.Service2111Impl.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.Service2112Impl.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.Service2113Impl.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.Service2121Impl.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.Service213Impl.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.Service214Impl.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.ServiceIface.class)
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/oasis/WEB-INF/bob.jks"), "classes/bob.jks")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/oasis/WEB-INF/bob.properties"), "classes/bob.properties")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/oasis/WEB-INF/wsdl/SecurityService21x.wsdl"), "wsdl/SecurityService21x.wsdl")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/oasis/WEB-INF/wsdl/SecurityService_schema1.xsd"), "wsdl/SecurityService_schema1.xsd");
      }
   });
   
   public static final String SERVER_23X_WAR = JBossWSTestHelper.writeToFile(new JBossWSTestHelper.WarDeployment("jaxws-samples-wsse-policy-oasis-23x.war") { {
         archive
            .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                  + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client\n"))
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHello.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHelloResponse.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.KeystorePasswordCallback.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.Service2311Impl.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.Service2312Impl.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.Service2313Impl.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.Service2314Impl.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.Service2315Impl.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.Service2321Impl.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.Service2322Impl.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.Service2323Impl.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.Service2324Impl.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.ServiceIface.class)
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/oasis/WEB-INF/bob.jks"), "classes/bob.jks")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/oasis/WEB-INF/bob.properties"), "classes/bob.properties")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/oasis/WEB-INF/wsdl/SecurityService23x.wsdl"), "wsdl/SecurityService23x.wsdl")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/oasis/WEB-INF/wsdl/SecurityService_schema1.xsd"), "wsdl/SecurityService_schema1.xsd");
      }
   });
   
   private DeploymentArchives() {
      //NOOP
   }
}
