/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.wsf.stack.cxf.saaj;

import static org.jboss.wsf.stack.cxf.i18n.Messages.MESSAGES;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.MimeHeader;
import jakarta.xml.soap.MimeHeaders;
import jakarta.xml.soap.SOAPConnection;
import jakarta.xml.soap.SOAPConstants;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;

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
import org.jboss.logging.Logger;
import org.jboss.wsf.stack.cxf.client.Constants;

public class SOAPConnectionImpl extends SOAPConnection 
{
    private volatile boolean closed = false;
    private boolean forceURLConnectionConduit = Boolean.getBoolean(Constants.FORCE_URL_CONNECTION_CONDUIT);

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
       exch.put("org.apache.cxf.transport.process_fault_on_http_400", true); //JBWS-3945
       if (forceURLConnectionConduit) {
          exch.put(Constants.FORCE_URL_CONNECTION_CONDUIT, true);
       }
        
       // sent SOAPMessage
       try 
       {
          final Conduit c = ci.getConduit(info, BusFactory.getThreadDefaultBus(false)); //TODO verify bus

          if (msgOut.saveRequired())
          {
             msgOut.saveChanges();
          }
        
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
          throw MESSAGES.soapMessageCouldNotBeSent(ex);
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
          final Conduit c = ci.getConduit(info, BusFactory.getThreadDefaultBus(false)); //TODO verify bus
          
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
          throw MESSAGES.getRequestCouldNotBeSent(ex);
       }    

       // read SOAPMessage
       return readSoapMessage(exch);
    }

    @Override
    public void close() throws SOAPException 
    {
       if (this.closed)
       {
          throw MESSAGES.connectionAlreadyClosed();
       }
       this.closed = true;
    }

    private String getAddress(Object addressObject) throws SOAPException 
    {
       if (addressObject instanceof URL || addressObject instanceof String) 
       {
          return addressObject.toString();
       }
       throw MESSAGES.addressTypeNotSupported(addressObject.getClass());
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
             ci = mgr.getConduitInitiator("http://cxf.apache.org/transports/http");
          } 
          if (ci == null) 
          {
             ci = mgr.getConduitInitiatorForUri(address);
          }
            
       } 
       catch (Exception ex) 
       {
          throw MESSAGES.noConduitInitiatorAvailableFor2(address, ex);
       }
        
       if (ci == null) 
       {
          throw MESSAGES.noConduitInitiatorAvailableFor(address);
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
                 Logger.getLogger(SOAPConnectionImpl.class).trace(e);
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
          
          if (ins == null) return null;
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
          throw MESSAGES.soapMessageCouldNotBeRead(ex);
       }
    }
    
    private void checkClosed() throws SOAPException 
    {
    	if (closed) {
           throw MESSAGES.cantSendMessagesOnClosedConnection();
        }   	
    }
    
}
