package org.jboss.test.ws.jaxws.cxf.jbws3809;

import javax.jws.WebParam;
import javax.jws.WebService;

/**
 * User: rsearls
 * Date: 7/25/14
 */
@WebService(
   targetNamespace = "http://org.jboss.ws.test")
public interface BasicEjb {
   String getStr (String str);
}
