/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.test.ws.jaxws.cxf.jbws3679;

import java.io.IOException;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.xml.ws.WebServiceRef;

public class ServletClient extends HttpServlet
{
   private static final long serialVersionUID = 1L;
   @WebServiceRef(value = EndpointOneService.class)
   private EndpointOne endpointOne;
   
   @Inject
   private CDIBeanClient cdiClient;

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
   {
      String clientParam = req.getParameter("client");
      if ("CDI".equals(clientParam)) {
         res.getWriter().write(cdiClient.performCall());
      } else {
         res.getWriter().write(endpointOne.echo("input"));
      }
   }
}