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
package org.jboss.wsf.stack.cxf.deployment.aspect;

import org.jboss.ws.common.integration.AbstractDeploymentAspect;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.stack.cxf.metadata.MetadataBuilder;
import org.jboss.wsf.stack.cxf.metadata.services.DDBeans;

/**
 * A deployer that locates or generates cxf.xml 
 *
 * @author Thomas.Diesler@jboss.org
 * @author alessio.soldano@jboss.com
 * @since 10-May-2007
 */
public class DescriptorDeploymentAspect extends AbstractDeploymentAspect
{
   @Override
   public void start(Deployment dep)
   {
      MetadataBuilder builder = new MetadataBuilder();
      dep.addAttachment(DDBeans.class, builder.build(dep));
   }
}
