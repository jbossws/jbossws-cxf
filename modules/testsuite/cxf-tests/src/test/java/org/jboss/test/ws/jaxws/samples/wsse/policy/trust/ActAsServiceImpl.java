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
package org.jboss.test.ws.jaxws.samples.wsse.policy.trust;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.annotations.EndpointProperties;
import org.apache.cxf.annotations.EndpointProperty;
import org.apache.cxf.interceptor.OutInterceptors;
import org.apache.cxf.ws.security.SecurityConstants;
import org.apache.cxf.ws.security.trust.STSClient;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Map;

/**
 * User: rsearls@redhat.com
 * Date: 1/26/14
 */

@WebService
(
   portName = "ActAsServicePort",
   serviceName = "ActAsService",
   wsdlLocation = "WEB-INF/wsdl/ActAsService.wsdl",
   targetNamespace = "http://www.jboss.org/jbossws/ws-extensions/actaswssecuritypolicy",
   endpointInterface = "org.jboss.test.ws.jaxws.samples.wsse.policy.trust.ActAsServiceIface"
)

@EndpointProperties(value = {
      @EndpointProperty(key = "ws-security.signature.username", value = "myactaskey"),
      @EndpointProperty(key = "ws-security.signature.properties", value =  "actasKeystore.properties"),
      @EndpointProperty(key = "ws-security.encryption.properties", value = "actasKeystore.properties"),
      @EndpointProperty(key = "ws-security.callback-handler", value = "org.jboss.test.ws.jaxws.samples.wsse.policy.trust.ActAsCallbackHandler")
})
@OutInterceptors(interceptors = {"org.jboss.test.ws.jaxws.samples.wsse.policy.trust.ActAsOutInterceptor"})
public class ActAsServiceImpl implements ActAsServiceIface
{
   public String sayHello() {
      try {
         ServiceIface proxy = setupService();
         return "ActAs " + proxy.sayHello();
      } catch (MalformedURLException e) {
         e.printStackTrace();
      }
      return null;
   }

   private  ServiceIface setupService()throws MalformedURLException {
      ServiceIface proxy = null;
      Bus bus = BusFactory.newInstance().createBus();

      try {
         BusFactory.setThreadDefaultBus(bus);

         final String serviceURL = "http://" + getServerHost() + ":8080/jaxws-samples-wsse-policy-trust/SecurityService";
         final QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy", "SecurityService");
         final URL wsdlURL = new URL(serviceURL + "?wsdl");
         Service service = Service.create(wsdlURL, serviceName);
         proxy = (ServiceIface) service.getPort(ServiceIface.class);

         Map<String, Object> ctx = ((BindingProvider) proxy).getRequestContext();
         ctx.put(SecurityConstants.CALLBACK_HANDLER, new ActAsCallbackHandler());


         ctx.put(SecurityConstants.SIGNATURE_PROPERTIES,
            Thread.currentThread().getContextClassLoader().getResource("actasKeystore.properties" ));
         ctx.put(SecurityConstants.SIGNATURE_USERNAME, "myactaskey" );
         ctx.put(SecurityConstants.ENCRYPT_PROPERTIES,
            Thread.currentThread().getContextClassLoader().getResource("../../META-INF/clientKeystore.properties" ));
         ctx.put(SecurityConstants.ENCRYPT_USERNAME, "myservicekey");


         STSClient stsClient = new STSClient(bus);
         Map<String, Object> props = stsClient.getProperties();
         props.put(SecurityConstants.USERNAME, "alice");
          //- these are not needed.  They are provided by the above (ctx) map.
        // props.put(SecurityConstants.CALLBACK_HANDLER, new ActAsCallbackHandler());
        // props.put(SecurityConstants.ENCRYPT_PROPERTIES,
        //    Thread.currentThread().getContextClassLoader().getResource("../../META-INF/clientKeystore.properties" ));
         props.put(SecurityConstants.ENCRYPT_USERNAME, "mystskey");
         props.put(SecurityConstants.STS_TOKEN_USERNAME, "myactaskey" );
         props.put(SecurityConstants.STS_TOKEN_PROPERTIES,
            Thread.currentThread().getContextClassLoader().getResource("actasKeystore.properties" ));
         props.put(SecurityConstants.STS_TOKEN_USE_CERT_FOR_KEYINFO, "true");

         ctx.put(SecurityConstants.STS_CLIENT, stsClient);

      } finally {
         bus.shutdown(true);
      }

      return proxy;
   }

   private  String getServerHost()
   {
      final String host = System.getProperty("jboss.bind.address", "localhost");
      return toIPv6URLFormat(host);
   }

   private  String toIPv6URLFormat(final String host)
   {
      try
      {
         if (host.startsWith(":"))
         {
            throw new IllegalArgumentException("JBossWS test suite requires IPv6 addresses to be wrapped with [] brackets. Expected format is: [" + host + "]");
         }
         if (host.startsWith("["))
         {
            if (System.getProperty("java.net.preferIPv4Stack") == null)
            {
               throw new IllegalStateException("always provide java.net.preferIPv4Stack JVM property when using IPv6 address format");
            }
            if (System.getProperty("java.net.preferIPv6Addresses") == null)
            {
               throw new IllegalStateException("always provide java.net.preferIPv6Addresses JVM property when using IPv6 address format");
            }
         }
         final boolean isIPv6Address = InetAddress.getByName(host) instanceof Inet6Address;
         final boolean isIPv6Formatted = isIPv6Address && host.startsWith("[");
         return isIPv6Address && !isIPv6Formatted ? "[" + host + "]" : host;
      }
      catch (final UnknownHostException e)
      {
         throw new RuntimeException(e);
      }
   }
}
