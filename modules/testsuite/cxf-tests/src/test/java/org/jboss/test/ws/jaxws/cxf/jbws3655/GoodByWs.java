package org.jboss.test.ws.jaxws.cxf.jbws3655;

import javax.jws.WebService;

@WebService(name = GoodByWs.NAME, targetNamespace = GoodByWs.TARGET_NAMESPACE)
public interface GoodByWs
{
    public final static String NAME = "GoodByService";

    public final static String TARGET_NAMESPACE = "http://goodby/test";

    public GoodByResponse doGoodBy(GoodByRequest request);
}