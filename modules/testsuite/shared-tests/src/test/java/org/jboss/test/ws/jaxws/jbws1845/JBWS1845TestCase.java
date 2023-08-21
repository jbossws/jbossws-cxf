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
package org.jboss.test.ws.jaxws.jbws1845;

import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-1854] Use of @ResponseWrapper annotation cause generation of incorrect wsdl upon deployment
 *
 * @author richard.opalka@jboss.com
 *
 * @since Jan 9, 2008
 */
@RunWith(Arquillian.class)
public final class JBWS1845TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static JavaArchive createDeployments() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-jbws1845.jar");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws1845.SpamComplaintWS.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1845.SpamComplaintWSIface.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1845.SpamResult.class);
      return archive;
   }

   @Test
   @RunAsClient
   public void testIssue() throws Exception
   {
      QName serviceName = new QName("http://service.responsys.com/rsystools/ws/SpamComplaintWS/1.0", "SpamService");
      URL wsdlURL = new URL(baseURL + "/jaxws-jbws1845/SpamService?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      SpamComplaintWSIface proxy = (SpamComplaintWSIface)service.getPort(SpamComplaintWSIface.class);

      String[] orig = { "email", "fromAddress", "mailDate", "complaintDate", "mailbox", "complainer", "xRext", "accountName"};
      String[] returned = proxy.processSpamComplaints(orig[0], orig[1], orig[2], orig[3], orig[4], orig[5], orig[6], orig[7]).get();
      for (int i = 0; i < orig.length; i++)
      {
         assertEquals(orig[i], returned[i]);
      }
   }

}
