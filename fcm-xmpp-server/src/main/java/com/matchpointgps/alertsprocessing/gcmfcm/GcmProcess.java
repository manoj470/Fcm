/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.matchpointgps.alertsprocessing.gcmfcm;

import static com.matchpointgps.alertsprocessing.gcmfcm.EntryPointKafka.AlertErrlogger;
import static com.matchpointgps.alertsprocessing.gcmfcm.EntryPointKafka.gcmlogger;

import com.matchpointgps.alertsprocessing.gcmfcm.mysql.MySql;
import com.matchpointgps.fcm.xmpp.server.Cquery;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.json.JSONObject;

/**
 * Last edited on 08/09/2017 by - Manish Jaiswal
 *
 * @author aspade
 */
public class GcmProcess {

    private static String osTypoName;
    final String topic_name = Constants.SAVE_DB;
    final Kafkaproducer kProducer = new Kafkaproducer();
    Cquery cql = new Cquery();

    public void pushNotification(int userId, JSONObject message, JSONObject notiPayload, String messageId,  Map<String, String> userMap) {
        try {

            MySql mysql = new MySql();
            String mdate = (String) message.get("date");
            String mtime = (String) message.get("time");
            String event_time = mdate + " " + mtime;
            List<Map<String, String>> gcmDevices = mysql.GcmDevices(userId);

            if (!gcmDevices.isEmpty()) {
                List<Map<String, String>> device_group_list = mysql.GcmDeviceGroup(userId);
                if (!device_group_list.isEmpty()) {
                    Map<String, String> device_group = device_group_list.get(0);
                    JSONObject group_data = new JSONObject();
                    group_data.put("message_id", messageId);
                    group_data.put("message", message.toString());
                    group_data.put("user_id", userId);
                    group_data.put("username", userMap.get("username"));
                    group_data.put("group_id", device_group.get("notification_key"));
                    group_data.put("date", mdate);
                    group_data.put("event_time", event_time);
                    group_data.put("status", "NONE");

                    kProducer.produce_message(group_data, topic_name, "mp_xmpp_group_notification");

                    JSONObject detailed_noti_data = new JSONObject();
                    detailed_noti_data.put("date", mdate);
                    detailed_noti_data.put("event_time", event_time);
                    detailed_noti_data.put("message_id", messageId);
                    detailed_noti_data.put("instance_id", device_group.get("notification_key"));
                    detailed_noti_data.put("os_type", Constants.OS_TYPE.ANDROID.name());
                    detailed_noti_data.put("message", notiPayload.getString("body"));
                    detailed_noti_data.put("username", userMap.get("username"));
                    detailed_noti_data.put("status", "NONE");
                    detailed_noti_data.put("alert_type", message.getInt("alertType"));

                    kProducer.produce_message(detailed_noti_data, topic_name, "mp_xmpp_detailed_notification");

                    kProducer.produce_message(detailed_noti_data, topic_name, "mp_xmpp_detailed_notification_by_username");

                    gcmlogger.log(Level.INFO, "gcm android msg : " + device_group.get("notification_key") + "," + messageId + "," + notiPayload.getString("body")
                            + "," + message);
                } else {
                    System.out.println("In else ");
                    return;
                }

                for (Map<String, String> de : gcmDevices) {
                    JSONObject notification_payload = notiPayload;
                    int osTypo = Integer.parseInt(de.get("os_type"));
                    if (osTypo == Constants.OS_TYPE.ANDROID.getValue()) {
                        osTypoName = Constants.OS_TYPE.ANDROID.name();
                        notification_payload = null;
                    } else if (osTypo == Constants.OS_TYPE.IOS.getValue()) {
                        osTypoName = Constants.OS_TYPE.IOS.name();
                        int statusNIOS = check_if_notify_ios(de, message.getInt("alertType"));
                        if (statusNIOS == 1) {
                            notification_payload = null;
                        } else {
                            try {
                                JSONObject data = new JSONObject();
                                data.put("event_time", event_time);
                                data.put("badge", 1);
                                data.put("user_id", userId);
                                data.put("token_id", de.get("token_id"));
                                kProducer.produce_message(data, topic_name, "mp_gcm_latest_notification_count_by_token_id");
                            } catch (Exception ex) {
                                AlertErrlogger.log(Level.DEBUG, "get badge_count ERROR>> " + ex + " " + ex.getStackTrace()[0]);
                            }
                        }
                        try {
                            JSONObject detailed_noti_data = new JSONObject();
                            detailed_noti_data.put("date", mdate);
                            detailed_noti_data.put("event_time", event_time);
                            detailed_noti_data.put("message_id", messageId);
                            detailed_noti_data.put("instance_id", de.get("token_id"));
                            detailed_noti_data.put("os_type", Constants.OS_TYPE.IOS.name());
                            detailed_noti_data.put("message", notiPayload.getString("body"));
                            detailed_noti_data.put("username", userMap.get("username"));
                            detailed_noti_data.put("status", "NONE");
                            detailed_noti_data.put("alert_type", message.getInt("alertType"));

                            kProducer.produce_message(detailed_noti_data, topic_name, "mp_xmpp_detailed_notification");

                            kProducer.produce_message(detailed_noti_data, topic_name, "mp_xmpp_detailed_notification_by_username");

                            gcmlogger.log(Level.INFO, "gcm IOS msg : " + de.get("token_id") + "," + messageId + "," + notiPayload.getString("body")
                                    + "," + message);
                        } catch (Exception ex) {
                            AlertErrlogger.log(Level.DEBUG, "com.matchpointgps.processgcm.gcmProcess.pushNotification() >> : " + ex);
                        }
                    }
                    JSONObject detvice_noti_data = new JSONObject();
                    detvice_noti_data.put("message_id", messageId);
                    detvice_noti_data.put("user_id", userId);
                    detvice_noti_data.put("username", userMap.get("username"));
                    detvice_noti_data.put("token_id", de.get("token_id"));
                    detvice_noti_data.put("status", "NONE");
                    detvice_noti_data.put("os_type", osTypoName);
                    detvice_noti_data.put("date", mdate);
                    detvice_noti_data.put("event_time", event_time);
                    detvice_noti_data.put("notificationPayload", notiPayload.toString());
                    kProducer.produce_message(detvice_noti_data, topic_name, "mp_xmpp_device_notification");
                }

                JSONObject triggered_data = new JSONObject();
                triggered_data.put("user_id", userId);
                triggered_data.put("serial_number_id", message.get("trackerId"));
                triggered_data.put("alert_type", message.getInt("alertType"));
                triggered_data.put("event_time", event_time);
                triggered_data.put("message_id", messageId);
                triggered_data.put("message", message.toString());

                kProducer.produce_message(triggered_data, topic_name, "tr_xmpp_alerts_triggered");

                JSONObject noti_status_data = new JSONObject();
                noti_status_data.put("date", mdate);
                noti_status_data.put("event_time", event_time);
                noti_status_data.put("message_id", messageId);
                noti_status_data.put("message", notiPayload.getString("body"));

                kProducer.produce_message(noti_status_data, topic_name, "mp_xmpp_notification_send_status");

                JSONObject send_msg_data = new JSONObject();
                send_msg_data.put("message_id", messageId);
                send_msg_data.put("message", message.toString());

                kProducer.produce_message(send_msg_data, topic_name, "mp_message_to_send");
            }
        } catch (Exception ex) {
            AlertErrlogger.log(Level.DEBUG, "eRRor in GCM process : " + ex.getStackTrace()[0]);
            Thread.currentThread().interrupt();
        }
    }

    private static int check_if_notify_ios(Map<String, String> de, int alert_type) {
        int status;
        switch (alert_type) {
            case Constants.ALERT_TYPE_SPEED:
                status = Integer.parseInt(de.get("speed_status"));
                break;
            case Constants.ALERT_TYPE_BOUNDARY:
                status = Integer.parseInt(de.get("boundary_status"));
                break;
            case Constants.ALERT_TYPE_EXCESS_IDLING:
                status = Integer.parseInt(de.get("excess_idling_status"));
                break;
            case Constants.ALERT_TYPE_MPROTECT:
                status = Integer.parseInt(de.get("mprotect_status"));
                break;
            default:
                status = 1;
                break;
        }
        return status;
    }

}
