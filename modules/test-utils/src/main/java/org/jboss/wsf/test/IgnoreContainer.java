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
package org.jboss.wsf.test;

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
 *  public IgnoreContainer rule = new IgnoreContainer("wildfly900", "wildfly820");
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
   private final String ignoreReason;
   private final String[] ignoredContainers;

   public IgnoreContainer(String ignoreReason, String... containers)
   {
      this.ignoreReason = ignoreReason;
      if (containers == null || containers.length == 0) {
         throw new IllegalArgumentException("Expected at least a container version!");
      }
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
            Assume.assumeFalse(ignoreReason, ignored);

            base.evaluate(); // always call base statement to continue in execution when assume passes
         }
      };
   }

}
