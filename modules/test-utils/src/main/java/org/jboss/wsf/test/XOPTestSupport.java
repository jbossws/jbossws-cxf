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

import javax.activation.DataHandler;
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
