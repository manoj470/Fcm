package com.matchpointgps.fcm.xmpp.rest;


import org.apache.log4j.Level;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.matchpointgps.alertsprocessing.gcmfcm.EntryPointKafka.Erlogger;

public class RestRepository {

    /*
    * This api call let app server know this fcm tokenId for user is no longer valid
    * */
    public void unregisterFCMTokenId(String urlToHit, String tokenId) {
        if(urlToHit == null || urlToHit.trim().isEmpty() || tokenId == null || tokenId.trim().isEmpty())return;
        new RestClient().get().deleteFcmTokenId(urlToHit,tokenId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                System.out.println("Gotten response msg as ****** "+response.body()+"response code is "+response.code());
                if(response.isSuccessful()){
                    System.out.println("TokenId deleted successfully ****");
                }else {
                    Erlogger.log(Level.ERROR, "Unknown error in deleting  tokenID "+tokenId );
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable throwable) {
                Erlogger.log(Level.ERROR, throwable+"Error in deleting  tokenID "+tokenId );
            }
        });
    }




}
