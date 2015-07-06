package org.jboss.wsf.stack.cxf.interceptor;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.cxf.common.util.UrlUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.interceptor.MessageSenderInterceptor;
import org.apache.cxf.interceptor.OutgoingChainInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.transport.common.gzip.GZIPOutInterceptor;

public abstract class AbstractMangementInInterceptor extends AbstractManagementInterceptor
{
   public AbstractMangementInInterceptor(String phase)
   {
      super(phase);
   }


   private static final String TRANSFORM_SKIP = "transform.skip";



   abstract Set<String> getAllowedMethod();

   protected boolean isAllowed(final Message message) throws Fault
   {
      String method = (String)message.get(Message.HTTP_REQUEST_METHOD);
      if (getAllowedMethod().contains(method) && isValidUser(getHttpRequest(message)))
      {
         return true;
      }
      return false;

   }

   private boolean isValidUser(final HttpServletRequest req)
   {
      if (req.getUserPrincipal() != null && req.isUserInRole("admin"))
      {
         return true;
      }
      return false;
   }

   protected HttpServletRequest getHttpRequest(Message message)
   {
      return (HttpServletRequest)message.get("HTTP.REQUEST");
   }

   protected Map<String, String> getQueryMap(Message message)
   {
      String query = (String)message.get(Message.QUERY_STRING);
      Map<String, String> map = UrlUtils.parseQueryString(query);
      return map;
   }

   protected void cleanUpOutInterceptors(Message outMessage)
   {
      // TODO - how can I improve this to provide a specific interceptor chain that just has the
      // stax, gzip and message sender components, while also ensuring that GZIP is only provided
      // if its already configured for the endpoint.
      Iterator<Interceptor<? extends Message>> iterator = outMessage.getInterceptorChain().iterator();
      while (iterator.hasNext())
      {
         Interceptor<? extends Message> inInterceptor = iterator.next();
         if (!inInterceptor.getClass().equals(GZIPOutInterceptor.class) && !inInterceptor.getClass().equals(MessageSenderInterceptor.class))
         {
            outMessage.getInterceptorChain().remove(inInterceptor);
         }
      }
      outMessage.getExchange().put(TRANSFORM_SKIP, Boolean.TRUE);

   }

   protected Message createOutMessage(Message message)
   {
      Message mout = new MessageImpl();
      mout.setExchange(message.getExchange());
      mout = message.getExchange().get(org.apache.cxf.endpoint.Endpoint.class).getBinding().createMessage(mout);
      mout.setInterceptorChain(OutgoingChainInterceptor.getOutInterceptorChain(message.getExchange()));
      this.setContentType(mout);
      message.getExchange().setOutMessage(mout);
      return mout;
   }
}
