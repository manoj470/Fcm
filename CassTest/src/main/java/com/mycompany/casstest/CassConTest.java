/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.casstest;

import com.datastax.driver.core.DataType;
import java.sql.Timestamp;
import java.util.Date;


/**
 *
 * @author aspade
 */
public class CassConTest {
    public static CassCon client = new CassCon();
    public static void main(String[] args){
                final String ipAddress = "192.168.0.25";
                final int port = 9042;
                System.out.println("Connecting to IP Address " + ipAddress + ":" + port + "...");
                client.connect(ipAddress, port);
                String ts= "2017-01-22 00:00:00";
               // long dt = Date.UTC(2017, 02, 22, 00, 00, 00);
              //  Timestamp ts = new Timestamp(dt);
                //CsQuery cql = new CsQuery();
                CsQuery.update_on_fcm_ack(ts);
                //client.close();
    }
}
