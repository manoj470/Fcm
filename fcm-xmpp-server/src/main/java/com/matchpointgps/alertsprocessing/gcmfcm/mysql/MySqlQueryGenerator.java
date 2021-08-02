package com.matchpointgps.alertsprocessing.gcmfcm.mysql;

public class MySqlQueryGenerator {


    public static String findById(String tableName,String id,String valueToCompare){
        return String.format(MySqlConstants.QUERIES.SELECT_QUERY_BY_ID,tableName,id,valueToCompare);
    }


    public static String findById(String tableName,String id,int valueToCompare){
        return String.format(MySqlConstants.QUERIES.SELECT_QUERY_BY_ID,tableName,id,valueToCompare);
    }


    public static String findAll(String tableName){
        return String.format(MySqlConstants.QUERIES.SELECT_QUERY_ALL,tableName);
    }






}
