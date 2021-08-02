package com.matchpointgps.fcm.xmpp.server;

import com.matchpointgps.alertsprocessing.gcmfcm.EntryPointKafka;
import static com.matchpointgps.alertsprocessing.gcmfcm.EntryPointKafka.dynmConfig;
import com.matchpointgps.fcm.xmpp.rest.RestClient;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.matchpointgps.fcm.xmpp.rest.RestRepository;
import org.apache.log4j.Logger;

import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.sm.predicates.ForEveryStanza;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import com.matchpointgps.fcm.xmpp.bean.CcsInMessage;
import com.matchpointgps.fcm.xmpp.bean.CcsOutMessage;
import com.matchpointgps.fcm.xmpp.service.PayloadProcessor;
import com.matchpointgps.fcm.xmpp.util.Util;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedList;
import java.util.TimeZone;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.log4j.Level;
import org.json.JSONObject;

/**
 * Sample Smack implementation of a client for FCM Cloud Connection Server. Most
 * of it has been taken more or less verbatim from Google's documentation:
 * https://firebase.google.com/docs/cloud-messaging/xmpp-server-ref
 */
public class CcsClient implements StanzaListener {

    private static CcsClient sInstance = null;
//    private XMPPTCPConnection connection;
    private String mApiKey = null;
    private boolean mDebuggable = false;
    private String fcmServerUsername = null;
    public static final Logger Conlogger = EntryPointKafka.Conlogger;
    public static final Logger Uplogger = EntryPointKafka.uplogger;
    public static final Logger Erlogger = EntryPointKafka.Erlogger;
    public static final Logger Acklogger = EntryPointKafka.acklogger;
    public static final Logger Nacklogger = EntryPointKafka.nacklogger;
    public static final Logger deliLogger = Logger.getLogger("deli_logger");
    final LinkedList<XMPPTCPConnection> connPool = new LinkedList<XMPPTCPConnection>();

    public static CcsClient getInstance() {

        if (sInstance == null) {
            throw new IllegalStateException("You have to prepare the client first");
        }
        return sInstance;
    }

    public static CcsClient prepareClient(String projectId, String apiKey, boolean debuggable) {

        synchronized (CcsClient.class) {
            if (sInstance == null) {
                sInstance = new CcsClient(projectId, apiKey, debuggable);
            }
        }
        return sInstance;
    }

    private CcsClient(String projectId, String apiKey, boolean debuggable) {
        this();
        mApiKey = apiKey;
        mDebuggable = debuggable;
        fcmServerUsername = projectId + "@" + Util.FCM_SERVER_CONNECTION;
    }

    private CcsClient() {
        // Add GcmPacketExtension
        ProviderManager.addExtensionProvider(Util.FCM_ELEMENT_NAME, Util.FCM_NAMESPACE,
                new ExtensionElementProvider<GcmPacketExtension>() {
            @Override
            public GcmPacketExtension parse(XmlPullParser parser, int initialDepth)
                    throws XmlPullParserException, IOException, SmackException {
                String json = parser.nextText();
                return new GcmPacketExtension(json);
            }
        });
    }

    /**
     * Connects to FCM Cloud Connection Server using the supplied credentials
     *
     * @throws org.jivesoftware.smack.XMPPException
     * @throws org.jivesoftware.smack.SmackException
     * @throws java.io.IOException
     */
    public void connect() throws XMPPException, SmackException, IOException {
        Instant a = Instant.now();
        int size = connPool.size();

        XMPPTCPConnection.setUseStreamManagementResumptionDefault(true);
        XMPPTCPConnection.setUseStreamManagementDefault(true);

        XMPPTCPConnectionConfiguration.Builder config = XMPPTCPConnectionConfiguration.builder();
        config.setServiceName("FCM XMPP Client Connection Server");
        config.setHost(Util.FCM_SERVER);
        config.setPort(Util.FCM_PORT);
        config.setSecurityMode(SecurityMode.ifpossible);
        config.setSendPresence(false);

        config.setSocketFactory(SSLSocketFactory.getDefault());
        // Launch a window with info about packets sent and received
        config.setDebuggerEnabled(mDebuggable);
//        for (int i = 0; connPool.size() <= dynmConfig.FCM_CONNECTION; i++) {
        while (connPool.size() <= dynmConfig.FCM_CONNECTION) {
            // Connect
            try {
                // Create the connection
                XMPPTCPConnection connect = new XMPPTCPConnection(config.build());

                connect.connect();

//                ProviderManager.addExtensionProvider(DeliveryReceipt.ELEMENT, DeliveryReceipt.NAMESPACE, new DeliveryReceipt.Provider());
//                ProviderManager.addExtensionProvider(DeliveryReceiptRequest.ELEMENT, new DeliveryReceiptRequest().getNamespace(), new DeliveryReceiptRequest.Provider());
//
//                DeliveryReceiptManager.getInstanceFor(connect).addReceiptReceivedListener(
//                        (String fromJid, String toJid, String receiptId, Stanza receipt) -> {
//                            System.out.println(">>>" + fromJid);
//                            System.out.println(">>>" + toJid);
//                            System.out.println(">>>" + "PACKED GOT--" + receiptId);
//                        });
                // Enable automatic reconnection
                ReconnectionManager.getInstanceFor(connect).enableAutomaticReconnection();

                ReconnectionManager.setEnabledPerDefault(true);
                // Handle reconnection and connection errors
                connect.addConnectionListener(new ConnectionListener() {

                    @Override
                    public void reconnectionSuccessful() {
                        Conlogger.log(Level.INFO, "Reconnection successful ...");
                        // TODO: handle the reconnecting successful
                    }

                    @Override
                    public void reconnectionFailed(Exception e) {
                        Conlogger.log(Level.INFO, "Reconnection failed: " + e.getMessage());
                        // TODO: handle the reconnection failed
                    }

                    @Override
                    public void reconnectingIn(int seconds) {
                        Conlogger.log(Level.INFO, "Reconnecting in %d secs " + seconds);
                        // TODO: handle the reconnecting in
                    }

                    @Override
                    public void connectionClosedOnError(Exception e) {
                        Conlogger.log(Level.INFO, "Connection closed on error");
                        // TODO: handle the connection closed on error
                    }

                    @Override
                    public void connectionClosed() {
                        Conlogger.log(Level.INFO, "Connection closed");
                        // TODO: handle the connection closed
                    }

                    @Override
                    public void authenticated(XMPPConnection arg0, boolean arg1) {
                        Conlogger.log(Level.INFO, "User authenticated");
                        // TODO: handle the authentication
                    }

                    @Override
                    public void connected(XMPPConnection arg0) {
                        Conlogger.log(Level.INFO, "Connection established");
                        // TODO: handle the connection
                    }
                });

                // Handle incoming packets (the class implements the StanzaListener)
                connect.addAsyncStanzaListener(this, new StanzaFilter() {
                    @Override
                    public boolean accept(Stanza stanza) {
                        return stanza.hasExtension(Util.FCM_ELEMENT_NAME, Util.FCM_NAMESPACE);
                    }
                });

                // Log all outgoing packets
                connect.addPacketInterceptor(new StanzaListener() {
                    @Override
                    public void processPacket(Stanza stanza) throws NotConnectedException {
                        //      logger.log(Level.INFO, "Sent: {}", stanza.toXML());
                    }
                }, ForEveryStanza.INSTANCE);

                connect.login(fcmServerUsername, mApiKey);
                connPool.add(connect);
                Conlogger.log(Level.INFO, "Logged in: " + fcmServerUsername);
                Conlogger.log(Level.INFO, "Created Connection " + (connPool.size() - size) + " in " + Duration.between(a, Instant.now()).toMillis());
            } catch (Exception ex) {
                Erlogger.log(Level.INFO, "Login failed: " + fcmServerUsername);
            }
        }
    }

    public void reconnect() {
        while (true) {
            try {
                connect();
                return;
            } catch (XMPPException | SmackException | IOException e) {
                Conlogger.log(Level.INFO, "Connecting again to FCM (manual reconnection)");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    Erlogger.log(Level.ERROR, e1);
                }
            }
        }
    }

    public synchronized XMPPTCPConnection getXmppConnection() {
        if (connPool.isEmpty()) {
            try {
                connect();
            } catch (SmackException | IOException | XMPPException ex) {
                Erlogger.log(Level.INFO, "error in pool connection" + ex);
            }
        }
        return connPool.pop();
    }

    public synchronized void returnXmppConnection(XMPPTCPConnection connection) {
        connPool.push(connection);
    }

    /**
     * Handles incoming messages
     *
     * @param packet
     */
    @SuppressWarnings("unchecked")
    @Override
    public void processPacket(Stanza packet) {
        Uplogger.log(Level.INFO, "Received: " + packet.toXML());

        GcmPacketExtension gcmPacket = (GcmPacketExtension) packet.getExtension(Util.FCM_NAMESPACE);
        String json = gcmPacket.getJson();
        try {
            Map<String, Object> jsonMap = (Map<String, Object>) JSONValue.parseWithException(json);
            Object messageType = jsonMap.get("message_type");

            if (messageType == null) {
                CcsInMessage inMessage = MessageHelper.createCcsInMessage(jsonMap);
                handleUpstreamMessage(inMessage); // normal upstream message
                return;
            }

            switch (messageType.toString()) {
                case "ack":
                    handleAckReceipt(jsonMap);
//                    Acklogger.log(Level.INFO,jsonMap);
                    break;
                case "nack":
                    handleNackReceipt(jsonMap);
                    break;
                case "receipt":
                    handleDeliveryReceipt(jsonMap);

                    /*delivered*/
                    break;
                case "response":
                    handleDeliveryResponse(jsonMap);
                    /*delivered*/
                    break;
                case "control":
                    handleControlMessage(jsonMap);
                    break;
                default:
                    Uplogger.log(Level.INFO, "Received unknown FCM message type: " + messageType);
            }
        } catch (ParseException e) {
//            System.out.println("exceptions>>>>" + e);
            Erlogger.log(Level.INFO, "Error parsing JSON: " + json + " error msg: " + e.getMessage());
        }

    }

    /**
     * Handles an upstream message from a device client through FCM
     */
    private void handleUpstreamMessage(CcsInMessage inMessage) {
        final String action = inMessage.getDataPayload().get(Util.PAYLOAD_ATTRIBUTE_ACTION);
        if (action != null) {
            PayloadProcessor processor = ProcessorFactory.getProcessor(action);
            processor.handleMessage(inMessage);
        }

        // Send ACK to FCM
        String ack = MessageHelper.createJsonAck(inMessage.getFrom(), inMessage.getMessageId());
        send(ack);

        System.out.println("saving state of upstream message>>" + inMessage.getDataPayload());
        Cquery.update_device_nt(inMessage.getDataPayload().get("received_message_id"),
                inMessage.getFrom(), Util.RS_Staus);

    }

    /**
     * Handles an ACK message from FCM
     */
    private void handleAckReceipt(Map<String, Object> jsonMap) {
        String msg_id = jsonMap.get("message_id").toString();
        String message_type = jsonMap.get("message_type").toString();
        if (jsonMap.containsKey("success")) {
            /*updaet group*/
            String grp = jsonMap.get("from").toString();
//            String msg_id = jsonMap.get("message_id").toString();
            Cquery.update_grp(msg_id, grp, Util.Ack_Staus);
//            System.out.println("in Group");
        } else {
            String token = jsonMap.get("from").toString();
//            String msg_id = jsonMap.get("message_id").toString();

            Cquery.update_device_nt(msg_id, token, Util.Ack_Staus);
//            System.out.println("in Device");
        }
        Acklogger.log(Level.INFO, " - MessageType: " + message_type + " - MessageId: " + msg_id + " - Reason: " + "SENT SUCCESSFULLY" + " - InstanceId: "
                + jsonMap.get("from").toString());
    }

    /**
     * Handles a NACK message from FCM
     */
    private void handleNackReceipt(Map<String, Object> jsonMap) {
        String errorCode = (String) jsonMap.get("error");

        if (errorCode == null) {
            Erlogger.log(Level.INFO, "Received null FCM Error Code");
            return;
        }
        Acklogger.log(Level.INFO, " - MessageType: " + jsonMap.get("message_type") + " - MessageId: " + jsonMap.get("message_id") + " - Reason: " + errorCode
                + " - InstanceId: " + jsonMap.get("from").toString());
        switch (errorCode) {
            case "INVALID_JSON":
                handleUnrecoverableFailure(jsonMap);
                break;
            case "BAD_REGISTRATION":
                /* update in database and curl request to app server*/
                handleFailReg(jsonMap);
                //handleUnrecoverableFailure(jsonMap);
                break;
            case "DEVICE_UNREGISTERED":
                /* update in database and curl request to app server*/
                handleFailReg(jsonMap);
                //handleUnrecoverableFailure(jsonMap);
                break;
            case "BAD_ACK":
                handleUnrecoverableFailure(jsonMap);
                break;
            case "SERVICE_UNAVAILABLE":
                handleServerFailure(jsonMap);
                break;
            case "INTERNAL_SERVER_ERROR":
                handleServerFailure(jsonMap);
                break;
            case "DEVICE_MESSAGE_RATE_EXCEEDED":
                handleUnrecoverableFailure(jsonMap);
                break;
            case "TOPICS_MESSAGE_RATE_EXCEEDED":
                handleUnrecoverableFailure(jsonMap);
                break;
            case "CONNECTION_DRAINING":
                handleConnectionDrainingFailure();
                break;
            default:
                Erlogger.log(Level.INFO, "Received unknown FCM Error Code: " + errorCode);
        }
    }

    /**
     * Handles a Delivery Receipt message from FCM (when a device confirms that
     * it received a particular message)
     */
    private void handleDeliveryReceipt(Map<String, Object> jsonMap) {
        // TODO: handle the delivery receipt

        Map data = (Map) jsonMap.get("data");
        String msg_id = data.get("original_message_id").toString();
        String token = (String) data.get("device_registration_id");
        Date deliveredDateTime = new Date(Long.parseLong((String) data.get("message_sent_timestamp")));
//        System.out.println("************************************ " + deliveredDateTime + " ***************************************************");
        List delivered = Cquery.updateDeviceNotiDelivered(msg_id, token, Util.RS_Staus, deliveredDateTime);
        //        System.out.println("in Device");
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String event = null, ackTime = null;
        if (!delivered.isEmpty()) {
            if (delivered.get(0) != null) {
                Date et = (Date) delivered.get(0);
                event = sdf.format(et);
            }
            if (delivered.get(1) != null) {
                Date dt = (Date) delivered.get(1);
                ackTime = sdf.format(dt);
            } else if (delivered.get(1) == null) {
                ackTime = sdf.format(deliveredDateTime);
            }
        }

        deliLogger.log(Level.INFO, "- MessageType: " + jsonMap.get("message_type") + ", MessageId: " + msg_id + ", InstanceId: " + token
                + ", eventTime: " + event
                + ", deliveredDateTime: " + ackTime
                + ", deviceRecvTime: " + sdf.format(deliveredDateTime)
        );

    }

    /*Handles a Response on delivery*/
    private void handleDeliveryResponse(Map<String, Object> jsonMap) {
        Map data = (Map) jsonMap.get("data");
        String msg_id = data.get("original_message_id").toString();
        String token = (String) data.get("device_registration_id");
        Date deliveredDateTime = new Date(Long.parseLong((String) data.get("message_sent_timestamp")));
//        System.out.println("************************************ " + deliveredDateTime + " ***************************************************");
        List delivered = Cquery.updateDeviceNotiDelivered(msg_id, token, Util.RS_Staus, deliveredDateTime);
        Erlogger.log(Level.INFO, "Getting message type RESPONSE from FCM" + jsonMap);
        Acklogger.log(Level.INFO, " - MessageType: " + jsonMap.get("message_type") + " - MessageId: " + jsonMap.get("message_id") + " - Reason: " + Util.RS_Staus + " - InstanceId: " + jsonMap.get("from"));
        // TODO: handle the delivery receipt

    }

    /**
     * Handles a Control message from FCM
     */
    private void handleControlMessage(Map<String, Object> jsonMap) {
        // TODO: handle the control message
        String controlType = (String) jsonMap.get("control_type");
        Acklogger.log(Level.INFO, " - MessageType: " + jsonMap.get("message_type") + " - MessageId: " + jsonMap.get("message_id") + " - Reason: " + controlType + " - InstanceId: " + jsonMap.get("from"));
        if (controlType.equals("CONNECTION_DRAINING")) {
            handleConnectionDrainingFailure();
        } else {
            Nacklogger.log(Level.INFO, "Received unknown FCM Control message: " + controlType);
        }
    }

    private void handleServerFailure(Map<String, Object> jsonMap) {
        // TODO: Resend the message
        String msg_id = jsonMap.get("message_id").toString();
        String token = (String) jsonMap.get("from");
        List delivered = Cquery.updateDeviceNotiDelivered(msg_id, token, jsonMap.get("error").toString(), new Date());
        Nacklogger.log(Level.INFO, "Server error: " + jsonMap.get("error") + " -> " + jsonMap.get("error_description") + " -> " + jsonMap);

    }

    private void handleUnrecoverableFailure(Map<String, Object> jsonMap) {
        // TODO: handle the unrecoverable failure

        String msg_id = jsonMap.get("message_id").toString();
        String token = jsonMap.getOrDefault("from", "").toString();
        List delivered = Cquery.updateDeviceNotiDelivered(msg_id, token, jsonMap.get("error").toString(), new Date());
        Nacklogger.log(Level.INFO,
                "Unrecoverable error: " + jsonMap.get("error") + " -> " + jsonMap.get("error_description") + " -> " + jsonMap);
    }

    private void handleFailReg(Map<String, Object> jsonMap) {
        try {
            String token = (String) jsonMap.get("from");
            String url = dynmConfig.DeleteToken_URL;
            RestClient conn = new RestClient();
            new RestRepository().unregisterFCMTokenId(url,token);
            String msg_id = jsonMap.get("message_id").toString();
            List delivered = Cquery.updateDeviceNotiDelivered(msg_id, token, jsonMap.get("error").toString(), new Date());
            Erlogger.log(Level.INFO,
                    "Registration Error: " + jsonMap.get("error") + " -> " + jsonMap.get("error_description") + ", InstanceId: " + jsonMap.get("from").toString());
        } catch (Exception ex) {
            Erlogger.log(Level.INFO, " Error in FailReg" + ex);
        }
    }

    static {
        try {
            SslTrustCertificate();
        } catch (NoSuchAlgorithmException | KeyManagementException ex) {
            Erlogger.log(Level.INFO, "Error in SSLtrust" + ex);
        }
    }

    private static void SslTrustCertificate() throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                }
            }
        };

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HostnameVerifier hv = new HostnameVerifier() {
            @Override
            public boolean verify(String urlHostName, SSLSession session) {
                if (!urlHostName.equalsIgnoreCase(session.getPeerHost())) {
                    System.out.println("Warning: URL host '" + urlHostName + "' is different to SSLSession host '" + session.getPeerHost() + "'.");
                }
                return true;
            }
        };
        HttpsURLConnection.setDefaultHostnameVerifier(hv);
    }

    private void handleConnectionDrainingFailure() {
        // TODO: handle the connection draining failure. Force reconnect?
        Nacklogger.log(Level.INFO, "FCM Connection is draining! Initiating reconnection ...");
        reconnect();
    }

    /**
     * Sends a downstream message to FCM
     *
     * @param jsonRequest
     */
    public void send(String jsonRequest) {
        while (true) {
            Stanza request = new GcmPacketExtension(jsonRequest).toPacket();
            try {
                XMPPTCPConnection xmppConnection = getXmppConnection();
                int size = 0;
                while (!xmppConnection.isConnected()) {
                    xmppConnection = getXmppConnection();
                    if (!xmppConnection.isConnected()) {
                        connPool.remove(xmppConnection);
                        if (connPool.size()<= dynmConfig.FCM_CONNECTION) {
                            reconnect();
                        }
                    }
                    size++;
                    if (size == dynmConfig.FCM_CONNECTION) {
                        break;
                    }
                }

                xmppConnection.sendStanza(request);
                JSONObject jsonData = new JSONObject(jsonRequest);
                Cquery.update_device_nt(jsonData.getString("message_id"), jsonData.getString("to"), Util.SUBMIT_STATUS);

                System.out.println("size of pool " + connPool.size());
                returnXmppConnection(xmppConnection);
                break;
            } catch (NotConnectedException e) {
                Nacklogger.log(Level.INFO, "There is no connection and the packet could not be sent: {}" + request.toXML());
                Erlogger.log(Level.ERROR, "FCM SEND NOTIFICATION ERROR :" + e + ", \nstatckTrace" + e.getStackTrace()[0]);

            }
        }
    }

    /**
     * Sends a message to multiple recipients (list). Kind of like the old HTTP
     * message with the list of regIds in the "registration_ids" field.
     */
    public void sendBroadcast(CcsOutMessage outMessage, List<String> recipients) {
        Map<String, Object> map = MessageHelper.createAttributeMap(outMessage);
        for (String toRegId : recipients) {
            String messageId = Util.getUniqueMessageId();
            map.put("message_id", messageId);
            map.put("to", toRegId);
            String jsonRequest = MessageHelper.createJsonMessage(map);
            send(jsonRequest);
        }
    }

}
