package com.techietitans.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.techietitans.libraries.AdaFruitCS;
import com.techietitans.libraries.DataLogger;
import com.qualcomm.robotcore.util.Range;


@Autonomous(name = "TTAutoV4", group = "TechieTitans")
public class TTAutoV4 extends TTTeleOp {

    int currentState = 0;
    int previousState = 0;
    boolean isRunning = false;
    boolean isResetRunning = false;
    DataLogger dl;
    private ElapsedTime runtime = new ElapsedTime(ElapsedTime.Resolution.MILLISECONDS);
    Colors allianceColor;
    Sides desiredBeaconSide = Sides.OTHER;
    boolean IsPushed = false;
    boolean isRobotLost = false;
    int leftStartPosition;
    int rightStartPosition;
    int startDirection = 0;
    Sides turnDirection = Sides.LEFT;
    private AdaFruitCS cs;
    boolean logEnabled = false;
    long logTime = System.currentTimeMillis();
    boolean colorSensorsDisabled = false;
    int recoveryCount = 0;
    int loopCounter = 0;
    int shooterInit = 0;
    int allianceSpecific;
    long ballshootTimeInit = 0;


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
    public TTAutoV4() {
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

    public static final double TURN_POWER = 0.45;
    public static final double NAV_HIGH_POWER = 0.7;
    public static final double NAV_MID_POWER = 0.4;


    @Override
    public void init() {
        //Get initialization..mainly servos.
        super.init();
        initBeaconPusher();
        // Calibrate the gyro.
        gyro.calibrate();
        // Set all drive train motors to run using encoders
        //useEncoders();
        //Turn on LED of bottom color sensor-Used to detect line.
        mrcolor_under.enableLed(true);
        mrcolor_front.enableLed(false);
        //Init AdaFruit Color sensor
        cs = new AdaFruitCS(hardwareMap, "adaColor", 2 * 0x29);
        cs.initColorSensor();
        isRunning = false;

        ball_dropper.setPosition(90.0 / 255.0);
    }

    //*****************************************************************************
    @Override
    public void init_loop() {
        //Get Alliance selection
        if (touch_left.isPressed()) {
            // RED Alliance
            allianceColor = Colors.RED;
        } else if (touch_right.isPressed()) {
            //BLUE Alliance
            allianceColor = Colors.BLUE;
        }
        //allianceColor = Colors.RED;
        telemetry.addData("**** ALLIANCE ****  ", allianceColor);
        if (gamepad1.y) {
            logEnabled = true;
        }
        logEnabled = true;
        dl = new DataLogger("Dl_TT_Auto_V4");
        telemetry.addData("**** Log Enabled ****", logEnabled);
    }

    //*****************************************************************************
    @Override
    public void start() {

        //Turn on AdaFruit
        cs.startColorPolling();
        //currentState = 15;
        shooterInit = shooter.getCurrentPosition();
        if (logEnabled) {
            //Set a new data logger and header of the file
            dl.addField("LoopCounter");
            dl.addField("State");
            dl.addField("Left Motor Position");
            dl.addField("Left Motor Power");
            dl.addField("Right Motor Position");
            dl.addField("Right Motor Power");
            dl.addField("Gyro");
            dl.addField("ODS");
            dl.addField("Left_Color");
            dl.addField("Left_R");
            dl.addField("Left_B");
            dl.addField("Left_G");
            dl.addField("Right_Color");
            dl.addField("Right_R");
            dl.addField("Right_B");
            dl.addField("Right_G");
            dl.addField("Left_Pusher");
            dl.addField("Right_Pusher");
            dl.addField("Bottom_Color");
            dl.addField("Bottom_R");
            dl.addField("Bottom_B");
            dl.addField("Bottom_G");
            dl.newLine();
        }
    }

    //*****************************************************************************
    @Override
    public void loop() {

        //*********Start State Machine
        switch (currentState) {
            //Tasks are broken down to finite STATES. We will increment to to next state after successful
            // completion of each state.

            case 0:
                //First state
                currentState++;
                initBeaconPusher();
                gyro.resetZAxisIntegrator();
                break;
            case 1:
                // Shoot the ball
                shooter.setPower(.6);
                if ((shooter.getCurrentPosition() - shooterInit) > 3750) {
                    shooter.setPower(0);
                    shooterInit = shooter.getCurrentPosition();
                    currentState++;
                }
                break;

            case 2:
                ball_dropper.setPosition(0);
                if (ball_dropper.getPosition() == 0) {
                    currentState++;
                    ballshootTimeInit = System.currentTimeMillis();
                }
                break;
            case 3:
                if (System.currentTimeMillis() - ballshootTimeInit > 2000) {
                    shooter.setPower(.6);
                    if ((shooter.getCurrentPosition() - shooterInit) > 2500) {
                        shooter.setPower(0);
                        shooterInit = shooter.getCurrentPosition();
                        currentState++;
                    }
                }

                break;
            case 4:
                // GO straight for a fixed distance at slow speed to line up
                // for turn towards 1st white line.
                if (driveWithEncoders(.3, .3, 500, 500)) {
                    currentState++;
                }
                break;

            case 5:
                //Turn 35-40 degree towards the first beacon's white line
                //TODO: Blue>>Right , Red>>Left
                turnDirection = (allianceColor == Colors.RED) ? Sides.LEFT : Sides.RIGHT;

                if (gyroPointTurn(.3, turnDirection, allianceColor == Colors.RED ? 55 : 40)) {
                    currentState++;
                    front_left.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
                    front_right.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
                }
                break;

            case 6:
                // GO straight for a fixed distance at high speed.
                if (driveWithEncoders(.8, .8, 3800, 3800)) {
                    currentState++;
                    front_left.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
                    front_right.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
                    recoveryCount = 0;
                }
                break;

            case 7:

                switch (driveToColor(.25, .25, Colors.WHITE, 2000)) {
                    case 1:
                        //reached white line..skip over next 2 RECOVERY steps
                        currentState = currentState + 2;
                    case 2:
                        recoveryCount++;
                        //Lost...take next 2 RECOVERY steps
                        if (recoveryCount > 2) {
                            currentState = 99;
                        } else {
                            currentState++;
                        }

                    default:
                        break;
                }

            case 8:

                // RECOVERY STEP -1
                // Come back
                if (driveWithEncoders(-.4, -.4, 2000, 2000)) {
                    currentState++;
                }
                break;

            case 9:
                // RECOVERY STEP -2
                // Turn another 10 degrees
                turnDirection = (allianceColor == Colors.RED) ? Sides.LEFT : Sides.RIGHT;
                if (gyroPointTurn(TURN_POWER, turnDirection, 10)) {
                    currentState = currentState - 2;
                    ;
                }
                break;

            case 10:
                // Come back
                if (driveWithEncoders(-.2, -.2, 50, 50)) {
                    currentState++;
                }
                break;

            case 11:
                // TODO: Red>>Turn Right(-,+), Blue>>Turn Left(+,-)
                if (allianceColor == Colors.RED) {
                    if (driveToODS(-.25, .25, .2, 2000)) {
                        currentState++;
                        runtime.reset();
                    }
                } else {
                    if (driveToODS(.25, -.25, .2, 2000)) {
                        currentState++;
                        runtime.reset();
                    }
                }
                break;

            case 12:
                // go touch the wall facing 1st beacon
                if (driveToTouch(.2, .2)) {
                    currentState++;
                    recoveryCount = 0;
                    runtime.reset();
                    gyro.resetZAxisIntegrator();
                }
                if (runtime.time()>3000){
                    currentState = currentState+2;
                }
                break;

            //TODO: First Button Push
            //**************************************
            case 13:
                pushBeacon();
                if (pushSuccessful()) {
                    //Push is successful..continue with usual path. skip over next 2 steps.
                    currentState = currentState + 3;
                } else if (runtime.time() > 2500) {
                    // Timed out.. take 2 additional RETRY steps - back and forward
                    if (recoveryCount < 3) {
                        recoveryCount++;
                        currentState++;
                    } else {
                        currentState = currentState + 3;
                    }
                }
                break;
            //**************************************

            case 14:
                //Beacon 1 - RECOVERY STEP 1: go back a little..leave the pusher extended
                if (driveWithEncoders(-.4, -.4, 500, 500)) {
                    initBeaconPusher();
                    currentState++;
                    runtime.reset();
                }
                break;

            case 15:
                //Beacon 1 - RECOVERY STEP 2: go forward..push again
                if (driveToTouch(.2, .2)) {
                    currentState = currentState - 2;
                    runtime.reset();
                }
                if (runtime.time()>3000){
                    currentState++;
                }
                break;

            case 16:
                // Come back after Beacon 1 push
                //TODO: At State.. Red 650, Blue 620
                initBeaconPusher();
                //TODO: ??? Not sure about encoder count to come back
                allianceSpecific = (allianceColor == Colors.RED) ? 650 : 630;
                if (driveWithEncoders(-.3, -.3, allianceSpecific, allianceSpecific)) {
                    currentState++;
                }
                break;

            case 17:
                //Turn 90 degree towards second beacon
                //TODO: Blue>>Left , Red>>Right
                turnDirection = (allianceColor == Colors.RED) ? Sides.RIGHT : Sides.LEFT;
                allianceSpecific = (allianceColor == Colors.RED) ? 85 : 82;
                if (gyroPointTurn(.3, turnDirection, allianceSpecific)) {
                    currentState++;
                    front_left.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
                    front_right.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
                }
                break;

            case 18:
                // GO straight toward 2nd beacon at high speed.
                if (driveWithEncoders(.8, .8, 2500, 2500)) {
                    currentState++;
                }
                break;

            case 19:
                if (driveToColor(.25, .25, Colors.WHITE, 8000) == 1) {
                    currentState++;
                }

                break;

            //TODO: Add corrections....Before turn
            case 20:
                allianceSpecific = (allianceColor == Colors.RED) ? 80 : 70;
                if (driveWithEncoders(-.2, -.2, allianceSpecific, allianceSpecific)) {
                    currentState++;
                }

                break;

            case 21:
                // Turn to the 2nd Beacon
                //TODO: Blue>>Right , Red>>Left
                turnDirection = (allianceColor == Colors.RED) ? Sides.LEFT : Sides.RIGHT;
                if (gyroPointTurn(.5, turnDirection, 80) || (ods_front.getLightDetected() > .19)) {
                    front_left.setPower(0);
                    front_right.setPower(0);
                    isRunning = false;
                    currentState++;

                }

                //.15 - works at home
               /* if (allianceColor == Colors.RED){
                    if (driveToODS(-.3, .3, .2, 2000)) {
                        currentState++;
                    }
                } else{
                if (driveToODS(.3, -.3, .2, 2000)) {
                    currentState++;
                }*/
                // }

                break;


            //TODO: Add corrections.... After turn
            case 22:

                //if (driveToColor(.25, .25, Colors.WHITE, 8000)==1) {
                currentState++;
                //runtime.reset();
                //}

                break;


            case 23:
                if (driveToTouch(.2, .2)) {
                    currentState++;
                    recoveryCount = 0;
                    IsPushed = false;
                    runtime.reset();
                }

                break;

            //TODO: 2nd Button Push
            //***************************************************
            case 24:
                pushBeacon();
                if (pushSuccessful()) {
                    //Push is successful..continue with usual path. skip over next 2 steps.
                    currentState = currentState + 3;
                } else if (runtime.time() > 2500) {
                    // Timed out.. take 2 additional RETRY steps - back and forward
                    recoveryCount++;
                    if (recoveryCount < 3) {
                        currentState++;
                    } else {
                        currentState = currentState + 3;
                    }
                }
                break;
            //***************************************************

            case 25:
                //Beacon 2 - RECOVERY STEP 1: go back a little..leave the pusher extended
                if (driveWithEncoders(-.2, -.2, 500, 500)) {
                    initBeaconPusher();
                    currentState++;
                }
                break;

            case 26:
                //Beacon 2 - RECOVERY STEP 2: go forward..push again
                if (driveToTouch(.2, .2)) {
                    currentState = currentState - 2;
                    runtime.reset();
                }
                break;

            case 27:
                initBeaconPusher();
                if (driveWithEncoders(-.3, -.3, 650, 650)) {
                    currentState++;
                }
                break;
            case 28:
                //TODO: Blue>>Right , Red>>Left
                turnDirection = (allianceColor == Colors.RED) ? Sides.LEFT : Sides.RIGHT;
                if (gyroPointTurn(.5, turnDirection, 120)) {
                    pusher_left.setPosition(170.0 / 255.0);
                    pusher_right.setPosition(170.0 / 255.0);
                    currentState++;
                }

                break;
            case 29:
                //5000
                if (driveWithEncoders(.8, .8, 4000, 4000)) {
                    currentState++;
                }
                break;

            case 99:
                // Recovery State. Any known failures will lead the state machine to this state.
                // Display in telemetry and log to the file
                front_left.setPower(0);
                front_right.setPower(0);
                telemetry.addData("**** ROBOT STOPPED at State: ****", previousState);
                break;

            default:
                // The autonomous actions have been accomplished (i.e. the state machine has
                // transitioned into its final state.
                break;
        }

        previousState = currentState;
        loopCounter++;
        telemetry.addData("Right Color", getRightBeaconColor());
        telemetry.addData("Left Color", getLeftBeaconColor());
        telemetry.addData("state: ", currentState);

        if (cs.isColorUpdate()) {
            telemetry.addData("red: ", cs.red());
            telemetry.addData("green: ", cs.green());
            telemetry.addData("blue: ", cs.blue());
        }
        //telemetry.addData("ods_front", ods_front.getLightDetected());
        //telemetry.addData("pushSuccess: ", pushSuccessful());


        // Write data to log file..if enabled and log duration has reached
        if ((logEnabled) && ((System.currentTimeMillis() - logTime) > 100)) {
            dl.addField(String.valueOf(loopCounter));
            dl.addField(String.valueOf(currentState));
            dl.addField(String.valueOf(front_left.getCurrentPosition()));
            dl.addField(String.valueOf(front_left.getPower()));
            dl.addField(String.valueOf(front_right.getCurrentPosition()));
            dl.addField(String.valueOf(front_right.getPower()));
            dl.addField(String.valueOf(gyro.getIntegratedZValue()));
            dl.addField(String.valueOf(ods_front.getLightDetected()));
            dl.addField(String.valueOf(getLeftBeaconColor()));
            dl.addField(String.valueOf(mrcolor_front.red()));
            dl.addField(String.valueOf(mrcolor_front.blue()));
            dl.addField(String.valueOf(mrcolor_front.green()));
            dl.addField(String.valueOf(getRightBeaconColor()));
            dl.addField(String.valueOf(cs.red()));
            dl.addField(String.valueOf(cs.blue()));
            dl.addField(String.valueOf(cs.green()));
            dl.addField(String.valueOf(pusher_left.getPosition()));
            dl.addField(String.valueOf(pusher_right.getPosition()));
            dl.addField(String.valueOf(getLineColor()));
            dl.addField(String.valueOf(mrcolor_under.red()));
            dl.addField(String.valueOf(mrcolor_under.blue()));
            dl.addField(String.valueOf(mrcolor_under.green()));
            dl.addField(String.valueOf(IsPushed));
            dl.addField(String.valueOf(pushSuccessful()));
            dl.addField(String.valueOf(recoveryCount));
            dl.newLine();
            //Reset counter
            logTime = System.currentTimeMillis();
        }
    }

    @Override
    public void stop() {
        //Close data logger and Adafruit
        if (logEnabled) {
            dl.closeDataLogger();
        }

        cs.close();
    }

     /*
     * ************** Helper Methods*************************
     */

    //driveWithEncoders:
    //==================
    //Drives all 4 wheel to a desired encoder count
    // it works on relative position. so, we don't need to reset encoder


    boolean driveWithEncoders
            (double left_power
                    , double right_power
                    , double left_count
                    , double right_count
            )

    {
        if (!isRunning) {
            //This block should only execute once
            //Set starting position
            leftStartPosition = front_left.getCurrentPosition();
            rightStartPosition = front_right.getCurrentPosition();
            //Set motor speed
            front_left.setPower(left_power);
            front_right.setPower(right_power);
            isRunning = true;
        }

        //ToDo: add proportional slow down

        //Done - if the target is reached
        if (leftEncoder_reached(left_count) || rightEncoder_reached(right_count)) {
            front_left.setPower(0);
            front_right.setPower(0);
            isRunning = false;
            return true;
        }
        return false;
    }

    //gyroPointTurn:
    //================
    //
    boolean gyroPointTurn(double power
            , Sides turnDirection
            , int angle
    ) {
        int progress;
        int error;
        double correction;

        if (!isRunning) {
            //This block should only execute once
            //Set starting position
            startDirection = gyro.getIntegratedZValue();
            isRunning = true;
        }

        //ToDo: add proportional slow down. This is a bit tricky
        // Power = Power*Error*P
        // IntegratedZ value behaves much like Motor encoders. It keeps increasing
        // Or decreasing from initial calibration point based on direction.
        // Progress = Abs(Current position- start position)
        // Error = Target - Progress
        // So, Target will be reached as soon as Error is below threshold

        progress = Math.abs(gyro.getIntegratedZValue() - startDirection);
        error = angle - progress;
        correction = Range.clip(error * 0.1, 0, 1); // P coefficient = .1
        power = power * correction;

        if (turnDirection == Sides.LEFT) {
            front_left.setPower(-power);
            front_right.setPower(power);
        }
        if (turnDirection == Sides.RIGHT) {
            front_left.setPower(power);
            front_right.setPower(-power);
        }
        // Target is reached if error is within threshold.. (2 degrees)
        if (error <= 2) {
            front_left.setPower(0);
            front_right.setPower(0);
            isRunning = false;
            return true;
        }
        return false;
    }

    //driveToTouch:
    //==================
    boolean driveToTouch
    (double left_power
            , double right_power
    )

    {
        if (!isRunning) {
            //
            startDirection = gyro.getIntegratedZValue();
            isRunning = true;
        }


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
            isRunning = false;
            return true;
        }
        return false;
    }

    //getBeaconColor:
    //==================
    public Colors getRightBeaconColor() {
        Colors l_beacon = Colors.OTHER;

        if ((cs.red() > cs.blue()) && (cs.red() > cs.green())) {
            l_beacon = Colors.RED;
        } else if ((cs.blue() > cs.red()) && (cs.blue() > cs.green())) {
            l_beacon = Colors.BLUE;
        }
        return l_beacon;
    }

    public Colors getLeftBeaconColor() {
        Colors l_beacon = Colors.OTHER;
        //TODO: Chenge it to ModernRobotics Left Color sensor

        if ((mrcolor_front.red() > mrcolor_front.blue()) && (mrcolor_front.red() > mrcolor_front.green())) {
            l_beacon = Colors.RED;
        } else if ((mrcolor_front.blue() > mrcolor_front.red()) && (mrcolor_front.blue() > mrcolor_front.green())) {
            l_beacon = Colors.BLUE;
        }
        return l_beacon;
    }

    //pushBeacon:
    //==================
    boolean pushBeacon() {
        if (!IsPushed) {
            if (getRightBeaconColor() == allianceColor) {
                //Push Right Button
                pusher_right.setPosition(125.0 / 255.0);
                pusher_left.setPosition(80.0 / 255.0);
                IsPushed = true;

            } else {
                //Push Left Button
                pusher_left.setPosition(125.0 / 255.0);
                pusher_right.setPosition(80.0 / 255.0);
                IsPushed = true;

            }
        }
        return true;
    }

    //pushSuccessful:
    //==================
    boolean pushSuccessful() {
        //Both Beacon Color matches alliance color
        if ((getRightBeaconColor() == allianceColor) && (getLeftBeaconColor() == allianceColor)) {
            return true;
        }
        return false;
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

    //driveToColor:
    //================
    //
    int driveToColor
    (double left_power
            , double right_power
            , Colors targetColor
            , int encoderMaxCount
    )

    {
        if (!isRunning) {
            //This block should only execute once
            //Set starting position
            leftStartPosition = front_left.getCurrentPosition();
            rightStartPosition = front_right.getCurrentPosition();
            //Set motor speed
            front_left.setPower(left_power);
            front_right.setPower(right_power);
            isRunning = true;
        }

        if (getLineColor() == targetColor) {
            // Stop the motors.
            front_left.setPower(0);
            front_right.setPower(0);
            isRunning = false;
            return 1;
        }

        if ((Math.abs(front_left.getCurrentPosition() - leftStartPosition) > encoderMaxCount) ||
                (Math.abs(front_right.getCurrentPosition() - rightStartPosition) > encoderMaxCount)) {
            front_left.setPower(0);
            front_right.setPower(0);
            isRunning = false;
            return 2;
        }
        // Return the status.
        return 0;
    }

    //driveToODS:
    //================
    //
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

    //Reset both drive wheel encoders.

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
            if (Math.abs(front_left.getCurrentPosition() - leftStartPosition) >= count) {
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
            if (Math.abs(front_right.getCurrentPosition() - rightStartPosition) >= count) {
                // Set the status to a positive indication.
                l_return = true;
            }
        }
        // Return the status.
        return l_return;
    }

    public boolean initBeaconPusher() {
        pusher_left.setPosition(80.0 / 255.0);
        pusher_right.setPosition(80.0 / 255.0);
        return true;
    }

    /***
     * enable color sensors by re-registering callbacks
     */
    public void colorEnable() {
        if (colorSensorsDisabled) {
            if (underColorCallback != null)
                underColorController.registerForI2cPortReadyCallback(underColorCallback, mrcolor_under.getPort());
            if (frontColorCallback != null)
                frontColorController.registerForI2cPortReadyCallback(frontColorCallback, mrcolor_front.getPort());
        }
        colorSensorsDisabled = false;
    }

    /***
     * disable color sensors by de-registering callbacks
     */
    public void colorDisable() {
        if (!colorSensorsDisabled) {
            underColorController.deregisterForPortReadyCallback(mrcolor_under.getPort());
            frontColorController.deregisterForPortReadyCallback(mrcolor_front.getPort());
        }
        colorSensorsDisabled = true;
    }
}

