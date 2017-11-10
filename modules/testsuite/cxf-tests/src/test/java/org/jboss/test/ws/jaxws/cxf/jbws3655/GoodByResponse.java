package org.jboss.test.ws.jaxws.cxf.jbws3655;

import java.util.ArrayList;
import java.util.List;

public class GoodByResponse
{
    private List<String> multiGoodBy = new ArrayList<String>();

    public List<String> getMultiGoodBy()
    {
        return multiGoodBy;
    }

    public void setMultiGoodBy(List<String> multiGoodBy)
    {
        this.multiGoodBy = multiGoodBy;
    }

}