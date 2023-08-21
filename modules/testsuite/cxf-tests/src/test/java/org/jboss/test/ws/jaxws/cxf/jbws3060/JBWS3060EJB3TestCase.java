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
package org.jboss.test.ws.jaxws.cxf.jbws3060;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.runner.RunWith;

/**
 *
 * @author alessio.soldano@jboss.com
 * @since 11-Jun-2010
 */
@RunWith(Arquillian.class)
public class JBWS3060EJB3TestCase extends JBWS3060Tests
{
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(testable = false)
   public static JavaArchive createDeployment() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-cxf-jbws3060.jar");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
                  + "Dependencies: org.jboss.logging\n"))
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3060.EndpointOneEJB3Impl.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3060.EndpointTwoEJB3Impl.class);
      return archive;
   }
   
   @Override
   protected String getEndpointOneURL()
   {
      return baseURL + "/jaxws-cxf-jbws3060/ServiceOne/EndpointOne";
   }


   @Override
   protected String getEndpointTwoURL()
   {
      return baseURL + "/jaxws-cxf-jbws3060/ServiceTwo/EndpointTwo";
   }
}
