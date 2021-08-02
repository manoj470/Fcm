/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.matchpointgps.fcm.xmpp.server;

/**
 * Last edited on 03/07/2017 by - Manish Jaiswal
 *
 * @author Manish Jaiswal
 * @created on 03/01/2017
 */
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SocketOptions;
import com.datastax.driver.core.ThreadingOptions;
import com.datastax.driver.core.policies.ConstantReconnectionPolicy;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.datastax.driver.core.policies.LatencyAwarePolicy;
import com.datastax.driver.core.policies.LoggingRetryPolicy;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import static com.matchpointgps.alertsprocessing.gcmfcm.EntryPointKafka.Conlogger;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Level;

public class CassCon {

    /**
     * Cassandra Cluster.
     */
    Cluster cluster;
    /**
     * Cassandra Session.
     */
    Session session;
    final String address;
    final String clstr;
    final String port;
    final String username;
    final String password;
    final int coreConnetion;
    final int maxConnection;

    public CassCon(String address, String port, String clstr, String username, String password, int coreConnetion, int maxConnection) {
        this.address = address;
        this.port = port;
        this.clstr = clstr;
        this.username = username;
        this.password = password;
        this.coreConnetion = coreConnetion;
        this.maxConnection = maxConnection;
    }


    /**
     * Connect to Cassandra Cluster specified by provided node IP address and
     * port number.
     *
     * @throws java.net.UnknownHostException
     */
    public void connect() throws UnknownHostException {
        List<String>  hostsList = new ArrayList<>(Arrays.asList(address.replaceAll(" ", "").split(",")));
        ArrayList<InetAddress> addresses = new ArrayList<>();
        for (Object x : hostsList) {
            InetAddress inet = InetAddress.getByName((String) x);
            addresses.add(inet);
        }
        LatencyAwarePolicy loadBalancingPolicy = LatencyAwarePolicy.builder(DCAwareRoundRobinPolicy.builder().build()).build();
        SocketOptions sOptions = new SocketOptions();
        sOptions.setKeepAlive(true);
        ThreadingOptions threadOptn = new ThreadingOptions();
        threadOptn.createThreadFactory("mpGPS", "testThread");
        threadOptn.createExecutor("mpGPS");
        PoolingOptions options = new PoolingOptions();
        options.setMaxRequestsPerConnection(HostDistance.LOCAL, 2048);
        options.setMaxRequestsPerConnection(HostDistance.REMOTE, 1024);
        options.setNewConnectionThreshold(HostDistance.LOCAL, 2500);
        options.setNewConnectionThreshold(HostDistance.REMOTE, 1200);
        options.setCoreConnectionsPerHost(HostDistance.LOCAL, 1);
        options.setMaxConnectionsPerHost(HostDistance.LOCAL, 5);
        options.setCoreConnectionsPerHost(HostDistance.REMOTE, 1);
        options.setMaxConnectionsPerHost(HostDistance.REMOTE, 5);

        cluster = Cluster.builder()
                .withClusterName(clstr)
                .addContactPoints(addresses)
                .withPort(Integer.parseInt(port))
                .withCredentials(username, password)
                .withPoolingOptions(options)
                .withSocketOptions(sOptions)
                .withThreadingOptions(threadOptn)
                .withLoadBalancingPolicy(loadBalancingPolicy)
                .withReconnectionPolicy(new ConstantReconnectionPolicy(TimeUnit.SECONDS.toSeconds(2)))
                .withRetryPolicy(new LoggingRetryPolicy(DefaultRetryPolicy.INSTANCE))
                .build();
        session = cluster.connect();
        final Metadata metadata = cluster.getMetadata();
        Conlogger.log(Level.INFO, "Connected to cluster: %s\n" + metadata.getClusterName());
        for (final Host host : metadata.getAllHosts()) {
            Conlogger.log(Level.INFO, "Datacenter: " + host.getDatacenter() + ",Host: " + host.getAddress() + ",+Rack:" + host.getRack());
        }
        session = cluster.connect();
    }

    /**
     * Provide my Session.
     *
     * @return My session.
     */
    public Session getSession() {
        return this.session;
    }

    /**
     * Close cluster.
     */
    public void close() {
        cluster.close();
    }
}
