package com.jlubecki.lucent.network.spotify.api;

import com.jlubecki.lucent.network.spotify.models.Playlist;
import com.jlubecki.lucent.network.spotify.models.TrackAudioFeatures;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Created by Jacob on 10/22/16.
 */

public interface SpotifyService {

    @GET("audio-features/{id}")
    Call<TrackAudioFeatures> getAudioFeatures(@Path("id") String id);

    @POST("users/{user-id}/playlists")
    Call<Playlist> createPlaylist(@Path("user-id") String userId);

    @PUT("users/{user-id}/playlists/{playlist-id}/tracks")
    Call updateTracks(@Path("user-id") String userId, String playlistId);
}
