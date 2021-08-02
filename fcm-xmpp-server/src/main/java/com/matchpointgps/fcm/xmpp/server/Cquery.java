/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.matchpointgps.fcm.xmpp.server;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.matchpointgps.alertsprocessing.gcmfcm.Constants;
import com.matchpointgps.alertsprocessing.gcmfcm.EntryPointKafka;
import static com.matchpointgps.alertsprocessing.gcmfcm.EntryPointKafka.Erlogger;
import static com.matchpointgps.alertsprocessing.gcmfcm.EntryPointKafka.timelog;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.log4j.Level;

/**
 *
 * @author aspade
 */
public class Cquery {

    static String query;
    static String query1;

    public static void update_grp(String mid, String gid, String st) {
        try {
            query = "Insert into matchsitedb.mp_fcm_xmpp_group_notification"
                    + " (message_id,group_id , status, \"sentDateTime\" ) VALUES (?,?,?,?) using ttl " + Constants.CassTTL;
            PreparedStatement pst = EntryPointKafka.client.session.prepare(query);
            BoundStatement bStatement;
            bStatement = pst.bind(mid, gid, st, Cquery.current_date());
            ResultSet resultSet;

            Instant st1 = Instant.now();
            resultSet = EntryPointKafka.client.session.execute(bStatement);
            Instant end1 = Instant.now();
            long ns1 = Duration.between(st1, end1).toMillis();
            timelog.log(Level.INFO, "update_grp-1 " + ns1 + "  by thread " + Thread.currentThread());

            // System.out.println("result>>>"+resultSet);
            query1 = "SELECT date,event_time,username from matchsitedb.mp_fcm_xmpp_group_notification WHERE message_id= '" + mid + "' AND group_id='" + gid + "';";
            PreparedStatement pst1 = EntryPointKafka.client.session.prepare(query1);
            BoundStatement bst1 = new BoundStatement(pst1);
            ResultSet rs;
            //ArrayList aq= new ArrayList();

            Instant st2 = Instant.now();
            rs = EntryPointKafka.client.session.execute(bst1);
            Instant end2 = Instant.now();
            long ns2 = Duration.between(st1, end1).toMillis();
            timelog.log(Level.INFO, "update_grp-2 " + ns2 + "  by thread " + Thread.currentThread());

            List aq = rs.all();
            //aq.get(0);

            if (aq.size() == 0) {
                System.out.println("group result set is empty");
            } else {

                Row rst = (Row) aq.get(0);
                System.out.println("at 0 ---- " + rst);
                update_detail_noti(rst.getTimestamp(0), rst.getTimestamp(1), mid, gid, st);
                update_detail_user(rst.getString(2), rst.getTimestamp(1), mid, mid, gid, st);

            }
        } catch (Exception e) {

            Erlogger.log(Level.DEBUG, "update Group exception " + e);
        }
    }

    public static void update_device_nt(String msg_id, String token, String st) {
        try {
            query1 = "SELECT date,event_time,username,alert_number from matchsitedb.mp_fcm_xmpp_device_notification WHERE message_id= '"
                    + msg_id + "' AND token_id='" + token + "';";
            PreparedStatement pst1 = EntryPointKafka.client.session.prepare(query1);
            BoundStatement bst1 = new BoundStatement(pst1);
            ResultSet rs;

            Instant st1 = Instant.now();
            rs = EntryPointKafka.client.session.execute(bst1);
            Instant end1 = Instant.now();
            long ns1 = Duration.between(st1, end1).toMillis();
            timelog.log(Level.INFO, "updateDeviceNotiDelivered-1 for "+st+" in " + ns1 + "  by thread " + Thread.currentThread());
            List aq = rs.all();

            if (aq.isEmpty()) {
                System.out.println("result set is empty");
            } else {
                query = "Insert into matchsitedb.mp_fcm_xmpp_device_notification"
                        + " (message_id,token_id,status,\"deliveredDateTime\") VALUES (?,?,?,?) using ttl " + Constants.CassTTL;
                PreparedStatement pst = EntryPointKafka.client.session.prepare(query);
                BoundStatement bStatement = pst.bind(msg_id, token, st, Cquery.current_date());
                ResultSet resultSet;

                Instant st2 = Instant.now();
                resultSet = EntryPointKafka.client.session.execute(bStatement);
                Instant end2 = Instant.now();
                long ns2 = Duration.between(st2, end2).toMillis();
                timelog.log(Level.INFO, "updateDeviceNotiDelivered-2 for "+st+" in " + ns2 + "  by thread " + Thread.currentThread());

                Row rst = (Row) aq.get(0);
                update_detail_noti(rst.getTimestamp(0), rst.getTimestamp(1), msg_id, token, st);
                update_detail_user(rst.getString(2), rst.getTimestamp(1), rst.getString("alert_number"), msg_id, token, st);
            }
        } catch (Exception e) {
            Erlogger.log(Level.DEBUG, "Update device Exception" + e);
        }

    }

    public static List updateDeviceNotiDelivered(String msg_id, String token, String st, Date deliveredTime) {
        try {
            query1 = "SELECT date,event_time,username,alert_number,\"deliveredDateTime\" from matchsitedb.mp_fcm_xmpp_device_notification WHERE message_id= '"
                    + msg_id + "' AND token_id='" + token + "';";
            PreparedStatement pst1 = EntryPointKafka.client.session.prepare(query1);
            BoundStatement bst1 = new BoundStatement(pst1);
            ResultSet rs;

            Instant st1 = Instant.now();
            rs = EntryPointKafka.client.session.execute(bst1);
            Instant end1 = Instant.now();
            long ns1 = Duration.between(st1, end1).toMillis();
            timelog.log(Level.INFO, "updateDeviceNotiDelivered-1 for "+st+" in " + ns1 + "  by thread " + Thread.currentThread());
            List aq = rs.all();
            if (aq.isEmpty()) {
                System.out.println("result set is empty");
                return null;
            } else {
//            System.out.println("***************** data "+msg_id+" , "+token);
                query = "Insert into matchsitedb.mp_fcm_xmpp_device_notification"
                        + " (message_id,token_id,status,device_recv_time) VALUES (?,?,?,?) using ttl " + Constants.CassTTL;
                PreparedStatement pst = EntryPointKafka.client.session.prepare(query);

                BoundStatement bStatement = pst.bind(msg_id, token, st, deliveredTime);
                ResultSet resultSet;

                Instant st2 = Instant.now();
                resultSet = EntryPointKafka.client.session.execute(bStatement);
                Instant end2 = Instant.now();
                long ns2 = Duration.between(st2, end2).toMillis();
                timelog.log(Level.INFO, "updateDeviceNotiDelivered-2 for "+st+" in " + ns2 + "  by thread " + Thread.currentThread());

                Row rst = (Row) aq.get(0);

                String strQuery = "Insert into matchsitedb.mp_fcm_xmpp_detailed_notification"
                        + " (date,event_time,message_id,instance_id, status,device_recv_time) VALUES (?,?,?,?,?,?) using ttl " + Constants.CassTTL;
                PreparedStatement pst2 = EntryPointKafka.client.session.prepare(strQuery);
                BoundStatement bst2 = pst2.bind(rst.getTimestamp(0), rst.getTimestamp(1), msg_id, token, st, deliveredTime);
                Instant st3 = Instant.now();
                resultSet = EntryPointKafka.client.session.execute(bStatement);
                Instant end3 = Instant.now();
                long ns3 = Duration.between(st3, end3).toMillis();
                timelog.log(Level.INFO, "updateDeviceNotiDelivered-3 for "+st+" in " + ns3 + "  by thread " + Thread.currentThread());

                String usrquery = "Insert into matchsitedb.mp_fcm_xmpp_detailed_notification_by_username "
                        + "(username,event_time,alert_number,message_id,instance_id, status ) VALUES (?,?,?,?,?,?) using ttl " + Constants.CassTTL;
                PreparedStatement pst3 = EntryPointKafka.client.session.prepare(usrquery);
                BoundStatement bst3 = pst3.bind(rst.getString(2), rst.getTimestamp(1), rst.getString("alert_number"), msg_id, token, st);
                Instant st4 = Instant.now();
                EntryPointKafka.client.session.execute(bst3);
                Instant end4 = Instant.now();
                long ns4 = Duration.between(st4, end4).toMillis();
                timelog.log(Level.INFO, "updateDeviceNotiDelivered-4 for "+st+" in " + ns4 + "  by thread " + Thread.currentThread());

                List list = new ArrayList();
                list.add(rst.getTimestamp(1));
                list.add(rst.getTimestamp(4));
                return list;
            }
        } catch (Exception e) {
            Erlogger.log(Level.DEBUG, "updateDeviceNotiDelivered Exception" + e);
        }
        return null;
    }

    public static void update_detail_noti(Date dt, Date edt, String mid, String Inst, String status) {
        try {
            query = "Insert into matchsitedb.mp_fcm_xmpp_detailed_notification"
                    + " (date,event_time,message_id,instance_id, status,\"delivered_datetime\" ) VALUES (?,?,?,?,?,?) using ttl " + Constants.CassTTL;
            PreparedStatement pst = EntryPointKafka.client.session.prepare(query);
            BoundStatement bStatement = pst.bind(dt, edt, mid, Inst, status, Cquery.current_date());
            ResultSet resultSet;

            Instant st = Instant.now();
            resultSet = EntryPointKafka.client.session.execute(bStatement);
            Instant end = Instant.now();
            long ns = Duration.between(st, end).toMillis();
            timelog.log(Level.INFO, "update_detail_noti for "+st+" in " + ns + "  by thread " + Thread.currentThread());
        } catch (Exception e) {
            Erlogger.log(Level.DEBUG, "updateDeviceNotiDelivered Exception" + e);
        }
    }

    public static void update_detail_user(String usr, Date edt, String alt_num, String mid, String Inst, String status) {
        try {
            query = "Insert into matchsitedb.mp_fcm_xmpp_detailed_notification_by_username "
                    + "(username,event_time,alert_number,message_id,instance_id, status,\"delivered_datetime\" ) VALUES (?,?,?,?,?,?,?) using ttl "
                    + Constants.CassTTL;
            PreparedStatement pst = EntryPointKafka.client.session.prepare(query);
            BoundStatement bStatement = pst.bind(usr, edt, alt_num, mid, Inst, status, Cquery.current_date());
            ResultSet resultSet;

            Instant st = Instant.now();
            resultSet = EntryPointKafka.client.session.execute(bStatement);

            Instant end = Instant.now();
            long ns = Duration.between(st, end).toMillis();
            timelog.log(Level.INFO, "update_detail_user for "+status+" in " + ns + "  by thread " + Thread.currentThread());

        } catch (Exception e) {
            Erlogger.log(Level.DEBUG, "updateDeviceNotiDelivered Exception" + e);
        }

    }

    public Row FcmAlertsTriggeredByTracker(int userId, int sr_no, int alert_ty) {
        String query2 = "select * from matchsitedb.mp_fcm_xmpp_alerts_triggered_by_tracker where user_id=" + userId + " and serial_number_id="
                + sr_no + " and  alert_type=" + alert_ty + ";";
        Instant st = Instant.now();
        ResultSet rst = EntryPointKafka.client.session.execute(query2);

        Instant end = Instant.now();
        long ns = Duration.between(st, end).toMillis();
        timelog.log(Level.INFO, "FcmAlertsTriggeredByTracker " + ns + "  by thread " + Thread.currentThread());

        List aq = rst.all();
        Row row = null;
        if (!aq.isEmpty()) {
            row = (Row) aq.get(aq.size() - 1);
//            System.out.println("row>>" + row);
        }
        return row;
    }

    public Row getFcmNotificationCount_by_tokenId(int userId, String tokenId) {
        Instant st = Instant.now();
        try {
            String getQuery = " SELECT * FROM matchsitedb.mp_fcm_latest_notification_count_by_token_id WHERE user_id =" + userId
                    + " AND token_id ='" + tokenId + "' ;";

            ResultSet rs = EntryPointKafka.client.session.execute(getQuery);
            List result = rs.all();
            Row row = null;
            if (!result.isEmpty()) {
                row = (Row) result.get(0);
//            System.out.println("row>>" + row);
            }
            Instant end = Instant.now();
            long ns = Duration.between(st, end).toMillis();
            timelog.log(Level.INFO, "getFcmNotificationCount_by_tokenId " + ns + "  by thread " + Thread.currentThread());

            return row;
        } catch (Exception err) {
            Erlogger.log(Level.INFO, "error in InsertRawData " + err);
        }
        return null;
    }

    public Row getGcmNotificationCount_by_tokenId(int userId, String tokenId) {
        Instant st = Instant.now();
        try {

            String getQuery = " SELECT * FROM matchsitedb.mp_gcm_latest_notification_count_by_token_id WHERE user_id =" + userId
                    + " AND token_id ='" + tokenId + "' ;";

            ResultSet rs = EntryPointKafka.client.session.execute(getQuery);
            List result = rs.all();
            Row row = null;
            if (!result.isEmpty()) {
                row = (Row) result.get(0);
            }

            Instant end = Instant.now();
            long ns = Duration.between(st, end).toMillis();
            timelog.log(Level.INFO, "getGcmNotificationCount_by_tokenId " + ns + "  by thread " + Thread.currentThread());
            return row;
        } catch (Exception err) {
            Erlogger.log(Level.INFO, "error in InsertRawData " + err);
        }
        return null;
    }

    public static Date current_date() {
        SimpleDateFormat Frmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String Cdate = Frmt.format(new Date());
        Date dtime = null;
        try {
            dtime = (Date) Frmt.parse(Cdate);

        } catch (ParseException ex) {
            Erlogger.log(Level.DEBUG, "exception in parsing date-- " + ex);
        }
        Frmt.setTimeZone(TimeZone.getTimeZone("UTC"));

        return dtime;

    }

}
