package com.matchpointgps.alertsprocessing.gcmfcm;

import static com.matchpointgps.alertsprocessing.gcmfcm.EntryPointKafka.Consumerlogger;
import static com.matchpointgps.alertsprocessing.gcmfcm.EntryPointKafka.dynmConfig;

import com.matchpointgps.alertsprocessing.gcmfcm.mysql.MySql;
import com.matchpointgps.fcm.xmpp.FcmProcess;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.log4j.Level;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author aspade
 */
public class ThreadConsumer implements Runnable {

    private final KafkaConsumer<String, String> consumer;
    private final String topic;
    public ExecutorService exService;
    final int workersThreads = dynmConfig.WORKERS;

    public ThreadConsumer(String brokers, String groupId, String topic, int i) {
        Properties prop = createConsumerConfig(brokers, groupId);
        this.consumer = new KafkaConsumer<>(prop);
        this.topic = topic;
    }

    private static Properties createConsumerConfig(String brokers, String groupId) {
        Properties props = new Properties();
        props.put("bootstrap.servers", brokers);
        props.put("group.id", groupId);
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("auto.offset.reset", "earliest");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        return props;
    }

    @Override
    public void run() {
        try {
            this.consumer.subscribe(Collections.singletonList(this.topic));
            /*
             * "Creatintin thread pool for executing msg"
             */
            exService = new ThreadPoolExecutor(10, workersThreads, 2000L, TimeUnit.MILLISECONDS,
                    new ArrayBlockingQueue<Runnable>(800), new ThreadPoolExecutor.CallerRunsPolicy());
            String status = dynmConfig.STATUS;
            if (status.equals("Live")) {
                while (true) {
                    ConsumerRecords<String, String> records = consumer.poll(100);
                    for (ConsumerRecord<String, String> record : records) {
                        Consumerlogger.log(Level.INFO, "Receive message: " + record.value() + ", Partition: "
                                + record.partition() + ", Offset: " + record.offset() + ", Key: " + record.key() + ", by ThreadID: "
                                + Thread.currentThread().getId());
                        if (record.key().equals("SupportMsg")) {
                            processSupportMsg(record.value());
                        } else {
                            processMsg(record.value());
                        }
                    }
                }
            } else if (status.equals("Flush")) {
                while (true) {
                    ConsumerRecords<String, String> records = consumer.poll(100);
                    for (ConsumerRecord<String, String> record : records) {
                        exService.submit(new Thread(() -> flushmsg(record)));
                    }
                }
            }
        } catch (WakeupException e) {
            // ignore for shutdown 
        } finally {
            consumer.close();
            exService.shutdown();
            try {
                boolean isTerminated = exService.awaitTermination(2, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                EntryPointKafka.Erlogger.log(Level.ERROR, "error in workers wait >>> " + ex);
            }
        }
    }

    public void flushmsg(ConsumerRecord<String, String> rec) {
        Consumerlogger.log(Level.INFO, "message:- value " + rec.value() + ", partition" + rec.partition()
                + ", offset" + rec.offset() + ", by" + Thread.currentThread().getId());
    }

    public void processMsg(String record) {
        FcmNotificationService fcm = new FcmNotificationService();
        GcmProcess gcm = new GcmProcess();
        JSONObject data = new JSONObject(record);

        JSONObject message = (JSONObject) data.get("alert_params");
        JSONObject notification_payload = (JSONObject) data.get("gcm_templates");
        notification_payload.put("sound", "");
        Date event_time = null;
        int alert_type = 0;
        try {
            String sr = (String) message.get("trackerId");
            alert_type = (int) message.get("alertType");
            message.put("trackerId", sr);

            String message_time = (String) message.get("date") + " " + (String) message.get("time");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            event_time = sdf.parse(message_time);
        } catch (Exception e) {
            EntryPointKafka.AlertErrlogger.log(Level.INFO, e + "processMsg---" + message.get("trackerId"));
        }

        /*
         * "get message,create UUID with userId and call fcm and gcm class methods "
         */
        if (isNotLateData(event_time) || alert_type == Constants.ALERT_TYPE.REFUEL.getIntValue() ||
                alert_type == Constants.ALERT_TYPE.FUELTHEFT.getIntValue() ) {
            if(data.has("user_id")){
                int userId = data.getInt("user_id");
                String messageId = userId + "-" + UUID.randomUUID();
                MySql sql = new MySql();
                List<Map<String, String>> userList = sql.AuthUser(userId);
                if (userList != null && !userList.isEmpty() ) {
                    Map<String, String> user = userList.get(0);
                    if (data.has("fcm_noti_type")) {
                        exService.submit(new Thread(() -> fcm.fcm_web_push_notification(userId, message, notification_payload, messageId, user)));
                    } else {
                        data.put("fcm_noti_type", JSONObject.NULL);
                        try {
                            exService.submit(new Thread(() -> fcm.fcm_push_notification(userId, message, notification_payload, messageId, user)));
                            exService.submit(new Thread(() -> gcm.pushNotification(userId, message, notification_payload, messageId, user)));
                        } catch (Exception error) {
                            System.out.println("error in Msg process " + error);
                        }
                    }
                }else {
                    System.out.println("No user found ******* ");
                }
            }
        }else{
            EntryPointKafka.AlertErrlogger.log(Level.INFO, "Delayed MSG NOT PROCESSED---" + message.get("trackerId") +", event time: "+event_time);
        }
    }

    private boolean isNotLateData(Date etime) {
        SimpleDateFormat ft = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        LocalDateTime allowed_datetime = LocalDateTime.now();
        allowed_datetime.atZone(ZoneId.of("UTC"));
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String temp1 = dateFormat.format(allowed_datetime.minusMinutes(Constants.CHECK_FOR_LATE_DATA));

        Date dtTemp = null;
        try {
            dtTemp = ft.parse(temp1);
        } catch (ParseException ex) {
            EntryPointKafka.AlertErrlogger.log(Level.ERROR, "error in not lateData " + ex);
        }
        if (etime.after(dtTemp) || etime.equals(dtTemp)) {
            return true;
        } else {
            return false;
        }
    }

    public void shutdown() {
        consumer.wakeup();
    }

    private void processSupportMsg(String value) {
        try {
            FcmProcess fp = new FcmProcess();
            JSONObject data = new JSONObject(value);
            exService.submit(new Thread(() -> fp.handleDelivery(data)));
        } catch (JSONException er) {
            System.out.println("error in processSupportMsg " + er);
        }
    }
}
