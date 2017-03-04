package com.techietitans.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@Autonomous
@Disabled
public class AutonomousTest extends LinearOpMode {

    DcMotor left;
    DcMotor right;

    @Override
    public void runOpMode() throws InterruptedException {
        left = hardwareMap.dcMotor.get("left");
        left.setDirection(DcMotorSimple.Direction.REVERSE);
        left.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);
        right = hardwareMap.dcMotor.get("right");
        right.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);

        left.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        right.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        left.setTargetPosition(5440);
        right.setTargetPosition(5440);

        left.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        right.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        
        waitOneFullHardwareCycle();
        waitForStart();

        while (left.getCurrentPosition() < 5440 && right.getCurrentPosition() < 5440) {
            left.setPower(.5);
            right.setPower(.5);
        }

        left.setPower(0);
        right.setPower(0);
    }
}