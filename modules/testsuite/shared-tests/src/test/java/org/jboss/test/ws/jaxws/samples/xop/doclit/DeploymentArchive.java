/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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
