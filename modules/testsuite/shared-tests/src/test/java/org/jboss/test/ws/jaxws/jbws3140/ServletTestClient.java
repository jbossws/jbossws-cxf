/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws3140;

import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
