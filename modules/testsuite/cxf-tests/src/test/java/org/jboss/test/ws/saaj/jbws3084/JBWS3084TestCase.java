/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.saaj.jbws3084;

import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPMessage;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSCXFTestSetup;

/**
 * [JBWS-3084] Enable control of chunked encoding when using SOAPConnection.
 *
 * @author sberyozk@redhat.com
 */
public class JBWS3084TestCase extends JBossWSTest
{
   public static Test suite()
   {
      return new JBossWSCXFTestSetup(JBWS3084TestCase.class, "saaj-soap-connection.war");
   }

   public void testSoapConnectionGet() throws Exception
   {
      final String serviceURL = "http://" + getServerHost() + ":8080/saaj-soap-connection/greetMe";
      SOAPConnectionFactory conFac = SOAPConnectionFactory.newInstance();

      SOAPConnection con = conFac.createConnection();
      URL endpoint = new URL(serviceURL);
      SOAPMessage response = con.get(endpoint);
      QName greetMeResp = new QName("http://www.jboss.org/jbossws/saaj", "greetMeResponse");

      Iterator<?> sayHiRespIterator = response.getSOAPBody().getChildElements(greetMeResp);
      SOAPElement soapElement = (SOAPElement) sayHiRespIterator.next();
      assertNotNull(soapElement);

      assertEquals(1, response.countAttachments());
   }
}
