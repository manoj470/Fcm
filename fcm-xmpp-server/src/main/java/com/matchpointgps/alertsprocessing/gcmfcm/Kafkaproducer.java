/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.matchpointgps.alertsprocessing.gcmfcm;

import static com.matchpointgps.alertsprocessing.gcmfcm.EntryPointKafka.dynmConfig;
import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * Last edited on 31/08/2017 by - Manish Jaiswal
 *
 * @author Manish Jaiswal
 */
public class Kafkaproducer {

    static final String bootstrap = dynmConfig.BOOTSTRAP;
    final Logger ProducerLogger = Logger.getLogger("Producer_logger");
    final Logger ErrLogger = Logger.getLogger("Error_logger");
    Properties props = new Properties();
    public static Producer<String, String> producer;

    public static Producer<String, String> initProducer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrap);
        props.put("acks", "all");
        props.put("retries", 3);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producer = new KafkaProducer<>(props);
        return producer;

    }

//org.apache.kafka.connect.json.JsonSerialize
    public void produce_message(JSONObject data, String topic, String key) {
        try {
            producer.send(new ProducerRecord<>(topic, key, data.toString()));
            ProducerLogger.log(Level.INFO, " topic: " + topic + ", key: " + key + ",data " + data);

        } catch (Exception e) {
            ErrLogger.log(Level.ERROR, "error in producer send " + e);
        }
    }
}
