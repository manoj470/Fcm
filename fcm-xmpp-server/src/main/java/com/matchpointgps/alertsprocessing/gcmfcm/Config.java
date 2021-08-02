/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.matchpointgps.alertsprocessing.gcmfcm;

import static com.matchpointgps.alertsprocessing.gcmfcm.EntryPointKafka.AlertErrlogger;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import org.apache.log4j.Level;

/**
 *
 * @author Manish Jaiswal
 */
public class Config {

    public InputStream input;
    public final Properties prop = new Properties();

    String GROUP_ID;
    String Consumer_TOPIC;
    String SaveData_TOPIC;
    int WORKERS;
    String BOOTSTRAP;
    String STATUS;
    int CONSUMERS_THREADS;
    public int FCM_CONNECTION;

    String MySQL_IP;
    String MySQL_PORT;
    String MySQL_DB;
    String MySQL_USERNAME;
    String MySQL_PASSWORD;
    int MySQL_Max_Connection;
    int MySQL_MaxIdle_Connection;
    int MySQL_MinIdle_Connection;

    String Cassandra_IP;
    String Cassandra_PORT;
    String Cassandra_Cluster;
    String Cassandra_USERNAME;
    String Cassandra_PASSWORD;
    String Cassandra_Keyspace;
    int Cassandra_coreConnection;
    int Cassandra_maxConnection;

    public String Matrix_Topic;
    public String DeleteToken_URL;

    public Config(String pathToFile) {
        try {
            input = new FileInputStream(pathToFile + "/config/config.properties");
            this.prop.load(input);
        } catch (Exception ex) {
            AlertErrlogger.log(Level.ERROR, "error while loading file" + ex);
        }
        try{
            this.GROUP_ID = prop.getProperty("groupId");
            this.Consumer_TOPIC = prop.getProperty("consumer.topic");
            this.SaveData_TOPIC = prop.getProperty("saveData.topic");
            this.WORKERS = Integer.parseInt(prop.getProperty("worker.threads"));
            this.FCM_CONNECTION = Integer.parseInt(prop.getProperty("fcm.conn"));
            this.BOOTSTRAP = prop.getProperty("server");
            this.STATUS = prop.getProperty("status");
            this.CONSUMERS_THREADS = Integer.parseInt(prop.getProperty("consumer.threads"));
            this.MySQL_IP = prop.getProperty("MySql.ip");
            this.MySQL_PORT = prop.getProperty("MySql.port");
            this.MySQL_DB = prop.getProperty("MySql.db");
            this.MySQL_USERNAME = prop.getProperty("MySql.username");
            this.MySQL_PASSWORD = prop.getProperty("MySql.password");
            this.MySQL_Max_Connection = Integer.parseInt(prop.getProperty("MySql.max.connection"));
            this.MySQL_MaxIdle_Connection = Integer.parseInt(prop.getProperty("MySql.max.connection.idle"));
            this.MySQL_MinIdle_Connection = Integer.parseInt(prop.getProperty("MySql.min.connection.idle"));
            this.Cassandra_IP = prop.getProperty("cassandra.hosts");
            this.Cassandra_PORT = prop.getProperty("cassandra.port");
            this.Cassandra_Keyspace = prop.getProperty("cassandra.keyspace");
            this.Cassandra_USERNAME = prop.getProperty("cassandra.username");
            this.Cassandra_PASSWORD = prop.getProperty("cassandra.password");
            this.Cassandra_Cluster = prop.getProperty("cassandra.cluster");
            this.Cassandra_coreConnection = Integer.parseInt(prop.getProperty("cassandra.coreConnection"));
            this.Cassandra_maxConnection = Integer.parseInt(prop.getProperty("cassandra.maxConnection"));
            this.DeleteToken_URL = prop.getProperty("deleteToken.URL");
        } catch (Exception e) {
            AlertErrlogger.log(Level.ERROR, "error while initialization" + e + "---" + e.getStackTrace()[0]);
        }
    }
}
