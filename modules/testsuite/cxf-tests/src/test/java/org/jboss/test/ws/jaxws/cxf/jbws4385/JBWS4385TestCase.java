package org.jboss.test.ws.jaxws.cxf.jbws4385;

import javax.xml.ws.Service;
import java.io.File;
import java.net.URL;
import javax.xml.namespace.QName;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class JBWS4385TestCase extends JBossWSTest {
    private static final String DEP = "jaxws-cxf-jbws4385";

    @ArquillianResource
    private URL baseURL;

    @Deployment(name = DEP, testable = false)
    public static WebArchive createDeployment() {
        final File xercesDir = new File(new File(JBossWSTestHelper.getTestResourcesDir()).getParentFile(), "xerces");

        WebArchive archive = ShrinkWrap.create(WebArchive.class, DEP + ".war");
        archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
                        + "Dependencies: org.apache.cxf\n"))
                .addClass(org.jboss.test.ws.jaxws.cxf.jbws4385.HelloBean.class)
                .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws4385/WEB-INF/wsdl/HelloWorld.wsdl"), "wsdl/HelloWorld.wsdl")
                .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws4385/WEB-INF/web.xml"));
        JBossWSTestHelper.addLibrary(xercesDir, archive);
        return archive;
    }

    @Test
    @RunAsClient
    public void testWS() throws Exception {
        QName serviceName = new QName("http://test.ws.jboss.org/", "HelloBeanService");
        QName portName = new QName("http://test.ws.jboss.org/", "HelloBeanPort");

        URL wsdlURL = new URL(baseURL + "?wsdl");

        Service service = Service.create(wsdlURL, serviceName);
        Hello proxy = (Hello) service.getPort(portName, Hello.class);

        assertTrue("Xerces implementation is expected , but it is :" + proxy.hello("world"), proxy.hello("world").contains("WEB-INF/lib/xerces"));
    }


}
