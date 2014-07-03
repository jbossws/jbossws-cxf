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
package org.jboss.test.ws.jaxws.samples.wsse.policy.trust;

import java.io.File;

import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.wsf.test.JBossWSTestHelper;

public final class DeploymentArchives
{
   public static final String STS_WAR = JBossWSTestHelper.writeToFile(new JBossWSTestHelper.WarDeployment("jaxws-samples-wsse-policy-trust-sts.war") { {
         archive
            .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                  + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client,org.apache.cxf.impl annotations\n")) //cxf impl required to extend STS impl
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.sts.STSCallbackHandler.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.sts.SampleSTS.class)
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/jboss-web.xml"), "jboss-web.xml")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/wsdl/ws-trust-1.4-service.wsdl"), "wsdl/ws-trust-1.4-service.wsdl")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/stsstore.jks"), "classes/stsstore.jks")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/stsKeystore.properties"), "classes/stsKeystore.properties")
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/web.xml"));
      }
   });
   
   public static final String SERVER_WAR = JBossWSTestHelper.writeToFile(new JBossWSTestHelper.WarDeployment("jaxws-samples-wsse-policy-trust.war") { {
         archive
            .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                  + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client\n"))
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHello.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHelloResponse.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.service.ServerCallbackHandler.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.service.ServiceIface.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.service.ServiceImpl.class)
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/wsdl/SecurityService.wsdl"), "wsdl/SecurityService.wsdl")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/wsdl/SecurityService_schema1.xsd"), "wsdl/SecurityService_schema1.xsd")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/servicestore.jks"), "classes/servicestore.jks")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/serviceKeystore.properties"), "classes/serviceKeystore.properties");
      }
   });
   
   public static final String CLIENT_JAR = JBossWSTestHelper.writeToFile(new JBossWSTestHelper.JarDeployment("jaxws-samples-wsse-policy-trust-client.jar") { {
         archive
            .addManifest()
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/META-INF/clientKeystore.properties"), "clientKeystore.properties")
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/META-INF/clientstore.jks"), "clientstore.jks");
      }
   });
   
   public static final String SERVER_ACTAS_WAR = JBossWSTestHelper.writeToFile(new JBossWSTestHelper.WarDeployment("jaxws-samples-wsse-policy-trust-actas.war") { {
         archive
            .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                  + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client, org.apache.cxf.impl\n"))
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHello.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHelloResponse.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.actas.ActAsCallbackHandler.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.actas.ActAsServiceIface.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.actas.ActAsServiceImpl.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.service.ServiceIface.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.shared.WSTrustAppUtils.class)
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/wsdl/ActAsService.wsdl"), "wsdl/ActAsService.wsdl")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/wsdl/ActAsService_schema1.xsd"), "wsdl/ActAsService_schema1.xsd")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/actasstore.jks"), "classes/actasstore.jks")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/actasKeystore.properties"), "classes/actasKeystore.properties")
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/META-INF/clientstore.jks"), "clientstore.jks")
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/META-INF/clientKeystore.properties"), "clientKeystore.properties")
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/META-INF/permissions.xml"), "permissions.xml");
      }
   });
   
   public static final String SERVER_ONBEHALFOF_WAR = JBossWSTestHelper.writeToFile(new JBossWSTestHelper.WarDeployment("jaxws-samples-wsse-policy-trust-onbehalfof.war") { {
         archive
            .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                  + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client, org.apache.cxf.impl\n"))
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHello.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHelloResponse.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.onbehalfof.OnBehalfOfCallbackHandler.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.onbehalfof.OnBehalfOfServiceIface.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.onbehalfof.OnBehalfOfServiceImpl.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.service.ServiceIface.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.shared.WSTrustAppUtils.class)
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/wsdl/OnBehalfOfService.wsdl"), "wsdl/OnBehalfOfService.wsdl")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/wsdl/OnBehalfOfService_schema1.xsd"), "wsdl/OnBehalfOfService_schema1.xsd")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/actasstore.jks"), "classes/actasstore.jks")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/actasKeystore.properties"), "classes/actasKeystore.properties")
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/META-INF/clientstore.jks"), "clientstore.jks")
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/META-INF/clientKeystore.properties"), "clientKeystore.properties")
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/META-INF/permissions.xml"), "permissions.xml");
         }
   });
   
   public static final String STS_HOLDEROFKEY_WAR = JBossWSTestHelper.writeToFile(new JBossWSTestHelper.WarDeployment("jaxws-samples-wsse-policy-trust-sts-holderofkey.war") { {
         archive
            .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                  + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client,org.apache.cxf.impl annotations\n")) //cxf impl required to extend STS impl
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.stsholderofkey.STSHolderOfKeyCallbackHandler.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.stsholderofkey.SampleSTSHolderOfKey.class)
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/jboss-web.xml"), "jboss-web.xml")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/wsdl/holderofkey-ws-trust-1.4-service.wsdl"), "wsdl/holderofkey-ws-trust-1.4-service.wsdl")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/stsstore.jks"), "classes/stsstore.jks")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/stsKeystore.properties"), "classes/stsKeystore.properties")
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/holderofkey/web.xml"));
      }
   });
   
   public static final String SERVER_HOLDEROFKEY_WAR = JBossWSTestHelper.writeToFile(new JBossWSTestHelper.WarDeployment("jaxws-samples-wsse-policy-trust-holderofkey.war") { {
         archive
            .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                  + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client\n"))
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHello.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHelloResponse.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.holderofkey.HolderOfKeyCallbackHandler.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.holderofkey.HolderOfKeyIface.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.holderofkey.HolderOfKeyImpl.class)
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/wsdl/HolderOfKeyService.wsdl"), "wsdl/HolderOfKeyService.wsdl")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/wsdl/HolderOfKeyService_schema1.xsd"), "wsdl/HolderOfKeyService_schema1.xsd")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/servicestore.jks"), "classes/servicestore.jks")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/serviceKeystore.properties"), "classes/serviceKeystore.properties");
      }
   });
   
   public static final String STS_PICKETLINK_WAR = JBossWSTestHelper.writeToFile(new JBossWSTestHelper.WarDeployment("jaxws-samples-wsse-policy-trustPicketLink-sts.war") { {
         archive
            .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                  + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client,org.picketlink\n"))
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.picketlink.PicketLinkSTService.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.sts.STSCallbackHandler.class)
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/jboss-web.xml"), "jboss-web.xml")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/wsdl/PicketLinkSTS.wsdl"), "wsdl/PicketLinkSTS.wsdl")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/stsstore.jks"), "classes/stsstore.jks")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/picketlink-sts.xml"), "classes/picketlink-sts.xml")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/stsKeystore.properties"), "classes/stsKeystore.properties");
      }
   });
   
   public static final String STS_BEARER_WAR = JBossWSTestHelper.writeToFile(new JBossWSTestHelper.WarDeployment("jaxws-samples-wsse-policy-trust-sts-bearer.war") { {
         archive
            .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                  + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client,org.apache.cxf.impl annotations\n")) //cxf impl required to extend STS impl
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.stsbearer.STSBearerCallbackHandler.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.stsbearer.SampleSTSBearer.class)
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/jboss-web.xml"), "jboss-web.xml")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/wsdl/bearer-ws-trust-1.4-service.wsdl"), "wsdl/bearer-ws-trust-1.4-service.wsdl")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/stsstore.jks"), "classes/stsstore.jks")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/stsKeystore.properties"), "classes/stsKeystore.properties")
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/bearer/web.xml"));
      }
   });
   
   public static final String SERVER_BEARER_WAR = JBossWSTestHelper.writeToFile(new JBossWSTestHelper.WarDeployment("jaxws-samples-wsse-policy-trust-bearer.war") { {
         archive
            .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                  + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client\n"))
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHello.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHelloResponse.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.bearer.BearerIface.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.bearer.BearerImpl.class)
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/wsdl/BearerService.wsdl"), "wsdl/BearerService.wsdl")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/wsdl/BearerService_schema1.xsd"), "wsdl/BearerService_schema1.xsd")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/servicestore.jks"), "classes/servicestore.jks")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/serviceKeystore.properties"), "classes/serviceKeystore.properties");
      }
   });
   
   private DeploymentArchives() {
      //NOOP
   }
}
