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
package org.jboss.test.ws.jaxrpc.samples.exception;

/**
 * JAX-RPC 1.1 WSDLFault
 * 
 * A service specific Java exception (mapped from a wsdl:fault and the corresponding
 * wsdl:message) extends the class java.lang.Exception directly or indirectly.
 * 
 * The single message part in the wsdl:message (referenced from the wsdl:fault
 * element) may be either a type or an element. If the former, it can be either a
 * xsd:complexType or a simple XML type.
 * 
 * Each element inside the xsd:complexType is mapped to a getter method and a
 * parameter in the constructor of the Java exception. Mapping of these elements follows
 * the standard XML to Java type mapping.
 * 
 * @author Thomas.Diesler@jboss.com
 */
public class ComplexUserArrayException extends Exception
{
   private int[] errorCodes;

   /** Constructor used by wscompile */
   public ComplexUserArrayException(String message, int[] errorCodes)
   {
      super(message);
      this.errorCodes = errorCodes;
   }

   /** Constructor used by the marshalling layer */
   public ComplexUserArrayException(String message)
   {
      super(message);
   }

   public int[] getErrorCodes()
   {
      return errorCodes;
   }

   /* http://jira.jboss.org/jira/browse/JBWS-714
    * Read-only array properties not supported for service specific exceptions
   public void setErrorCodes(int[] errorCodes)
   {
      this.errorCodes = errorCodes;
   }
   */
}
