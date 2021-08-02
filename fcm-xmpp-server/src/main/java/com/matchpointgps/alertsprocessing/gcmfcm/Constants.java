/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.matchpointgps.alertsprocessing.gcmfcm;

/**
 * Last edited on 21/06/2017 by - Manish Jaiswal
 *  
 * @author Manish Jaiswal
 * @Created on 07/04/2017
 */
public class Constants {

    public static final int CassTTL = 60 * 60 * 24 * 90;
    public static final int MsgCassTTL = 60 * 60 * 24;
    public static final int ALERT_TYPE_EXCESS_IDLING = 6;
    public static final int ALERT_TYPE_MPROTECT = 14;
    public static final int GROUP_PERIODIC = 6, GROUP_EXCESS_IDLING = 16;
    public static final int IGNITION_ON = 1;
    public static final String[][] METRICS = {{"1", "MILES"}, {"2", "KM"}};
    public static final int SPEED_ALERT_STATE_DEACTIVE = 2;
    public static final int SPEED_ALERT_STATE_INITIATED = 0;
    public static final int SPEED_ALERT_STATE_ACTIVE = 1;
    public static final int SPEED_ALERT_BACKOFF_SPEED_PERCENTAGE = 10;
    public static final int BOUNDARY_ALERT_ACTIVATE_HYSTERISIS_TIME_SEC = 30;
    public static final int BOUNDARY_ALERT_DEACTIVATE_HYSTERISIS_TIME_SEC = 30;
    public static final int ALERT_TYPE_BOUNDARY = 2;
    public static final int SPEED_ALERT_HYSTERISIS_TIME_SEC = 30;
    public static final int ALERT_TYPE_SPEED = 1;
    public static final String MSPEED_ALERT = "SPEED ALERT: >><< Speeding Over >><< Location: <<>>";
    public static final String XMPP_APP = "xmpp_app_user";
    public static final String SMS_PREFERENCE = "sms_preference";
    public static final String[][] NOTIFICATION_TYPE = {
        {"0", "NONE"},
        {"1", "SMS"},
        {"2", "EMAIL"},
        {"3", "BOTH"},};
    public static final String SMS_NOTIFICATION = NOTIFICATION_TYPE[1][0];
    public static final String EMAIL_NOTIFICATION = NOTIFICATION_TYPE[2][0];
    public static final String BOTH_NOTIFICATION = NOTIFICATION_TYPE[3][0];
    public static final String APP = "app_user";
    public static final String SMS_PROVIDER = "gupshup";
    public static final String GUPSHUP_URL = "http://enterprise.smsgupshup.com";
    public static final String[][] sms_alert_format = {
        {null, "SELECT SMS FORMAT"},
        {"1", "MAPLINK"},
        {"0", "ADDRESS"},};
    public static final String SMS_ALERT_FORMAT_MAPLINK = sms_alert_format[1][0];
    public static final String SMS_ALERT_FORMAT_ADDRESS = sms_alert_format[2][0];
    public static final String MAPLINK = "https://maps.google.com/maps?q=";
    public static final String SPEED = ">><< exceeded >><< km/h on >><< at >><< Now moving at >><< km/h.";
    public static final String SAVE_DB = "saveDB";
    public static final int CHECK_FOR_LATE_DATA = 20;
    
    public enum OS_TYPE {
        ANDROID(0),
        IOS(1),
        WEB(2);

        private final int value;

        OS_TYPE(int val) {
            this.value = val;
        }

        public int getValue() {
            return value;
        }
    }


    public enum GCM_TITLE {
        MPrt("mProtect Alert"),
        Spd("Speed Alert"),
        Idl("Excessive Idling Alert"),
        Geo("Boundary Alert");
        private final String value;

        GCM_TITLE(String val) {
            this.value = val;
        }

        public String getValue() {
            return value;
        }
    }

    public enum ALERT_TYPE {
        SPEED(1),
        GEO(2),
        IGN(4),
        SUMMARY(5),
        IDL(6),
        TOW(20),
        VOLT(26),
        SLEEP(11),
        MPROTECT(14),
        FUELTHEFT(36),
        REFUEL(37);
        private final int value;

        ALERT_TYPE(int val) {
            this.value = val;
        }

        public int getIntValue() {
            return value;
        }
    }

}
