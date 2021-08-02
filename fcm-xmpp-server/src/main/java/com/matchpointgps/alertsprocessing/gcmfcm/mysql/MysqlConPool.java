/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.matchpointgps.alertsprocessing.gcmfcm.mysql;

import static com.matchpointgps.alertsprocessing.gcmfcm.EntryPointKafka.Conlogger;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.log4j.Level;

/**
 * Last edited on 21/06/2017 by - Manish Jaiswal
 *
 * @author Manish Jaiswal
 * @Created on 27/06/2017
 */
public class MysqlConPool {

    private static GenericObjectPool objPool;
    public static final String DRIVER = "com.mysql.jdbc.Driver";
    public final String URL;
    public final String USERNAME;
    public final String PASSWORD;

    public MysqlConPool(String ip, String user, String password) {
        this.URL = ip;
        this.USERNAME = user;
        this.PASSWORD = password;
    }


    public  DataSource getDataSource() {
        //
        // First, we'll create a ConnectionFactory that the
        // pool will use to create Connections.
        // We'll use the DriverManagerConnectionFactory,
        // using the connect string passed in the command line
        // arguments.
        //
        ConnectionFactory connectionFactory
                = new DriverManagerConnectionFactory(URL, USERNAME, PASSWORD);

        //
        // Next we'll create the PoolableConnectionFactory, which wraps
        // the "real" Connections created by the ConnectionFactory with
        // the classes that implement the pooling functionality.
        //
        PoolableConnectionFactory poolableConnectionFactory
                = new PoolableConnectionFactory(connectionFactory, null);
        poolableConnectionFactory.setDefaultAutoCommit(Boolean.FALSE);
        //
        // Now we'll need a ObjectPool that serves as the
        // actual pool of connections.
        //
        // We'll use a GenericObjectPool instance, although
        // any ObjectPool implementation will suffice.
        //
        objPool = new GenericObjectPool<>(poolableConnectionFactory);
        objPool.setMinIdle(5);
        objPool.setMaxIdle(5);
        objPool.setMaxTotal(30);
        try {
            objPool.borrowObject();
        } catch (Exception ex) {
            Conlogger.log(Level.INFO, "Error in Mysql Connection " + ex);
        }
        ObjectPool<PoolableConnection> connectionPool
                = objPool;
        // Set the factory's pool property to the owning pool
        poolableConnectionFactory.setPool(connectionPool);

        //
        // Finally, we create the PoolingDriver itself,
        // passing in the object pool we created.
        //
        PoolingDataSource<PoolableConnection> dataSource
                = new PoolingDataSource<>(connectionPool);
        Conlogger.log(Level.INFO, "Mysql Connection created ");
        return dataSource;
    }

    public void printStatus() {
        System.out.println("Max   : " + objPool.getMaxTotal() + "; "
                + "Active: " + objPool.getNumActive() + "; "
                + "Idle  : " + objPool.getNumIdle());
    }
}
