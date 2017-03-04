package com.techietitans.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.techietitans.libraries.AdaFruitCS;
import com.techietitans.libraries.DataLogger;

//import com.techietitans.TTActivity;


@Autonomous(name = "Blue Auto",group = "TechieTitans")
@Disabled
public class TTAutoV3_Blue extends TTTeleOp {

    int currentState = 0;
    int previousState = 0;
    boolean isRunning = false;
    boolean isResetRunning = false;
    DataLogger dl;
    private ElapsedTime runtime = new ElapsedTime(ElapsedTime.Resolution.MILLISECONDS);
    private ElapsedTime droppertime = new ElapsedTime(ElapsedTime.Resolution.MILLISECONDS);
    int beaconChangeCounter = 0;
    Colors currentBeaconColor = Colors.OTHER;
    Colors allianceColor;
    Sides desiredBeaconSide = Sides.OTHER;
    double startingLightLevel = 0;
    boolean IsPushed = false;
    boolean isRobotLost = false;
    int distance;
    int previousDistance;
    int direction;
    long pingTime;
    boolean gyroCorrection = false;
    int leftStartPosition;
    int rightStartPosition;
    int startDirection = 0;
    double startODS;
    Sides turnDirection = Sides.LEFT;
    private AdaFruitCS cs;


    // Colors used in Alliance, resQ beacon and line
    public enum Colors {
        RED, BLUE, WHITE, OTHER
    }

    // Sides used in turn, edge of a line
    public enum Sides {
        LEFT, RIGHT, OTHER
    }


    /**
     * Construct the class.
     * The system calls this member when the class is instantiated.
     */
    public TTAutoV3_Blue() {
        // Initialize base classes.
        // All via self-construction.

        // Initialize class members.
        // All via self-construction.
    }

    //*** Autonomous constants
    public static final int RED_LINE = 2;
    public static final int BLUE_LINE = 2;
    public static final int WHITE_LINE = 8;
    public static final int WHITE_CLOSENESS = 2;

    public static final double TURN_POWER = 0.25;
    public static final double NAV_HIGH_POWER = 0.7;
    public static final double NAV_MID_POWER = 0.4;


    @Override
    public void init() {

        //Get initialization..mainly servos.
        super.init();
        pusher_left.setPosition(63.0 / 255.0);
        pusher_right.setPosition(60.0 / 255.0);
        //resetEncoders();
        //allianceColor = (TTActivity.getProperty("alliance") == "Red") ? Colors.RED : Colors.BLUE;
        // Calibrate the gyro.
        gyro.calibrate();
        // Set all drive train motors to run using encoders
        //useEncoders();
        //Turn on LED of bottom color sensor-Used to detect line.
        mrcolor_under.enableLed(true);
        //dropper_1.setPosition(0);*/

        // Set all drive train motors to run using encoders
        //useEncoders();
        cs = new AdaFruitCS(hardwareMap, "adaColor", 2 * 0x29);
        cs.initColorSensor();


    }


    @Override
    public void start() {
        //Turn on AdaFruit
        cs.startColorPolling();
        //currentState = 8;
    }

    @Override
    public void loop() {

        /***************** Master stop/pause*************************
         * In case something goes wrong turn off ALL DC motors
         */

        if ((gamepad2.x) || (gamepad1.x)) {

//            dcRun(front_left, 0);
//            dcRun(front_right, 0);
//            dcRun(back_left, 0);
//            dcRun(back_right, 0);

        }

        switch (currentState) {
            //Tasks are broken down to finite STATES. We will increment to to next state after successful
            // completion of each state.

            case 0:
                //First state
                currentState++;
                pusher_left.setPosition(63.0 / 255.0);
                pusher_right.setPosition(60.0 / 255.0);
                leftStartPosition = Math.abs(front_left.getCurrentPosition());
                rightStartPosition = Math.abs(front_right.getCurrentPosition());
                break;
            case 1:
                // GO straight for a fixed distance (2.5 tiles/6800 enc count) at high speed.
                if (driveWithEncoders(.2, .2, 500, 500)) {
                    currentState++;
                    startDirection = gyro.getIntegratedZValue();
                }
                break;

            case 2:
                //Turn 90 degree towards the field goal
                //TODO: Blue>>Right , Red>>Left
                if (gyroPointTurn(TURN_POWER, Sides.RIGHT, 38)) {
                    currentState++;
                    leftStartPosition = Math.abs(front_left.getCurrentPosition());
                    rightStartPosition = Math.abs(front_right.getCurrentPosition());
                }
                break;


            case 3:
                // GO straight for a fixed distance (2.5 tiles/6800 enc count) at high speed.
                if (driveWithEncoders(.6, .6, 3000, 3000)) {
                    currentState++;
                }
                break;

            case 4:
                if (driveToColor(.1, .1, Colors.WHITE, 8000)) {
                    currentState++;
                    runtime.reset();
                }

                break;

            case 5:
//                //Empty state
                currentState++;
                // }
                break;
            case 6:
                //TODO: Red>>Turn Right(-,+), Blue>>Turn Left(+,-)
                if (driveToODS(.3, -.3, .2, 2000)) {
                    currentState++;
                    startDirection = gyro.getIntegratedZValue();
                }

                break;

            case 7:
                if (driveToTouch(.1, .1)) {
                    currentState++;
                    runtime.reset();
                }
                break;

            //TODO: Add First Button Push
            //**************************************
            case 8:
                if (!IsPushed) {
                    if (getBeaconColor() == Colors.BLUE) {
                        //Push Right Button
                        pusher_right.setPosition(125.0 / 255.0);
                        pusher_left.setPosition(63.0 / 255.0);
                        IsPushed = true;
                    } else if (getBeaconColor() == Colors.RED) {
                        //Push Left Button
                        pusher_left.setPosition(130.0 / 255.0);
                        pusher_right.setPosition(60.0 / 255.0);
                        IsPushed = true;
                    }
                }
                if (runtime.time() > 2500) {
                    currentState++;
                    runtime.reset();
                }
                break;
            //**************************************
            case 9:
                //Come back after button push
                pusher_left.setPosition(63.0 / 255.0);
                pusher_right.setPosition(60.0 / 255.0);
                if (runtime.time() < 250) {
                    front_left.setPower(-.3);
                    front_right.setPower(-.3);
                } else {
                    front_left.setPower(0);
                    front_right.setPower(0);
                    currentState++;
                    startDirection = gyro.getIntegratedZValue();
                }
                break;

            case 10:
                //Turn 90 degree towards second beacon
                //TODO: Blue>>Left , Red>>Right
                if (gyroPointTurn(.3, Sides.LEFT, 86)) {
                    IsPushed = false;
                    currentState++;
                    leftStartPosition = Math.abs(front_left.getCurrentPosition());
                    rightStartPosition = Math.abs(front_right.getCurrentPosition());
                }
                break;

            case 11:
                // GO straight toward 2nd beacon at high speed.
                if (driveWithEncoders(.3, .3, 2500, 1500)) {
                    currentState++;
                }
                break;

            case 12:
                if (driveToColor(.1, .1, Colors.WHITE, 8000)) {
                    currentState++;
                    //startDirection = gyro.getIntegratedZValue();
                    //Runtime.reset();
                    leftStartPosition = Math.abs(front_left.getCurrentPosition());
                    rightStartPosition = Math.abs(front_right.getCurrentPosition());
               runtime.reset();
                }

                break;

            case 13:

//                if (driveWithEncoders(.1, .1, 100, 100)) {
//                    currentState++;
//                    startDirection = gyro.getIntegratedZValue();
//                }

                if (runtime.time() < 100) {
                    front_left.setPower(-.2);
                    front_right.setPower(-.2);
                } else {
                    front_left.setPower(0);
                    front_right.setPower(0);
                    currentState++;
                    startDirection = gyro.getIntegratedZValue();
                }
                break;

            case 14:
                //TODO: Blue>>Right , Red>>Left
                if (gyroPointTurn(TURN_POWER, Sides.RIGHT, 88)) {
                    currentState++;

                }

                break;

            case 15:
                if (driveToTouch(.1, .1)) {
                    currentState++;
                    runtime.reset();
                }

                break;

            //TODO: Add 2nd Button Push
            //***************************************************
            case 16:
                if (!IsPushed) {
                    if (getBeaconColor() == Colors.BLUE) {
                        //Push Right Button
                        pusher_right.setPosition(125.0 / 255.0);
                        pusher_left.setPosition(63.0 / 255.0);
                        IsPushed = true;
                    } else if (getBeaconColor() == Colors.RED) {
                        //Push Left Button
                        pusher_left.setPosition(130.0 / 255.0);
                        pusher_right.setPosition(60.0 / 255.0);
                        IsPushed = true;
                    }
                }
                if (runtime.time() > 2500) {
                    currentState++;
                    runtime.reset();
                }
                break;
            //***************************************************
            case 17:
                pusher_left.setPosition(63.0 / 255.0);
                pusher_right.setPosition(60.0 / 255.0);
                if (runtime.time() < 500) {
                    front_left.setPower(-.2);
                    front_right.setPower(-.2);
                } else {
                    front_left.setPower(0);
                    front_right.setPower(0);
                    currentState++;
                    startDirection = gyro.getIntegratedZValue();
                }
                break;

            case 18:
                //TODO: Blue>>Right , Red>>Left
                if (gyroPointTurn(.5, Sides.RIGHT, 120)) {

                    pusher_left.setPosition(170.0 / 255.0);
                    pusher_right.setPosition(170.0 / 255.0);
                    leftStartPosition = Math.abs(front_left.getCurrentPosition());
                    rightStartPosition = Math.abs(front_right.getCurrentPosition());
                    currentState++;
                }

                break;
            case 19:
            if (driveWithEncoders(.4, .4, 5000, 5000)) {
                currentState++;
                startDirection = gyro.getIntegratedZValue();
            }
            break;


            case 99:
                // Recovery State. Any known failures will lead the state machine to this state.
                // Display in telemetry and log to the file
                break;

            default:
                // The autonomous actions have been accomplished (i.e. the state machine has
                // transitioned into its final state.
                break;

        }

        previousState = currentState;


        //Get Gyro reading. Ensure that it is not calibrating
        if (!gyro.isCalibrating()) {
            ///direction = gyro.getIntegratedZValue();
            //telemetry.addData("Gyro rotation", gyro_1.getRotation());
            telemetry.addData("start G", startDirection);
            telemetry.addData("Gyro intZ", gyro.getIntegratedZValue());

        }



        // Post telemetry
        telemetry.addData("BeaconColor", getBeaconColor());

        if (cs.isColorUpdate()) {
            telemetry.addData("red: ", cs.red());
            telemetry.addData("green: ", cs.green());
            telemetry.addData("blue: ", cs.blue());
        }
        telemetry.addData("ods_front", ods_front.getLightDetected());


    }

    @Override
    public void stop() {
        //Close data logger and Wire class of Maxbotix
        /*dl.closeDataLogger();
        ds.close();*/
        cs.close();
    }


     /*
       * ************** Helper Methods*************************
     */

    //Set both drive wheel encoder to run, if the mode is appropriate.
    public void useEncoders() {
        // perform the action on both motors.
        if (front_left != null) {
            front_left.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        }
        if (front_right != null) {
            front_right.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        }
    }

    //Reset both drive wheel encoders

    public void resetEncoders() {

        front_left.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        front_right.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
    }

    //Completion of Reset - both drive wheel encoders.

    public boolean resetComplete() {
        // Assume failure.
        boolean l_return = false;
        int pos = 0;
        if (front_left != null) {
            pos = +front_left.getCurrentPosition();
        }
        if (front_right != null) {
            pos = +front_right.getCurrentPosition();
        }


        if (pos == 0) {
            l_return = true;
            isResetRunning = false;
        }
        return l_return;
    }


    public boolean leftEncoder_reached(double count) {
        // Assume failure.
        boolean l_return = false;

        if (front_left != null) {
            // Has the encoder reached the specified values?
            if (Math.abs(front_left.getCurrentPosition()) >= count) {
                // Set the status to a positive indication.
                l_return = true;
            }
        }
        // Return the status.
        return l_return;
    }

    public boolean rightEncoder_reached(double count) {
        // Assume failure.
        boolean l_return = false;

        if (front_right != null) {
            // Has the encoder reached the specified values?
            if (Math.abs(front_right.getCurrentPosition()) >= count) {
                // Set the status to a positive indication.
                l_return = true;
            }
        }
        // Return the status.
        return l_return;
    }


    //driveWithEncoders:
    //==================
    //Drives all 4 wheel to a desired encoder count
    // it works on relative position. so, we don't need to reset encoder
    //TODO: fix going backward. It doesn't work

    boolean driveWithEncoders
            (double left_power
                    , double right_power
                    , double left_count
                    , double right_count
            )

    {

        front_left.setPower(left_power);
        front_right.setPower(right_power);
        if (leftEncoder_reached(left_count + leftStartPosition) || rightEncoder_reached(right_count + rightStartPosition)) {
            front_left.setPower(0);
            front_right.setPower(0);
            return true;
        }
        return false;
    }

    boolean driveToTouch
            (double left_power
                    , double right_power
            )

    {

        if (touch_left.isPressed()) {
            front_left.setPower(0);
        } else {
            front_left.setPower(left_power);
            if (touch_right.isPressed()) {
                front_left.setPower(left_power + .3);
            }

        }


        if (touch_right.isPressed()) {
            front_right.setPower(0);
        } else {
            front_right.setPower(right_power);
            if (touch_left.isPressed()) {
                front_right.setPower(right_power + .3);
            }


        }

        if ((touch_left.isPressed()) && (touch_right.isPressed())) {
            front_right.setPower(0);
            front_left.setPower(0);
            return true;
        }
        return false;
    }


    public Colors getBeaconColor() {
        Colors l_beacon = Colors.OTHER;

        if ((cs.red() > cs.blue()) && (cs.red() > cs.green())) {
            l_beacon = Colors.RED;
        } else if ((cs.blue() > cs.red()) && (cs.blue() > cs.green())) {
            l_beacon = Colors.BLUE;
        }
        return l_beacon;
    }

    //getLineColor:
    //================
    //


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


    boolean driveToColor
            (double left_power
                    , double right_power
                    , Colors targetColor
                    , int encoderMaxCount
            )

    {

        front_left.setPower(left_power);
        front_right.setPower(right_power);
        // Stop if we have detected the color OR the robot is lost
        //double y = 0.2126 * mrcolor_under.red() + 0.7152 * mrcolor_under.green() + 0.0722 * mrcolor_under.blue();
        //telemetry.addData("Colorcheck", y > 128);
        if (getLineColor() == targetColor) {
            // Stop the motors.
            front_left.setPower(0);
            front_right.setPower(0);
            return true;
        }
        // Return the status.
        return false;
    }


    boolean driveToODS
            (double left_power
                    , double right_power
                    , double odsValue
                    , int encoderMaxCount
            )

    {
        //We are straight, reading 0 heading
        front_left.setPower(left_power);
        front_right.setPower(right_power);

        // Stop if we have detected the color OR the robot is lost
        if ((ods_front.getLightDetected() > odsValue)) {
            // Stop the motors.
            front_left.setPower(0);
            front_right.setPower(0);
            //isRunning = false;
            return true;
        }

        // Return the status.
        return false;
    }

    boolean gyroPointTurn(double power
            , Sides turnDirection
            , int angle
    ) {

        if (turnDirection == Sides.LEFT) {
            front_left.setPower(-power);
            front_right.setPower(power);
        }
        if (turnDirection == Sides.RIGHT) {
            front_left.setPower(power);
            front_right.setPower(-power);
        }
        if ((gyro.getIntegratedZValue() >= (startDirection + angle)) || (gyro.getIntegratedZValue() <= (startDirection - angle))) {
            front_left.setPower(0);
            front_right.setPower(0);
            return true;
        }
        return false;
    }


}

