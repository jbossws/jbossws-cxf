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

import static org.jboss.wsf.stack.cxf.i18n.Loggers.SECURITY_LOGGER;
import static org.jboss.wsf.stack.cxf.i18n.Messages.MESSAGES;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.Principal;
import java.util.Base64;
import java.util.Calendar;
import java.util.TimeZone;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.cxf.common.security.SimplePrincipal;
import org.jboss.wsf.spi.security.SecurityDomainContext;
import org.jboss.wsf.stack.cxf.security.authentication.callback.UsernameTokenCallbackHandler;
import org.jboss.wsf.stack.cxf.security.nonce.NonceStore;
import org.wildfly.security.auth.server.RealmIdentity;
import org.wildfly.security.auth.server.RealmUnavailableException;
import org.wildfly.security.auth.server.SecurityDomain;
import org.wildfly.security.credential.PasswordCredential;
import org.wildfly.security.password.interfaces.ClearPassword;

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
   private static final int TIMESTAMP_FRESHNESS_THRESHOLD = 300;

   private boolean propagateContext;

   private int timestampThreshold = TIMESTAMP_FRESHNESS_THRESHOLD;

   private NonceStore nonceStore;

   private boolean decodeNonce = true;
   
   public Subject createSubject(SecurityDomainContext ctx, String name, String password, boolean isDigest, byte[] nonce, String created)
   {
      //TODO, revisit
      final String sNonce = convertNonce(nonce);
      return createSubject(ctx, name, password, isDigest, sNonce, created);
   }

   public Subject createSubject(SecurityDomainContext ctx, String name, String password, boolean isDigest, String nonce, String created)
   {
      if (isDigest)
      {
         verifyUsernameToken(nonce, created);
      }

      // authenticate and populate Subject
      Principal principal = new SimplePrincipal(name);
      Subject subject = new Subject();

      boolean TRACE = SECURITY_LOGGER.isTraceEnabled();
      if (TRACE)
         SECURITY_LOGGER.aboutToAuthenticate(ctx.getSecurityDomain());

      try {
         SecurityDomain securityDomain = ctx.getElytronSecurityDomain();
         if (securityDomain == null) {
            SECURITY_LOGGER.noSecurityDomain();
            return null;
         }
         RealmIdentity identity = securityDomain.getIdentity(principal.getName());
         if (identity.equals(RealmIdentity.NON_EXISTENT)) {
            throw MESSAGES.authenticationFailed(principal.getName());
         }
         ClearPassword clearPassword = identity.getCredential(PasswordCredential.class).getPassword(ClearPassword.class);
         // only realms supporting getCredential with clear password can be used with Username Token profile
         if (clearPassword == null) {
            throw MESSAGES.authenticationFailed(principal.getName());
         }
         String expectedPassword = new String(clearPassword.getPassword());
         if (isDigest && created != null && nonce != null) { // username token profile is using digest
            // verify client's digest
            if (!getUsernameTokenPasswordDigest(nonce, created, expectedPassword).equals(password)) {
               throw MESSAGES.authenticationFailed(principal.getName());
            }
            // client's digest is valid so expected password can be used to authenticate to the domain
            if (!ctx.isValid(principal, expectedPassword, subject)) {
               throw MESSAGES.authenticationFailed(principal.getName());
            }
         } else {
            if (!ctx.isValid(principal, password, subject)) {
               throw MESSAGES.authenticationFailed(principal.getName());
            }
         }

      } catch (RealmUnavailableException e) {
         throw MESSAGES.authenticationFailed(principal.getName());
      }

      if (TRACE)
         SECURITY_LOGGER.authenticated(name);

      if (propagateContext)
      {
         ctx.pushSubjectContext(subject, principal, password);
         if (TRACE)
            SECURITY_LOGGER.securityContextPropagated(name);
      }
      return subject;
   }

   private String convertNonce(byte[] nonce)
   {
      //TODO, revisit
      try
      {
         if (nonce != null)
         {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(nonce);
            return bos.toString();
         }
         else
         {
            return null;
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }
   

   protected void verifyUsernameToken(String nonce, String created)
   {
      if (created != null)
      {
         Calendar cal = unmarshalDateTime(created);
         Calendar ref = Calendar.getInstance();
         ref.add(Calendar.SECOND, -timestampThreshold);
         if (ref.after(cal))
            throw MESSAGES.requestRejectedTimeStamp(created);
      }

      if (nonce != null && nonceStore != null)
      {
         if (nonceStore.hasNonce(nonce))
            throw MESSAGES.requestRejectedSameNonce(nonce);
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
         throw MESSAGES.invalidDateTimeFormat(value.charAt(timeInd));
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
         throw MESSAGES.invalidDateValueFormat(value);
      }

      int nextToken = value.indexOf('-', start);
      if (nextToken == -1 || nextToken - start < 4)
      {
         throw MESSAGES.invalidDateValueFormat(value);
      }

      int year = Integer.parseInt(value.substring(start, nextToken));

      start = nextToken + 1;
      nextToken = value.indexOf('-', start);
      if (nextToken == -1 || nextToken - start < 2)
      {
         throw MESSAGES.invalidDateValueFormat(value);
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
         throw MESSAGES.invalidTimeValueFormat(value);
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
            throw MESSAGES.invalidTimeZoneValueFormat(value.substring(start));
         }
      }
      else if (value.charAt(start) == 'Z')
      {
         tz = TimeZone.getTimeZone("GMT");
      }
      else
      {
         throw MESSAGES.invalidTimeZoneValueFormat(value.substring(start));
      }
      return tz;
   }

   /**
    * Get UsernameToken profile digest
    *
    * @param nonce nonce
    * @param created creation timestamp
    * @param password clear text password
    * @return Password_Digest = Base64 ( SHA-1 ( nonce + created + password ) )
    */
   private static String getUsernameTokenPasswordDigest(String nonce, String created, String password) {
      ByteBuffer buf = ByteBuffer.allocate(1000);
      buf.put(Base64.getDecoder().decode(nonce));
      try {
         buf.put(created.getBytes("UTF-8"));
         buf.put(password.getBytes("UTF-8"));
      } catch (UnsupportedEncodingException e) {
         SECURITY_LOGGER.failedToComputeUsernameTokenProfileDigest();
      }
      byte[] toHash = new byte[buf.position()];
      buf.rewind();
      buf.get(toHash);
      byte[] hash = DigestUtils.sha(toHash);
      return new String(Base64.getEncoder().encode(hash));
   }
}
