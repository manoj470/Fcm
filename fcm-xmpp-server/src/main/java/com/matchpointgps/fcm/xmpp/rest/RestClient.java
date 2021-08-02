/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.matchpointgps.fcm.xmpp.rest;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import javax.net.ssl.*;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author aspade
 */
public class RestClient {

    private static RestApi restApi;

    private static final String DEFAULT_BASE_URL = "https://app3.matchpointgps.in";

    public RestClient() {
        restApi = setupRestClient();
    }

    public RestApi get() {
        return restApi;
    }

    private RestApi setupRestClient() {
        final Gson gson = new GsonBuilder()
                .setLenient()
                .setPrettyPrinting()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(DEFAULT_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(new SelfSigningClientBuilder().createClient())
                .build();
        return retrofit.create(RestApi.class);
    }

     static class SelfSigningClientBuilder {
        @SuppressWarnings("null")
        public OkHttpClient configureClient(final OkHttpClient.Builder client) {
            final TrustManager[] certs = new TrustManager[]{new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
                @Override
                public void checkServerTrusted(final X509Certificate[] chain,
                                               final String authType) throws CertificateException {
                }
                @Override
                public void checkClientTrusted(final X509Certificate[] chain,
                                               final String authType) throws CertificateException {
                }
            }};

            SSLContext ctx = null;
            try {
                ctx = SSLContext.getInstance("TLS");
                ctx.init(null, certs, new SecureRandom());
            } catch (final java.security.GeneralSecurityException ex) {
                ex.printStackTrace();
            }
            try {
                final HostnameVerifier hostnameVerifier = (hostname, session) -> true;
                client.hostnameVerifier(hostnameVerifier);
                client.sslSocketFactory(ctx.getSocketFactory(), (X509TrustManager) certs[0]);
            } catch (final Exception e) {
                 e.printStackTrace();
            }
            return client.build();
        }


        public OkHttpClient createClient() {
            OkHttpClient.Builder client = new OkHttpClient.Builder();
            client.readTimeout(2, TimeUnit.MINUTES);
            client.connectTimeout(2, TimeUnit.MINUTES);
            return configureClient(client);
        }

    }








}
