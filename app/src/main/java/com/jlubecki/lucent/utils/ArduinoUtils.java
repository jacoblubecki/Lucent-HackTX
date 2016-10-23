package com.jlubecki.lucent.utils;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.UnsupportedEncodingException;

import timber.log.Timber;

/**
 * Created by Jacob on 10/23/16.
 */

public class ArduinoUtils implements UsbSerialDevice.UsbReadCallback {

    private static final char START_TOKEN = '{';
    private static final char END_TOKEN = '}';
    private static final char DELIMITER = ',';

    private static final int ELEMENT_COUNT_EXPECTED = 12;

    @Override
    public void onReceivedData(byte[] bytes) {
        next(bytes);
    }

    private enum ParseState {
        BEGINNING,
        MIDDLE,
        END,
        NONE
    }

    private StringBuilder current = new StringBuilder(400);
    private int elementCount = 0;
    private ParseState state = ParseState.NONE;
    private ResultCallback callback;

    public ArduinoUtils(ResultCallback callback) {
        this.callback = callback;
    }

    private void next(byte[] nextBytes) {
        String fromBytes = null;
        try {
            fromBytes = new String(nextBytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Timber.i(fromBytes);

        if (fromBytes != null) {
            for (char c : fromBytes.toCharArray()) {
                switch (state) {
                    case NONE:
                        if(c == START_TOKEN) {
                            this.state = ParseState.BEGINNING;
                        } else {
                            current.delete(0, current.length());
                        }
                        break;

                    // Last character was '{'
                    case BEGINNING:
                        this.elementCount = 1;

                        if (c == START_TOKEN || c == END_TOKEN) { // Should not encounter {} after '{'
                            this.state = ParseState.NONE;
                            this.callback.onError(current.toString(), new AssertionError("Found start or end token when state was START."));
                        } else {
                            this.state = ParseState.MIDDLE;
                        }
                        break;

                    case MIDDLE:
                        if (c == END_TOKEN) {
                            this.state = ParseState.END;
                        } else if (c == START_TOKEN) {
                            this.state = ParseState.NONE;
                            this.callback.onError(current.toString(), new AssertionError("Found start token when state was MIDDLE."));
                        } else if (c == DELIMITER) {
                            Timber.i("Delimiter found.");
                            this.elementCount++;
                        }
                        break;

                    case END:
                        if(this.elementCount != ELEMENT_COUNT_EXPECTED) {
                            this.callback.onError(current.toString(), new AssertionError("Expected 12 elements but found " + elementCount + "."));
                            this.state = ParseState.NONE;
                        } else {
                            Timber.i("Printing json.");
                            this.callback.onJsonString(current.toString());

                            if(c == START_TOKEN) {
                                this.state = ParseState.BEGINNING;
                            } else {
                                this.state = ParseState.NONE;
                            }
                        }
                        break;
                }

                current.append(c);
            }
        }


    }

    public interface ResultCallback {
        void onJsonString(String json);
        void onError(String json, Throwable t);
    }
}
