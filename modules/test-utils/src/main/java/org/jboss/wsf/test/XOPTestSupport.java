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

import java.awt.Image;
import java.awt.Toolkit;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import jakarta.activation.DataHandler;
import javax.xml.transform.stream.StreamSource;

import org.jboss.ws.common.IOUtils;

/**
 * @author Heiko Braun <heiko.braun@jboss.com>
 * @since 22-Sep-2006
 */
public class XOPTestSupport
{

   public static byte[] getBytesFromFile(File file) throws IOException
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      IOUtils.copyStream(baos, new FileInputStream(file));
      return baos.toByteArray();
   }

   public static Image createTestImage(File imgFile)
   {
      Image image = null;
      try
      {
         URL url = imgFile.toURI().toURL();

         image = null;
         try
         {
            image = Toolkit.getDefaultToolkit().createImage(url);
         }
         catch (Throwable th)
         {
            //log.warn("Cannot create Image: " + th);
         }
      }
      catch (MalformedURLException e)
      {
         throw new RuntimeException(e);
      }

      return image;
   }

   public static StreamSource createTestSource()
   {
      return new StreamSource(new ByteArrayInputStream("<some><nestedXml/></some>".getBytes(StandardCharsets.UTF_8)));
   }

   public static DataHandler createDataHandler(File imgFile)
   {
      try
      {
         URL url = imgFile.toURI().toURL();
         return new DataHandler(url);
      }
      catch (MalformedURLException e)
      {
         throw new RuntimeException(e);
      }
   }
}
