/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.matchpointgps.alertsprocessing.gcmfcm;


import com.matchpointgps.alertsprocessing.gcmfcm.mysql.MysqlConPool;
import com.matchpointgps.fcm.xmpp.server.CassCon;
import com.matchpointgps.fcm.xmpp.server.CcsClient;
import com.matchpointgps.fcm.xmpp.server.Logging;
import com.matchpointgps.fcm.xmpp.util.Util;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.apache.kafka.clients.producer.Producer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

/**
 * Last edited on 03/07/2017 by - Manish Jaiswal
 *
 * @author Manish Jaiswal
 * @created on 03/05/2017
 */
public class EntryPointKafka {

    public static Config dynmConfig;

    private static List<ThreadConsumer> consumers;
    public static ExecutorService executor;
    public static CassCon client;
    public static CcsClient ccsClient;

    public static Logger fcmlogger;
    public static Logger gcmlogger;
    public static Logger Erlogger;
    public static Logger Conlogger;
    public static Logger uplogger;
    public static Logger nacklogger;
    public static Logger acklogger;
    public static Logger Consumerlogger;
    public static Logger AlertErrlogger;
    public static Logger tsdb_log;
    public static Logger timelog;
    public static Logger statuslogger;
    public static String project_dir;
    public static Producer producer;
    public static MysqlConPool pool;
    public static DataSource dsc;


    public static void main(String[] args) throws IOException, SmackException {

        Logging.initLogger();
        tieSystemOutAndErrToLog();
        fcmlogger = Logger.getLogger("fcm_logger");
        gcmlogger = Logger.getLogger("Gcm_logger");
        timelog = Logger.getLogger("time_logger");
        Conlogger = Logger.getLogger("Conn_logger");
        Erlogger = Logger.getLogger("Error_logger");
        tsdb_log = Logger.getLogger("tsdb_logger");
        uplogger = Logger.getLogger("UpStream_logger");
        acklogger = Logger.getLogger("Ack_logger");
        nacklogger = Logger.getLogger("Nack_logger");
        statuslogger = Logger.getLogger("status_logger");
        Consumerlogger = Logger.getLogger("consumer_logger");
        AlertErrlogger = Logger.getLogger("AlertErr");

        if (project_dir == null || project_dir.trim().isEmpty())
                project_dir = "/opt/fcm_server_test";

        dynmConfig = new Config(project_dir);

        /* ============== CONNECT TO DATABASE ================== */
        pool = new MysqlConPool("jdbc:mysql://" + dynmConfig.MySQL_IP + ":" + dynmConfig.MySQL_PORT + "/" + dynmConfig.MySQL_DB,
                dynmConfig.MySQL_USERNAME, dynmConfig.MySQL_PASSWORD);
        dsc = pool.getDataSource();



        client = new CassCon(dynmConfig.Cassandra_IP, dynmConfig.Cassandra_PORT, dynmConfig.Cassandra_Cluster,
                dynmConfig.Cassandra_USERNAME, dynmConfig.Cassandra_PASSWORD, dynmConfig.Cassandra_coreConnection, dynmConfig.Cassandra_maxConnection);
        client.connect();


        producer = Kafkaproducer.initProducer();
        /* ============== CONNECT TO FCM Server ================== */
        ccsClient = CcsClient.prepareClient(Util.fcmProjectSenderId, Util.fcmServerKey, true);
        try {
            ccsClient.connect();
        } catch (XMPPException e) {
            e.printStackTrace();
            Erlogger.log(Level.DEBUG, "error in conn " + e);
        }

        /* ================ KAFKA Thread consumer Group ========================== */
        consumers = KafkaConsumerClient.initKafkaConsumers(dynmConfig.CONSUMERS_THREADS, dynmConfig.GROUP_ID,
                dynmConfig.Consumer_TOPIC, dynmConfig.BOOTSTRAP);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                for (ThreadConsumer consumer : consumers) {
                    consumer.shutdown();
                }
                executor.shutdown();
                try {
                    boolean isTerminated = executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void tieSystemOutAndErrToLog() {
        System.setOut(createLoggingProxy(System.out));
        System.setErr(createLoggingProxy(System.err));
    }

    public static PrintStream createLoggingProxy(final PrintStream realPrintStream) {
        return new PrintStream(realPrintStream) {
            @Override
            public void print(final String string) {
                realPrintStream.print(string);
                statuslogger.info(string);
            }
        };
    }
}
