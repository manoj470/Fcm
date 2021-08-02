package com.matchpointgps.alertsprocessing.gcmfcm;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class KafkaConsumerClient {

    private final List<ThreadConsumer> consumers;

    private KafkaConsumerClient(int numberOfConsumers, String groupId, String topic, String brokers) {
        EntryPointKafka.executor = Executors.newFixedThreadPool(numberOfConsumers);
        consumers = new ArrayList<>();
        for (int i = 0; i < numberOfConsumers; i++) {
            ThreadConsumer consumer = new ThreadConsumer(brokers, groupId, topic, i);
            consumers.add(consumer);
            EntryPointKafka.executor.submit(consumer);
        }
    }

    public static List<ThreadConsumer> initKafkaConsumers(int numberOfConsumers, String groupId, String topic, String brokers){
        KafkaConsumerClient kafkaConsumerClient = new KafkaConsumerClient(numberOfConsumers,groupId,topic,brokers);
        return kafkaConsumerClient.consumers;
    }



}
