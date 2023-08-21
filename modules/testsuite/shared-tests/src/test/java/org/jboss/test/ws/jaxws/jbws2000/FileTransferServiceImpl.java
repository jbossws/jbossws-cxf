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
package org.jboss.test.ws.jaxws.jbws2000;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import jakarta.activation.DataHandler;
import jakarta.ejb.Stateless;
import jakarta.jws.WebService;

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
