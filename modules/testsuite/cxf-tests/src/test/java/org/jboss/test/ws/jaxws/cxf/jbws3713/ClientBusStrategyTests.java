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
package org.jboss.test.ws.jaxws.cxf.jbws3713;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.jboss.wsf.stack.cxf.client.Constants;
import org.jboss.wsf.stack.cxf.client.ProviderImpl;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;

public class ClientBusStrategyTests extends JBossWSTest //*Tests does not match the configured surefire filter on test classes' names
{
   public final String endpointAddress = "http://" + getServerHost() + ":8080/jaxws-cxf-jbws3713/HelloService";
   private final String FS = System.getProperty("file.separator"); // '/' on unix, '\' on windows

   /**
    * Verifies jaxws client bus selection strategy controlled by system properties; in order for checking that,
    * starting a new process is required, as the system property is read once and cached in JBossWS. 
    * 
    * @param strategy
    * @param wsdlAddress
    * @param threadPoolSize
    * @param invocations
    * @return
    * @throws Exception
    */
   protected List<Integer> runJBossModulesClient(final String strategy,
                                               final String wsdlAddress,
                                               final int threadPoolSize,
                                               final int invocations) throws Exception {
      File javaFile = new File (System.getProperty("java.home") + FS + "bin" + FS + "java");
      String javaCmd = javaFile.exists() ? javaFile.getCanonicalPath() : "java";
      
      final String jbh = System.getProperty("jboss.home");
      final String jbm = jbh + FS + "modules";
      final String jbmjar = jbh + FS + "jboss-modules.jar";
      
      final File f = new File(JBossWSTestHelper.getTestArchiveDir(), DeploymentArchives.CLIENT_JAR);

      //java -jar $JBOSS_HOME/jboss-modules.jar -mp $JBOSS_HOME/modules -jar client.jar
      String props = " -Djavax.xml.ws.spi.Provider=" + ProviderImpl.class.getName() + " -Dlog4j.output.dir=" + System.getProperty("log4j.output.dir") +
            " -D" + Constants.JBWS_CXF_JAXWS_CLIENT_BUS_STRATEGY + "=" + strategy + " -jar " + jbmjar + " -mp " + jbm;
      final String command = javaCmd + props + " -jar " + f.getAbsolutePath() + " " + wsdlAddress + " " + threadPoolSize + " " + invocations;
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      executeCommand(command, bout);
      StringTokenizer st = new StringTokenizer(readFirstLine(bout), " ");
      List<Integer> list = new LinkedList<Integer>();
      while (st.hasMoreTokens()) {
         list.add(Integer.parseInt(st.nextToken()));
      }
      return list;
   }
   
   private static String readFirstLine(ByteArrayOutputStream bout) throws IOException {
      bout.flush();
      final byte[] bytes = bout.toByteArray();
      if (bytes != null) {
          BufferedReader reader = new BufferedReader(new java.io.StringReader(new String(bytes)));
          return reader.readLine();
      } else {
         return null;
      }
   }
}
