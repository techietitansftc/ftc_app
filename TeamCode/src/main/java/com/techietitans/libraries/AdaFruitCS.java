package com.techietitans.libraries;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.techietitans.libraries.DataLogger;
import com.techietitans.libraries.Wire;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import android.util.Log;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

/**
 * Created by ruhul on 2/4/16.
 */
public class AdaFruitCS {


    // --------------------------------- CLASS VARIABLES -------------------------------------------
    private Wire cs;
    private int readCount = 0;
    private long timeStamp;              // In microseconds
    private int clear, red, green, blue;


    public AdaFruitCS(HardwareMap hardwareMap, String deviceName, int devAddr) {
        cs = new Wire(hardwareMap, deviceName, devAddr);
    }

    public void close() {
        cs.close();
    }

    public void initColorSensor() {
        cs.write(0x80, 0x03);                // R[00] = 3    to enable power
        cs.requestFrom(0x92, 1);            // R[12]        is the device ID
        cs.write(0x8F, 0x02);                // R[0F] = 2    to set gain 16
        cs.write(0x81, 0xEC);                // R[01] = EC   to set integration time to 20* 2.4 ms
        // 256 - 20 = 236 = 0xEC
    }

    public void startColorPolling() {
        cs.requestFrom(0x93, 1);            // Get sensor status
    }

    public boolean isColorUpdate() {
        boolean isNew = false;
        if (cs.responseCount() > 0) {
            cs.getResponse();
            int regNumber = cs.registerNumber();
            if (cs.isRead()) {
                int regCount = cs.available();
                switch (regNumber) {
                    case 0x93:
                        if (regCount == 1) {
                            int status = cs.read();
                            if ((status & 1) != 0) {
                                cs.requestFrom(0x94, 8);             // Get colors
                            } else {
                                cs.requestFrom(0x93, 1);             // Keep polling
                            }
                        } else {
                            Log.i("GST", String.format("ERROR reg 0x%02X Len = 0x%02X (!= 1)",
                                    regNumber, regCount));
                        }
                        break;
                    case 0x94:
                        cs.requestFrom(0x93, 1);                     // Keep polling
                        if (regCount == 8) {                        // Check register count
                            timeStamp = cs.micros();              // Reading time
                            clear = cs.readLH();              // Clear color
                            red = cs.readLH();              // Red color
                            green = cs.readLH();              // Green color
                            blue = cs.readLH();              // Blue color
                            isNew = true;
                        } else {
                            Log.i("GST", String.format("ERROR reg 0x%02X Len = 0x%02X (!= 8)",
                                    regNumber, regCount));
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        return isNew;
    }


    public int red() {
        return red;
    }

    public int green() {
        return green;
    }

    public int blue() {
        return blue;
    }

}