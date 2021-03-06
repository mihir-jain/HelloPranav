package org.firstinspires.ftc.teamcode;

//importing the statements for the code below
import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.arcrobotics.ftclib.hardware.RevIMU;
import com.arcrobotics.ftclib.hardware.ServoEx;
import com.arcrobotics.ftclib.hardware.motors.Motor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;
import com.arcrobotics.ftclib.controller.PIDController;


//importing the statements for the code below
import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;
import com.vuforia.CameraDevice;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.matrices.VectorF;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackableDefaultListener;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;
import static org.firstinspires.ftc.robotcore.external.navigation.AngleUnit.DEGREES;
import static org.firstinspires.ftc.robotcore.external.navigation.AxesOrder.XYZ;
import static org.firstinspires.ftc.robotcore.external.navigation.AxesOrder.XZY;
import static org.firstinspires.ftc.robotcore.external.navigation.AxesReference.EXTRINSIC;

@Config
@TeleOp(name="TelehuhuuiuuiuiuOp", group="Iterative TeamCode")
public class TeleOpStates extends OpMode {

    //defining all of the variables needed for the code
    private ElapsedTime runtime = new ElapsedTime();
    private Motor LFMotor, LBMotor, RFMotor, RBMotor, conveyorMotor, elevatorMotor, leftShooterMotor, rightShooterMotor;
    private Servo rightIntakeDownServo, leftIntakeDownServo, wobbleGoalClawServo;
    private CRServo liftWobbleGoalServo;
    private BNO055IMU imu;
    private double lastAngles = 0;
    private boolean fieldRelativeMode = false;
    private double globalAngle, speed = 0.75;
    private static double shooterSpeed = 0.632;
    private static double anglioso = 0;
    public static double ratioNumber = 1.2;
    private static double highGoalNumber = 0.64284;
    public static double powerGoalNumber = 0.515;
    private static double xDistanceAdder = 0;

    private static final String VUFORIA_KEY =
            "AdK8BJf/////AAABmYCQFYMhCUEGpGiqBsjt6S9yKYcJbGmiZ8d9viFyvxFFTKpiCBwppicoI9FIGnm94cMjowewKG6d+1qKG55H92H6z2NVPrO4tplSO73k3cADtvGj/Zf9ennYyphiQdOJQSty+0MhKTcPUL9BokHQauvZR5v/mmYt+wGaoGuKB6jwprg7XGCR11UvFtafrntEn2p6EMMGy0ctEpA8dMIV0qT4pGi5w6/xve/yBegOt/mBbkaFViA8he6YjJHfS3xAGUShtWhgcPmqeM2c4nkDFfRxRhtBWPIgdc2Wu2Ud/kcw3SHId0DGSOauW6YWVnYGv7FJ5EzCDXYfmttCQEw5P9Rku0RL5um/e6yDNvWbRlmD";

    // Since ImageTarget trackables use mm to specifiy their dimensions, we must use mm for all the physical dimension.
    // We will define some constants and conversions here
    private static final float mmPerInch        = 25.4f;
    private static final float mmTargetHeight   = (6) * mmPerInch;          // the height of the center of the target image above the floor

    // Constants for perimeter targets
    private static final float halfField = 72 * mmPerInch;
    private static final float quadField  = 36 * mmPerInch;

    // Class Members
    private OpenGLMatrix lastLocation = null;
    private VuforiaLocalizer vuforia = null;
    private PIDController pidRotate;
    private org.firstinspires.ftc.teamcode.PIDController pidRotateLeft;

    List<VuforiaTrackable> allTrackables;

    WebcamName webcamName = null;

    private boolean targetVisible = false;

    VuforiaTrackables targetsUltimateGoal;

    @Override
    public void init() throws IllegalArgumentException {

        //grabbing the hardware from the expansion hubs, and the configuration
        LFMotor  = new Motor(hardwareMap, "LF Motor", Motor.GoBILDA.RPM_1150);
        LBMotor  = new Motor(hardwareMap, "LB Motor", Motor.GoBILDA.RPM_1150);
        RFMotor  = new Motor(hardwareMap, "RF Motor", Motor.GoBILDA.RPM_1150);
        RBMotor  = new Motor(hardwareMap, "RB Motor", Motor.GoBILDA.RPM_1150);
        conveyorMotor = new Motor(hardwareMap, "Conveyor Motor", Motor.GoBILDA.RPM_435);
        elevatorMotor = new Motor(hardwareMap, "Elevator Motor", Motor.GoBILDA.RPM_84);
        leftShooterMotor = new Motor(hardwareMap, "Left Shooter Motor", Motor.GoBILDA.RPM_1150);
        rightShooterMotor = new Motor(hardwareMap, "Right Shooter Motor", Motor.GoBILDA.RPM_1150);

        liftWobbleGoalServo = hardwareMap.crservo.get("Lift Wobble Goal Servo");
        wobbleGoalClawServo = hardwareMap.get(Servo.class, "Wobble Goal Claw Servo");
        rightIntakeDownServo = hardwareMap.get(Servo.class, "Right Intake Down Servo");
        leftIntakeDownServo = hardwareMap.get(Servo.class, "Left Intake Down Servo");

        imu = hardwareMap.get(BNO055IMU.class, "imu 1");

        //reversing the motors that need to be reversed, otherwise it sets it as forward
        LFMotor.setInverted(false);
        LBMotor.setInverted(false);
        RFMotor.setInverted(true);
        RBMotor.setInverted(true);
        conveyorMotor.setInverted(true);
        elevatorMotor.setInverted(true);
        leftShooterMotor.setInverted(false);
        rightShooterMotor.setInverted(true);

        rightIntakeDownServo.setDirection(Servo.Direction.REVERSE);
        leftIntakeDownServo.setDirection(Servo.Direction.FORWARD);
        wobbleGoalClawServo.setDirection(Servo.Direction.FORWARD);
        liftWobbleGoalServo.setDirection(CRServo.Direction.FORWARD);

        LFMotor.motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        LBMotor.motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        RFMotor.motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        RBMotor.motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        conveyorMotor.motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        elevatorMotor.motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        leftShooterMotor.motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightShooterMotor.motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        //setting up the IMU on the expansion hubs, for our use
        BNO055IMU.Parameters parameter = new BNO055IMU.Parameters();

        parameter.mode = BNO055IMU.SensorMode.IMU;
        parameter.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        parameter.accelUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameter.loggingEnabled = false;

        imu.initialize(parameter);

        pidRotate = new PIDController(.003, .00003, 0);
        pidRotateLeft = new org.firstinspires.ftc.teamcode.PIDController(.003, .00003, 0);

        webcamName = hardwareMap.get(WebcamName.class, "gamer");

        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters(cameraMonitorViewId);

        parameters.vuforiaLicenseKey = VUFORIA_KEY;

        /**
         * We also indicate which camera on the RC we wish to use.
         */
        parameters.cameraName = webcamName;

        parameters.maxWebcamAspectRatio = 1920/1080;
        // Make sure extended tracking is disabled for this example.
        parameters.useExtendedTracking = false;

        //  Instantiate the Vuforia engine
        vuforia = ClassFactory.getInstance().createVuforia(parameters);

        // Load the data sets for the trackable objects. These particular data
        // sets are stored in the 'assets' part of our application.
        targetsUltimateGoal = this.vuforia.loadTrackablesFromAsset("UltimateGoal");
        VuforiaTrackable blueTowerGoalTarget = targetsUltimateGoal.get(0);
        blueTowerGoalTarget.setName("Blue Tower Goal Target");
        VuforiaTrackable redTowerGoalTarget = targetsUltimateGoal.get(1);
        redTowerGoalTarget.setName("Red Tower Goal Target");
        VuforiaTrackable redAllianceTarget = targetsUltimateGoal.get(2);
        redAllianceTarget.setName("Red Alliance Target");
        VuforiaTrackable blueAllianceTarget = targetsUltimateGoal.get(3);
        blueAllianceTarget.setName("Blue Alliance Target");
        VuforiaTrackable frontWallTarget = targetsUltimateGoal.get(4);
        frontWallTarget.setName("Front Wall Target");

        // For convenience, gather together all the trackable objects in one easily-iterable collection */
        allTrackables = new ArrayList<VuforiaTrackable>();
        allTrackables.addAll(targetsUltimateGoal);

        //Set the position of the perimeter targets with relation to origin (center of field)
        redAllianceTarget.setLocation(OpenGLMatrix
                .translation(0, -halfField, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, 180)));

        blueAllianceTarget.setLocation(OpenGLMatrix
                .translation(0, halfField, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, 0)));
        frontWallTarget.setLocation(OpenGLMatrix
                .translation(-halfField, 0, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, 90)));

        // The tower goal targets are located a quarter field length from the ends of the back perimeter wall.
        blueTowerGoalTarget.setLocation(OpenGLMatrix
                .translation(halfField, quadField, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, -90)));
        redTowerGoalTarget.setLocation(OpenGLMatrix
                .translation(halfField, -quadField, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, -90)));

        final float CAMERA_FORWARD_DISPLACEMENT  = 4.0f * mmPerInch;   // eg: Camera is 4 Inches in front of robot-center
        final float CAMERA_VERTICAL_DISPLACEMENT = 2.0f * mmPerInch;   // eg: Camera is 8 Inches above ground
        final float CAMERA_LEFT_DISPLACEMENT     = 0;     // eg: Camera is ON the robot's center line

        OpenGLMatrix cameraLocationOnRobot = OpenGLMatrix
                .translation(CAMERA_FORWARD_DISPLACEMENT, CAMERA_LEFT_DISPLACEMENT, CAMERA_VERTICAL_DISPLACEMENT)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XZY, DEGREES, 90, 90, 0));

        /**  Let all the trackable listeners know where the phone is.  */
        for (VuforiaTrackable trackable : allTrackables) {
            ((VuforiaTrackableDefaultListener) trackable.getListener()).setCameraLocationOnRobot(parameters.cameraName, cameraLocationOnRobot);
        }

        telemetry.addData("Status", "Initialized");

        targetsUltimateGoal.activate();

        resetAngle();
    }


    @Override
    public void init_loop() {}

    @Override
    public void start() {
        runtime.reset();
    }


    @Override
    public void loop() {

        targetVisible = false;
        for (VuforiaTrackable trackable : allTrackables) {
            if (((VuforiaTrackableDefaultListener)trackable.getListener()).isVisible()) {
                telemetry.addData("Visible Target", trackable.getName());
                targetVisible = true;

                // getUpdatedRobotLocation() will return null if no new information is available since
                // the last time that call was made, or if the trackable is not currently visible.
                OpenGLMatrix robotLocationTransform = ((VuforiaTrackableDefaultListener)trackable.getListener()).getUpdatedRobotLocation();
                if (robotLocationTransform != null) {
                    lastLocation = robotLocationTransform;
                }
                break;
            }
        }

        // Provide feedback as to where the robot is located (if we know).
        if (targetVisible) {
            // express position (translation) of robot in inches.
            VectorF translation = lastLocation.getTranslation();
            double updatedX = (translation.get(0) / mmPerInch) - xDistanceAdder;

            double distanceforspeed = Math.sqrt(Math.pow((translation.get(0) + xDistanceAdder * mmPerInch), 2) + Math.pow((translation.get(1)), 2)) / 1000;
            double highGoalVelocity = Math.sqrt((5.4228 * Math.pow(distanceforspeed, 2)) / (highGoalNumber - 0.3249 * distanceforspeed));
            double powerShotVelocity = Math.sqrt((5.4228 * Math.pow(distanceforspeed, 2)) / (powerGoalNumber - 0.3249 * distanceforspeed));

            double speedForTarget;

            if (xDistanceAdder == 0) {
                speedForTarget  = Math.abs((highGoalVelocity * 2.23694 - 17.4) / 9.02) - 0.07;
            } else {
                speedForTarget = Math.abs((powerShotVelocity * 2.23694 - 17.4) / 9.02);
            }

            if (gamepad1.dpad_up) {
                xDistanceAdder = 0;
            } else if (gamepad1.dpad_right) {
                xDistanceAdder = 15.75;
            } else if (gamepad1.dpad_down) {
                xDistanceAdder = 15.75 + 7.5;
            } else if (gamepad1.dpad_left) {
                xDistanceAdder = 15.75 + (7.5 * 2);
            }

            // express the rotation of the robot in degrees.
            Orientation rotation = Orientation.getOrientation(lastLocation, EXTRINSIC, XYZ, DEGREES);
            telemetry.addData("Angle (deg)", "{Roll, Pitch, Heading} = %.0f, %.0f, %.0f", rotation.firstAngle, rotation.secondAngle, rotation.thirdAngle);

            telemetry.addData("Ratio", updatedX / rotation.thirdAngle);
            if (gamepad1.a) {
                anglioso = rotation.thirdAngle - (updatedX / ratioNumber);
                shooterSpeed = speedForTarget;
            }
            telemetry.addData("Angleosing", anglioso);
        }
        else {
            telemetry.addData("Visible Target", "none");
        }

        if (gamepad1.b && anglioso != 0) {
            if (anglioso < 0 && xDistanceAdder == 0) {
                TurnLeftDegrees(0.75, -anglioso);
            } else if (anglioso > 0) {
                TurnRightDegrees(0.75, Math.abs(anglioso));
            }
        }

        telemetry.addData("Current Angle", getAngle());

        //defining the value to get from phones
        double LFPower, LBPower, RFPower, RBPower, xValue, turnValue, yValue;

        //checking to see if field relative mode is on
        /*if (gamepad1.back) {
            resetAngle();
            fieldRelativeMode = !fieldRelativeMode;
        }*/

        //getting the movement values from the gamepad
        yValue = gamepad1.left_stick_y;
        turnValue = gamepad1.right_stick_x;
        xValue = gamepad1.left_stick_x;

        //changing the values for the field relative mode
        if (fieldRelativeMode){
            double angle = getAngle();
            double tempX = (xValue * Math.cos(Math.toRadians(angle))) - (yValue * Math.sin(Math.toRadians(angle)));
            yValue = (xValue * Math.sin(Math.toRadians(angle))) + (yValue * Math.cos(Math.toRadians(angle)));
            xValue = tempX;
        } else {
            //add the thing here
        }

        //getting the values for the powers for each motor
        LFPower = Range.clip(-yValue + turnValue + xValue,-1,1);
        LBPower = Range.clip(-yValue + turnValue - xValue,-1,1);
        RBPower = Range.clip(-yValue - turnValue + xValue,-1,1);
        RFPower = Range.clip(-yValue - turnValue - xValue,-1,1);

        //applying the ramping up and ramping down features
        if (LFPower < 0){
            LFPower = (float) Math.pow(Math.abs(LFPower),2);
        } else if (LFPower > 0){
            LFPower = (float) -Math.pow(Math.abs(LFPower),2);
        }

        if (LBPower < 0){
            LBPower = (float) -Math.pow(Math.abs(LBPower),2);
        } else if (LBPower > 0){
            LBPower = (float) Math.pow(Math.abs(LBPower),2);
        }

        if (RFPower < 0){
            RFPower = (float) -Math.pow(Math.abs(RFPower),2);
        } else if (RFPower > 0){
            RFPower = (float) Math.pow(Math.abs(RFPower),2);
        }

        if (RBPower < 0){
            RBPower = (float) -Math.pow(Math.abs(RBPower),2);
        } else if (RBPower > 0){
            RBPower = (float) Math.pow(Math.abs(RBPower),2);
        }

        //game pad 1 left trigger is intake and right trigger is shooting
        //trigger values are all in the float
        //need to set up motors, set their inversion factor, set speeds, based on trigger values
        //convereyr is counter, elevator is counter, shooting right is counter and shooting left is clock
        //
        if (gamepad1.a || gamepad1.back){
            speed = 0.2;
        } else {
            speed = 0.75;
        }

        //shooters
        if (gamepad1.right_trigger > 0.0){
            rightShooterMotor.set(shooterSpeed);
            leftShooterMotor.set(shooterSpeed);
        } else{
            rightShooterMotor.set(0.0);
            leftShooterMotor.set(0.0);
        }

        //conveyor & elevator
        if (gamepad1.left_trigger > 0.0){
            conveyorMotor.set(1.0);

        } else{
            conveyorMotor.set(0.0);
        }

        //revers converyor and elevator to unstuck smth.
        if (gamepad1.left_bumper){
            conveyorMotor.set(-1.0);
        }

        if (gamepad1.x){
            rightIntakeDownServo.setPosition(Servo.MAX_POSITION);
            leftIntakeDownServo.setPosition(Servo.MAX_POSITION);
        }

        telemetry.addData("Shooter Speed: ", shooterSpeed);
        telemetry.log();

        if (gamepad1.dpad_up){
            wobbleGoalClawServo.setPosition(Servo.MAX_POSITION);
        }

        if (gamepad1.dpad_down){
            wobbleGoalClawServo.setPosition(Servo.MIN_POSITION);
        }

        if (gamepad1.y) {
            liftWobbleGoalServo.setPower(1.0);
        } else {
            liftWobbleGoalServo.setPower(0.0);
        }

        if (gamepad1.right_bumper) {
            elevatorMotor.set(1);
        } else if (gamepad1.left_bumper) {
            elevatorMotor.set(-1);
        } else {
            elevatorMotor.set(0.0);
        }

        if (gamepad1.dpad_left){
            shooterSpeed = 0.59;
        } else if (gamepad1.dpad_right){
            shooterSpeed = 0.632;
        }

        LFMotor.set(Range.clip(LFPower, -speed, speed));
        LBMotor.set(Range.clip(LBPower, -speed, speed));
        RFMotor.set(Range.clip(RFPower, -speed, speed));
        RBMotor.set(Range.clip(RBPower, -speed, speed));

        telemetry.addData("Status", "Run Time: " + runtime.toString());
        telemetry.update();
    }


    @Override
    public void stop() {
        targetsUltimateGoal.deactivate();
    }

    public void TurnLeftDegrees(double power, double degrees) {
        resetAngle();

        degrees += 5;

        if (Math.abs(degrees) > 359) degrees = Math.copySign(359, degrees);

        pidRotateLeft.reset();
        pidRotateLeft.setSetpoint(degrees);
        pidRotateLeft.setInputRange(0, degrees);
        pidRotateLeft.setOutputRange(0, power);
        pidRotateLeft.setTolerance(1);
        pidRotateLeft.enable();

        do {
            power = pidRotateLeft.performPID(getAngle());
            LFMotor.motor.setPower(-power);
            LBMotor.motor.setPower(-power);
            RFMotor.motor.setPower(power);
            RBMotor.motor.setPower(power);
        } while (!pidRotateLeft.onTarget());

        // turn the motors off.
        LFMotor.motor.setPower(0);
        LBMotor.motor.setPower(0);
        RFMotor.motor.setPower(0);
        RBMotor.motor.setPower(0);

        // reset angle tracking on new heading.
        resetAngle();
    }

    public void resetAngle() {
        lastAngles += globalAngle;
        globalAngle = 0;
    }

    //getting the current angle of the IMU
    public double getAngle() {
        double angles = (double) imu.getAngularOrientation().firstAngle;

        globalAngle = angles - lastAngles;

        globalAngle = ((globalAngle % 360) + 360) % 360;
        return globalAngle;
    }

    public void TurnRightDegrees(double power, double degrees) {
        resetAngle();
        //degrees = -degrees;

        pidRotate.reset();
        pidRotate.setSetPoint(degrees);
        //pidRotate.setInputRange(degrees, 0);
        //pidRotate.setOutputRange(0, power);
        pidRotate.setTolerance(1);
        //pidRotate.enable();

        do {
            power = pidRotate.calculate(getAngle());// * damp;
            LFMotor.motor.setPower(-power);
            LBMotor.motor.setPower(-power);
            RFMotor.motor.setPower(power);
            RBMotor.motor.setPower(power);
        } while (!pidRotate.atSetPoint());// && power < 0);

        // turn the motors off.
        LFMotor.motor.setPower(0);
        LBMotor.motor.setPower(0);
        RFMotor.motor.setPower(0);
        RBMotor.motor.setPower(0);

        // reset angle tracking on new heading.
        resetAngle();
    }
}