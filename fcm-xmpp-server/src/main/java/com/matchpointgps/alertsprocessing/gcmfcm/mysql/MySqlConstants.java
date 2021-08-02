package com.matchpointgps.alertsprocessing.gcmfcm.mysql;

public interface MySqlConstants {

    interface QUERIES{
        String SELECT_QUERY_BY_ID = "select * from %s where %s=%s";
        String SELECT_QUERY_ALL = "select * from %s";
    }


    interface Tables{

        interface MpUserAddress{
            String TABLE_NAME = "mp_user_address";
            String COLUMN_ID = "id";
        }

        interface AuthUser{
            String TABLE_NAME = "auth_user";
            String COLUMN_ID = "id";
        }

        interface FcmTokens{
            String TABLE_NAME = "mp_fcm_tokens";
            String COLUMN_USER_ID = "user_id";
        }

        interface GcmWebInstance{
            String TABLE_NAME = "mp_gcm_web_app_instance";
            String COLUMN_USER_ID = "user_id";
        }

        interface GcmDevices{
            String TABLE_NAME = "mp_gcm_devices";
            String COLUMN_USER_ID = "user_id";
        }

        interface GcmDeviceGroup{
            String TABLE_NAME = "mp_gcm_device_group";
            String COLUMN_USER_ID = "user_id";
        }




    }









}
