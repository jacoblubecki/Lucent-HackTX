package com.jlubecki.lucent.ui.activities;

import android.app.PendingIntent;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jlubecki.lucent.R;
import com.jlubecki.lucent.utils.ArduinoUtils;
import com.tooleap.sdk.Tooleap;
import com.tooleap.sdk.TooleapPopOutMiniApp;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {
    private static final String ACTION_USB_PERMISSION = "com.jlubecki.lucent.USB_PERMISSION";
    private static final int BLEND_MICRO_VENDOR_ID = 0x3EB;
    public static int OVERLAY_PERMISSION_REQ_CODE = 1234;

    private FirebaseUser mUser;

    private UsbDeviceConnection connection;
    private UsbManager usbManager;
    private UsbDevice device;
    private UsbSerialDevice serialPort;

    @BindView(R.id.text)
    TextView textView;
    @BindView(R.id.switch1)
    Switch mSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        usbManager = (UsbManager) getSystemService(USB_SERVICE);

        checkSignedIn(mUser);

        openArduinoConnection();

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
        mUser=FirebaseAuth.getInstance().getCurrentUser();
        checkSignedIn(mUser);
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

    private void checkSignedIn(FirebaseUser user){
        if(user==null){
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            Toast.makeText(this, "Please sign in to continue", Toast.LENGTH_SHORT).show();
            startActivity(intent);
        }
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
        Gson gson = new GsonBuilder().create();

        @Override
        public void onJsonString(String json) {
            updateText(json);
        }

        @Override
        public void onError(String json, Throwable t) {
//            updateText("Error: " + t.getMessage() + "\n" + json);
            Timber.e(t);
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
            }
        });
    }

    public void switchOnClick(View view) {
        Intent intent = new Intent(getApplicationContext(), MiniAppActivity.class);
        TooleapPopOutMiniApp miniApp = new TooleapPopOutMiniApp(getApplicationContext(), intent);
        Tooleap tooleap = Tooleap.getInstance(getApplicationContext());
        miniApp.contentTitle = "Lucent Bubble";
        miniApp.notificationText = "Press to see data";
        miniApp.bubbleBackgroundColor = 0x78FFFFFF;

        if(mSwitch.isChecked()){
            if(Build.VERSION.SDK_INT>=23) {
                if (!Settings.canDrawOverlays(this)) {
                    intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
                }
                else
                    tooleap.addMiniApp(miniApp);
            }
            else
            tooleap.addMiniApp(miniApp);

        }
        else {
            tooleap.removeAllMiniApps();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(Build.VERSION.SDK_INT>=23) {
            if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
                if (!Settings.canDrawOverlays(this)) {
                    mSwitch.setChecked(false);
                    mSwitch.setClickable(false);
                    Toast.makeText(getApplicationContext(), "Permission for overlays denied, please enable", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
