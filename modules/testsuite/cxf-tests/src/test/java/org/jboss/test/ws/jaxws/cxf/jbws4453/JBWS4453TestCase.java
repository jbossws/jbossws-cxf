package org.jboss.test.ws.jaxws.cxf.jbws4453;

import jakarta.xml.ws.Service;
import java.io.File;
import java.net.URL;
import javax.xml.namespace.QName;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jboss.wsf.stack.cxf.client.CachePortFeature;

@ExtendWith(ArquillianExtension.class)
public class JBWS4453TestCase extends JBossWSTest {
    private static final String DEP = "jaxws-cxf-jbws4453";

    @ArquillianResource
    private URL baseURL;

    @Deployment(name = DEP, testable = false)
    public static WebArchive createDeployment() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, DEP + ".war");
        archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
                        + "Dependencies: org.apache.cxf\n"))
                .addClass(org.jboss.test.ws.jaxws.cxf.jbws4453.HelloBean.class)
                .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws4453/WEB-INF/wsdl/HelloWorld.wsdl"), "wsdl/HelloWorld.wsdl")
                .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws4453/WEB-INF/web.xml"));
        return archive;
    }

    @Test
    @RunAsClient
    public void testNoCaching() throws Exception {
        QName serviceName = new QName("http://test.ws.jboss.org/", "HelloBeanService");
        QName portName = new QName("http://test.ws.jboss.org/", "HelloBeanPort");
        URL wsdlURL = new URL(baseURL + "?wsdl");
        Service service = Service.create(wsdlURL, serviceName);

        Hello proxy1 = service.getPort(portName, Hello.class);
        assertTrue(proxy1.hello("world").equals("Hello world"), "Wrong answer");

        Hello proxy2 = service.getPort(portName, Hello.class);
        assertTrue(proxy2.hello("world").equals("Hello world"), "Wrong answer");

        assertNotSame(proxy1, proxy2);
    }

    @Test
    @RunAsClient
    public void testCachingOnService() throws Exception {
        QName serviceName = new QName("http://test.ws.jboss.org/", "HelloBeanService");
        QName portName = new QName("http://test.ws.jboss.org/", "HelloBeanPort");
        URL wsdlURL = new URL(baseURL + "?wsdl");
        Service service = Service.create(wsdlURL, serviceName, new CachePortFeature());

        Hello proxy1 = service.getPort(portName, Hello.class);
        assertTrue(proxy1.hello("world").equals("Hello world"), "Wrong answer");

        Hello proxy2 = service.getPort(portName, Hello.class);
        assertTrue(proxy2.hello("world").equals("Hello world"), "Wrong answer");

        assertSame(proxy1, proxy2);
    }

    @Test
    @RunAsClient
    public void testCachingOnPort() throws Exception {
        QName serviceName = new QName("http://test.ws.jboss.org/", "HelloBeanService");
        QName portName = new QName("http://test.ws.jboss.org/", "HelloBeanPort");
        URL wsdlURL = new URL(baseURL + "?wsdl");
        Service service = Service.create(wsdlURL, serviceName);

        Hello proxy1 = service.getPort(portName, Hello.class, new CachePortFeature());
        assertTrue(proxy1.hello("world").equals("Hello world"), "Wrong answer");

        Hello proxy2 = service.getPort(portName, Hello.class, new CachePortFeature(false));
        assertTrue(proxy2.hello("world").equals("Hello world"), "Wrong answer");

        Hello proxy3 = service.getPort(portName, Hello.class, new CachePortFeature());
        assertTrue(proxy3.hello("world").equals("Hello world"), "Wrong answer");

        assertSame(proxy1, proxy3);
        assertNotSame(proxy1, proxy2);
        assertNotSame(proxy2, proxy3);
    }

    @Test
    @RunAsClient
    public void testCachingOnBothServiceAndPort() throws Exception {
        QName serviceName = new QName("http://test.ws.jboss.org/", "HelloBeanService");
        QName portName = new QName("http://test.ws.jboss.org/", "HelloBeanPort");
        URL wsdlURL = new URL(baseURL + "?wsdl");
        Service service = Service.create(wsdlURL, serviceName, new CachePortFeature());

        Hello proxy1 = service.getPort(portName, Hello.class);
        assertTrue(proxy1.hello("world").equals("Hello world"), "Wrong answer");

        Hello proxy2 = service.getPort(portName, Hello.class, new CachePortFeature(false));
        assertTrue(proxy2.hello("world").equals("Hello world"), "Wrong answer");

        Hello proxy3 = service.getPort(portName, Hello.class, new CachePortFeature());
        assertTrue(proxy3.hello("world").equals("Hello world"), "Wrong answer");

        assertSame(proxy1, proxy3);
        assertNotSame(proxy1, proxy2);
        assertNotSame(proxy2, proxy3);
    }

}
