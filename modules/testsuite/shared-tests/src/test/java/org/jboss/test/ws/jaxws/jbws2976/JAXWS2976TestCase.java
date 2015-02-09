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
package org.jboss.test.ws.jaxws.jbws2976;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.ws.Dispatch;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.http.HTTPBinding;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;

/**
 * @author <a href="ema@redhat.com">Jim Ma</a>
 */
@RunWith(Arquillian.class)
public class JAXWS2976TestCase extends junit.framework.TestCase
{

   @Test
   @RunAsClient
   public void testAddingIncomptiableHandler() throws Exception
   {
      try
      {
         Dispatch<Source> source = createDispatchSource();
         @SuppressWarnings("rawtypes")
         List<Handler> handlers = new ArrayList<Handler>();
         handlers.add(new SOAPHandler());
         source.getBinding().setHandlerChain(handlers);
         fail("WebServiceException is not thrown");
      }
      catch (WebServiceException e)
      {
         //expected and do nothing
      }
   }

   private Dispatch<Source> createDispatchSource() throws Exception
   {
      javax.xml.ws.Service service = javax.xml.ws.Service.create(new QName("http://ws.jboss.org", "HelloService"));
      service.addPort(new QName("http://ws.jboss.org", "HelloPort"), HTTPBinding.HTTP_BINDING,
            "http://ws.jboss.org/endpointAddress");
      return service.createDispatch(new QName("http://ws.jboss.org", "HelloPort"), Source.class,
            javax.xml.ws.Service.Mode.PAYLOAD);
   }
}
