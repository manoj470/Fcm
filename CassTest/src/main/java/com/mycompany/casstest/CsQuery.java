/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.casstest;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import static com.datastax.driver.core.utils.UUIDs.random;
import static java.lang.Math.random;
import java.sql.Timestamp;
import java.util.List;

/**
 *
 * @author aspade
 */
public class CsQuery {
    public static void update_on_fcm_ack(String dt)
    {  
       // String dt = "2017-01-22 00:00:00";
       String query="SELECT * from matchsitedb.mp_fcm_xmpp_detailed_notification WHERE date = '" + dt+"';";
      // final ResultSet rs = CassConTest.client.
        PreparedStatement pStatement = CassConTest.client.session.prepare(query);
      
        BoundStatement bStatement = new BoundStatement(pStatement);
        ResultSet resultSet; 
        resultSet = CassConTest.client.session.execute(bStatement);
        List ls = resultSet.all();
        System.out.println("length of ls>>>>>>>>"+ls.size());
        System.out.println("list>>"+ls);
       }
         
}
