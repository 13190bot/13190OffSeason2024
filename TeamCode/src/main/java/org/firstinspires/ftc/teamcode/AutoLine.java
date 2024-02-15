/*
NOTES:
- using front left - 43000
- making the bot move one tile
*/
package org.firstinspires.ftc.teamcode;


import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@Autonomous
public class AutoLine extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        DcMotor fl = hardwareMap.get(DcMotor.class, "backLeft");
        DcMotor bl = hardwareMap.get(DcMotor.class, "frontLeft");
        DcMotor fr = hardwareMap.get(DcMotor.class, "backRight");
        DcMotor br = hardwareMap.get(DcMotor.class, "frontRight");
        bl.setDirection(DcMotorSimple.Direction.REVERSE);
        fr.setDirection(DcMotorSimple.Direction.REVERSE);
        fl.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        bl.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        fr.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        br.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        fl.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        bl.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        fr.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        br.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        fl.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        bl.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        fr.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        br.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        int distance = 43000;
        double power = 0.2;

        waitForStart();
        while (opModeIsActive()) {
        telemetry.addData("frontLeft", fl.getCurrentPosition());
            if (fl.getCurrentPosition() < distance) {
                fl.setPower(power);
                bl.setPower(power);
                fr.setPower(power);
                br.setPower(power);
            } else {
                fl.setPower(0);
                bl.setPower(0);
                fr.setPower(0);
                br.setPower(0);
            }

            telemetry.addData("br", br.getCurrentPosition());
            telemetry.addData("fr", fr.getCurrentPosition());
            telemetry.addData("bl", bl.getCurrentPosition());
            telemetry.addData("fl", fl.getCurrentPosition());
            telemetry.update();
        }
    }
}
