package com.jlubecki.lucent.ui.services;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

/**
 * Created by Jacob on 10/23/16.
 */

public class UsbConnectionService extends IntentService {

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static final int ARDUINO_VENDOR_ID = 0x2341;

    private UsbDeviceConnection connection;
    private UsbManager usbManager;
    private UsbDevice device;
    private UsbSerialDevice serialPort;

    public UsbConnectionService() {
        this(UsbConnectionService.class.getSimpleName());
    }

    public UsbConnectionService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
            boolean granted =
                    intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
            if (granted) {
                connection = usbManager.openDevice(device);
                serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
                if (serialPort != null) {
                    if (serialPort.open()) { //Set Serial Connection Parameters.
                        serialPort.setBaudRate(9600);
                        serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                        serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                        serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                        serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                        serialPort.read(mCallback); //

                    } else {
                        Timber.d("PORT NOT OPEN");
                    }
                } else {
                    Timber.d("PORT IS NULL");
                }
            } else {
                Timber.d("PERM NOT GRANTED");
            }
        } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
            openArduinoConnection();
        } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
            if (serialPort != null) {
                serialPort.close();
            }
        }
    }

    private void closeConnection() {
        if (serialPort != null) {
            serialPort.close();
        }
    }

    private void openArduinoConnection() {
        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();

        if (!usbDevices.isEmpty()) {
            boolean keep = true;

            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                device = entry.getValue();
                int deviceVID = device.getVendorId();

                if (deviceVID == ARDUINO_VENDOR_ID) {
                    PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    usbManager.requestPermission(device, pi);
                    keep = false;
                } else {
                    connection = null;
                    device = null;
                }

                if (!keep)
                    break;
            }
        }
    }


    private UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {
        //Defining a Callback which triggers whenever data is read.
        @Override
        public void onReceivedData(byte[] arg0) {
            String data;
            try {
                data = new String(arg0, "UTF-8");
                Timber.d(data);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    };
}
