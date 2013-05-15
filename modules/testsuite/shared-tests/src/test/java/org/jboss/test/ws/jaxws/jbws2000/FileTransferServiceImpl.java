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
package org.jboss.test.ws.jaxws.jbws2000;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.activation.DataHandler;
import javax.ejb.Stateless;
import javax.jws.WebService;

@Stateless
@WebService(
  endpointInterface = "org.jboss.test.ws.jaxws.jbws2000.FileTransferService",
  name = "FileTransfer",
  targetNamespace = "http://service.mtom.test.net/"
)
public class FileTransferServiceImpl implements FileTransferService {

   public boolean transferFile(String fileName, DataHandler contents) {
      final List<File> tempFiles = new ArrayList<File>();
      final File deploymentTempDirectory = getTempDirectory();
      try {
         FileOutputStream fileOutputStream = null;
         try {
            final File outputFile = new File(deploymentTempDirectory, fileName);

            System.out.println("Write file '"+fileName+"' to dir " + deploymentTempDirectory);
            
            fileOutputStream = new FileOutputStream(outputFile);
            contents.writeTo(fileOutputStream);
            tempFiles.add(outputFile);
            outputFile.deleteOnExit();
         } finally {
            if (fileOutputStream != null) {
               fileOutputStream.close();
            }
         }

         return true;
      } catch (Exception e) {
         throw new RuntimeException("Failed to schedule deployment", e);
      }
   }

   private File getTempDirectory() {
      final File deploymentTempDirectory = new File(System.getProperty("java.io.tmpdir"), Long.toString(System.currentTimeMillis()));
      deploymentTempDirectory.mkdir();
      return deploymentTempDirectory;
   }
}
