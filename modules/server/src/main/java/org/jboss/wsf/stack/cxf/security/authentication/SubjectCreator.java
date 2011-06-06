/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.stack.cxf.security.authentication;

import java.security.Principal;
import java.util.Calendar;
import java.util.TimeZone;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;

import org.apache.cxf.common.security.SimplePrincipal;
import org.jboss.logging.Logger;
import org.jboss.security.auth.callback.CallbackHandlerPolicyContextHandler;
import org.jboss.ws.common.utils.DelegateClassLoader;
import org.jboss.wsf.spi.classloading.ClassLoaderProvider;
import org.jboss.wsf.spi.security.SecurityDomainContext;
import org.jboss.wsf.stack.cxf.security.authentication.callback.UsernameTokenCallbackHandler;
import org.jboss.wsf.stack.cxf.security.nonce.NonceStore;

/**
 * Creates Subject instances after having authenticated / authorized the provided
 * user against the specified SecurityDomainContext.
 * 
 * @author alessio.soldano@jboss.com
 * @author Sergey Beryozkin
 *
 */
public class SubjectCreator
{
   private static final Logger log = Logger.getLogger(SubjectCreator.class);

   private static final int TIMESTAMP_FRESHNESS_THRESHOLD = 300;

   private boolean propagateContext;

   private int timestampThreshold = TIMESTAMP_FRESHNESS_THRESHOLD;

   private NonceStore nonceStore;

   private boolean decodeNonce = true;
   
   public Subject createSubject(SecurityDomainContext ctx, String name, String password, boolean isDigest, String nonce, String created)
   {
      if (isDigest)
      {
         verifyUsernameToken(nonce, created);
         // It is not possible at the moment to figure out if the digest has been created 
         // using the original nonce bytes or the bytes of the (Base64)-encoded nonce, some 
         // legacy clients might use the (Base64)-encoded nonce bytes when creating a digest; 
         // lets default to true and assume the nonce has been Base-64 encoded, given that 
         // WSS4J client Base64-decodes the nonce before creating the digest

         CallbackHandler handler = new UsernameTokenCallbackHandler(nonce, created, decodeNonce);
         CallbackHandlerPolicyContextHandler.setCallbackHandler(handler);
      }

      // authenticate and populate Subject
      

      Principal principal = new SimplePrincipal(name);
      Subject subject = new Subject();

      boolean TRACE = log.isTraceEnabled();
      if (TRACE)
         log.trace("About to authenticate, using security domain '" + ctx.getSecurityDomain() + "'");

      try
      {
         ClassLoader tccl = SecurityActions.getContextClassLoader();
         //allow PicketBox to see jbossws modules' classes
         SecurityActions.setContextClassLoader(new DelegateClassLoader(ClassLoaderProvider.getDefaultProvider().getServerIntegrationClassLoader(), tccl));
         try
         {
            if (ctx.isValid(principal, password, subject) == false)
            {
               String msg = "Authentication failed, principal=" + principal.getName();
               log.error(msg);
               throw new SecurityException(msg);
            }
         }
         finally
         {
            SecurityActions.setContextClassLoader(tccl);
         }
      }
      finally
      {
         if (isDigest)
         {
            // does not remove the TL entry completely but limits the potential
            // growth to a number of available threads in a container 
            CallbackHandlerPolicyContextHandler.setCallbackHandler(null);
         }
      }

      if (TRACE)
         log.trace("Authenticated, principal=" + name);

      if (propagateContext)
      {
         ctx.pushSubjectContext(subject, principal, password);
         if (TRACE)
            log.trace("Security Context has been propagated");
      }
      return subject;
   }

   private void verifyUsernameToken(String nonce, String created)
   {
      if (created != null)
      {
         Calendar cal = unmarshalDateTime(created);
         Calendar ref = Calendar.getInstance();
         ref.add(Calendar.SECOND, -timestampThreshold);
         if (ref.after(cal))
            throw new SecurityException("Request rejected since a stale timestamp has been provided: " + created);
      }

      if (nonce != null && nonceStore != null)
      {
         if (nonceStore.hasNonce(nonce))
            throw new SecurityException(
                  "Request rejected since a message with the same nonce has been recently received; nonce = " + nonce);
         nonceStore.putNonce(nonce);
      }
   }

   public void setPropagateContext(boolean propagateContext)
   {
      this.propagateContext = propagateContext;
   }

   public void setTimestampThreshold(int timestampThreshold)
   {
      this.timestampThreshold = timestampThreshold;
   }

   public void setNonceStore(NonceStore nonceStore)
   {
      this.nonceStore = nonceStore;
   }

   public void setDecodeNonce(boolean decodeNonce)
   {
      this.decodeNonce = decodeNonce;
   }
   
   private static Calendar unmarshalDateTime(String value)
   {
      Calendar cal = Calendar.getInstance();
      cal.clear();

      int timeInd = parseDate(value, 0, cal);
      if (value.charAt(timeInd) != 'T')
      {
         throw new IllegalArgumentException(
               "DateTime value does not follow the format '[-]yyyy-mm-ddThh:mm:ss[.s+][timezone]': expected 'T' but got "
                     + value.charAt(timeInd));
      }

      int tzStart = parseTime(value, timeInd + 1, cal);

      TimeZone tz = null;
      if (value.length() > tzStart)
      {
         tz = parseTimeZone(value, tzStart);
      }

      if (tz != null)
      {
         cal.setTimeZone(tz);
      }

      return cal;
   }

   private static int parseDate(String value, int start, Calendar cal)
   {
      if (value.charAt(start) == '-')
      {
         ++start;
      }

      if (!Character.isDigit(value.charAt(start)))
      {
         throw new IllegalArgumentException("Date value does not follow the format '-'? yyyy '-' mm '-' dd: " + value);
      }

      int nextToken = value.indexOf('-', start);
      if (nextToken == -1 || nextToken - start < 4)
      {
         throw new IllegalArgumentException("Date value does not follow the format '-'? yyyy '-' mm '-' dd: " + value);
      }

      int year = Integer.parseInt(value.substring(start, nextToken));

      start = nextToken + 1;
      nextToken = value.indexOf('-', start);
      if (nextToken == -1 || nextToken - start < 2)
      {
         throw new IllegalArgumentException("Date value does not follow the format '-'? yyyy '-' mm '-' dd: " + value);
      }

      int month = Integer.parseInt(value.substring(start, nextToken));

      start = nextToken + 1;
      nextToken += 3;
      int day = Integer.parseInt(value.substring(start, nextToken));

      cal.set(Calendar.YEAR, year);
      cal.set(Calendar.MONTH, month - 1);
      cal.set(Calendar.DAY_OF_MONTH, day);

      return nextToken;
   }

   private static int parseTime(String value, int start, Calendar cal)
   {
      if (value.charAt(start + 2) != ':' || value.charAt(start + 5) != ':')
      {
         throw new IllegalArgumentException("Time value does not follow the format 'hh:mm:ss.[s+]': " + value);
      }

      int hh = Integer.parseInt(value.substring(start, start + 2));
      int mm = Integer.parseInt(value.substring(start + 3, start + 5));
      int ss = Integer.parseInt(value.substring(start + 6, start + 8));

      int millis = 0;

      int x = start + 8;

      if (value.length() > x && value.charAt(x) == '.')
      {
         int mul = 100;
         for (x += 1; x < value.length(); x++)
         {
            char c = value.charAt(x);

            if (Character.isDigit(c))
            {
               if (mul != 0)
               {
                  millis += Character.digit(c, 10) * mul;
                  mul = (mul == 1) ? 0 : mul / 10;
               }
            }
            else
            {
               break;
            }
         }
      }

      cal.set(Calendar.HOUR_OF_DAY, hh);
      cal.set(Calendar.MINUTE, mm);
      cal.set(Calendar.SECOND, ss);
      cal.set(Calendar.MILLISECOND, millis);

      return x;
   }

   private static TimeZone parseTimeZone(String value, int start)
   {
      TimeZone tz;
      if (value.charAt(start) == '+' || (value.charAt(start) == '-'))
      {
         if (value.length() - start == 6 && Character.isDigit(value.charAt(start + 1))
               && Character.isDigit(value.charAt(start + 2)) && value.charAt(start + 3) == ':'
               && Character.isDigit(value.charAt(start + 4)) && Character.isDigit(value.charAt(start + 5)))
         {
            tz = TimeZone.getTimeZone("GMT" + value.substring(start));
         }
         else
         {
            throw new NumberFormatException("Timezone value does not follow the format ([+/-]HH:MM): "
                  + value.substring(start));
         }
      }
      else if (value.charAt(start) == 'Z')
      {
         tz = TimeZone.getTimeZone("GMT");
      }
      else
      {
         throw new NumberFormatException("Timezone value does not follow the format ([+/-]HH:MM): "
               + value.substring(start));
      }
      return tz;
   }

}
