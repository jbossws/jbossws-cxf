/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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

import java.util.Arrays;

import org.junit.Assume;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * TestRule class to ignore test for the specified container. You can use this class to ignore 
 * test when the container is the specified ones:
 * <pre>
 * public class IgnoreContainerTest {
 *  &#064;Rule
 *  public IgnoreContainer rule = new IgnoreContainer("wildfly900", "wildfly800");
 *  &#064;Test
 *  public void testFoo() throws Exception {
 *  }
 * }
 * </pre>
 * @author <a href="mailto:ema@redhat.com">Jim Ma</a>
 *
 */
public class IgnoreContainer implements TestRule
{

   private String[] ignoredContainers;

   public IgnoreContainer(String... containers)
   {
      this.ignoredContainers = containers;
   }

   @Override
   public Statement apply(final Statement base, final Description description)
   {
      return new Statement() {
         boolean ignored = false;

         @Override
         public void evaluate() throws Throwable
         {
            for (String container : ignoredContainers)
            {
               if (JBossWSTestHelper.getIntegrationTarget().startsWith(container))
               {
                  ignored = true;
                  break;
               }
            }
            //Check if ignore this test
            Assume.assumeFalse(description.getClassName() + " is excluded for container: " + Arrays.toString(ignoredContainers), ignored);
         }
      };
   }

}
