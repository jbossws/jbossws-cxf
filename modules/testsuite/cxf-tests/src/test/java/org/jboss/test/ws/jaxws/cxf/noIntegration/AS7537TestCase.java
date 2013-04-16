/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.noIntegration;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;

/**
 * [AS7-537] Filter Apache CXF and dependencies
 * 
 * Verifies deployment fails if the webservices subsystem is not disabled for the current deployment
 * 
 * @author alessio.soldano@jboss.com
 * @since 15-Apr-2013
 */
public class AS7537TestCase extends JBossWSTest
{
   public void testFailureWithoutJBossDeploymentStructure() throws Exception {
      boolean undeploy = true;
      try {
         JBossWSTestHelper.deploy("jaxws-cxf-embedded-fail.war");
         fail("Deployment failure expected");
      } catch (Exception e) {
         undeploy = false;
         assertTrue(e.getMessage().contains("JBAS015599"));
      } finally {
         if (undeploy) {
            try {
               JBossWSTestHelper.undeploy("jaxws-cxf-embedded-fail.war");
            } catch (Exception e) {
               //ignore
            }
         }
      }
   }
}
