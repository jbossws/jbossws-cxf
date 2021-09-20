/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
