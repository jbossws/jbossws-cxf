/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
