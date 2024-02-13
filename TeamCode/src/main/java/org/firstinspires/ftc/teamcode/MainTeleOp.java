package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.Servo;
import org.firstinspires.ftc.teamcode.MicroCMD.Command;
import org.firstinspires.ftc.teamcode.MicroCMD.CommandScheduler;

import java.util.function.Consumer;

@TeleOp(name="org.firstinspires.ftc.teamcode.MainTeleOp")
public class MainTeleOp extends LinearOpMode {
    DcMotor fl, bl, fr, br;
    Servo arm, pitch, claw;
    public double armIncrement = 0.0005;


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
                new Command(() -> {
                    arm.setPosition(armPosition);
                }),
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
                new Command(() -> updateArm()),
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
                new Command(() -> updateArm()),
                new Command(250),

                new Command(() -> claw.setPosition(clawClosed)), // Close claw
                new Command(200),
                new Command(() -> armPosition = 0.66),
                new Command(() -> updateArm()),
                new Command(() -> pitch.setPosition(0.168))
            ).schedule();

            armPickupStage = 1;
            isClawOpen = false;
        } else if (armPickupStage == 1) {
            // Arm is currently hovering over dustpan with a pixel

            new Command(
                new Command(() -> armPosition = armMin), // Slightly above pixel bottom
                new Command(() -> updateArm())
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
                    new Command(() -> updateArm())
            ).schedule();

            armPickupStage = 0;
            isClawOpen = true;
        }
    }




    @Override
    public void runOpMode() throws InterruptedException {
        fl = hardwareMap.dcMotor.get("backLeft");
        bl = hardwareMap.dcMotor.get("frontLeft");
        fr = hardwareMap.dcMotor.get("backRight");
        br = hardwareMap.dcMotor.get("frontRight");
        bl.setDirection(DcMotorSimple.Direction.REVERSE);
        fr.setDirection(DcMotorSimple.Direction.REVERSE);

        fl.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        bl.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        fr.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        br.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);


        arm = hardwareMap.servo.get("arm");
        pitch = hardwareMap.servo.get("pitch");
        claw = hardwareMap.servo.get("claw");

        Gamepad lastGamepad1 = new Gamepad();
        Gamepad lastGamepad2 = new Gamepad();

        waitForStart();

        while (opModeIsActive()) {
            CommandScheduler.loop();

            double y = -gamepad1.left_stick_y; // Remember, Y stick value is reversed
            double x = gamepad1.left_stick_x * 1.1; // Counteract imperfect strafing
//            double rx = gamepad1.right_stick_x;
            double rx = gamepad1.right_trigger - gamepad1.left_trigger;

            if (gamepad1.left_bumper && gamepad1.right_bumper) {

            } else if (gamepad1.left_bumper) {
                rx = rx - y;
            } else if (gamepad1.right_bumper) {
                rx = rx + y;
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


            if (gamepad1.dpad_left) {
                armPosition += armIncrement;
                arm.setPosition(armPosition);
            }
            if (gamepad1.dpad_right) {
                armPosition -= armIncrement;
                arm.setPosition(armPosition);
            }

            if (gamepad1.a && !lastGamepad1.a) {
                CommandScheduler.schedule(
                        new Command(
                                new Command(() -> {
                                    arm.setPosition(0.6);
                                }),
                                new Command(10000),
                                new Command(() -> {
                                    arm.setPosition(0.4);
                                })
                        )
                );

//                armPickup.schedule();
            }





            lastGamepad1.copy(gamepad1);
            lastGamepad2.copy(gamepad2);
        }
    }
}
