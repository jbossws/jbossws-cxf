/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.stack.cxf.configuration;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;

/**
 * A StreamReaderDelegate that expands system property references in element and attribute values.
 * 
 */
public class SysPropExpandingStreamReader extends StreamReaderDelegate
{
   public static final String DELIMITER = "@";

   public SysPropExpandingStreamReader(XMLStreamReader reader)
   {
      super(reader);
   }

   protected String expandSystemProperty(String value)
   {
      if (isEmpty(value))
      {
         return value;
      }
      final int startIndx = value.indexOf(DELIMITER);
      if (startIndx > -1)
      {
         final int endIndx = value.lastIndexOf(DELIMITER);
         if (endIndx > -1 && startIndx + 1 < endIndx)
         {
            final String propName = value.substring(startIndx + 1, endIndx);
            if (!isEmpty(propName))
            {
               final String envValue = System.getProperty(propName);
               if (!isEmpty(envValue))
               {
                  StringBuilder sb = new StringBuilder();
                  sb.append(value.substring(0, startIndx));
                  sb.append(envValue);
                  sb.append(value.substring(endIndx + 1));
                  value = sb.toString();
               }
            }
         }
      }

      return value;
   }

   private static boolean isEmpty(String str)
   {
      if (str != null)
      {
         int len = str.length();
         for (int x = 0; x < len; ++x)
         {
            if (str.charAt(x) > ' ')
            {
               return false;
            }
         }
      }
      return true;
   }

   @Override
   public String getElementText() throws XMLStreamException
   {
      return expandSystemProperty(super.getElementText());
   }

   @Override
   public String getAttributeValue(String namespaceURI, String localName)
   {
      return expandSystemProperty(super.getAttributeValue(namespaceURI, localName));
   }

   @Override
   public String getAttributeValue(int index)
   {
      return expandSystemProperty(super.getAttributeValue(index));
   }

   @Override
   public String getText()
   {
      return expandSystemProperty(super.getText());
   }
}
