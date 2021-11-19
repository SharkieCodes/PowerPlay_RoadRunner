package org.firstinspires.ftc.teamcode.probotix.drive;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.probotix.main.SampleMecanumDriveCancelable;
import org.firstinspires.ftc.teamcode.probotix.main.hardware;

/**
 * This opmode demonstrates how one can augment driver control by following Road Runner arbitrary
 * Road Runner trajectories at any time during teleop. This really isn't recommended at all. This is
 * not what Trajectories are meant for. A path follower is more suited for this scenario. This
 * sample primarily serves as a demo showcasing Road Runner's capabilities.
 * <p>
 * This bot starts in driver controlled mode by default. The player is able to drive the bot around
 * like any teleop opmode. However, if one of the select buttons are pressed, the bot will switch
 * to automatic control and run to specified location on its own.
 * <p>
 * If A is pressed, the bot will generate a splineTo() trajectory on the fly and follow it to
 * targetA (x: 45, y: 45, heading: 90deg).
 * <p>
 * If B is pressed, the bot will generate a lineTo() trajectory on the fly and follow it to
 * targetB (x: -15, y: 25, heading: whatever the heading is when you press B).
 * <p>
 * If Y is pressed, the bot will turn to face 45 degrees, no matter its position on the field.
 * <p>
 * Pressing X will cancel trajectory following and switch control to the driver. The bot will also
 * cede control to the driver once trajectory following is done.
 * <p>
 * The following may be a little off with this method as the trajectory follower and turn
 * function assume the bot starts at rest.
 * <p>
 * This sample utilizes the SampleMecanumDriveCancelable.java class.
 */
@TeleOp(group = "advanced")
public class roadRunnerDrive extends LinearOpMode {
    private hardware Hardware;


    // Define 2 states, drive control or automatic control
    enum Mode {
        DRIVER_CONTROL,
        AUTOMATIC_CONTROL
    }

    Mode currentMode = Mode.DRIVER_CONTROL;

    @Override
    public void runOpMode() throws InterruptedException {
        SampleMecanumDriveCancelable drive = new SampleMecanumDriveCancelable(hardwareMap);
        this.Hardware = new hardware(hardwareMap);
        Hardware.init();
        Hardware.setGear(hardware.Gear.FOURTH);



        double turnspeed = 1.2;
        int xl = 1;
        int xr = 1;
        boolean g2ltPS = false;

        drive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        waitForStart();

        if (isStopRequested()) return;

        while (opModeIsActive() && !isStopRequested()) {

            drive.update();

            switch (currentMode) {
                case DRIVER_CONTROL:
                    drive.setWeightedDrivePower(
                            new Pose2d(
                                    -gamepad1.left_stick_y * Hardware.getGear().getMaxSpeed(),
                                    gamepad1.left_stick_x * Hardware.getGear().getMaxSpeed(),
                                    -gamepad1.right_stick_x * Hardware.getGear().getMaxSpeed() * turnspeed
                            )
                    );


                    if (Hardware.getGear() == null) {
                        Hardware.setGear(hardware.Gear.SECOND);
                    }

                    if (gamepad1.a) {
                        Hardware.setGear(hardware.Gear.FIRST);
                        turnspeed = 1;
                    } else if (gamepad1.b) {
                        Hardware.setGear(hardware.Gear.SECOND);
                        turnspeed = 0.8;
                    } else if (gamepad1.x) {
                        Hardware.setGear(hardware.Gear.THIRD);
                        turnspeed = 0.6;
                    } else if (gamepad1.y) {
                        Hardware.setGear(hardware.Gear.FOURTH);
                        turnspeed = 0.6;
                    }

                    //lift: start:0 eind:-2050
                    if (gamepad2.dpad_down) {
                        Hardware.getLiftMotor().setTargetPosition(0);
                        Hardware.getLiftMotor().setPower(0.4);

                    } else if (gamepad2.dpad_right) {
                        Hardware.getLiftMotor().setTargetPosition(-400);
                        Hardware.getLiftMotor().setPower(0.4);

                    } else if (gamepad2.dpad_left) {
                        Hardware.getLiftMotor().setTargetPosition(-1000);
                        Hardware.getLiftMotor().setPower(0.4);

                    } else if (gamepad2.dpad_up) {
                        Hardware.getLiftMotor().setTargetPosition(-1510);
                        Hardware.getLiftMotor().setPower(0.4);

                    }


                    //servo: start:0.73 mid:0.45 eind:0.15
                    if (gamepad2.a) {
                        Hardware.getDeliverServo().setPosition(0.75);
                    } else if (gamepad2.b) {
                        Hardware.getDeliverServo().setPosition(0.45);
                    } else if (gamepad2.y) {
                        Hardware.getDeliverServo().setPosition(0.14);
                    }

                    if (gamepad2.right_trigger > 0.1) {
                        Hardware.getIntakeMotor().setPower(xr * -0.7);
                    } else {
                        Hardware.getIntakeMotor().setPower(0);
                    }

                    if (leftBumperClick(gamepad2.left_bumper)) {
                        xl = xl * -1;
                    }

                    if (rightBumperClick(gamepad2.right_bumper)) {
                        xr = xr * -1;
                    }

                    if (gamepad2.left_trigger > 0.2){
                        Hardware.getCarouselMotor().setPower(xl * 0.5);
                    } else {
                        Hardware.getCarouselMotor().setPower(0);
                    }

                    if (xl == 1) {
                        telemetry.addData("CarouselColour","Red");
                    } else if (xl == -1) {
                        telemetry.addData("CarouselColour","Blue");
                    }

                    telemetry.addData("liftTicks", Hardware.getLiftMotor().getCurrentPosition());
                    telemetry.update();

                    //servo: start:0.73 mid:0.45 eind:0.12
                    //lift: start:0 eind:-2050


                case AUTOMATIC_CONTROL:

                    if (gamepad2.x) {
                        drive.cancelFollowing();
                        currentMode = Mode.DRIVER_CONTROL;
                    }

                    if (!drive.isBusy()) {
                        currentMode = Mode.DRIVER_CONTROL;
                    }

                    break;


            }
        }
    }

    private boolean leftBumperPreviousState;
    public boolean leftBumperClick (boolean button) {
        boolean returnVal;
        returnVal = button && !leftBumperPreviousState;
        leftBumperPreviousState = button;
        return returnVal;
    }

    private boolean rightBumperPreviousState;
    public boolean rightBumperClick (boolean button) {
        boolean returnVal;
        returnVal = button && !rightBumperPreviousState;
        rightBumperPreviousState = button;
        return returnVal;
    }

}