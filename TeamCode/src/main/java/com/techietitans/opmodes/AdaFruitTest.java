package com.techietitans.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import com.techietitans.libraries.AdaFruitCS;
import com.techietitans.libraries.DataLogger;
import com.techietitans.libraries.HardwareClass_V2;
import com.techietitans.libraries.Wire;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import android.util.Log;

/**
 * Created by ruhul on 2/4/16.
 */
@Autonomous(group = "TechieTitans")
public class AdaFruitTest extends HardwareClass_V2 {

    //    private DataLogger              dl;
    private Wire                    ds;
    //    private int                     readCount = 0;
//    private long                    timeStamp;              // In microseconds
    private AdaFruitCS cs;
    private ElapsedTime runtime = new ElapsedTime(ElapsedTime.Resolution.MILLISECONDS);



    public static final int RED_LINE = 2;
    public static final int BLUE_LINE = 2;
    public static final int WHITE_LINE = 2;
    public static final int WHITE_CLOSENESS = 2;

    // Colors used in Alliance, resQ beacon and line
    public enum Colors {
        RED, BLUE, WHITE, OTHER
    }

    @Override
    public void init() {
        super.init();
        cs = new AdaFruitCS(hardwareMap, "adaColor", 2 * 0x29);
        cs.initColorSensor();
        //pusher_left.setPosition(63.0 / 255.0);
        //pusher_right.setPosition(60.0 / 255.0);


//        // Calibrate the gyro.
        gyro.calibrate();
//        //Turn on LED of bottom color sensor-Used to detect line.
       mrcolor_under.enableLed(true);
        mrcolor_front.enableLed(false);

    }

    @Override
    public void start() {
        cs.startColorPolling();

    }

    @Override
    public void loop() {
        if (cs.isColorUpdate()) {
            telemetry.addData("Ada---",
                    " R:" + cs.red() +
                            " G:" + cs.green() +
                            " B:" + cs.blue());
        }
        telemetry.addData("Front---",
                " R:" + mrcolor_front.red() +
                        " G:" + mrcolor_front.green() +
                        " B:" + mrcolor_front.blue());
        telemetry.addData("Under---",
                " R:" + mrcolor_under.red() +
                        " G:" + mrcolor_under.green() +
                        " B:" + mrcolor_under.blue());

        telemetry.addData("Ada---",
                " R:" + cs.red() +
                        " G:" + cs.green() +
                        " B:" + cs.blue());

       // shooter.setPower(.6);

        if (touch_left.isPressed()) {
            mrcolor_under.enableLed(true);
        }
        if (touch_right.isPressed()) {
            mrcolor_under.enableLed(false);
        }


//        if (!gyro.isCalibrating()) {
//            //TODO: Validate/test ..subtraction covers both + and - offset
//            //telemetry.addData("Gyro rotation", gyro_1.getRotation());
//            telemetry.addData("Gyro heading", gyro.getHeading());
//            telemetry.addData("Gyro intZ", gyro.getIntegratedZValue());
//        }

       //telemetry.addData("ODS", "ODS: " + String.valueOf(ods_front.getLightDetected()));


    }

    public void stop() {
        cs.close();
        shooter.setPower(0);
    }




    Colors getLineColor() {
        int r = mrcolor_under.red();
        int g = mrcolor_under.green();
        int b = mrcolor_under.blue();

        if ((r - g) >= RED_LINE && (r - b) >= RED_LINE) {
            return Colors.RED;
        } else if ((b - g) >= BLUE_LINE && (b - r) >= BLUE_LINE) {
            return Colors.BLUE;
        } else if (((b - g) < WHITE_CLOSENESS) && ((b - r) < WHITE_CLOSENESS) && (r > WHITE_LINE) && (g > WHITE_LINE) && (b > WHITE_LINE)) {
            return Colors.WHITE;
        } else {
            return Colors.OTHER;
        }

    }
}
