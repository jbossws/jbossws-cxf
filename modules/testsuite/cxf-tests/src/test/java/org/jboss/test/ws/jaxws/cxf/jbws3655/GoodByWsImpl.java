package org.jboss.test.ws.jaxws.cxf.jbws3655;

import javax.jws.WebService;
@WebService(wsdlLocation = "META-INF/wsdl/GoodBy.wsdl",
    name = GoodByWs.NAME,
    serviceName = GoodByWs.NAME,
    targetNamespace = GoodByWs.TARGET_NAMESPACE,
    endpointInterface = "org.jboss.test.ws.jaxws.cxf.jbws3655.GoodByWs")
public class GoodByWsImpl implements GoodByWs
{
    public GoodByResponse doGoodBy(GoodByRequest request)
    {
        GoodByResponse response = new GoodByResponse();
        response.getMultiGoodBy().add(request.getInput());
        response.getMultiGoodBy().add("world");
        return response;
    }
}