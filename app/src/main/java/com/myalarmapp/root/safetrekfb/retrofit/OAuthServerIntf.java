package com.myalarmapp.root.safetrekfb.retrofit;

import com.myalarmapp.root.safetrekfb.models.OAuthToken;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface OAuthServerIntf {
    @FormUrlEncoded
    @POST("oauth/token")
    Call<OAuthToken> requestToken(
            @Field ("grant_type") String grantType,
            @Field("code") String code,
            @Field("client_id") String client_id,
            @Field("client_secret") String client_secret,
            @Field("redirect_uri") String redirect_url
    );
    @FormUrlEncoded
    @POST("oauth/token")
    Call<OAuthToken> getNewAccessToken(
            @Field ("refresh_token") String refresh_Token,
            @Field("client_id") String client_id,
            @Field("client_secret") String client_secret,
            @Field ("grant_type") String grant_type
    );
}
