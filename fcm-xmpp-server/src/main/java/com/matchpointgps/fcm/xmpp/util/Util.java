package com.matchpointgps.fcm.xmpp.util;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Util class for constants and generic methods
 *
 */
public class Util {

    //Status
    public static final String ver = "rc170630";
    public static final String SUBMIT_STATUS = "SUBMITTED";
    public static final String Ack_Staus = "SENT";
    public static final String RC_Staus = "RECEIPT";
    public static final String RS_Staus = "DELIVERED";

    public static final ArrayList<String> CASS_HOSTS = new ArrayList();
    public static final int CASS_POST = 9042;
    // For the GCM connection
    public static final String FCM_SERVER = "fcm-xmpp.googleapis.com";
    public static final int FCM_PORT = 5235;
    public static final String FCM_ELEMENT_NAME = "gcm";
    public static final String FCM_NAMESPACE = "google:mobile:data";
    public static final String FCM_SERVER_CONNECTION = "gcm.googleapis.com";


    //**** TESTING ******///
//    public static final String fcmServerKey = "AAAAxAdWpgA:APA91bGIEFP9luVUK22vvQUr_ova-LQgcQfBRNM3V8TujDMf81hHSg53-Qp7CV8rq06ijctZYOR3B9xukTsIP11jGRssfrPaANIkwazNuCXRqCBgH7v9VjJfWIYo8mwknQJUKkC7LChH";
//    public static final String fcmProjectSenderId = "841936709120";  // mpfcm-test  FirebaseProject Name


    //****  LIVE ******///
    public static final String fcmServerKey = "AAAAQFmA90I:APA91bFC9F_w10Is2GIn8oj1FJ9ZffAaOJcv6BAs3_nLcyELhnX3u9CsI1pJF_-mV13jvkgfnzN-MZtQxiNmTirhsh9nn23xjcHr7d1UZX3e9nboyUo52pDH3OWKh2AbuwtztMGrzv-9";
    public static final String fcmProjectSenderId = "276379531074";  // mpConsumerApp3 FirebaseProject Name





    public static final String RABBITMQ_HOST_PRIVATE = "10.130.13.40";   // p.rabbitmq02.do-sg.mpgps.aspade.in
//    public static final String RABBITMQ_HOST_PRIVATE = "localhost";   // local host
    public static final String RABBITMQ_USERNAME = "webcelery";
    public static final String RABBITMQ_PASSWORD = "matchpoint@123";
    public static final String RABBITMQ_VHOST = "web-host";

    // For the processor factory
    public static final String PACKAGE = "com.matchpointgps.fcm";
    public static final String BACKEND_ACTION_REGISTER = PACKAGE + ".REGISTER";
    public static final String BACKEND_ACTION_ECHO = PACKAGE + ".ECHO";
    public static final String BACKEND_ACTION_MESSAGE = PACKAGE + ".MESSAGE";

    // For the app common payload message attributes (android - xmpp server)
    public static final String PAYLOAD_ATTRIBUTE_MESSAGE = "data";
    public static final String PAYLOAD_ATTRIBUTE_MESSAGE_ID = "message_id";
    public static final String PAYLOAD_ATTRIBUTE_ACTION = "action";
    public static final String PAYLOAD_ATTRIBUTE_RECIPIENT = "recipient";
    public static final String PAYLOAD_ATTRIBUTE_ACCOUNT = "account";

    /**
     * Returns a random message id to uniquely identify a message
     */
    public static String getUniqueMessageId() {
        // TODO: replace for your own random message ID that the DB generates
        return "m-" + UUID.randomUUID();
    }

    public static final String[][] ACTION_TYPE = {{"0", "DEVICE"}, {"1", "GROUP"}};
    public static final String[][] OS_TYPE = {
        {"0", "ANDROID"},
        {"1", "IOS"},
        {"2", "WINDOWS"}};

}
