/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.matchpointgps.alertsprocessing.gcmfcm;

import static com.matchpointgps.alertsprocessing.gcmfcm.EntryPointKafka.AlertErrlogger;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.matchpointgps.alertsprocessing.gcmfcm.mysql.MySql;
import com.matchpointgps.fcm.xmpp.FcmProcess;

import java.util.List;
import java.util.TimeZone;
import org.apache.log4j.Level;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Last_Modified on 08/09/2017 by - Manish Jaiswal Author ---> Manish Jaiswal
 *
 * @Created on 26/06/2017
 */
public class FcmNotificationService {

    final Kafkaproducer kProducer = new Kafkaproducer();
    final String topic_name = Constants.SAVE_DB;

    public void fcm_push_notification(int user_id, JSONObject messages, JSONObject notification_payload, String msg_id, Map<String, String> userMap) {
        try {
            ArrayList<HashMap<String,Object>> fcm_device = fcm_save_compute_msg_for_queue(user_id, messages, notification_payload, msg_id, userMap);
            System.out.println("data" + fcm_device);
            JSONObject json;
            if (!fcm_device.isEmpty()) {
                    for (HashMap<String,Object> obj : fcm_device) {
                        json = new JSONObject(obj);
                        if (obj.get("notification_payload") == null) {
                            json.put("notification_payload", JSONObject.NULL);
                        }
                        new FcmProcess().handleDelivery(json);
                    }
            }
        } catch (JSONException e) {
            AlertErrlogger.log(Level.DEBUG, "error in fcm_push_notification : " + e);
        }
    }

    public void fcm_web_push_notification(int user_id, JSONObject messages, JSONObject notification_payload, String msg_id, Map userMap) {

    }

    private int check_if_notify_ios(Map<String, String> de, int alert_type) {
        //        """ check if need to notify ios device"""

        int status;
        try {
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
        } catch (NumberFormatException e) {
            status = 1;
        }
        return status;

    }

    private HashMap<String,Object>  compute_notification_payload(Map<String, String> d, int user_id, JSONObject messages, JSONObject noti_payload,
            String os_type, String message_change_id) {
        HashMap<String,Object> result_data = new HashMap<>();
        try {
            String mdate = (String) messages.get("date");
            String mtime = (String) messages.get("time");
            String token_id = (String) d.get("token_id");
            int alertType = (int) messages.get("alertType");
            String message_time = mdate + " " + mtime;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            JSONObject notification_payload = noti_payload;
            if ((check_if_notify_ios(d, alertType)) == 0) {
                notification_payload = null;
            } else if (notification_payload != null) {
                Instant ist_now = Instant.now().plus(5, ChronoUnit.HOURS).plus(30, ChronoUnit.MINUTES);
                int badge_count = 1;
                JSONObject payload;
                notification_payload.put("badge", badge_count);
                if (d.containsKey("dnd") && d.get("dnd") != null && "1".equals(d.get("dnd"))) {
                    notification_payload.put("sound", "silent.wav");
                } else {
                    notification_payload.put("sound", "default");
                }
                JSONObject data = new JSONObject();
                data.put("event_time", message_time);
                data.put("badge", badge_count);
                data.put("user_id", user_id);
                data.put("token_id", token_id);
                kProducer.produce_message(data, topic_name, "mp_fcm_latest_notification_count_by_token_id");
            }
            messages.put("event_time", message_time);
            result_data.put("event_time", message_time);
            result_data.put("message_id", message_change_id);
            result_data.put("message", messages);
            result_data.put("unique_id", token_id);
            result_data.put("type", os_type);
            result_data.put("notification_payload", notification_payload);
            System.out.println("comp " + result_data);
            return result_data;
        } catch (Exception e) {
            AlertErrlogger.log(Level.DEBUG, "error in compute notification: " + e.getStackTrace()[0]);
        }
        return result_data;
    }

    public ArrayList<HashMap<String,Object>> fcm_save_compute_msg_for_queue(int user_id, JSONObject messages,
                                                                            JSONObject notification_payload, String message_id, Map<String, String> userMap) {
        try {
            MySql mysql = new MySql();
            ArrayList<HashMap<String,Object>> result_list = new ArrayList<>();
            List<Map<String, String>> web_devices = mysql.WebInstance(user_id);
            List<Map<String, String>> fcm_devices = mysql.FcmDevices(user_id);
            if (fcm_devices.size() < 1 && web_devices.size() < 1) {
                return null;
            }
            JSONObject notification_payload_tmp = notification_payload;
            String mdate = (String) messages.get("date");
            String mtime = (String) messages.get("time");

            int alert_type = (int) messages.get("alertType");

            String message_time = mdate + " " + mtime;

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

            int counter = 0;
            String os_type = null;

            for (Map<String, String> mapResult : fcm_devices) {
                try {
                    String message_change_id = message_id + "-" + counter;
                    String InstanceId = mapResult.get("token_id");
                    int osType = Integer.parseInt(mapResult.get("os_type"));
                    if (osType == Constants.OS_TYPE.ANDROID.getValue()) {
                        // #if Android
                        os_type = Constants.OS_TYPE.ANDROID.name();
                    } else if (osType == Constants.OS_TYPE.IOS.getValue()) // #if iOS
                    {
                        os_type = Constants.OS_TYPE.IOS.name();
                    }
                    JSONObject copy_payload = new JSONObject(notification_payload, JSONObject.getNames(notification_payload));
                    HashMap<String,Object> compute_data = compute_notification_payload(mapResult, user_id, messages, copy_payload, os_type, message_change_id);
                    result_list.add(compute_data);
                    System.out.println("q1 " + result_list);
                    String noti_pay;
                    if ((compute_data.get("notification_payload") == null)) {
                        noti_pay = null;
                    } else {
                        //                    System.out.println("in else check");
                        notification_payload = (JSONObject) compute_data.get("notification_payload");
                        noti_pay = notification_payload.toString();

                    }
                    try {
                        JSONObject data = new JSONObject();
                        data.put("date", mdate);
                        data.put("event_time", message_time);
                        data.put("message_id", message_change_id);

                        data.put("instance_id", InstanceId);
                        data.put("alert_number", message_id);
                        data.put("message", notification_payload_tmp.getString("body"));
                        if (noti_pay != null) {
                            data.put("notificationPayload", noti_pay);
                        } else {
                            data.put("notificationPayload", JSONObject.NULL);
                        }
                        data.put("os_type", os_type);
                        data.put("user_id", user_id);
                        data.put("status", "NONE");
                        data.put("username", userMap.get("username"));
                        data.put("alert_type", alert_type);

                        kProducer.produce_message(data, topic_name, "mp_fcm_xmpp_detailed_notification");
                        kProducer.produce_message(data, topic_name, "mp_fcm_xmpp_detailed_notification_by_username");
                        kProducer.produce_message(data, topic_name, "mp_fcm_xmpp_device_notification");
                    } catch (Exception e) {
                        AlertErrlogger.log(Level.DEBUG, "ERROR IN DETAILED NOTIFICATION " + e);
                    }
                    counter++;
                } catch (Exception e) {
                    AlertErrlogger.log(Level.DEBUG, "ERROR IN FCM DEVICE FETCH " + e.getStackTrace()[0]);
                }

            }

            JSONObject triggered_data = new JSONObject();
            triggered_data.put("user_id", user_id);
            triggered_data.put("username", userMap.get("username"));
            triggered_data.put("alert_number", message_id);
            triggered_data.put("serial_number_id", messages.get("trackerId"));
            triggered_data.put("alert_type", alert_type);
            triggered_data.put("message_id", message_id);
            triggered_data.put("event_time", message_time);
            triggered_data.put("message", messages.toString());

            kProducer.produce_message(triggered_data, topic_name, "tr_fcm_xmpp_alerts_triggered");

            kProducer.produce_message(triggered_data, topic_name, "latest_fcm_xmpp_alerts_triggered");

            kProducer.produce_message(triggered_data, topic_name, "tr_fcm_xmpp_alerts_triggered_by_tracker");

            kProducer.produce_message(triggered_data, topic_name, "tr_fcm_xmpp_alerts_triggered_by_event_time");

            JSONObject send_noti_data = new JSONObject();
            send_noti_data.put("date", mdate);
            send_noti_data.put("event_time", message_time);
            send_noti_data.put("message_id", message_id);
            send_noti_data.put("message", notification_payload_tmp.getString("body"));

            kProducer.produce_message(send_noti_data, topic_name, "mp_fcm_xmpp_notification_send_status");
            System.out.println("qf " + result_list);
            return result_list;

        } catch (Exception e) {
            AlertErrlogger.log(Level.DEBUG, "error fcm_save_compute_msg_for_queue " + e.getStackTrace()[0] + "====" + e.getStackTrace()[0].getLineNumber());
            return new ArrayList<>();
        }

    }
}
