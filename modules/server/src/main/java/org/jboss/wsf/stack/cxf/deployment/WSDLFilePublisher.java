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
package org.jboss.wsf.stack.cxf.deployment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.cxf.Bus;
import org.apache.cxf.helpers.FileUtils;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.staxutils.StaxUtils;
import org.apache.cxf.wsdl.WSDLManager;
import org.apache.cxf.wsdl11.ServiceWSDLBuilder;
import org.jboss.ws.common.utils.AbstractWSDLFilePublisher;
import org.jboss.wsf.spi.deployment.ArchiveDeployment;
import org.jboss.wsf.stack.cxf.i18n.Loggers;
import org.jboss.wsf.stack.cxf.i18n.Messages;
import org.w3c.dom.Document;
import org.jboss.logging.Logger;

/**
 * A WSDL file publisher for CXF based stack
 * 
 * @author alessio.soldano@jboss.com
 * @since 25-Mar-2010
 *
 */
public class WSDLFilePublisher extends AbstractWSDLFilePublisher
{
   private static final String[] wsdlLocationPrefixes = {"vfsfile:", "file:", "jar:", "vfszip:"};
   
   public WSDLFilePublisher(ArchiveDeployment dep)
   {
      super(dep);
   }
   
   /** Publish the deployed wsdl file to the data directory
    */
   public void publishWsdlFiles(QName serviceName, String wsdlLocation, Bus bus, List<ServiceInfo> serviceInfos) throws IOException
   {
      String deploymentName = dep.getCanonicalName();
      File wsdlFile = getPublishLocation(serviceName.getLocalPart(), deploymentName, wsdlLocation);
      if (wsdlFile == null) return;
      createParentDir(wsdlFile);
      try
      {
         // Write the wsdl def to file
         ServiceWSDLBuilder builder = new ServiceWSDLBuilder(bus, serviceInfos);
         Definition def = builder.build();

         Document doc = getWsdlDocument(bus, def);
         writeDocument(doc, wsdlFile);

         URL wsdlPublishURL = new URL(URLDecoder.decode(wsdlFile.toURI().toURL().toExternalForm(), "UTF-8"));
         Loggers.DEPLOYMENT_LOGGER.wsdlFilePublished(wsdlPublishURL);

         // Process the wsdl imports
         if (def != null)
         {
            List<String> published = new LinkedList<String>();
            String expLocation = getExpLocation(wsdlLocation);
            publishWsdlImports(wsdlPublishURL, def, published, expLocation);

            // Publish XMLSchema imports
            publishSchemaImports(wsdlPublishURL, doc.getDocumentElement(), published, expLocation);

            dep.addAttachment(WSDLFilePublisher.class, this);
         }
         else
         {
            throw Messages.MESSAGES.wsdl20NotSupported();
         }
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception e)
      {
         throw Messages.MESSAGES.cannotPublishWSDLTo(serviceName, wsdlFile, e);
      }
   }

   public void unpublishWsdlFiles()
   {
      try
      {
         File parentDir = new File(serverConfig.getServerDataDir().getCanonicalPath() + "/wsdl");
         ArchiveDeployment deployment = dep;
         while (deployment.getParent() != null)
         {
            deployment = deployment.getParent();
         }
         String deploymentName = deployment.getCanonicalName();
         if (deploymentName.startsWith("http://"))
         {
            deploymentName = deploymentName.replace("http://", "http-");
         }
         File targetDir = new File(parentDir, deploymentName);
         FileUtils.removeDir(targetDir);
      }
      catch (IOException e)
      {
         Loggers.DEPLOYMENT_LOGGER.couldNotCreateWsdlDataPath();
      }
   }

   private static Document getWsdlDocument(Bus bus, Definition def) throws WSDLException
   {
      WSDLWriter wsdlWriter = bus.getExtension(WSDLManager.class).getWSDLFactory().newWSDLWriter();
      def.setExtensionRegistry(bus.getExtension(WSDLManager.class).getExtensionRegistry());
      return wsdlWriter.getDocument(def);
   }
   
   private static void writeDocument(Document doc, File file) throws IOException, XMLStreamException
   {
      String enc = null;
      try
      {
         enc = doc.getXmlEncoding();
      }
      catch (Exception ex)
      {
         //ignore - not dom level 3
         Logger.getLogger(WSDLFilePublisher.class).trace(ex);
      }
      if (enc == null)
      {
         enc = "utf-8";
      }
      FileOutputStream fos = new FileOutputStream(new File(file.toURI()));
      try
      {
         XMLStreamWriter writer = StaxUtils.createXMLStreamWriter(fos, enc);
         StaxUtils.writeNode(doc, writer, true);
         writer.flush();
      }
      finally
      {
         fos.close();
      }
   }
   
   /**
    * Get the file publish location
    */
   private File getPublishLocation(String serviceName, String archiveName, String wsdlLocation) throws IOException
   {
      if (wsdlLocation == null && serviceName == null)
      {
         Loggers.DEPLOYMENT_LOGGER.cannotGetWsdlPublishLocation();
         return null;
      }

      //JBWS-2829: windows issue
      if (archiveName.startsWith("http://"))
      {
         archiveName = archiveName.replace("http://", "http-");
      }

      File locationFile = new File(serverConfig.getServerDataDir().getCanonicalPath() + "/wsdl/" + archiveName);

      if (wsdlLocation != null && wsdlLocation.indexOf(expLocation) >= 0)
      {
         wsdlLocation = wsdlLocation.substring(wsdlLocation.indexOf(expLocation) + expLocation.length());
         return new File(locationFile + "/" + wsdlLocation);
      }
      else if (wsdlLocation != null && !wsdlLocation.startsWith("vfs:"))
      {
         for (String wsdlLocationPrefix : wsdlLocationPrefixes) {
            if (wsdlLocation.startsWith(wsdlLocationPrefix)) {
               return new File(locationFile, encodeLocation(wsdlLocation.substring(wsdlLocationPrefix.length(), wsdlLocation.lastIndexOf("/") + 1)));
            }
         }
         return new File(locationFile, encodeLocation(wsdlLocation));
      }
      else
      {
         return new File(locationFile + "/" + serviceName + ".wsdl");
      }
   }
   
   private String encodeLocation(String location) throws UnsupportedEncodingException {
      StringBuilder sb = new StringBuilder();
      StringTokenizer st = new StringTokenizer(location, "/", false);
      List<String> l = new ArrayList<String>();
      while (st.hasMoreTokens()) {
         l.add(URLEncoder.encode(st.nextToken(), "UTF-8"));
      }
      final int size = l.size();
      for (int i = 0; i < size; i++) {
         sb.append(l.get(i));
         if (i < size - 1) {
            sb.append("/");
         }
      }
      return sb.toString();
   }
   
   private String getExpLocation(String wsdlLocation) {
      if (wsdlLocation == null || wsdlLocation.indexOf(expLocation) >= 0) {
         return expLocation;
      } else {
         //JBWS-3540
         return wsdlLocation.startsWith("vfs:") && wsdlLocation.contains("/") ? wsdlLocation.substring(0, wsdlLocation.lastIndexOf("/") + 1) : "";
      }
   }
}
