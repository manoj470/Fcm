package com.matchpointgps.fcm.xmpp.rest;

import retrofit2.Call;
import retrofit2.http.*;

public interface RestApi {

    @FormUrlEncoded
    @POST
    Call<Void> deleteFcmTokenId(@Url String url,
                                                  @Field("fcm_token_id") String tokenId );





}
