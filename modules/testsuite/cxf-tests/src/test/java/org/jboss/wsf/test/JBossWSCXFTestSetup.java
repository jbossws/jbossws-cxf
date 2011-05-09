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
package org.jboss.wsf.test;

import junit.framework.Test;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.jboss.logging.Logger;
import org.jboss.wsf.test.JBossWSTestSetup;

public class JBossWSCXFTestSetup extends JBossWSTestSetup
{
   private Bus defaultBus;
   
   public JBossWSCXFTestSetup(Class<?> testClass, String archiveList)
   {
      super(testClass, archiveList);
   }

   public JBossWSCXFTestSetup(Test test, String archiveList)
   {
      super(test, archiveList);
   }

   public JBossWSCXFTestSetup(Test test)
   {
      super(test);
   }
   
   @Override
   protected void setUp() throws Exception {
      defaultBus = BusFactory.getDefaultBus(false);
      super.setUp();
   }
   
   @Override
   protected void tearDown() throws Exception {
      try
      {
         Bus afterTestsDefaultBus = BusFactory.getDefaultBus(false);
         
         if (defaultBus == null && afterTestsDefaultBus != null)
         {
            Logger.getLogger(this.getClass()).info("Default CXF bus has been set during test execution");
         }
         else if (defaultBus != afterTestsDefaultBus)
         {
            throw new Exception("CXF Default bus changed during test: \nBEFORE: " + defaultBus + "\nAFTER: "
                  + afterTestsDefaultBus);
         }
      }
      finally
      {
         defaultBus = null; //remove reference, to help GC
         super.tearDown();
      }
   }
}
