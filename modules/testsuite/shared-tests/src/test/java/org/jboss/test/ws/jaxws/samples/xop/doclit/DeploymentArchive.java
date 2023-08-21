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
package org.jboss.test.ws.jaxws.samples.xop.doclit;

import java.io.File;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTestHelper;

public final class DeploymentArchive
{
   public static WebArchive createDeployment(String nameSuffix) {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-xop-doclit-" + nameSuffix + ".war");
      archive
         .addManifest()
         .addClass(org.jboss.test.ws.jaxws.samples.xop.doclit.DHRequest.class)
         .addClass(org.jboss.test.ws.jaxws.samples.xop.doclit.DHResponse.class)
         .addClass(org.jboss.test.ws.jaxws.samples.xop.doclit.FakeInputStream.class)
         .addClass(org.jboss.test.ws.jaxws.samples.xop.doclit.GeneratorDataSource.class)
         .addClass(org.jboss.test.ws.jaxws.samples.xop.doclit.ImageRequest.class)
         .addClass(org.jboss.test.ws.jaxws.samples.xop.doclit.ImageResponse.class)
         .addClass(org.jboss.test.ws.jaxws.samples.xop.doclit.MTOMCheckClientHandler.class)
         .addClass(org.jboss.test.ws.jaxws.samples.xop.doclit.MTOMEndpoint.class)
         .addClass(org.jboss.test.ws.jaxws.samples.xop.doclit.MTOMEndpointBean.class)
         .addClass(org.jboss.test.ws.jaxws.samples.xop.doclit.MTOMProtocolHandler.class)
         .addClass(org.jboss.test.ws.jaxws.samples.xop.doclit.SourceRequest.class)
         .addClass(org.jboss.test.ws.jaxws.samples.xop.doclit.SourceResponse.class)
         .addClass(org.jboss.test.ws.jaxws.samples.xop.doclit.WrappedEndpoint.class)
         .addClass(org.jboss.test.ws.jaxws.samples.xop.doclit.WrappedEndpointImpl.class)
         .addClass(org.jboss.test.ws.jaxws.samples.xop.doclit.XOPBase.class)
         .addAsResource("org/jboss/test/ws/jaxws/samples/xop/doclit/jaxws-handlers-server.xml")
         .addClass(org.jboss.test.ws.jaxws.samples.xop.doclit.jaxws.ParameterAnnotation.class)
         .addClass(org.jboss.test.ws.jaxws.samples.xop.doclit.jaxws.ParameterAnnotationResponse.class)
         .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/xop/doclit/WEB-INF/web.xml"));
      return archive;
   }

   private DeploymentArchive() {
      //NOOP
   }
}
