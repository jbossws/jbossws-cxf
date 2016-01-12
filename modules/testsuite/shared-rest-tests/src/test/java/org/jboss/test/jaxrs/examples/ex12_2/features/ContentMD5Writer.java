/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.jaxrs.examples.ex12_2.features;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

import org.jboss.ws.common.utils.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 */
public class ContentMD5Writer implements WriterInterceptor
{
   @Override
   public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException
   {
      MessageDigest digest = null;
      try
      {
         digest = MessageDigest.getInstance("MD5");
      }
      catch (NoSuchAlgorithmException e)
      {
         throw new IllegalArgumentException(e);
      }
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      DigestOutputStream digestStream = new DigestOutputStream(buffer, digest);
      OutputStream old = context.getOutputStream();
      context.setOutputStream(digestStream);

      try
      {
         context.proceed();

         byte[] hash = digest.digest();
         String encodedHash = Base64.encodeBytes(hash);
         context.getHeaders().putSingle("Content-MD5", encodedHash);

         byte[] content = buffer.toByteArray();
         old.write(content);
      }
      finally
      {
         context.setOutputStream(old);
      }
   }
}