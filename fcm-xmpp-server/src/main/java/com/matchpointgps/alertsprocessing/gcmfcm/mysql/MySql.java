/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.matchpointgps.alertsprocessing.gcmfcm.mysql;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.Duration;
import java.time.Instant;

import com.matchpointgps.alertsprocessing.gcmfcm.EntryPointKafka;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Last edited on 30/06/2017 by - Manish Jaiswal
 *
 * @Created on 20/04/2017
 * @author aspade
 */
public class MySql {

    final Logger AlertErrlogger = Logger.getLogger("AlertErr");
    final Logger timelog = Logger.getLogger("time_logger");


    public List<Map<String, String>> AuthUser(int userid) {
        List<Map<String, String>> data;
        try {
            //String query = "select * from auth_user where id=" + userid;
            String query = MySqlQueryGenerator.findById(MySqlConstants.Tables.AuthUser.TABLE_NAME,MySqlConstants.Tables.AuthUser.COLUMN_ID,userid);
            data = getMySqlData(query);
            return data;
        } catch (Exception e) {
            AlertErrlogger.log(Level.ERROR, "Error in AuthUser " + e);
        }
        return null;
    }

    // mp_fcm_xmpp_alerts_triggered table query structure
    public List<Map<String, String>> FcmDevices(int userid) {
        List<Map<String, String>> data = new ArrayList<>();
        try {
//            String query = "select * from mp_fcm_tokens where user_id =" + userid;
            String query = MySqlQueryGenerator.findById(MySqlConstants.Tables.FcmTokens.TABLE_NAME,
                    MySqlConstants.Tables.FcmTokens.COLUMN_USER_ID,userid);
            data = getMySqlData(query);
        } catch (Exception e) {
            AlertErrlogger.log(Level.ERROR, "Error in FcmDevices " + e);
        }
        return data;
    }

    // mp_fcm_xmpp_alerts_triggered table query structure
    public List<Map<String, String>> WebInstance(int userid) {
        List<Map<String, String>> data = new ArrayList<>();
        try {
//            String query = "select * from mp_gcm_web_app_instance where user_id =" + userid;
            String query = MySqlQueryGenerator.findById(MySqlConstants.Tables.GcmWebInstance.TABLE_NAME,
                    MySqlConstants.Tables.GcmWebInstance.COLUMN_USER_ID,userid);
            data = getMySqlData(query);
        } catch (Exception e) {
            AlertErrlogger.log(Level.ERROR, "Error in FcmDevices " + e);
        }
        return data;
    }

    public List<Map<String, String>> GcmDevices(int userid) {
        List<Map<String, String>> data = new ArrayList<>();
        try {
//            String query = "select * from mp_gcm_devices where user_id=" + userid;
            String query = MySqlQueryGenerator.findById(MySqlConstants.Tables.GcmDevices.TABLE_NAME,
                    MySqlConstants.Tables.GcmDevices.COLUMN_USER_ID,userid);
            data = getMySqlData(query);
        } catch (Exception e) {
            AlertErrlogger.log(Level.ERROR, "gcm device query error : " + e);
        }
        return data;
    }

    public List<Map<String, String>> GcmDeviceGroup(int userid) {
        List<Map<String, String>> data = new ArrayList<>();
        try {
//            String query = "select * from mp_gcm_device_group where user_id=" + userid;
            String query = MySqlQueryGenerator.findById(MySqlConstants.Tables.GcmDeviceGroup.TABLE_NAME,
                    MySqlConstants.Tables.GcmDeviceGroup.COLUMN_USER_ID,userid);
            data = getMySqlData(query);
        } catch (Exception e) {
            AlertErrlogger.log(Level.ERROR, "gcm device query error : " + e);
        }
        return data;
    }



    private List<Map<String, String>> getMySqlData(String query) {
        List<Map<String, String>> data = new ArrayList<>();
        Map<String, String> rows;
        try {
            Connection connection = EntryPointKafka.dsc.getConnection();
            PreparedStatement stmt1 = connection.prepareStatement(query);
            Instant a1 = Instant.now();
            ResultSet rs = stmt1.executeQuery();
            Instant b1 = Instant.now();
            timelog.log(Level.INFO, "query get_mysql_data : " + Duration.between(a1, b1).toMillis()+" by thread " + Thread.currentThread());
            ResultSetMetaData metaData = rs.getMetaData();
            List<String> columns = new ArrayList<>(metaData.getColumnCount());
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                columns.add(metaData.getColumnName(i));
            }
            while (rs.next()) {
                rows = new HashMap<>();
                for (String col : columns) {
                    rows.put(col, rs.getString(col));
                }
                data.add(rows);
            }
            rs.close();
            connection.commit();
            if (!stmt1.isClosed())
                stmt1.close();
            if (!connection.isClosed())
                connection.close();
        } catch (Exception e) {
            AlertErrlogger.log(Level.ERROR, "Error in get_mysql_data error : " + e);
        }
        return data;
    }





    /////*************************** NOT BEING USED AT THE MOMENT ************************************** ////////

    /*

    private void delete_mysql_data(String query) {
        try {
            Connection connection = EntryPointKafka.dsc.getConnection();
            PreparedStatement stmt1 = connection.prepareStatement(query);
            Instant a1 = Instant.now();
            stmt1.execute();
            Instant b1 = Instant.now();
            timelog.log(Level.INFO, "query get_mysql_data : " + Duration.between(a1, b1).toMillis() + " by thread " + Thread.currentThread());
            connection.commit();
            if (!stmt1.isClosed()) {
                stmt1.close();
            }
            if (!connection.isClosed()) {
                connection.close();
            }
        } catch (Exception e) {
            AlertErrlogger.log(Level.ERROR, "Error in get_mysql_data error : " + e);
        }
    }
    public List<Map<String, String>> userGroupQuery(int userId, String[] groupName) {
        List<Map<String, String>> data = new ArrayList<>();
        StringBuilder GroupIN = new StringBuilder();
        int counter = 1;
        for (String x : groupName) {
            if (counter++ != groupName.length) {
                GroupIN.append("'").append(x).append("',");
            } else {
                GroupIN.append("'").append(x);
            }
        }
        GroupIN.append("'");
        try {
            String query = "SELECT auth_group.id, auth_group.name,auth_user.id,auth_user.username FROM auth_group INNER JOIN auth_user_groups ON "
                    + "(auth_group.id = auth_user_groups.group_id) "
                    + "INNER JOIN auth_user ON "
                    + "(auth_user.id = auth_user_groups.user_id) "
                    + "WHERE (auth_user_groups.user_id =" + userId + " AND auth_group.name in ("
                    + GroupIN
                    + "))";
            data = getMySqlData(query);
        } catch (Exception ex) {
            AlertErrlogger.log(Level.ERROR, "Error in usergroup : " + ex);
        }
        return data;
    }



    public List<Map<String, String>> UserAddress(int userId) {
        List<Map<String, String>> data = new ArrayList<>();
        try {
            String query = "select * from mp_user_address where id=" + userId + ";";
            data = getMySqlData(query);
        } catch (Exception e) {
            AlertErrlogger.log(Level.ERROR, "Error in userAddress : " + e);
        }
        return data;
    }



    public List<Map<String, String>> Vehicle(int serial_number) {
        List<Map<String, String>> data = new ArrayList<>();
        Map<String, String> rows;
        try {
            String query = "select * from mp_vehicle where serial_number_id=" + serial_number;
            data = getMySqlData(query);
        } catch (Exception e) {
            AlertErrlogger.log(Level.ERROR, "Error in vehicle query : " + e);

        }
        return data;
    }

    public List<Map<String, String>> Vehicle(int serial_number, int userid) {
        List<Map<String, String>> data = new ArrayList<>();
        try {
            String query = "select * from mp_vehicle where serial_number_id=" + serial_number + " AND owner_id=" + userid;
            data = getMySqlData(query);
        } catch (Exception e) {
            AlertErrlogger.log(Level.ERROR, "Error in vehicle : " + e);
        }
        return data;
    }






    public List<Map<String, String>> AuthUser(List<String> emailId) {        
        StringBuilder EmailIN = new StringBuilder();
        int counter = 1;
        for (String x : emailId) {
            if (counter++ != emailId.size()) {
                EmailIN.append("'").append(x).append("',");
            } else {
                EmailIN.append("'").append(x);
            }
        }
        EmailIN.append("'");
        try {
                String query = "select * from auth_user where email in(" + EmailIN + ")";
            return getMySqlData(query);
        } catch (Exception e) {
            AlertErrlogger.log(Level.ERROR, "Error in AuthUser " + e);
        }
        return Collections.emptyList();
    }

    public void AlertsTriggeredInsert(int alert_type, String value, int serial_number, String triggered_on, int userid, int status) {
        try {
            Connection connct = EntryPointKafka.dsc.getConnection();
            PreparedStatement stmt1 = connct.prepareStatement("Insert into mp_alerts_triggered (alert_type,value,serial_number,triggered_on,"
                    + "user_id,status) VALUES (?,?,?,?,?,?)");
            stmt1.setInt(1, alert_type);
            stmt1.setObject(2, value);
            stmt1.setInt(3, serial_number);
            stmt1.setString(4, triggered_on);
            stmt1.setInt(5, userid);
            stmt1.setInt(6, status);

            Instant a1 = Instant.now();
            Boolean rs = stmt1.execute();
            Instant b1 = Instant.now();
            timelog.log(Level.INFO, "save AlertsTriggered : " + Duration.between(a1, b1).toMillis());
            rs = true;
            connct.commit();
            if (!stmt1.isClosed()) {
                stmt1.close();
            }
            if (!connct.isClosed()) {
                connct.close();
            }
        } catch (Exception e) {
            AlertErrlogger.log(Level.ERROR, "Error in AlertsTriggeredInsert " + e);
        }
    }

    public List<Map<String, String>> UserProfile(int userid) {
        List<Map<String, String>> data = new ArrayList<>();
        try {
            String query = "select * from mp_user_profile where user_id=" + userid;
            data = getMySqlData(query);
        } catch (Exception e) {
            AlertErrlogger.log(Level.ERROR, "Error in UserProfile " + e);
        }

        return data;
    }



    public List<Map<String, String>> FcmDevices_byToken(String token) {
        List<Map<String, String>> data = new ArrayList<>();
        try {

            String query = "select * from mp_fcm_devices where token_id ='" + token + "';";
            data = getMySqlData(query);
        } catch (Exception e) {
            AlertErrlogger.log(Level.ERROR, "Error in FcmDevices " + e);
        }
        return data;
    }

    public List<Map<String, String>> FcmDevices_byGroupId(int groupId) {
        List<Map<String, String>> data = new ArrayList<>();
        try {

            String query = "select * from mp_fcm_devices where group_id=" + groupId;
            data = getMySqlData(query);
        } catch (Exception e) {
            AlertErrlogger.log(Level.ERROR, "Error in FcmDevices " + e);
        }
        return data;
    }

    public boolean DeleteToken_fcmDevices(String token) {
        boolean flag = false;
        try {
            String query = "delete from mp_fcm_devices where token_id ='" + token + "';";
            delete_mysql_data(query);
            flag = true;
        } catch (Exception e) {
            AlertErrlogger.log(Level.ERROR, "Error in FcmDevices " + e);
        }
        return flag;
    }

    public List<Map<String, String>> FcmDeviceGroup_byToken(String token) {
        List<Map<String, String>> data = new ArrayList<>();
        try {
            String query = "select * from mp_fcm_device_group WHERE notification_key='" + token + "';";
            data = getMySqlData(query);
        } catch (Exception e) {
            AlertErrlogger.log(Level.ERROR, "Error in FcmDevices " + e);
        }
        return data;
    }

    public List<Map<String, String>> FcmDeviceGroup_byGroupId(int groupId) {
        List<Map<String, String>> data = new ArrayList<>();
        try {
            String query = "select * from mp_fcm_device_group WHERE group_id=" + groupId;
            data = getMySqlData(query);
        } catch (Exception e) {
            AlertErrlogger.log(Level.ERROR, "Error in FcmDevices " + e);
        }
        return data;
    }

    public List<Map<String, String>> FcmDeviceGroup_byUserId(int userID) {
        List<Map<String, String>> data = new ArrayList<>();
        try {
            String query = "select * from mp_fcm_device_group WHERE user_id=" + userID;
            data = getMySqlData(query);
        } catch (Exception e) {
            AlertErrlogger.log(Level.ERROR, "Error in FcmDevices " + e);
        }
        return data;
    }

    public Boolean DeleteToken_fcmDeviceGroup(String token) {
        boolean flag = false;
        try {

            String query = "delete from mp_fcm_device_group WHERE notification_key= '" + token + "';";
            delete_mysql_data(query);
            flag = true;
        } catch (Exception e) {
            AlertErrlogger.log(Level.ERROR, "Error in FcmDevices " + e);
        }
        return flag;
    }

    public void insertDeletedFcmId(int userid, String tokenId, int actionType, int osType, String product) {
        boolean flag = false;
        try {
            Connection connection = EntryPointKafka.dsc.getConnection();
            PreparedStatement stmt1 = connection.prepareStatement("Insert into mp_fcm_device_group (user_id,fcm_id,action_type,os_type,"
                    + "product) VALUES (?,?,?,?,?)");
            stmt1.setInt(1, userid);
            stmt1.setInt(3, actionType);
            stmt1.setInt(4, osType);
            stmt1.setString(2, tokenId);
            stmt1.setString(5, product);

            Instant a1 = Instant.now();
            Boolean rs = stmt1.execute();

            Instant b1 = Instant.now();
            timelog.log(Level.INFO, "save DeletedFcmId : " + Duration.between(a1, b1).toMillis());
            rs = true;
            connection.commit();
            if (!stmt1.isClosed()) {
                stmt1.close();
            }
            if (!connection.isClosed()) {
                connection.close();
            }
        } catch (Exception e) {
            AlertErrlogger.log(Level.ERROR, "Error in AlertsTriggeredInsert " + e);
        }
    }



    public List<Map<String, String>> userMobileNumbers(int userId) {
        List<Map<String, String>> data = new ArrayList<>();
        try {
            String query = "select * from mp_user_mobile_numbers where user_id=" + userId;
            data = getMySqlData(query);
        } catch (Exception e) {
            AlertErrlogger.log(Level.ERROR, "Error in mobile_no query : " + e);
        }
        return data;
    }

    public List<Map<String, String>> userMobileNumbers(String rmn1, String rmn2, String rmn3) {
        List<Map<String, String>> data = new ArrayList<>();
        Map<String, String> rows;
        try {
            String query = "select user_id,username from  mp_user_mobile_numbers INNER JOIN auth_user ON (auth_user.id =user_id) "
                    + "where rmn_1 = '" + rmn1 + "' or rmn_2 = '" + rmn2 + "' or rmn_3 = '" + rmn3;
            data = getMySqlData(query);
            System.out.println("userMobileNumbers " + data);
        } catch (Exception e) {
            AlertErrlogger.log(Level.ERROR, "Error in mobile_no query : " + e);
        }

        return data;
    }

    public List<Map<String, String>> msgTypeTag(String alert, String speed) {
        List<Map<String, String>> data = new ArrayList<>();
        try {
            String query = "select * from mp_message_type_tag where type_name='" + alert + "' and tag_name='" + speed + "'";
            data = getMySqlData(query);
            System.out.println("msgTypeTag " + data);
        } catch (Exception e) {
            AlertErrlogger.log(Level.ERROR, "Error in msgTypeTag : " + e);
        }

        return data;
    }

    */

}
