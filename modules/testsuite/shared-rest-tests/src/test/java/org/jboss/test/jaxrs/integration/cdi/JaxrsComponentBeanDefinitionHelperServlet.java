/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.test.jaxrs.integration.cdi;

import java.io.IOException;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = "test")
public class JaxrsComponentBeanDefinitionHelperServlet extends HttpServlet
{

   private static final long serialVersionUID = 2807497165909866373L;

   @Inject
   BeanManager beanManager;

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
   {
      testResource();
      testApplication();
      testProvider();
      res.getWriter().print("OK");
   }

   public void testResource()
   {
      // There's one bean of type CDIResource and it's scope is @RequestScoped
      Set<Bean<?>> beans = beanManager.getBeans(CDIResource.class);
      if (beans.size() != 1 || !RequestScoped.class.equals(beans.iterator().next().getScope()))
      {
         throw new RuntimeException();
      }
   }

   public void testApplication()
   {
      // There's one bean of type CDIApplication and it's scope is @ApplicationScoped
      Set<Bean<?>> beans = beanManager.getBeans(CDIApplication.class);
      if (beans.size() != 1 || !ApplicationScoped.class.equals(beans.iterator().next().getScope()))
      {
         throw new RuntimeException();
      }
   }

   public void testProvider()
   {
      // There's one bean of type CDIProvider and it's scope is @ApplicationScoped
      Set<Bean<?>> beans = beanManager.getBeans(CDIProvider.class);
      if (beans.size() != 1 || !ApplicationScoped.class.equals(beans.iterator().next().getScope()))
      {
         throw new RuntimeException();
      }
   }

}
