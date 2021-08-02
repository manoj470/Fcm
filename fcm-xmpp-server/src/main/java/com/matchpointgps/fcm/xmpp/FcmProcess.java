/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.matchpointgps.fcm.xmpp;

import com.matchpointgps.alertsprocessing.gcmfcm.EntryPointKafka;
import static com.matchpointgps.alertsprocessing.gcmfcm.EntryPointKafka.fcmlogger;
import com.matchpointgps.fcm.xmpp.bean.CcsOutMessage;
import static com.matchpointgps.alertsprocessing.gcmfcm.EntryPointKafka.Erlogger;
import com.matchpointgps.fcm.xmpp.server.MessageHelper;
import com.matchpointgps.fcm.xmpp.util.Util;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Level;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.json.JSONObject;

/**
 *
 * @author aspade
 */
public class FcmProcess {

    public void handleDelivery(JSONObject myJson) {
        try {
            Map<String, String> dataPayload = new HashMap<>();
            Map<String, Object> notificationPayload;
            String payload = myJson.get("message").toString();
            String message_id = myJson.get("message_id").toString();
            String RegId = myJson.get("unique_id").toString();
            String notification_payload;

            if (myJson.has("notification_payload") &&
                    myJson.get("notification_payload") != JSONObject.NULL) {
                notification_payload = myJson.get("notification_payload").toString();
            } else
                notification_payload = null;

            dataPayload.put(Util.PAYLOAD_ATTRIBUTE_MESSAGE, payload);
            dataPayload.put(Util.PAYLOAD_ATTRIBUTE_MESSAGE_ID, message_id);

            CcsOutMessage fcm_message;
            if (notification_payload == null) {
                fcm_message = new CcsOutMessage(RegId, message_id, dataPayload);
            } else {
                JSONObject notification_payload_obj = new JSONObject(notification_payload);
                notificationPayload = notification_payload_obj.toMap();
                boolean mutableContent = false;
                if(myJson.has("mutable_content"))
                    mutableContent = myJson.getBoolean("mutable_content");
                fcm_message = new CcsOutMessage(RegId, message_id, dataPayload, notificationPayload,mutableContent);
            }

            String jsonRequest = MessageHelper.createJsonOutMessage(fcm_message);
            fcmlogger.log(Level.INFO, "Send data === " + jsonRequest);
            EntryPointKafka.ccsClient.send(jsonRequest);
        } catch (Exception ex) {
            Erlogger.log(Level.DEBUG, "FcmProcess point parse Error-- " + ex);
            try {
                EntryPointKafka.ccsClient.connect();
            } catch (XMPPException | SmackException | IOException ex1) {
                Erlogger.log(Level.DEBUG, "FcmProcess point parse Error-- " + ex);
            }
        }
    }

}
