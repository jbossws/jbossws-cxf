/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.samples.wsse.policy.basic;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;

/**
 * WS-Security Policy sign & encrypt test case
 * using GCM algorithm suite
 *
 * @author alessio.soldano@jboss.com
 * @since 27-Feb-2012
 */
public final class SignEncryptGCMTestCase extends JBossWSTest
{
   private final String serviceURL = "http://" + getServerHost() + ":8080/jaxws-samples-wsse-policy-sign-encrypt-gcm";
   
   public static Test suite()
   {
      return new JBossWSCXFTestSetup(SignEncryptGCMTestCase.class, 
            "jaxws-samples-wsse-policy-sign-encrypt-client.jar " +
            "jaxws-samples-wsse-policy-sign-encrypt-client.war " +
            "jaxws-samples-wsse-policy-sign-encrypt-gcm.war");
   }
   
   public void testClientSide() throws Exception
   {
      SignEncryptHelper helper = new SignEncryptHelper();
      helper.setTargetEndpoint(serviceURL);
      assertTrue(helper.testSignEncrypt());
   }
   
   public void testClientSideUsingConfigProperties() throws Exception
   {
      SignEncryptHelper helper = new SignEncryptHelper();
      helper.setTargetEndpoint(serviceURL);
      assertTrue(helper.testSignEncryptUsingConfigProperties());
   }
   
   public void testServerSide() throws Exception
   {
      URL url = new URL("http://" + getServerHost() + ":8080/jaxws-samples-wsse-policy-sign-encrypt-client?" +
            "path=/jaxws-samples-wsse-policy-sign-encrypt-gcm&method=testSignEncrypt&helper=" + SignEncryptHelper.class.getName());
      BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
      assertEquals("1", br.readLine());
   }
   
   public void testServerSideUsingConfigProperties() throws Exception
   {
      //TODO!!
      testServerSide();
//      URL url = new URL("http://" + getServerHost() + ":8080/jaxws-samples-wsse-policy-sign-encrypt-client?" +
//            "path=/jaxws-samples-wsse-policy-sign-encrypt-gcm&method=testSignEncryptUsingConfigProperties&helper=" + SignEncryptHelper.class.getName());
//      BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
//      assertEquals("1", br.readLine());
   }
}
