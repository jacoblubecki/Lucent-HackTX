package com.jlubecki.lucent.ui.activities;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jlubecki.lucent.R;
import com.jlubecki.lucent.neuralnet.NeuralNetwork;
import com.jlubecki.lucent.sensor.EEGReceiver;
import com.jlubecki.lucent.ui.services.LucentService;
import com.jlubecki.lucent.utils.ArduinoUtils;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationHandler;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.authentication.SpotifyAuthActivity;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {
    private static final String ACTION_USB_PERMISSION = "com.jlubecki.lucent.USB_PERMISSION";
    private static final int BLEND_MICRO_VENDOR_ID = 0x3EB;

    private UsbDeviceConnection connection;
    private UsbManager usbManager;
    private UsbDevice device;
    private UsbSerialDevice serialPort;

    @BindView(R.id.text)
    TextView textView;

    @BindView(R.id.text2)
    TextView textView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        usbManager = (UsbManager) getSystemService(USB_SERVICE);

        openArduinoConnection();

        startService(new Intent(this, LucentService.class));

        AuthenticationRequest request = new AuthenticationRequest.Builder("a523781978814629865f58b502049327", AuthenticationResponse.Type.TOKEN, "lucent://spotify_callback")
                .setScopes(new String[]{ "playlist-modify-private", "playlist-read-private",  })
                .build();

        AuthenticationClient.openLoginActivity(this, 1337, request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1337) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);

            if(response != null) {
                String token = response.getAccessToken();
                if(token != null) {
                    getSharedPreferences(LucentService.PREFS_NAME, MODE_PRIVATE).edit()
                            .putString("TOKEN", token)
                            .apply();
                } else {
                    Toast.makeText(this, "Err: " + response.getError(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Err: no response", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (serialPort != null) {
            serialPort.close();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();

        if(intent != null && intent.getAction() != null) {

            Timber.i(intent.getAction());

            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted =
                        intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) {
                    connect();
                } else {
                    Timber.d("PERM NOT GRANTED");
                    Toast.makeText(MainActivity.this, "Permission failed.", Toast.LENGTH_SHORT).show();
                }
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                openArduinoConnection();
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                if (serialPort != null) {
                    serialPort.close();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Intent intent = new Intent(LucentService.STOP);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.settings) {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void openArduinoConnection() {
        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();

        if (!usbDevices.isEmpty()) {
            boolean keep = true;

            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                device = entry.getValue();
                int deviceVID = device.getVendorId();

                Timber.i(String.valueOf(deviceVID));

                if (deviceVID == BLEND_MICRO_VENDOR_ID) {
                    Intent usbIntent = new Intent(ACTION_USB_PERMISSION);
                    usbIntent.setPackage("com.jlubecki.lucent");
                    PendingIntent pi = PendingIntent.getBroadcast(this, 0, usbIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    if(!usbManager.hasPermission(device)) {
                        usbManager.requestPermission(device, pi);
                    } else {
                        connect();
                    }
                    keep = false;
                } else {
                    connection = null;
                    device = null;
                }

                if (!keep)
                    break;
            }
        } else {
            Timber.w("Devices Empty.... :(");
        }
    }

    private final ArduinoUtils.ResultCallback callback = new ArduinoUtils.ResultCallback() {
        @Override
        public void onJsonString(String json) {
            updateText(json);
            Intent eeg = new Intent(EEGReceiver.ACTION_EEG);
            eeg.putExtra(EEGReceiver.EXTRA_SENSOR_JSON, json);
            sendBroadcast(eeg);
        }

        @Override
        public void onError(String json, Throwable t) {
            updateText("Error: " + t.getMessage() + "\n" + json);
        }
    };

    private final ArduinoUtils utils = new ArduinoUtils(callback);

    private void connect() {
        connection = usbManager.openDevice(device);
        serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
        if (serialPort != null) {
            if (serialPort.open()) { //Set Serial Connection Parameters.
                serialPort.setBaudRate(9600);
                serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                serialPort.read(utils);

                Toast.makeText(MainActivity.this, "Serial connection established.", Toast.LENGTH_SHORT).show();
            } else {
                Timber.d("PORT NOT OPEN");
                Toast.makeText(MainActivity.this, "Port not open.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Timber.d("PORT IS NULL");
            Toast.makeText(MainActivity.this, "Port null.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateText(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(text);

                if(NeuralNetwork.instance != null) {
                    textView2.setText(NeuralNetwork.instance.toString());
                }
            }
        });
    }
}
