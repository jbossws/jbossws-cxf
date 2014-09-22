package org.jboss.test.ws.jaxws.cxf.jbws3805;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Test;

import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;
import org.jboss.wsf.test.JBossWSTestSetup;

public class JBWS3805TestCase extends JBossWSTest
{
   private static String publishURL = "http://" + getServerHost() + ":8080/jaxws-cxf-jbws3805/HelloService";

   public static BaseDeployment<?>[] createDeployments()
   {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-cxf-jbws3805.war") {
         {
            archive.setManifest(new StringAsset("Manifest-Version: 1.0\n" + "Dependencies: org.jboss.ws.common\n"))
                  .addClass(org.jboss.test.ws.jaxws.cxf.jbws3805.EndpointOne.class).addClass(org.jboss.test.ws.jaxws.cxf.jbws3805.EndpointOneImpl.class)
                  .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3805/WEB-INF/jboss-webservices.xml"), "jboss-webservices.xml")
                  .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3805/WEB-INF/web.xml"));
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS3805TestCase.class, JBossWSTestHelper.writeToFile(createDeployments()));
   }

   public void testWsdlSoapAddress() throws Exception
   {
      URL wsdlURL = new URL(publishURL + "?wsdl");
      HttpURLConnection connection = (HttpURLConnection)wsdlURL.openConnection();
      try
      {
         connection.connect();
         assertEquals(200, connection.getResponseCode());
         connection.getInputStream();

         BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
         String line;
         while ((line = in.readLine()) != null)
         {
            if (line.contains("address location"))
            {
               assertTrue("Unexpected uri scheme", line.contains("https://foo:8443/jaxws-cxf-JBWS3805/HelloService"));
               return;
            }
         }
         fail("Could not check soap:address!");
      }
      finally
      {
         connection.disconnect();
      }

   }

}
