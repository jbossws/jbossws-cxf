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
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * [JBWS-1854] Use of @ResponseWrapper annotation cause generation of incorrect wsdl upon deployment
 *
 * @author richard.opalka@jboss.com
 *
 * @since Jan 9, 2008
 */
public final class JBWS1845TestCase extends JBossWSTest
{
   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.JarDeployment("jaxws-jbws1845.jar") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws1845.SpamComplaintWS.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1845.SpamComplaintWSIface.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1845.SpamResult.class);
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS1845TestCase.class, JBossWSTestHelper.writeToFile(createDeployments()));
   }

   public void testIssue() throws Exception
   {
      QName serviceName = new QName("http://service.responsys.com/rsystools/ws/SpamComplaintWS/1.0", "SpamService");
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbws1845/SpamService?wsdl");

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
