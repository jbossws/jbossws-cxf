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
package org.jboss.test.ws.jaxws.jbws3140;

import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.naming.InitialContext;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ServletTestClient extends HttpServlet
{
   private static final long serialVersionUID = 1L;
   public static StringBuffer resultTrace = new StringBuffer();
   MTOMTest mtomTestPort = null;

   public void init(ServletConfig config) throws ServletException
   {
      super.init(config);
      try
      {
         InitialContext ctx = new InitialContext();
         mtomTestPort = (MTOMTest) ctx.lookup("java:comp/env/service/mtomTest");

      }
      catch (Exception e)
      {
         System.err.println("ServletClient:init() Exception: " + e);
         e.printStackTrace();
      }
   }

   public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
   {
      resultTrace = new StringBuffer();
      String reqParas[] = req.getParameterValues("mtom");
      if (reqParas != null && reqParas[0].equals("large")) 
      {
         doMtom(req, res, true);        
      }
      if (reqParas != null && reqParas[0].equals("small")) 
      {
         doMtom(req, res, false);        
      }
      
   }


   public void doMtom(HttpServletRequest req, HttpServletResponse res, boolean largeImage) throws ServletException, IOException
   {  
      try
      {
         String jpgName = largeImage ? "large.jpg" : "small.jpg"; 
         URL url = this.getClass().getClassLoader().getResource(jpgName);
         InputStream ins = url.openStream();
         Image image = ImageIO.read(ins);
         DataType request= new DataType();
         request.setDoc(image);
         request.setRequest("");
         ResponseType responseType = mtomTestPort.mtomIn(request);
         resultTrace.append(responseType.getResponse());
         ins.close();
         res.getOutputStream().println(resultTrace.toString());
      }
      catch (Throwable e)
      {
         e.printStackTrace(res.getWriter());
      }
      
   }
}
