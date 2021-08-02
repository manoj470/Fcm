/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.matchpointgps.fcm.xmpp.server;

import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.rolling.FixedWindowRollingPolicy;
import org.apache.log4j.rolling.RollingFileAppender;
import org.apache.log4j.rolling.SizeBasedTriggeringPolicy;

/**
 *
 * @author aspade
 */
public class Logging {

    public static void initLogger() {
        try {
            PatternLayout fcmLayout = new PatternLayout("%-5p %d %m%n");
            String fcmfile = "/var/log/fcm_xmpp/fcm_logger.log";
            RollingFileAppender appender = new RollingFileAppender();
            appender.setName("fcm_logger");
            appender.setLayout(fcmLayout);
            appender.setFile(fcmfile);
            SizeBasedTriggeringPolicy trpolicy = new SizeBasedTriggeringPolicy(524288000);
            appender.setTriggeringPolicy(trpolicy);
            FixedWindowRollingPolicy policy = new FixedWindowRollingPolicy();
            policy.setFileNamePattern("/var/log/fcm_xmpp/fcm_logger.log.%i.gz");
            policy.setMaxIndex(7);
            appender.setRollingPolicy(policy);
            appender.activateOptions();
            Logger.getLogger("fcm_logger").addAppender(appender);

            PatternLayout conLayout = new PatternLayout("%-5p %d %m%n");
            String Confile = "/var/log/fcm_xmpp/conn_logger.log";
            RollingFileAppender cappender = new RollingFileAppender();
            cappender.setName("Conn_logger");
            cappender.setLayout(conLayout);
            cappender.setFile(Confile);
            SizeBasedTriggeringPolicy conTrPolicy = new SizeBasedTriggeringPolicy(524288000);
            cappender.setTriggeringPolicy(conTrPolicy);
            FixedWindowRollingPolicy conWrPolicy = new FixedWindowRollingPolicy();
            conWrPolicy.setFileNamePattern("/var/log/fcm_xmpp/conn_logger.log.%i.gz");
            conWrPolicy.setMaxIndex(7);
            cappender.setRollingPolicy(conWrPolicy);
            cappender.activateOptions();
            Logger.getLogger("Conn_logger").addAppender(cappender);

            PatternLayout errLayout = new PatternLayout("%-5p %d %m%n");
            String Erfile = "/var/log/fcm_xmpp/error_logger.log";
            RollingFileAppender eappender = new RollingFileAppender();
            eappender.setName("Error_logger");
            eappender.setLayout(errLayout);
            eappender.setFile(Erfile);
            SizeBasedTriggeringPolicy erTrPolicy = new SizeBasedTriggeringPolicy(524288000);
            eappender.setTriggeringPolicy(erTrPolicy);
            FixedWindowRollingPolicy erWrPolicy = new FixedWindowRollingPolicy();
            erWrPolicy.setFileNamePattern("/var/log/fcm_xmpp/error_logger.log.%i.gz");
            erWrPolicy.setMaxIndex(7);
            eappender.setRollingPolicy(erWrPolicy);
            eappender.activateOptions();
            Logger.getLogger("Error_logger").addAppender(eappender);

            PatternLayout ackLayout = new PatternLayout("%-5p %d %m%n");
            String Ackfile = "/var/log/fcm_xmpp/ack_logger.log";
            RollingFileAppender Ackappender = new RollingFileAppender();
            Ackappender.setName("Ack_logger");
            Ackappender.setLayout(ackLayout);
            Ackappender.setFile(Ackfile);
            SizeBasedTriggeringPolicy ackTrPolicy = new SizeBasedTriggeringPolicy(524288000);
            Ackappender.setTriggeringPolicy(ackTrPolicy);
            FixedWindowRollingPolicy ackWrPolicy = new FixedWindowRollingPolicy();
            ackWrPolicy.setFileNamePattern("/var/log/fcm_xmpp/ack_logger.log.%i.gz");
            ackWrPolicy.setMaxIndex(7);
            Ackappender.setRollingPolicy(ackWrPolicy);
            Ackappender.activateOptions();
            Logger.getLogger("Ack_logger").addAppender(Ackappender);

            PatternLayout nackLayout = new PatternLayout("%-5p %d %m%n");
            String Nackfile = "/var/log/fcm_xmpp/nack_logger.log";
            RollingFileAppender Nackappender = new RollingFileAppender();
            Nackappender.setName("Nack_logger");
            Nackappender.setLayout(nackLayout);
            Nackappender.setFile(Nackfile);
            SizeBasedTriggeringPolicy naTrPolicy = new SizeBasedTriggeringPolicy(524288000);
            Nackappender.setTriggeringPolicy(naTrPolicy);
            FixedWindowRollingPolicy naWrPolicy = new FixedWindowRollingPolicy();
            naWrPolicy.setFileNamePattern("/var/log/fcm_xmpp/nack_logger.log.%i.gz");
            naWrPolicy.setMaxIndex(7);
            Nackappender.setRollingPolicy(naWrPolicy);
            Nackappender.activateOptions();
            Logger.getLogger("Nack_logger").addAppender(Nackappender);

            PatternLayout upLayout = new PatternLayout("%-5p %d %m%n");
            String upfile = "/var/log/fcm_xmpp/upStream_logger.log";
            RollingFileAppender upappender = new RollingFileAppender();
            upappender.setName("UpStream_logger");
            upappender.setLayout(upLayout);
            upappender.setFile(upfile);
            SizeBasedTriggeringPolicy upTrPolicy = new SizeBasedTriggeringPolicy(524288000);
            upappender.setTriggeringPolicy(upTrPolicy);
            FixedWindowRollingPolicy upWrPolicy = new FixedWindowRollingPolicy();
            upWrPolicy.setFileNamePattern("/var/log/fcm_xmpp/upStream_logger.log.%i.gz");
            upWrPolicy.setMaxIndex(7);
            upappender.setRollingPolicy(upWrPolicy);
            upappender.activateOptions();
            Logger.getLogger("UpStream_logger").addAppender(upappender);

            PatternLayout rawLayout = new PatternLayout("%-5p %d %m%n");
            String status = "/var/log/fcm_xmpp/status_logger.log";
            RollingFileAppender stats = new RollingFileAppender();
            stats.setName("status_logger");
            stats.setLayout(rawLayout);
            stats.setFile(status);
            SizeBasedTriggeringPolicy stTrPolicy = new SizeBasedTriggeringPolicy(524288000);
            stats.setTriggeringPolicy(stTrPolicy);
            FixedWindowRollingPolicy stWrPolicy = new FixedWindowRollingPolicy();
            stWrPolicy.setFileNamePattern("/var/log/fcm_xmpp/status_logger.log.%i.gz");
            stWrPolicy.setMaxIndex(7);
            stats.setRollingPolicy(stWrPolicy);
            stats.activateOptions();
            Logger.getLogger("status_logger").addAppender(stats);

            PatternLayout aErrLayout = new PatternLayout("%-5p %d %m%n");
            String AlertErfile = "/var/log/alerts_process/error_logger.log";
            RollingFileAppender alertError = new RollingFileAppender();
            alertError.setName("AlertErr");
            alertError.setLayout(aErrLayout);
            alertError.setFile(AlertErfile);
            SizeBasedTriggeringPolicy aerTrPolicy = new SizeBasedTriggeringPolicy(524288000);
            alertError.setTriggeringPolicy(aerTrPolicy);
            FixedWindowRollingPolicy aerWrPolicy = new FixedWindowRollingPolicy();
            aerWrPolicy.setFileNamePattern("/var/log/alerts_process/error_logger.log.%i.gz");
            aerWrPolicy.setMaxIndex(7);
            alertError.setRollingPolicy(aerWrPolicy);
            alertError.activateOptions();
            Logger.getLogger("AlertErr").addAppender(alertError);

            PatternLayout gcmLayout = new PatternLayout("%-5p %d %m%n");
            String gcmFile = "/var/log/alerts_process/gcm_logger.log";
            RollingFileAppender gcmAppnd = new RollingFileAppender();
            gcmAppnd.setName("Gcmlog");
            gcmAppnd.setLayout(gcmLayout);
            gcmAppnd.setFile(gcmFile);
            SizeBasedTriggeringPolicy gcmTrPolicy = new SizeBasedTriggeringPolicy(524288000);
            gcmAppnd.setTriggeringPolicy(gcmTrPolicy);
            FixedWindowRollingPolicy gcmWrPolicy = new FixedWindowRollingPolicy();
            gcmWrPolicy.setFileNamePattern("/var/log/alerts_process/gcm_logger.log.%i.gz");
            gcmWrPolicy.setMaxIndex(7);
            gcmAppnd.setRollingPolicy(gcmWrPolicy);
            gcmAppnd.activateOptions();
            Logger.getLogger("Gcm_logger").addAppender(gcmAppnd);

            PatternLayout layout = new PatternLayout("%-5p %d %m%n");
            String consumerFile = "/var/log/alerts_process/consumer_logger.log";
            RollingFileAppender consumer = new RollingFileAppender();
            consumer.setName("consumer_logger");
            consumer.setLayout(layout);
            consumer.setFile(consumerFile);
            SizeBasedTriggeringPolicy comTrPolicy = new SizeBasedTriggeringPolicy(524288000);
            consumer.setTriggeringPolicy(comTrPolicy);
            FixedWindowRollingPolicy comWrPolicy = new FixedWindowRollingPolicy();
            comWrPolicy.setFileNamePattern("/var/log/alerts_process/consumer_logger.log.%i.gz");
            comWrPolicy.setMaxIndex(7);
            consumer.setRollingPolicy(comWrPolicy);
            consumer.activateOptions();
            Logger.getLogger("consumer_logger").addAppender(consumer);

            PatternLayout prolayout = new PatternLayout("%-5p %d %m%n");
            String producerFile = "/var/log/alerts_process/producer_logger.log";
            RollingFileAppender proAppdr = new RollingFileAppender();
            proAppdr.setName("Producer_logger");
            proAppdr.setLayout(prolayout);
            proAppdr.setFile(producerFile);
            SizeBasedTriggeringPolicy prTrPolicy = new SizeBasedTriggeringPolicy(524288000);
            proAppdr.setTriggeringPolicy(prTrPolicy);
            FixedWindowRollingPolicy prWrPolicy = new FixedWindowRollingPolicy();
            prWrPolicy.setFileNamePattern("/var/log/alerts_process/producer_logger.log.%i.gz");
            prWrPolicy.setMaxIndex(7);
            proAppdr.setRollingPolicy(prWrPolicy);
            proAppdr.activateOptions();
            Logger.getLogger("Producer_logger").addAppender(proAppdr);

            PatternLayout delilayout = new PatternLayout("%-5p %d %m%n");
            String deliveryFile = "/var/log/fcm_xmpp/delivery_logger.log";
            RollingFileAppender deliAppdr = new RollingFileAppender();
            deliAppdr.setName("deli_logger");
            deliAppdr.setLayout(delilayout);
            deliAppdr.setFile(deliveryFile);
            SizeBasedTriggeringPolicy deliTrPolicy = new SizeBasedTriggeringPolicy(524288000);
            deliAppdr.setTriggeringPolicy(deliTrPolicy);
            FixedWindowRollingPolicy deliWrPolicy = new FixedWindowRollingPolicy();
            deliWrPolicy.setFileNamePattern("/var/log/fcm_xmpp/delivery_logger.log.%i.gz");
            deliWrPolicy.setMaxIndex(7);
            deliAppdr.setRollingPolicy(deliWrPolicy);
            deliAppdr.activateOptions();
            Logger.getLogger("deli_logger").addAppender(deliAppdr);

            PatternLayout tlayout = new PatternLayout("%-5p %d %m%n");
            String time = "/var/log/fcm_xmpp/time_Dbsave.log";
            RollingFileAppender timeappender = new RollingFileAppender();
            timeappender.setName("time_logger");
            timeappender.setFile(time);
            timeappender.setLayout(tlayout);
            SizeBasedTriggeringPolicy tTrPolicy = new SizeBasedTriggeringPolicy(524288000);
            timeappender.setTriggeringPolicy(tTrPolicy);
            FixedWindowRollingPolicy tWrPolicy = new FixedWindowRollingPolicy();
            tWrPolicy.setFileNamePattern("/var/log/fcm_xmpp/time_Dbsave.log.%i.gz");
            tWrPolicy.setMaxIndex(7);
            timeappender.setRollingPolicy(tWrPolicy);
            timeappender.activateOptions();
            Logger.getLogger("time_logger").addAppender(timeappender);

            PatternLayout tsdblayout = new PatternLayout("%-5p %d %m%n");
            String tsdbFile = "/var/log/fcm_xmpp/tsdb_logger.log";
            RollingFileAppender tsdbAppdr = new RollingFileAppender();
            tsdbAppdr.setName("tsdb_logger");
            tsdbAppdr.setLayout(tsdblayout);
            tsdbAppdr.setFile(tsdbFile);
            SizeBasedTriggeringPolicy tsdbTrPolicy = new SizeBasedTriggeringPolicy(524288000);
            tsdbAppdr.setTriggeringPolicy(tsdbTrPolicy);
            FixedWindowRollingPolicy tsdbWrPolicy = new FixedWindowRollingPolicy();
            tsdbWrPolicy.setFileNamePattern("/var/log/fcm_xmpp/tsdb_logger.log.%i.gz");
            tsdbWrPolicy.setMaxIndex(7);
            tsdbAppdr.setRollingPolicy(tsdbWrPolicy);
            tsdbAppdr.activateOptions();
            Logger.getLogger("tsdb_logger").addAppender(tsdbAppdr);
        } catch (Exception e) {
            System.out.println(e);
        }

    }

}
