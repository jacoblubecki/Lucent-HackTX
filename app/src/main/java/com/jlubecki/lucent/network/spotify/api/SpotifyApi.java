package com.jlubecki.lucent.network.spotify.api;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Jacob on 10/23/16.
 */

public class SpotifyApi {

    private SpotifyService service;

    public SpotifyApi(final String clientId) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request newRequest = chain.request()
                                .newBuilder()
                                .addHeader("Bearer", clientId)
                                .build();

                        return chain.proceed(newRequest);
                    }
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.spotify.com/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        service = retrofit.create(SpotifyService.class);
    }

    public SpotifyService getService() {
        return service;
    }
}
