/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.jboss.test.ws.appclient;

import java.io.File;

/**
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public final class AppclientKiller
{

   private final static int MAX_COUNT_OF_ATTEMTS = 600;

   public static void main(final String[] args) throws Exception {
      if (args.length != 1) {
         throw new RuntimeException("shutdown mark file name missing");
      }
      final File shutdownMarkFile = new File(args[0]);
      int countOfAttempts = 0;
      while (!shutdownMarkFile.exists()) {
         countOfAttempts++;
         Thread.sleep(100);
         if (countOfAttempts == MAX_COUNT_OF_ATTEMTS) {
            System.out.println("appclient timeout");
            break;
         }
      }
      System.out.println("forcibly stopped in AppclientKiller");
      System.exit(1);
   }

}
