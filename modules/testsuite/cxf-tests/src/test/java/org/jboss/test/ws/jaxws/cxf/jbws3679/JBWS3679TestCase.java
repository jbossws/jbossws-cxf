/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.test.ws.jaxws.cxf.jbws3679;

import java.net.URL;

import junit.framework.Test;

import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

public class JBWS3679TestCase extends JBossWSTest
{
   public final String endpointAddress = "http://" + getServerHost() + ":8080/jaxws-cxf-jbws3679/ServletClient";

   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS3679TestCase.class, "jaxws-cxf-jbws3679.war");
   }

   public void testSchemaImport() throws Exception
   {
      URL url = new URL(endpointAddress);
      assertEquals("Echoded with:input", IOUtils.readAndCloseStream(url.openStream()));
   }

}
