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
package org.jboss.test.ws.jaxws.benchmark.test.datatypes.wrapped.pojo;

import org.jboss.test.ws.jaxws.benchmark.test.datatypes.DataTypesPOJOWrappedDocTest;
import org.jboss.test.ws.jaxws.benchmark.test.datatypes.EndpointWrappedDoc;

/**
 * @author pmacik@redhat.com
 * @since 25-Mar-2010
 */
public class ValuesToStringDocTest extends DataTypesPOJOWrappedDocTest
{

   public void performIteration(Object port) throws Exception
   {

      String ret = ((EndpointWrappedDoc) port).valuesToString(testedByte, testedByteArray, testedShort, testedInt,
            testedLong, testedFloat, testedDouble, testedString, testedQName, testedDate, testedCalendar);
      if (!ret.equals(expectedValuesString))
      {
         throw new Exception("Unexpected result: " + ret + "\nExpected:" + expectedValuesString);
      }

   }
}
