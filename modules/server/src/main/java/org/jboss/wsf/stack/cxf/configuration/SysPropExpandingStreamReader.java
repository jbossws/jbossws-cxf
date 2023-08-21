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

   @Override
   public String getElementText() throws XMLStreamException
   {
      return SysPropUtils.expandSystemProperty(super.getElementText());
   }

   @Override
   public String getAttributeValue(String namespaceURI, String localName)
   {
      return SysPropUtils.expandSystemProperty(super.getAttributeValue(namespaceURI, localName));
   }

   @Override
   public String getAttributeValue(int index)
   {
      return SysPropUtils.expandSystemProperty(super.getAttributeValue(index));
   }

   @Override
   public String getText()
   {
      return SysPropUtils.expandSystemProperty(super.getText());
   }
}
