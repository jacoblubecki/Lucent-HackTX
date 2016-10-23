package com.jlubecki.lucent.ui.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.google.gson.GsonBuilder;
import com.jlubecki.lucent.R;
import com.jlubecki.lucent.network.spotify.SpotifyReceiver;
import com.jlubecki.lucent.network.spotify.api.SpotifyApi;
import com.jlubecki.lucent.network.spotify.models.PlaybackMeta;
import com.jlubecki.lucent.network.spotify.models.TrackAudioFeatures;
import com.jlubecki.lucent.neuralnet.NetworkState;
import com.jlubecki.lucent.neuralnet.NeuralNetwork;
import com.jlubecki.lucent.neuralnet.TrainingModel;
import com.jlubecki.lucent.sensor.EEGData;
import com.jlubecki.lucent.sensor.EEGReceiver;

import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Created by Jacob on 10/22/16.
 */

public class LucentService extends Service {

    private static final String NEURAL_NET_PREF = "com.jlubecki.lucent.ML_DATA";
    public static final String PREFS_NAME = "LUCENT_PREFS";

    public static final String STOP = "HAMMER_TIME";

    private final int UPDATE_INTERVAL = 10 * 1000;
    private Timer timer = new Timer();

    private NeuralNetwork network;
    private SpotifyReceiver receiver = new SpotifyReceiver();
    private EEGReceiver eegReceiver = new EEGReceiver();
    private SpotifyApi api;

    public LucentService() {
    }

    @Override
    public void onCreate() {
        IntentFilter filter = new IntentFilter(SpotifyReceiver.BroadcastTypes.METADATA_CHANGED);
        IntentFilter eegFilter = new IntentFilter(EEGReceiver.ACTION_EEG);

        registerReceiver(receiver, filter);
        registerReceiver(eegReceiver, eegFilter);

        String neurons = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(NEURAL_NET_PREF, null);
        String token = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString("TOKEN", null);
        NetworkState state = null;

        if(neurons != null) {
            state = new GsonBuilder().create().fromJson(neurons, NetworkState.class);
        }

        if(state == null) {
            network = new NeuralNetwork();
        } else {
            network = new NeuralNetwork(state.inputLayer, state.hiddenLayer, state.outputLayer);
            aToast("Loaded existing net.");
        }

        api = new SpotifyApi(getString(R.string.spotify_thing));
        api.setToken(token);
    }

    @Override
    public void onDestroy() {
        if (timer != null) {
            timer.cancel();
        }

        unregisterReceiver(receiver);
        unregisterReceiver(eegReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startid) {
        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                PlaybackMeta meta = receiver.pollData();
                final EEGData data = eegReceiver.pollData();

                aToast("Scheduled learning.");

                if(meta != null && data != null) {
                    String id = meta.trackId.replace("spotify:track:", "");
                    api.getService().getAudioFeatures(id).enqueue(new Callback<TrackAudioFeatures>() {
                        @Override
                        public void onResponse(Call<TrackAudioFeatures> call, Response<TrackAudioFeatures> response) {
                            if(response.isSuccessful()) {

                                aToast("Begin neural net.");
                                network.train(new TrainingModel(data, response.body()));
                                network.print(getApplicationContext());
                            } else {
                                Timber.wtf("WTF");
                            }

                            Timber.d(response.message());
                        }

                        @Override
                        public void onFailure(Call<TrackAudioFeatures> call, Throwable t) {
                            Timber.wtf("WTF");
                        }
                    });
                }
            }
        }, 0, UPDATE_INTERVAL);
        return START_STICKY;
    }

    private void aToast(final String a) {
        Handler handler = new Handler(Looper.getMainLooper());

        handler.post(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), a, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void stopService() {
        if (timer != null) timer.cancel();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
