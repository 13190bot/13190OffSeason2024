/*
MainTeleOp.java

For 2023 CENTERSTAGE FTC




Ideas:
- Possibly use left and right bumpers for intake and advance armstage
- Recorder?
- LockRotate
 */

package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.*;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.teamcode.MicroCMD.Command;
import org.firstinspires.ftc.teamcode.MicroCMD.CommandScheduler;

@TeleOp(name="org.firstinspires.ftc.teamcode.MainTeleOp")
public class MainTeleOp extends LinearOpMode {
    DcMotor fl, bl, fr, br, intake, liftLeft, liftRight;
    Servo arm, pitch, claw;
    IMU imu;

//    public double armIncrement = 0.0005;
    public double intakePower = 0.4;
    public static double maxLift = 4600;
    public static double minLift = 0;
    // LockRotate
    public static double lockRotateIncrement = Math.toRadians(180);
    public Double lastLockRotateStartHeading = null;
    public static double wheelRotateMultiplier = 0.9; // forward will be multiplied by this when WheelRotating




    // OLD
    public final static double armMin = 0.15; // arm on board
    public final static double armMax = 0.72; // arm: when arm on dustpan

    public final static double pitchMin = 0.238; // pitch: when arm on dustpan // 0.22 when red tape
    public final static double pitchMax = 0.72; // pitch: when arm on board

    public final static double clawClosed = 0.19; // claw: when closed
    public final static double clawOpen = 0.04; // claw: when open
    public boolean isClawOpen = true;

    public boolean axonInitialized = false;
    public boolean noPitchDelayForNext = true; // well, we need to delay pitch when axon is initializing to make it sync
    public double armPosition = 1;
    public void updateArm() {
        double armPercent = (armPosition - armMin) / (armMax - armMin);
        if (armPercent < 0.5) {
            // arm is in "scoring position"
            new Command(
                // arm is mostly up: adjust pitch so that pitch/claw is perpendicular against wall
                new Command(() -> arm.setPosition(armPosition)),
                // pitch delay code to sync with axon
                new Command(axonInitialized ? (noPitchDelayForNext ? 0 : 300) : 500),
                new Command(() -> {
                    double pitchPercent = (0.5 - armPercent) / (0.5);
                    // Adjust pitch to be perpendicular against board
                    pitch.setPosition((1 - pitchPercent) * (1 - pitchMax) + pitchMax); // perpendicular to board
                    axonInitialized = true;
                    noPitchDelayForNext = false;
                })
            ).schedule();
        } else {
            // arm is mostly down: we do not want to move the pitch
            pitch.setPosition(pitchMin); // ready to pick up
            arm.setPosition(armPosition);
        }
    }
    /*
    -1: first run
    0: on dustpan
    1: hovering over dustpan
    2: scoring (extended)
     */
    public double armPickupStage = -1;

    // Moves to the next stage of arm
    public void armPickup() {
        if (armPickupStage == -1) {
            // Arm is currently unpowered, on dustpan

            new Command(
                new Command(() -> claw.setPosition(clawClosed)), // Close claw
                new Command(250),
                new Command(() -> armPosition = 0.66),
                new Command(this::updateArm),
                new Command(250),
                new Command(() -> pitch.setPosition(0.168))
            ).schedule();

            armPickupStage = 1;
            isClawOpen = false;
        } else if (armPickupStage == 0) {
            // Arm is currently hovering over dustpan

            new Command(
                new Command(() -> arm.setPosition(armMax - 0.02 * 5)),
                new Command(50),
                new Command(() -> arm.setPosition(armMax - 0.02 * 4)),
                new Command(50),
                new Command(() -> arm.setPosition(armMax - 0.02 * 3)),
                new Command(50),
                new Command(() -> arm.setPosition(armMax - 0.02 * 2)),
                new Command(50),
                new Command(() -> arm.setPosition(armMax - 0.02)),
                new Command(50),

                new Command(() -> armPosition = armMax),
                new Command(this::updateArm),
                new Command(250),

                new Command(() -> claw.setPosition(clawClosed)), // Close claw
                new Command(200),
                new Command(() -> armPosition = 0.66),
                new Command(this::updateArm),
                new Command(() -> pitch.setPosition(0.168))
            ).schedule();

            armPickupStage = 1;
            isClawOpen = false;
        } else if (armPickupStage == 1) {
            // Arm is currently hovering over dustpan with a pixel

            new Command(
                new Command(() -> armPosition = armMin), // Slightly above pixel bottom
                new Command(this::updateArm)
            ).schedule();

            armPickupStage = 2;
            isClawOpen = false;
        } else if (armPickupStage == 2) {
            // Arm is currently hovering scoring

            new Command(
                    new Command(() -> claw.setPosition(clawOpen)), // Open claw
                    new Command(100),

                    // Shake it
                    new Command(() -> pitch.setPosition(pitch.getPosition() + 0.1)),
                    new Command(() -> arm.setPosition(arm.getPosition() + 0.1)),
                    new Command(100),
                    new Command(() -> pitch.setPosition(pitch.getPosition() - 0.1)),
                    new Command(100),

                    new Command(500),

                    // Move arm back
                    new Command(() -> armPosition = 0.655),
                    new Command(this::updateArm)
            ).schedule();

            armPickupStage = 0;
            isClawOpen = true;
        }
    }




    @Override
    public void runOpMode() {
        fl = hardwareMap.get(DcMotor.class, "backLeft");
        bl = hardwareMap.get(DcMotor.class, "frontLeft");
        fr = hardwareMap.get(DcMotor.class, "backRight");
        br = hardwareMap.get(DcMotor.class, "frontRight");
        bl.setDirection(DcMotorSimple.Direction.REVERSE);
        fr.setDirection(DcMotorSimple.Direction.REVERSE);

        fl.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        bl.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        fr.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        br.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        arm = hardwareMap.get(Servo.class, "arm");
        pitch = hardwareMap.get(Servo.class, "pitch");
        claw = hardwareMap.get(Servo.class, "claw");

        intake = hardwareMap.get(DcMotor.class, "intakeMotor");

        liftLeft = hardwareMap.get(DcMotor.class, "liftLeft");
        liftRight = hardwareMap.get(DcMotor.class, "liftRight");
        liftRight.setDirection(DcMotorSimple.Direction.REVERSE);

        liftLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        liftRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        liftLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        liftRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        imu = hardwareMap.get(IMU.class, "imu");
        IMU.Parameters parameters = new IMU.Parameters(new RevHubOrientationOnRobot(
                RevHubOrientationOnRobot.LogoFacingDirection.UP,
                RevHubOrientationOnRobot.UsbFacingDirection.FORWARD));
        imu.initialize(parameters);


        Gamepad lastGamepad1 = new Gamepad();
        Gamepad lastGamepad2 = new Gamepad();

        waitForStart();

        while (opModeIsActive()) {
            CommandScheduler.loop();

            // IMU
            // This button choice was made so that it is hard to hit on accident,
            // it can be freely changed based on preference.
            // The equivalent button is start on Xbox-style controllers.
            if (gamepad1.options) {
                imu.resetYaw();
            }
            double botHeading = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS);

            // DRIVE
            double y = -gamepad1.left_stick_y; // Remember, Y stick value is reversed
            double x = gamepad1.left_stick_x * 1.1; // Counteract imperfect strafing
//            double rx = gamepad1.right_stick_x;
            double rx = gamepad1.right_trigger - gamepad1.left_trigger;

            if (gamepad1.left_bumper & !gamepad1.right_bumper) {
                rx = rx - y;
                y *= wheelRotateMultiplier;
            } else if (!gamepad1.left_bumper & gamepad1.right_bumper) {
                rx = rx + y;
                y *= wheelRotateMultiplier;
            }

            // LockRotate
            if (rx != 0) {
                if (lastLockRotateStartHeading == null) {
                    lastLockRotateStartHeading = botHeading;
                }

                // Limit
                if (botHeading - lastLockRotateStartHeading > lockRotateIncrement || botHeading - lastLockRotateStartHeading < -lockRotateIncrement) {
                    rx = 0;
                }
            } else {
                lastLockRotateStartHeading = null;
            }

            // Denominator is the largest motor power (absolute value) or 1
            // This ensures all the powers maintain the same ratio,
            // but only if at least one is out of the range [-1, 1]
            double denominator = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 1);
            double frontLeftPower = (y + x + rx) / denominator;
            double backLeftPower = (y - x + rx) / denominator;
            double frontRightPower = (y - x - rx) / denominator;
            double backRightPower = (y + x - rx) / denominator;

            fl.setPower(frontLeftPower);
            bl.setPower(backLeftPower);
            fr.setPower(frontRightPower);
            br.setPower(backRightPower);

            // ARM
            if (gamepad1.b && !lastGamepad1.b) {
                armPickup();
            }

            // INTAKE
            if (gamepad1.a) {
                intake.setPower(intakePower);
            } else {
                intake.setPower(0);
            }

            // LIFT
            double ry = -gamepad1.left_stick_y; // Remember, Y stick value is reversed
            double cep = liftLeft.getCurrentPosition();
            if (ry > 0 && cep < maxLift || ry < 0 && cep > minLift) {
                liftLeft.setPower(-ry);
                liftRight.setPower(-ry);
            }






            // TESTING

            if (gamepad1.x && !lastGamepad1.x) {
                new Command(
                    new Command(() -> arm.setPosition(0.6)),
                    new Command(1000),
                    new Command(() -> arm.setPosition(0.4))
                ).schedule();
            }





            lastGamepad1.copy(gamepad1);
            lastGamepad2.copy(gamepad2);
        }
    }
}
