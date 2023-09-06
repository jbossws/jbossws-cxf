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
package org.jboss.test.ws.jaxws.samples.wsse.policy.trust;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.xml.namespace.QName;
import jakarta.xml.ws.BindingProvider;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.ws.security.SecurityConstants;
import org.apache.cxf.ws.security.trust.STSClient;
import org.jboss.test.ws.jaxws.samples.wsse.policy.trust.service.ServiceIface;
import org.jboss.test.ws.jaxws.samples.wsse.policy.shared.ClientCallbackHandler;
import org.jboss.test.ws.jaxws.samples.wsse.policy.shared.UsernameTokenCallbackHandler;

/**
 * Some client util methods for WS-Trust testcases 
 *
 * @author alessio.soldano@jboss.com
 * @since 08-May-2012
 */
public class WSTrustK8sTestUtils
{
   public static void setupWsseAndSTSClient(ServiceIface proxy, String stsWsdlLocation, QName stsService, QName stsPort)
   {
      Map<String, Object> ctx = ((BindingProvider) proxy).getRequestContext();
      setServiceContextAttributes(ctx);
      ctx.put(SecurityConstants.STS_CLIENT, createSTSClient(BusFactory.getDefaultBus(), stsWsdlLocation, stsService, stsPort));
   }

   public static void setupWsse(ServiceIface proxy, Bus bus)
   {
      Map<String, Object> ctx = ((BindingProvider) proxy).getRequestContext();
      setServiceContextAttributes(ctx);
      ctx.put(appendIssuedTokenSuffix(SecurityConstants.USERNAME), "alice");
      ctx.put(appendIssuedTokenSuffix(SecurityConstants.CALLBACK_HANDLER), new ClientCallbackHandler());
      ctx.put(appendIssuedTokenSuffix(SecurityConstants.ENCRYPT_PROPERTIES), getResourceFile("META-INF/clientKeystore.properties"));
      ctx.put(appendIssuedTokenSuffix(SecurityConstants.ENCRYPT_USERNAME), "mystskey");
      ctx.put(appendIssuedTokenSuffix(SecurityConstants.STS_TOKEN_USERNAME), "myclientkey");
      ctx.put(appendIssuedTokenSuffix(SecurityConstants.STS_TOKEN_PROPERTIES), getResourceFile("META-INF/clientKeystore.properties"));
      ctx.put(appendIssuedTokenSuffix(SecurityConstants.STS_TOKEN_USE_CERT_FOR_KEYINFO), "true");
   }


   /**
    * A PASSWORD is provided in place of the ClientCallbackHandler in the
    * STSClient.  A USERNAME and PASSWORD is required by CXF in the msg.
    *
    * @param proxy
    * @param bus
    * @param stsWsdlLocation
    * @param stsService
    * @param stsPort
    * @see org.apache.cxf.ws.security.SecurityConstants#PASSWORD
    */
   public static void setupWsseAndSTSClientNoCallbackHandler(ServiceIface proxy, Bus bus, String stsWsdlLocation, QName stsService, QName stsPort) {
      Map<String, Object> ctx = ((BindingProvider) proxy).getRequestContext();
      setServiceContextAttributes(ctx);

      STSClient stsClient = new STSClient(bus);
      if (stsWsdlLocation != null) {
         stsClient.setWsdlLocation(stsWsdlLocation);
         stsClient.setServiceQName(stsService);
         stsClient.setEndpointQName(stsPort);
      }
      Map<String, Object> props = stsClient.getProperties();
      props.put(SecurityConstants.USERNAME, "alice");
      props.put(SecurityConstants.PASSWORD, "clarinet");
      props.put(SecurityConstants.ENCRYPT_PROPERTIES, getResourceFile("META-INF/clientKeystore.properties"));
      props.put(SecurityConstants.ENCRYPT_USERNAME, "mystskey");
      props.put(SecurityConstants.STS_TOKEN_USERNAME, "myclientkey");
      props.put(SecurityConstants.STS_TOKEN_PROPERTIES, getResourceFile("META-INF/clientKeystore.properties"));
      props.put(SecurityConstants.STS_TOKEN_USE_CERT_FOR_KEYINFO, "true");
      ctx.put(SecurityConstants.STS_CLIENT, stsClient);
   }

   /**
    * Uses the SIGNATURE_PROPERTIES keystore's  "alias name" as the SIGNATURE_USERNAME when
    * USERNAME and SIGNATURE_USERNAME is not provided.
    *
    * @param proxy
    * @param bus
    * @param stsWsdlLocation
    * @param stsService
    * @param stsPort
    * @see org.apache.cxf.ws.security.SecurityConstants#SIGNATURE_PROPERTIES
    */
   public static void setupWsseAndSTSClientNoSignatureUsername(ServiceIface proxy, Bus bus, String stsWsdlLocation, QName stsService, QName stsPort) {
      Map<String, Object> ctx = ((BindingProvider) proxy).getRequestContext();
      ctx.put(SecurityConstants.CALLBACK_HANDLER, new ClientCallbackHandler());
      ctx.put(SecurityConstants.SIGNATURE_PROPERTIES, getResourceFile("META-INF/clientKeystore.properties"));
      ctx.put(SecurityConstants.ENCRYPT_PROPERTIES, getResourceFile("META-INF/clientKeystore.properties"));
      ctx.put(SecurityConstants.ENCRYPT_USERNAME, "myservicekey");

      ctx.put(SecurityConstants.STS_CLIENT, createSTSClient(bus, stsWsdlLocation, stsService, stsPort));
   }

   /**
    * Request a security token that allows it to act as if it were somebody else.
    *
    * @param proxy
    * @param bus
    */
   public static void setupWsseAndSTSClientActAs(BindingProvider proxy, Bus bus) {

      Map<String, Object> ctx = proxy.getRequestContext();

      ctx.put(SecurityConstants.CALLBACK_HANDLER, new ClientCallbackHandler());
      ctx.put(SecurityConstants.ENCRYPT_PROPERTIES, getResourceFile("META-INF/clientKeystore.properties"));
      ctx.put(SecurityConstants.ENCRYPT_USERNAME, "myactaskey");
      ctx.put(SecurityConstants.SIGNATURE_PROPERTIES, getResourceFile("META-INF/clientKeystore.properties"));
      ctx.put(SecurityConstants.SIGNATURE_USERNAME, "myclientkey");


      UsernameTokenCallbackHandler ch = new UsernameTokenCallbackHandler();
      String str = ch.getUsernameTokenString("alice","clarinet");

      ctx.put(SecurityConstants.STS_TOKEN_ACT_AS, str);


      STSClient stsClient = new STSClient(bus);
      Map<String, Object> props = stsClient.getProperties();
      props.put(SecurityConstants.USERNAME, "bob");
      props.put(SecurityConstants.CALLBACK_HANDLER, new ClientCallbackHandler());
      props.put(SecurityConstants.ENCRYPT_PROPERTIES, getResourceFile("META-INF/clientKeystore.properties"));
      props.put(SecurityConstants.ENCRYPT_USERNAME, "mystskey");
      props.put(SecurityConstants.STS_TOKEN_USERNAME, "myclientkey");
      props.put(SecurityConstants.STS_TOKEN_PROPERTIES, getResourceFile("META-INF/clientKeystore.properties"));
      props.put(SecurityConstants.STS_TOKEN_USE_CERT_FOR_KEYINFO, "true");

      ctx.put(SecurityConstants.STS_CLIENT, stsClient);
   }

   /**
    * Request a security token that allows it to act on the behalf of somebody else.
    *
    * @param proxy
    * @param bus
    */
   public static void setupWsseAndSTSClientOnBehalfOf(BindingProvider proxy, Bus bus) {

      Map<String, Object> ctx = proxy.getRequestContext();

      ctx.put(SecurityConstants.CALLBACK_HANDLER, new ClientCallbackHandler());
      ctx.put(SecurityConstants.ENCRYPT_PROPERTIES, getResourceFile("META-INF/clientKeystore.properties"));
      ctx.put(SecurityConstants.ENCRYPT_USERNAME, "myactaskey");
      ctx.put(SecurityConstants.SIGNATURE_PROPERTIES, getResourceFile("META-INF/clientKeystore.properties"));
      ctx.put(SecurityConstants.SIGNATURE_USERNAME, "myclientkey");
      ctx.put(SecurityConstants.USERNAME,"alice");
      ctx.put(SecurityConstants.PASSWORD, "clarinet");

      STSClient stsClient = new STSClient(bus);
      stsClient.setOnBehalfOf(new UsernameTokenCallbackHandler());

      Map<String, Object> props = stsClient.getProperties();
      props.put(SecurityConstants.CALLBACK_HANDLER, new ClientCallbackHandler());
      props.put(SecurityConstants.ENCRYPT_PROPERTIES, getResourceFile("META-INF/clientKeystore.properties"));
      props.put(SecurityConstants.ENCRYPT_USERNAME, "mystskey");
      props.put(SecurityConstants.STS_TOKEN_USERNAME, "myclientkey");
      props.put(SecurityConstants.STS_TOKEN_PROPERTIES, getResourceFile("META-INF/clientKeystore.properties"));
      props.put(SecurityConstants.STS_TOKEN_USE_CERT_FOR_KEYINFO, "true");

      ctx.put(SecurityConstants.STS_CLIENT, stsClient);
   }

   public static void setupWsseAndSTSClientBearer(BindingProvider proxy, Bus bus) {

      Map<String, Object> ctx = proxy.getRequestContext();

      STSClient stsClient = new STSClient(bus);

      ctx.put(SecurityConstants.CALLBACK_HANDLER, new ClientCallbackHandler());
      ctx.put(SecurityConstants.SIGNATURE_PROPERTIES, getResourceFile("META-INF/clientKeystore.properties"));
      ctx.put(SecurityConstants.ENCRYPT_PROPERTIES, getResourceFile("META-INF/clientKeystore.properties"));
      ctx.put(SecurityConstants.SIGNATURE_USERNAME, "myclientkey");
      ctx.put(SecurityConstants.ENCRYPT_USERNAME, "myservicekey");
      ctx.put(appendIssuedTokenSuffix(SecurityConstants.USERNAME), "alice");
      ctx.put(appendIssuedTokenSuffix(SecurityConstants.CALLBACK_HANDLER), new ClientCallbackHandler());
      ctx.put(appendIssuedTokenSuffix(SecurityConstants.ENCRYPT_PROPERTIES), getResourceFile("META-INF/clientKeystore.properties"));
      ctx.put(appendIssuedTokenSuffix(SecurityConstants.ENCRYPT_USERNAME), "mystskey");
      ctx.put(appendIssuedTokenSuffix(SecurityConstants.STS_TOKEN_USERNAME), "myclientkey");
      ctx.put(appendIssuedTokenSuffix(SecurityConstants.STS_TOKEN_PROPERTIES), getResourceFile("META-INF/clientKeystore.properties"));
      ctx.put(appendIssuedTokenSuffix(SecurityConstants.STS_TOKEN_USE_CERT_FOR_KEYINFO), "true");

      ctx.put(SecurityConstants.STS_CLIENT, stsClient);
   }

   public static void setupWsseAndSTSClientHolderOfKey(BindingProvider proxy, Bus bus) {

      Map<String, Object> ctx = proxy.getRequestContext();

      STSClient stsClient = new STSClient(bus);

      ctx.put(SecurityConstants.CALLBACK_HANDLER, new ClientCallbackHandler());
      ctx.put(SecurityConstants.SIGNATURE_PROPERTIES, getResourceFile("META-INF/clientKeystore.properties"));
      ctx.put(SecurityConstants.ENCRYPT_PROPERTIES, getResourceFile("META-INF/clientKeystore.properties"));
      ctx.put(SecurityConstants.SIGNATURE_USERNAME, "myclientkey");
      ctx.put(SecurityConstants.ENCRYPT_USERNAME, "myservicekey");
      ctx.put(appendIssuedTokenSuffix(SecurityConstants.USERNAME), "alice");
      ctx.put(appendIssuedTokenSuffix(SecurityConstants.CALLBACK_HANDLER), new ClientCallbackHandler());
      ctx.put(appendIssuedTokenSuffix(SecurityConstants.ENCRYPT_PROPERTIES), getResourceFile("META-INF/clientKeystore.properties"));
      ctx.put(appendIssuedTokenSuffix(SecurityConstants.ENCRYPT_USERNAME), "mystskey");
      ctx.put(appendIssuedTokenSuffix(SecurityConstants.STS_TOKEN_USERNAME), "myclientkey");
      ctx.put(appendIssuedTokenSuffix(SecurityConstants.STS_TOKEN_PROPERTIES), getResourceFile("META-INF/clientKeystore.properties"));
      ctx.put(appendIssuedTokenSuffix(SecurityConstants.STS_TOKEN_USE_CERT_FOR_KEYINFO), "true");

      ctx.put(SecurityConstants.STS_CLIENT, stsClient);
   }

   private static String appendIssuedTokenSuffix(String prop)
   {
      return prop + ".it";
   }

    /**
     * Create and configure an STSClient for use by service ServiceImpl.
     *
     * Whenever an "<sp:IssuedToken>" policy is configured on a WSDL port, as is the
     * case for ServiceImpl, a STSClient must be created and configured in
     * order for the service to connect to the STS-server to obtain a token.
     *
     * @param bus
     * @param stsWsdlLocation
     * @param stsService
     * @param stsPort
     * @return
     */
   private static STSClient createSTSClient(Bus bus, String stsWsdlLocation, QName stsService, QName stsPort){
      STSClient stsClient = new STSClient(bus);
      if (stsWsdlLocation != null) {
         stsClient.setWsdlLocation(stsWsdlLocation);
         stsClient.setServiceQName(stsService);
         stsClient.setEndpointQName(stsPort);
      }
      Map<String, Object> props = stsClient.getProperties();
      props.put(SecurityConstants.USERNAME, "alice");
      props.put(SecurityConstants.CALLBACK_HANDLER, new ClientCallbackHandler());
      props.put(SecurityConstants.ENCRYPT_PROPERTIES, getResourceFile("META-INF/clientKeystore.properties"));
      props.put(SecurityConstants.ENCRYPT_USERNAME, "mystskey");
      props.put(SecurityConstants.STS_TOKEN_USERNAME, "myclientkey");
      props.put(SecurityConstants.STS_TOKEN_PROPERTIES, getResourceFile("META-INF/clientKeystore.properties"));
      props.put(SecurityConstants.STS_TOKEN_USE_CERT_FOR_KEYINFO, "true");
      return stsClient;
   }

   private static void setServiceContextAttributes(Map<String, Object> ctx){
      ctx.put(SecurityConstants.CALLBACK_HANDLER, new ClientCallbackHandler());
      ctx.put(SecurityConstants.SIGNATURE_PROPERTIES, getResourceFile("META-INF/clientKeystore.properties"));
      ctx.put(SecurityConstants.ENCRYPT_PROPERTIES, getResourceFile("META-INF/clientKeystore.properties"));
      ctx.put(SecurityConstants.SIGNATURE_USERNAME, "myclientkey");
      ctx.put(SecurityConstants.ENCRYPT_USERNAME, "myservicekey");
   }

   private static URL getResourceFile(String file) {
      File resFile = new File("src/test/resources/" + file);
      if (resFile.exists()) {
         try {
            return resFile.toURI().toURL();
         } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Failed to get URL from resource file " + resFile.getAbsolutePath());
         }
      }
      throw new IllegalArgumentException("Resource file " + file + " doesn't exist");

   }
}
