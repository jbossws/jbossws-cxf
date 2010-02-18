/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.stack.cxf;

import org.jboss.wsf.common.integration.AbstractDeploymentAspect;
import org.jboss.wsf.spi.deployment.ArchiveDeployment;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.ResourceResolver;

/**
 * A deployment aspect for attaching a custom JBoss resources resolver to the endpoint;
 * CXF requires a ResourceResolver for getting wsdl, xsd, etc. correctly when they're
 * not reachable using the context classloader (for instance if they're out of
 * WEB-INF/classes in a war deployed on AS 5 or greater - see. JBAS-5151)
 *
 * @author alessio.soldano@jboss.com
 * @since 19-Nov-2009
 */
public class ResourceResolverDeploymentAspect extends AbstractDeploymentAspect
{
   @Override
   public void start(Deployment dep)
   {
      if (dep instanceof ArchiveDeployment)
      {
         ResourceResolver resolver = ((ArchiveDeployment)dep).getResourceResolver();
         for (Endpoint ep : dep.getService().getEndpoints())
         {
            ep.addAttachment(org.apache.cxf.resource.ResourceResolver.class, new JBossWSResourceResolver(resolver));
         }
      }
   }

   @Override
   public void stop(Deployment dep)
   {
      if (dep instanceof ArchiveDeployment)
      {
         for (Endpoint ep : dep.getService().getEndpoints())
         {
            ep.removeAttachment(org.apache.cxf.resource.ResourceResolver.class);
         }
      }
   }
}
