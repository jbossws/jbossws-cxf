/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.stack.cxf.extensions.policy;


/**
 * Some JBossWS-CXF stack policy extension constants
 * 
 * @author alessio.soldano@jboss.com
 * @since 26-Jul-2013
 *
 */
public interface Constants
{
   public static final String AsymmetricBinding_X509v1_TripleDesRsa15_EncryptBeforeSigning_ProtectTokens_POLICY_SET = "AsymmetricBinding_X509v1_TripleDesRsa15_EncryptBeforeSigning_ProtectTokens";
   public static final String AsymmetricBinding_X509v1_GCM256OAEP_ProtectTokens_POLICY_SET = "AsymmetricBinding_X509v1_GCM256OAEP_ProtectTokens";
   public static final String WS_SP_EX2121_SSL_UT_Supporting_Token_POLICY_SET = "WS-SP-EX2121_SSL_UT_Supporting_Token";
   public static final String WS_SP_EX213_WSS10_UT_Mutual_Auth_X509_Sign_Encrypt_POLICY_SET = "WS-SP-EX213_WSS10_UT_Mutual_Auth_X509_Sign_Encrypt";
   public static final String WS_SP_EX214_WSS11_User_Name_Cert_Sign_Encrypt_POLICY_SET = "WS-SP-EX214_WSS11_User_Name_Cert_Sign_Encrypt";
   public static final String WS_SP_EX221_WSS10_Mutual_Auth_X509_Sign_Encrypt_POLICY_SET = "WS-SP-EX221_WSS10_Mutual_Auth_X509_Sign_Encrypt";
   public static final String WS_SP_EX222_WSS10_Mutual_Auth_X509_Sign_Encrypt_POLICY_SET = "WS-SP-EX222_WSS10_Mutual_Auth_X509_Sign_Encrypt";
   public static final String WS_SP_EX223_WSS11_Anonymous_X509_Sign_Encrypt_POLICY_SET = "WS-SP-EX223_WSS11_Anonymous_X509_Sign_Encrypt";
   public static final String WS_SP_EX224_WSS11_Mutual_Auth_X509_Sign_Encrypt_POLICY_SET = "WS-SP-EX224_WSS11_Mutual_Auth_X509_Sign_Encrypt";
   public static final String WS_RM_Policy_spec_example_POLICY_SET = "WS-RM_Policy_spec_example";
   public static final String WS_Addressing_POLICY_SET = "WS-Addressing";
}
