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
package org.jboss.wsf.test;

import java.security.NoSuchAlgorithmException;

import org.apache.cxf.binding.soap.SoapFault;

public final class CryptoHelper
{
   public static Exception checkAndWrapException(Exception e) throws Exception {
      if (!isBouncyCastleAvailable()) {
         return new Exception("Bouncy Castle JCE provider does not seem to be properly installed; either install it " +
                 "or run the testuite with -Dexclude-integration-tests-BC-related=true to exclude this test.", e);
      } else if(!isUnlimitedStrengthCryptographyAvailable()) {
         return new Exception("JCE unlimited strength cryptography extension does not seem to be properly installed; either install it " +
               "or run the testuite with '-Dexclude-integration-tests-unlimited-strength-related=true' to exclude this test.", e);
      } else if (e.getCause() != null && e.getCause() instanceof SoapFault && e.getMessage() != null && e.getMessage().contains("algorithm")) {
         return new Exception("Please check for Bouncy Castle JCE provider and JCE unlimited strenght cryptography extension availability on server side.", e);
      } else {
         return e;
      }
   }
   
   public static boolean isBouncyCastleAvailable() {
      return java.security.Security.getProvider("BC") != null;
   }
   
   public static boolean isUnlimitedStrengthCryptographyAvailable() {
      try {
         return (javax.crypto.Cipher.getMaxAllowedKeyLength("RC5") >= 256);
      } catch (NoSuchAlgorithmException e) {
         throw new RuntimeException(e);
      }
   }
}
