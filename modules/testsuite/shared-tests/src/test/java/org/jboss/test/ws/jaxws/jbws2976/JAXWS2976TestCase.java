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
package org.jboss.test.ws.jaxws.jbws2976;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import jakarta.xml.ws.Dispatch;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.handler.Handler;
import jakarta.xml.ws.http.HTTPBinding;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;

import static org.junit.Assert.fail;

/**
 * @author <a href="ema@redhat.com">Jim Ma</a>
 */
@RunWith(Arquillian.class)
public class JAXWS2976TestCase
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
      jakarta.xml.ws.Service service = jakarta.xml.ws.Service.create(new QName("http://ws.jboss.org", "HelloService"));
      service.addPort(new QName("http://ws.jboss.org", "HelloPort"), HTTPBinding.HTTP_BINDING,
            "http://ws.jboss.org/endpointAddress");
      return service.createDispatch(new QName("http://ws.jboss.org", "HelloPort"), Source.class,
            jakarta.xml.ws.Service.Mode.PAYLOAD);
   }
}
