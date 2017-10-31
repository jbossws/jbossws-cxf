package org.jboss.test.ws.jaxws.cxf.jbws3655;

import org.jboss.ws.api.annotation.WebContext;

import javax.ejb.Stateless;
import javax.jws.WebService;

@Stateless
@WebService(wsdlLocation = "META-INF/wsdl/GoodBy.wsdl",
    name = GoodByWs.NAME,
    serviceName = GoodByWs.NAME,
    targetNamespace = GoodByWs.TARGET_NAMESPACE,
    endpointInterface = "org.jboss.test.ws.jaxws.cxf.jbws3655.GoodByWs")
@WebContext(contextRoot = "/jaxws-cxf-jbws3655-goodby", urlPattern = GoodByWs.NAME)
public class GoodByWSEJBImpl implements GoodByWs
{

    public GoodByResponse doGoodBy(GoodByRequest request)
    {
        GoodByResponse response = new GoodByResponse();
        response.getMultiGoodBy().add(request.getInput());
        response.getMultiGoodBy().add("world");
        return response;
    }

}
