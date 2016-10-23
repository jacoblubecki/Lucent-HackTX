package com.jlubecki.lucent.network.spotify.api;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.bind.DateTypeAdapter;

import java.io.IOException;
import java.util.Date;

import okhttp3.HttpUrl;
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
    private final String clientId;
    private String token;

    public SpotifyApi(final String clientId) {
        this.clientId = clientId;

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new SpotifyInterceptor())
                .build();

        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(Date.class, new DateTypeAdapter())
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl("https://api.spotify.com/v1/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        service = retrofit.create(SpotifyService.class);
    }

    public void setToken(String token) {
        this.token = token;
    }

    private class SpotifyInterceptor implements Interceptor {
            @Override
            public Response intercept(Interceptor.Chain chain) throws IOException {

                Request request = chain.request();

                HttpUrl.Builder urlBuilder = request.url().newBuilder();

                urlBuilder.addEncodedQueryParameter("client_id", clientId);

                Request.Builder newRequestBuilder = request.newBuilder();

                if(token != null) {
                    newRequestBuilder.addHeader("Authorization", "Bearer " + token);
                }


                Request newRequest = newRequestBuilder.url(urlBuilder.build())
                        .build();

                return chain.proceed(newRequest);
            }
    };

    public SpotifyService getService() {
        return service;
    }
}
