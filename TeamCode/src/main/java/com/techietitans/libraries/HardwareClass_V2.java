package com.techietitans.libraries;

import com.qualcomm.ftccommon.DbgLog;
import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cGyro;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.OpticalDistanceSensor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.TouchSensor;
import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cColorSensor;
import com.qualcomm.robotcore.hardware.I2cController;

//import com.qualcomm.robotcore.hardware.

/**
 * Created by vinayjagan on 10/3/15.
 */
public abstract class HardwareClass_V2 extends OpMode {

    protected static DcMotor front_left;
    protected static DcMotor front_right;
    protected static DcMotor lift;
    protected static DcMotor shooter;
    protected static OpticalDistanceSensor ods_front;
    protected static ModernRoboticsI2cGyro gyro;
    public static Servo pusher_left;
    public static Servo pusher_right;
    protected static Servo latch;
    protected static Servo release;
    protected static TouchSensor touch_left;
    protected static TouchSensor touch_right;

    // Declare Color Sensor objects (and other items required to enable/disable)
    protected static ModernRoboticsI2cColorSensor  mrcolor_under = null;
    protected static ModernRoboticsI2cColorSensor  mrcolor_front = null;

    protected static I2cAddr underColorAddress  = I2cAddr.create8bit(0x4c);
    protected static I2cAddr frontColorAddress = I2cAddr.create8bit(0x3c);

    protected static I2cController   underColorController;
    protected static I2cController   frontColorController;

    protected static I2cController.I2cPortReadyCallback underColorCallback;
    protected static I2cController.I2cPortReadyCallback frontColorCallback;




    @Override
    public void init() {

        mrcolor_under   = hardwareMap.get(ModernRoboticsI2cColorSensor.class, "mrcolor_under");
        mrcolor_under.setI2cAddress(underColorAddress);
        underColorController = mrcolor_under.getI2cController();
        underColorCallback =  underColorController.getI2cPortReadyCallback(mrcolor_under.getPort());

        mrcolor_front  = hardwareMap.get(ModernRoboticsI2cColorSensor.class, "mrcolor_front");
        mrcolor_front.setI2cAddress(frontColorAddress);
        frontColorController = mrcolor_front.getI2cController();
        frontColorCallback =  frontColorController.getI2cPortReadyCallback(mrcolor_front.getPort());


        try {
            front_left = hardwareMap.dcMotor.get("front_left");
            front_left.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        } catch (Exception p_exeception) {
            DbgLog.msg(p_exeception.getLocalizedMessage());
            front_left = null;
        }

        try {
            front_right = hardwareMap.dcMotor.get("front_right");
            front_right.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            front_right.setDirection(DcMotor.Direction.REVERSE);
        } catch (Exception p_exeception) {
            DbgLog.msg(p_exeception.getLocalizedMessage());
            front_right = null;
        }


        try {
            ods_front = hardwareMap.opticalDistanceSensor.get("ods_front");
        } catch (Exception p_exeception) {
            DbgLog.msg(p_exeception.getLocalizedMessage());
            ods_front = null;
        }

        try {
            lift = hardwareMap.dcMotor.get("lift");
        } catch (Exception p_exeception) {
            DbgLog.msg(p_exeception.getLocalizedMessage());
            lift = null;
        }

        try {
            shooter = hardwareMap.dcMotor.get("shooter");
        } catch (Exception p_exeception) {
            DbgLog.msg(p_exeception.getLocalizedMessage());
            shooter = null;
        }

       /* try {
            mrcolor_under = hardwareMap.colorSensor.get("mrcolor_under");
        } catch (Exception p_exception) {
            DbgLog.msg(p_exception.getLocalizedMessage());
            mrcolor_under = null;
        }*/

        /*try {
            mrcolor_front = hardwareMap.colorSensor.get("mrcolor_front");
        } catch (Exception p_exception) {
            DbgLog.msg(p_exception.getLocalizedMessage());
            mrcolor_under = null;
        }*/

//        try {
//            adaColor = hardwareMap.colorSensor.get("adaColor");
//        } catch (Exception p_exeception) {
//            DbgLog.msg(p_exeception.getLocalizedMessage());
//            adaColor = null;
//       }


        try {
            gyro = (ModernRoboticsI2cGyro) hardwareMap.gyroSensor.get("gyro");
        } catch (Exception p_exeception) {
            DbgLog.msg(p_exeception.getLocalizedMessage());
            gyro = null;
        }



        try {
            pusher_left = hardwareMap.servo.get("pusher_left");
        } catch (Exception p_exeception) {
            DbgLog.msg(p_exeception.getLocalizedMessage());
            pusher_left = null;
        }
        try {
            pusher_right = hardwareMap.servo.get("pusher_right");
        } catch (Exception p_exeception) {
            DbgLog.msg(p_exeception.getLocalizedMessage());
            pusher_right = null;
        }
        try {
            latch = hardwareMap.servo.get("holder");
        } catch (Exception p_exeception) {
            DbgLog.msg(p_exeception.getLocalizedMessage());
            latch = null;
        }
        try {
            release = hardwareMap.servo.get("release");
        } catch (Exception p_exeception) {
            DbgLog.msg(p_exeception.getLocalizedMessage());
            release = null;
        }
        try {
            touch_left = hardwareMap.touchSensor.get("touch_left");
        } catch (Exception p_exeception) {
            DbgLog.msg(p_exeception.getLocalizedMessage());
            touch_left = null;
        }
        try {
            touch_right = hardwareMap.touchSensor.get("touch_right");
        } catch (Exception p_exeception) {
            DbgLog.msg(p_exeception.getLocalizedMessage());
            touch_right = null;
        }


    }

    @Override
    public void loop() {

    }

}
