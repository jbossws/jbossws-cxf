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
package org.jboss.test.ws.jaxws.samples.wsse.policy.trust;

import junit.framework.Test;
import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.jboss.test.ws.jaxws.samples.wsse.policy.trust.onbehalfof.OnBehalfOfServiceIface;
import org.jboss.wsf.test.JBossWSTest;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import java.net.URL;

/**
 * A demo of using WS-Trust ActAs extension.
 *
 * User: rsearls@redhat.com
 * Date: 1/26/14
 */
public class WSTrustOnBehalfOfTestCase extends JBossWSTest
{
   private final String serviceURL = "http://" + getServerHost() + ":8080/jaxws-samples-wsse-policy-trust-onbehalfof/OnBehalfOfService";

   public static Test suite()
   {
      //deploy client, STS and service; start a security domain to be used by the STS for authenticating client
      return WSTrustTestUtils.getTestSetup(WSTrustOnBehalfOfTestCase.class,
            DeploymentArchives.CLIENT_JAR + " " + DeploymentArchives.STS_WAR + " " + DeploymentArchives.SERVER_WAR + " " + DeploymentArchives.SERVER_ONBEHALFOF_WAR);
   }

   /**
    *  Request a security token that allows it to act on behalf of somebody else.
    *
    * @throws Exception
    */
   public void testOnBehalfOf() throws Exception
   {
      Bus bus = BusFactory.newInstance().createBus();
      try
      {
         BusFactory.setThreadDefaultBus(bus);

         final QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/onbehalfofwssecuritypolicy", "OnBehalfOfService");
         final URL wsdlURL = new URL(serviceURL + "?wsdl");
         Service service = Service.create(wsdlURL, serviceName);
         OnBehalfOfServiceIface proxy = (OnBehalfOfServiceIface) service.getPort(OnBehalfOfServiceIface.class);

         /* TODO explain why this is not needed for setup and then remove
         final QName stsServiceName = new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "SecurityTokenService");
         final QName stsPortName = new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "UT_Port");
         */
         WSTrustTestUtils.setupWsseAndSTSClientOnBehalfOf((BindingProvider) proxy, bus);

         assertEquals("OnBehalfOf WS-Trust Hello World!", proxy.sayHello());
      }
      finally
      {
         bus.shutdown(true);
      }
   }

}
