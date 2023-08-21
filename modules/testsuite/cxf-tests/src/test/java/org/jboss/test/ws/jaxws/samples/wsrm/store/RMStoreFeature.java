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
package org.jboss.test.ws.jaxws.samples.wsrm.store;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.interceptor.InterceptorProvider;
import org.apache.cxf.ws.rm.RM11Constants;
import org.apache.cxf.ws.rm.feature.RMFeature;
import org.apache.cxf.ws.rm.persistence.jdbc.RMTxStore;
import org.apache.cxf.ws.rmp.v200502.RMAssertion;
import org.apache.cxf.ws.rmp.v200502.RMAssertion.BaseRetransmissionInterval;
import org.jboss.logging.Logger.Level;
import org.jboss.ws.common.Loggers;
import org.jboss.wsf.spi.WSFException;

/**
 * Another {@link RMFeature} which allows set {@link Connection} or {@link DataSource} to store
 * RMSequence or DestionationSequence message 
 * @author <a herf="mailto:ema@redhat.com">Jim Ma</a>
 *
 */
public class RMStoreFeature extends RMFeature
{
   public static final String serverDataSource = "java:jboss/datasources/rmdb";
   private InitialContext ctx;

   protected void initializeProvider(InterceptorProvider provider, Bus bus)
   {
      String dataSource = serverDataSource;
      RMTxStore rmStore = new RMDataSourceStore();
      if (provider instanceof Client)
      {
         try
         {
            Connection con = DriverManager.getConnection("jdbc:derby:./target/wsrmdb;create=true", rmStore.getUserName(), rmStore.getPassword());
            rmStore.setConnection(con);
         }
         catch (SQLException e)
         {
            Loggers.ROOT_LOGGER.log(Level.FATAL, "Can't create connection from " + rmStore.getUrl(), e);
            throw new WSFException(e);
         }
      }
      else
      {
         //server side
         if (ctx == null)
         {
            try
            {
               ctx = new InitialContext();
               DataSource rmDs = (DataSource)ctx.lookup(dataSource);
               rmStore.setDataSource(rmDs);
            }
            catch (NamingException e)
            {
               Loggers.DEPLOYMENT_LOGGER.log(Level.FATAL, "Can't create datasource with " + dataSource, e);
               throw new WSFException(e);
            }
         }

      }
      rmStore.init();
      this.setStore(rmStore);
      //force to use RM11 and it can only work with wsa200508 (http://www.w3.org/2005/08/addressing) which is enabled with @Addressing
      this.setRMNamespace(RM11Constants.NAMESPACE_URI);
      RMAssertion assertion = new RMAssertion();
      BaseRetransmissionInterval retransMissionInveral = new BaseRetransmissionInterval();
      retransMissionInveral.setMilliseconds(10000L);
      assertion.setBaseRetransmissionInterval(retransMissionInveral);
      this.setRMAssertion(assertion);
      super.initializeProvider(provider, bus);
   }

   class RMDataSourceStore extends RMTxStore
   {
      protected Connection verifyConnection()
      {
         Connection con = super.verifyConnection();
         try
         {
            if (con.getAutoCommit())
            {
               con.setAutoCommit(false);
            }
         }
         catch (SQLException e)
         {

            Loggers.ROOT_LOGGER.log(Level.ERROR, "Can't setAutoCommit(false) for RMStore connection", e);
            throw new WSFException(e);
         }
         return con;
      }
   }
}
