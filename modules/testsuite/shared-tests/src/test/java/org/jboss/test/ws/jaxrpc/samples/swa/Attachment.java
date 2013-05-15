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
package org.jboss.test.ws.jaxrpc.samples.swa;

import java.awt.Image;
import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.mail.internet.MimeMultipart;
import javax.xml.transform.Source;

/**
 * Service Endpoint Interface for the MIME mapping required by JAXRPC-1.1
 *
 * image/gif         java.awt.Image
 * image/jpeg        java.awt.Image
 * text/plain        java.lang.String
 * multipart/*       javax.mail.internet.MimeMultipart
 * text/xml          javax.xml.transform.Source
 * application/xml   javax.xml.transform.Source
 *
 * @author Thomas.Diesler@jboss.org
 * @since Nov 17, 2004
 */
public interface Attachment extends Remote
{
   /** Service endpoint method for image/gif
    */
   String sendMimeImageGIF(String message, Object mimepart) throws RemoteException;

   /** Service endpoint method for image/jpeg
    */
   String sendMimeImageJPEG(String message, Image mimepart) throws RemoteException;

   /** Service endpoint method for text/plain
    */
   String sendMimeTextPlain(String message, String mimepart) throws RemoteException;

   /** Service endpoint method for multipart/*
    */
   String sendMimeMultipart(String message, MimeMultipart mimepart) throws RemoteException;

   /** Service endpoint method for text/xml
    */
   String sendMimeTextXML(String message, Object mimepart) throws RemoteException;

   /** Service endpoint method for application/xml
    */
   String sendMimeApplicationXML(String message, Source mimepart) throws RemoteException;
}
