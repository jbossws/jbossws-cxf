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
package org.jboss.test.ws.jaxws.jbws1841;

import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;
import jakarta.xml.ws.WebServiceRef;

/**
 * A test bean that delegates to a web service provided through serviceref injection.
 *
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 * @author alessio.soldano@jboss.com
 */
@Stateless(name="StatelessBean")
@Remote(StatelessRemote.class)
public class StatelessBean implements StatelessRemote
{

   @WebServiceRef(value = EndpointService.class, mappedName="jbossws-client/service/TestService", wsdlLocation="META-INF/wsdl/TestService.wsdl")
   EndpointInterface endpoint1;

   EndpointInterface _endpoint2;

   @WebServiceRef(value = EndpointService.class, mappedName="jbossws-client/service/TestService", wsdlLocation="META-INF/wsdl/TestService.wsdl")
   public void setEndpoint2(EndpointInterface endpoint2)
   {
      this._endpoint2 = endpoint2;
   }

   // via XML
   EndpointInterface endpoint3;

   // via XML
   EndpointInterface _endpoint4;

   public void setEndpoint4(EndpointInterface endpoint4)
   {
      this._endpoint4 = endpoint4;
   }


   @Override
   public String echo1(String string) throws Exception
   {
      if(null==endpoint1)
         throw new IllegalArgumentException("Serviceref for property 'endpoint1' not injected");

      return endpoint1.echo(string);
   }

   @Override
   public String echo2(String string) throws Exception
   {
      if(null==_endpoint2)
         throw new IllegalArgumentException("Serviceref for property 'endpoint2' not injected");

      return _endpoint2.echo(string);
   }

   @Override
   public String echo3(String string) throws Exception
   {
      if(null==endpoint3)
         throw new IllegalArgumentException("Serviceref for property 'endpoint3' not injected");

      return endpoint3.echo(string);
   }

   @Override
   public String echo4(String string) throws Exception
   {
      if(null==_endpoint4)
         throw new IllegalArgumentException("Serviceref for property 'endpoint4' not injected");

      return _endpoint4.echo(string);
   }

}
