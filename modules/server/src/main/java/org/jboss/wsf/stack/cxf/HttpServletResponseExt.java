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
package org.jboss.wsf.stack.cxf;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * A HttpServletResponse delegate that externalizes fields.
 * 
 * @author alessio.soldano@jboss.com
 * @since 17-Jul-2009
 */
public class HttpServletResponseExt implements HttpServletResponse
{
   private HttpServletResponse delegate;
   private int sc;
   
   public HttpServletResponseExt(HttpServletResponse delegate)
   {
      this.delegate = delegate;
   }
   
   /**
    * Get the status currently set in the HttpServletResponse
    * 
    * @return the http status
    */
   public int getStatus()
   {
      return this.sc;
   }

   /* HttpServletResponse API */
   
   public void addCookie(Cookie cookie)
   {
      delegate.addCookie(cookie);
   }

   public void addDateHeader(String name, long date)
   {
      delegate.addDateHeader(name, date);
   }

   public void addHeader(String name, String value)
   {
      delegate.addHeader(name, value);
   }

   public void addIntHeader(String name, int value)
   {
      delegate.addIntHeader(name, value);
   }

   public boolean containsHeader(String name)
   {
      return delegate.containsHeader(name);
   }

   public String encodeRedirectURL(String url)
   {
      return delegate.encodeRedirectURL(url);
   }

   @Deprecated
   public String encodeRedirectUrl(String url)
   {
      return delegate.encodeRedirectUrl(url);
   }

   public String encodeURL(String url)
   {
      return delegate.encodeURL(url);
   }

   @Deprecated
   public String encodeUrl(String url)
   {
      return delegate.encodeUrl(url);
   }

   public void sendError(int sc) throws IOException
   {
      delegate.sendError(sc);
   }

   public void sendError(int sc, String msg) throws IOException
   {
      delegate.sendError(sc, msg);
   }

   public void sendRedirect(String location) throws IOException
   {
      delegate.sendRedirect(location);
   }

   public void setDateHeader(String name, long date)
   {
      delegate.setDateHeader(name, date);
   }

   public void setHeader(String name, String value)
   {
      delegate.setHeader(name, value);
   }

   public void setIntHeader(String name, int value)
   {
      delegate.setIntHeader(name, value);
   }

   public void setStatus(int sc)
   {
      delegate.setStatus(sc);
      this.sc = sc;
   }

   @Deprecated
   public void setStatus(int sc, String sm)
   {
      delegate.setStatus(sc, sm);
      this.sc = sc;
   }
   
   public void flushBuffer() throws IOException
   {
      delegate.flushBuffer();
   }

   public int getBufferSize()
   {
      return delegate.getBufferSize();
   }

   public String getCharacterEncoding()
   {
      return delegate.getCharacterEncoding();
   }

   public String getContentType()
   {
      return delegate.getContentType();
   }

   public Locale getLocale()
   {
      return delegate.getLocale();
   }

   public ServletOutputStream getOutputStream() throws IOException
   {
      return delegate.getOutputStream();
   }

   public PrintWriter getWriter() throws IOException
   {
      return delegate.getWriter();
   }

   public boolean isCommitted()
   {
      return delegate.isCommitted();
   }

   public void reset()
   {
      delegate.reset();
   }

   public void resetBuffer()
   {
      delegate.resetBuffer();
   }

   public void setBufferSize(int size)
   {
      delegate.setBufferSize(size);
   }

   public void setCharacterEncoding(String charset)
   {
      delegate.setCharacterEncoding(charset);
   }

   public void setContentLength(int len)
   {
      delegate.setContentLength(len);
   }

   public void setContentType(String type)
   {
      delegate.setContentType(type);
   }

   public void setLocale(Locale loc)
   {
      delegate.setLocale(loc);
   }
   
}
