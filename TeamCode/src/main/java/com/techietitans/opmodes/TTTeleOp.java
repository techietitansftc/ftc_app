package com.techietitans.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.robocol.PeerApp;
import com.techietitans.libraries.HardwareClass_V2;

/**
 * Created by vinayjagan on 12/4/16.
 */

@TeleOp(group = "TechieTitans")
public class TTTeleOp extends HardwareClass_V2 {

    boolean xIsPressed = false;
    int direction = 1;
    double speed = .4;
    boolean leftBumperPressed = false;
    boolean rightBumperPressed = false;

    @Override
    public void init() {
        super.init();
        latch.setPosition(0);
        pusher_left.setPosition(170.0 / 255.0);
        pusher_right.setPosition(170.0 / 255.0);
        release.setPosition(0);
    }

    @Override
    public void loop() {
        if (gamepad1.x && !xIsPressed) {
            direction *= -1;
            xIsPressed = true;
        }
        if (xIsPressed && !gamepad1.x) {
            xIsPressed = false;
        }
        front_left.setPower(scaleInput(gamepad1.left_stick_y) * direction * speed);
        front_right.setPower(scaleInput(gamepad1.right_stick_y) * direction * speed);
        if (gamepad1.y) {
            release.setPosition(150.0 / 255.0);
        }
        if (gamepad1.left_bumper && !leftBumperPressed) {
            speed -= .1;
            leftBumperPressed = true;
        }
        if (leftBumperPressed && !gamepad1.left_bumper)
            leftBumperPressed = false;
        if (gamepad1.right_bumper && !rightBumperPressed) {
            speed += .1;
            rightBumperPressed = true;
        }
        if (rightBumperPressed && !gamepad1.right_bumper)
            rightBumperPressed = false;

        if (gamepad1.right_trigger > 0){
            shooter.setPower(.6);
        } else
            shooter.setPower(0);
        if (gamepad2.left_trigger >= 0.5) {
            pusher_left.setPosition(128.0 / 255.0);
        }
        if (gamepad2.right_trigger >= 0.5) {
            pusher_right.setPosition(128.0 / 255.0);
        }
        if (gamepad2.left_bumper) {
            pusher_left.setPosition(0);
        }
        if (gamepad2.right_bumper) {
            pusher_right.setPosition(0);
        }
        lift.setPower(scaleInput(gamepad2.left_stick_y));
        if (gamepad2.dpad_up)
            latch.setPosition(0);
        if (gamepad2.dpad_down)
            latch.setPosition(1);
    }

    double scaleInput(double dVal) {
        double[] scaleArray = {0.0, 0.05, 0.09, 0.10, 0.12, 0.15, 0.18, 0.24,
                0.30, 0.36, 0.43, 0.50, 0.60, 0.72, 0.85, 1.00, 1.00};

        // get the corresponding index for the scaleInput array.
        int index = (int) (dVal * 16.0);
        if (index < 0) {
            index = -index;
        } else if (index > 16) {
            index = 16;
        }

        double dScale = 0.0;
        if (dVal < 0) {
            dScale = -scaleArray[index];
        } else {
            dScale = scaleArray[index];
        }

        return dScale;
    }

}


