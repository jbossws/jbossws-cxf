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

import java.io.InputStream;
import java.util.ResourceBundle;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * StAX utils
 * 
 * @author alessio.soldano@jboss.com
 * @since 27-Nov-2010
 */
public class StAXUtils
{
   private static final BlockingQueue<XMLInputFactory> INPUT_FACTORY_POOL;

   static
   {
      int i = 10;
      try
      {
         String s = System.getProperty("org.jboss.ws.staxutils.pool-size", "10");
         i = Integer.parseInt(s);
      }
      catch (Throwable t)
      {
         i = 10;
      }
      if (i <= 0)
      {
         i = 10;
      }
      INPUT_FACTORY_POOL = new LinkedBlockingQueue<XMLInputFactory>(i);
   }

   /**
    * Return a new factory so that the caller can set sticky parameters.
    * @param nsAware
    * @return
    */
   public static XMLInputFactory createXMLInputFactory(boolean nsAware)
   {
      XMLInputFactory factory = XMLInputFactory.newInstance();
      factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, nsAware);
      factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
      factory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE);
      factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
      factory.setXMLResolver(new XMLResolver()
      {
         public Object resolveEntity(String publicID, String systemID, String baseURI, String namespace)
               throws XMLStreamException
         {
            throw new XMLStreamException("READING_EXTERNAL_ENTITIES_IS_DISABLED");
         }
      });
      return factory;
   }

   private static XMLInputFactory getXMLInputFactory()
   {
      XMLInputFactory f = INPUT_FACTORY_POOL.poll();
      if (f == null)
      {
         f = createXMLInputFactory(true);
      }
      return f;
   }

   private static void returnXMLInputFactory(XMLInputFactory factory)
   {
      INPUT_FACTORY_POOL.offer(factory);
   }

   public static XMLStreamReader createXMLStreamReader(InputStream in)
   {
      XMLInputFactory factory = getXMLInputFactory();
      try
      {
         return factory.createXMLStreamReader(in);
      }
      catch (XMLStreamException e)
      {
         throw new RuntimeException("Could not parse stream",  e);
      }
      finally
      {
         returnXMLInputFactory(factory);
      }
   }

   public static boolean match(XMLStreamReader reader, QName name)
   {
      return reader.getName().equals(name);
   }
   
   public static boolean match(XMLStreamReader reader, String namespace, String localName)
   {
      QName name = reader.getName();
      return localName.equals(name.getLocalPart()) && namespace.equals(name.getNamespaceURI());
   }
   
   public static boolean match(XMLStreamReader reader, String[] namespaces, String[] localNames)
   {
      QName name = reader.getName();
      boolean matchedNS = false;
      for (String n : namespaces) {
         if (!matchedNS && n.equals(name.getNamespaceURI())) {
            matchedNS = true;
         }
      }
      if (matchedNS) {
         for (String l : localNames) {
            if (l.equals(name.getLocalPart())) {
               return true;
            }
         }
      }
      return false;
   }

   public static String elementAsString(XMLStreamReader reader) throws XMLStreamException
   {
      String elementtext = reader.getElementText();
      return elementtext == null ? null : elementtext.trim();
   }

   public static QName elementAsQName(XMLStreamReader reader) throws XMLStreamException
   {
      String text = elementAsString(reader);
      return stringToQName(reader, text, reader.getNamespaceURI());
   }
   
   public static boolean elementAsBoolean(XMLStreamReader reader) throws XMLStreamException
   {
      String text = elementAsString(reader);
      return Boolean.parseBoolean(text);
   }
   
   public static int elementAsInt(XMLStreamReader reader) throws XMLStreamException
   {
      String text = elementAsString(reader);
      return Integer.parseInt(text);
   }
   
   public static QName attributeAsQName(XMLStreamReader reader, String namespace, String localName) throws XMLStreamException
   {
      String text = reader.getAttributeValue(namespace, localName);
      return stringToQName(reader, text, reader.getNamespaceURI());
   }
   
   public static QName attributeAsQName(XMLStreamReader reader, String namespace, String localName, String targetNS) throws XMLStreamException
   {
      String text = reader.getAttributeValue(namespace, localName);
      return stringToQName(reader, text, targetNS);
   }
   
   private static QName stringToQName(XMLStreamReader reader, String text, String defaultNS)
   {
      String localPart = text.substring(text.indexOf(':') + 1, text.length());
      int i = text.indexOf(':');
      String prefix = i < 0 ? null : text.substring(0, i);
      String namespaceURI = prefix == null ? defaultNS : reader.getNamespaceURI(prefix);
      return prefix == null ? new QName(namespaceURI, localPart) : new QName(namespaceURI, localPart, prefix);
   }
   
   public static int nextElement(XMLStreamReader reader)
   {
      try
      {
         int x = reader.next();
         while (x != XMLStreamReader.START_ELEMENT && x != XMLStreamReader.END_ELEMENT && reader.hasNext())
         {
            x = reader.next();
         }
         return x;
      }
      catch (XMLStreamException e)
      {
         throw new RuntimeException("COULDN'T_PARSE_STREAM",  e);
      }
   }
}
