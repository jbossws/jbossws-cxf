/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.stack.cxf.saaj;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.helpers.LoadingByteArrayOutputStream;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.ConduitInitiator;
import org.apache.cxf.transport.ConduitInitiatorManager;
import org.apache.cxf.transport.MessageObserver;
import org.apache.cxf.transport.http.HTTPConduit;

public class SOAPConnectionImpl extends SOAPConnection 
{
   private boolean closed = false;

    @Override
    public SOAPMessage call(SOAPMessage msgOut, Object addressObject) throws SOAPException 
    {
       checkClosed();
       
       String address = getAddress(addressObject);
       ConduitInitiator ci = getConduitInitiator(address);
        
        
       // create a new Message and Exchange
       EndpointInfo info = new EndpointInfo();
       info.setAddress(address);
       Message outMessage = new MessageImpl();
       Exchange exch = new ExchangeImpl();
       outMessage.setExchange(exch);
        
       // sent SOAPMessage
       try 
       {
          final Conduit c = ci.getConduit(info);
            
        
          Map<String, List<String>> outHeaders = new HashMap<String, List<String>>();
          for (Iterator<?> it = msgOut.getMimeHeaders().getAllHeaders(); it.hasNext();) 
          {
             MimeHeader mimeHeader = (MimeHeader)it.next();
             if ("Content-Type".equals(mimeHeader.getName())) 
             {
                outMessage.put(Message.CONTENT_TYPE, mimeHeader.getValue());
             }
                
             // disable the chunked encoding if requested
             if ("Transfer-Encoding".equals(mimeHeader.getName())
                 && "disabled".equals(mimeHeader.getValue())
                 && c instanceof HTTPConduit) 
             {
                ((HTTPConduit)c).getClient().setAllowChunking(false);
                continue;
             }
                
             List<String> values = outHeaders.get(mimeHeader.getName());
             if (values == null) 
             {
                values = new ArrayList<String>();
                outHeaders.put(mimeHeader.getName(), values);
             } 
             values.add(mimeHeader.getValue());
          }
          outMessage.put(Message.HTTP_REQUEST_METHOD, "POST");
          outMessage.put(Message.PROTOCOL_HEADERS, outHeaders);
          c.prepare(outMessage);
            
          OutputStream outs = outMessage.getContent(OutputStream.class);
          msgOut.writeTo(outs);
            
          c.setMessageObserver(createMessageObserver(c));
            
          c.close(outMessage);
       } 
       catch (Exception ex) 
       {
          throw new SOAPException("SOAPMessage can not be sent", ex);
       }    

       // read SOAPMessage        
       return readSoapMessage(exch); 
    }
    
    @Override
    public SOAPMessage get(Object addressObject) throws SOAPException 
    {
       checkClosed();
       
       String address = getAddress(addressObject);
       ConduitInitiator ci = getConduitInitiator(address);
        
        
       // create a new Message and Exchange
       EndpointInfo info = new EndpointInfo();
       info.setAddress(address);
       Message outMessage = new MessageImpl();
       Exchange exch = new ExchangeImpl();
       outMessage.setExchange(exch);
        
       // sent GET request
       try 
       {
          final Conduit c = ci.getConduit(info);
          
          if (c instanceof HTTPConduit)
          {
             ((HTTPConduit)c).getClient().setAutoRedirect(true);
          }
            
          outMessage.put(Message.HTTP_REQUEST_METHOD, "GET");
          c.prepare(outMessage);
            
          c.setMessageObserver(createMessageObserver(c));
            
          c.close(outMessage);
       } 
       catch (Exception ex) 
       {
          throw new SOAPException("GET request can not be sent", ex);
       }    

       // read SOAPMessage
       return readSoapMessage(exch);
    }

    @Override
    public void close() throws SOAPException 
    {
       if (this.closed)
       {
          throw new SOAPException("Connection already closed!");
       }
       this.closed = true;
    }

    private String getAddress(Object addressObject) throws SOAPException 
    {
       if (addressObject instanceof URL || addressObject instanceof String) 
       {
          return addressObject.toString();
       }
       throw new SOAPException("Address object of type " + addressObject.getClass().getName()
                                + " is not supported");
    }
    
    private ConduitInitiator getConduitInitiator(String address) throws SOAPException 
    {
       ConduitInitiator ci = null;
       try 
       {
          //do not use getThreadDefaultBus(true) in order to avoid getting the default bus
          Bus bus = BusFactory.getThreadDefaultBus(false);
          if (bus == null)
          {
             bus = BusFactory.newInstance().createBus();
          }
          ConduitInitiatorManager mgr = bus.getExtension(ConduitInitiatorManager.class);
            
          if (address.startsWith("http")) 
          {
             ci = mgr.getConduitInitiator("http://cxf.apache.org/transports/http/configuration");
          } 
          if (ci == null) 
          {
             ci = mgr.getConduitInitiatorForUri(address);
          }
            
       } 
       catch (Exception ex) 
       {
          throw new SOAPException("No ConduitInitiator is available for " + address, ex);
       }
        
       if (ci == null) 
       {
          throw new SOAPException("No ConduitInitiator is available for " + address);
       }
       return ci;
    }

    @SuppressWarnings("unchecked")
    private MessageObserver createMessageObserver(final Conduit c)
    {
       return new MessageObserver() 
       {
           public void onMessage(Message inMessage) 
           {
              LoadingByteArrayOutputStream bout = new LoadingByteArrayOutputStream();
              try 
              {
                 IOUtils.copy(inMessage.getContent(InputStream.class), bout);
                 inMessage.getExchange().put(InputStream.class, bout.createInputStream());
                       
                 Map<String, List<String>> inHeaders = 
                    (Map<String, List<String>>)inMessage.get(Message.PROTOCOL_HEADERS);
                       
                 inMessage.getExchange().put(Message.PROTOCOL_HEADERS, inHeaders);
                 c.close(inMessage);
              } 
              catch (IOException e) 
              {
                 //ignore
              }
           }
       };
    }
    
    @SuppressWarnings("unchecked")
    private SOAPMessage readSoapMessage(Exchange exch) throws SOAPException
    {
       // read SOAPMessage        
       try 
       {
          InputStream ins = exch.get(InputStream.class);
          
          Map<String, List<String>> inHeaders = 
             (Map<String, List<String>>)exch.get(Message.PROTOCOL_HEADERS);
             
          MimeHeaders mimeHeaders = new MimeHeaders();
          if (inHeaders != null) 
          {
             for (Map.Entry<String, List<String>> entry : inHeaders.entrySet()) 
             {
                if (entry.getValue() != null) 
                {
                   for (String value : entry.getValue()) 
                   {
                      mimeHeaders.addHeader(entry.getKey(), value);
                   }
                }
             }
          }
          
          //if inputstream is empty, no need to build
          if (ins.markSupported())
          {
             ins.mark(1);
             final int bytesRead = ins.read(new byte[1]);
             ins.reset();
             if (bytesRead == -1)
             {
                return null;
             }
          }
          else if (ins.available() == 0)
          {
             return null;
          }

          MessageFactory msgFac = MessageFactory.newInstance(SOAPConstants.DYNAMIC_SOAP_PROTOCOL);
          return msgFac.createMessage(mimeHeaders, ins);
       } 
       catch (Exception ex) 
       {    
          throw new SOAPException("SOAPMessage can not be read", ex);
       }
    }
    
    private void checkClosed() throws SOAPException 
    {
    	if (closed) {
           throw new SOAPException("Cannot send messages using a previously closed connection!");
        }   	
    }
    
}
