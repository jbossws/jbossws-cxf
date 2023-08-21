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
package org.jboss.test.ws.jaxws.samples.xop.doclit;

import java.awt.Image;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import jakarta.activation.DataHandler;
import javax.xml.transform.Source;
import jakarta.xml.ws.soap.SOAPBinding;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.wsf.test.XOPTestSupport;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.Test;

/**
 * User: hbraun
 * Date: 08.12.2006
 */
public abstract class XOPBase extends JBossWSTest
{
   private static final String FS = System.getProperty("file.separator"); // '/' on unix, '\' on windows
   private File imgFile = getResourceFile("jaxws" + FS + "samples" + FS + "xop" + FS + "shared" + FS + "attach.jpeg");

   protected MTOMEndpoint port;
   protected SOAPBinding binding;

   protected MTOMEndpoint getPort()
   {
      return port;
   }

   protected SOAPBinding getBinding()
   {
      return binding;
   }

   /**
    * Marshalling/Unmarshalling of DataHandler types is different
    * when handlers are in place.
    * @throws Exception
    */
   public abstract void testDataHandlerRoundtrip() throws Exception;

   /**
    * Marshalling/Unmarshalling of DataHandler types is different
    * when handlers are in place.
    * @throws Exception
    */
   public abstract void testDataHandlerResponseOptimzed() throws Exception;

   @Test
   @RunAsClient
   public void testImgRoundtrip() throws Exception
   {
      assertTrue("Cannot find: " + imgFile, imgFile.exists());

      getBinding().setMTOMEnabled(true);

      Image img = XOPTestSupport.createTestImage(imgFile);
      if (img != null) // might fail on unix
      {
         ImageRequest request = new ImageRequest();
         request.setData(img);

         ImageResponse response = getPort().echoImage(request);

         assertNotNull(response);
         assertTrue(response.getData() instanceof Image);
      }
   }

   @Test
   @RunAsClient
   public void testImgResponseOptimized() throws Exception
   {
      assertTrue("Cannot find: " + imgFile, imgFile.exists());

      getBinding().setMTOMEnabled(false);

      Image img = XOPTestSupport.createTestImage(imgFile);

      if (img != null) // might fail on unix
      {
         ImageRequest request = new ImageRequest();
         request.setData(img);

         ImageResponse response = getPort().echoImage(request);

         assertNotNull(response);
         assertTrue(response.getData() instanceof Image);
      }
   }

   @Test
   @RunAsClient
   public void testSourceRoundtrip() throws Exception
   {
      getBinding().setMTOMEnabled(true);

      Source src = XOPTestSupport.createTestSource();
      SourceRequest request = new SourceRequest();
      request.setData(src);

      SourceResponse response = getPort().echoSource(request);

      assertNotNull(response);
      assertTrue(response.getData() instanceof Source);
   }

   @Test
   @RunAsClient
   public void testSourceResponseOptimized() throws Exception
   {
      getBinding().setMTOMEnabled(false);

      Source src = XOPTestSupport.createTestSource();
      SourceRequest request = new SourceRequest();
      request.setData(src);

      SourceResponse response = getPort().echoSource(request);

      assertNotNull(response);
      assertTrue(response.getData() instanceof Source);
   }

   protected Object getContent(DataHandler dh) throws IOException
   {
      Object content = dh.getContent();

      // Metro returns an ByteArrayInputStream
      if (content instanceof InputStream)
      {
         try
         {
            BufferedReader br = new BufferedReader(new InputStreamReader((InputStream)content));
            return br.readLine();
         }
         finally
         {
            ((InputStream)content).close();
         }
      }
      return content;
   }
}
