package frc.robot.subsystems

import com.ctre.phoenix.motorcontrol.*
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import frc.robot.Robot
import frc.robot.commands.RunDriveTrainCommand
import frc.robot.maps.*
import frc.robot.maps.RobotMap.BL_TALONSRX
import frc.robot.maps.RobotMap.BR_TALONSRX
import frc.robot.maps.RobotMap.FL_TALONSRX
import frc.robot.maps.RobotMap.FR_TALONSRX
import frc.robot.utilties.ReportableSubsystem
import frc.robot.utilties.configGains
import frc.robot.utilties.onPressed
import kotlin.math.PI

class DriveTrain : ReportableSubsystem() {

    private val frontRight = TalonSRX(FR_TALONSRX)
    private val frontLeft = TalonSRX(FL_TALONSRX)
    private val backRight = TalonSRX(BR_TALONSRX)
    private val backLeft = TalonSRX(BL_TALONSRX)

    private var currentMaxSpeed = MAX_MOTOR_SPEED

    private var direction = Direction.HATCH_FORWARD


    private var rotating = false // Used for turning to an angle
    private var drivingDistance = false //Used for driving to a specific distance
    private var currentHeading = 0.0 // Used for driving straight
    private var targetAngle = 0.0 // Used for turning to an angle
    private var targetDistance = 0.0 //Used for driving to a specific distance

    private var povPressed = false

    override fun initDefaultCommand() {
        defaultCommand = RunDriveTrainCommand()
        resetEncoderCounts()

        SmartDashboard.putNumber("CurrentMaxSpeed", currentMaxSpeed)

        //Configure Front Right TalonSRX to follow Back Right
        frontRight.apply {
            follow(backRight)
            inverted = INVERT_RIGHT
        }

        //Configure Front Left TalonSRX to follow Front Left
        frontLeft.apply {
            follow(backLeft)
            inverted = INVERT_LEFT
        }

        //Configure back left TalonSRX to use Encoder
        backLeft.apply {
            set(ControlMode.PercentOutput, 0.0)
            setNeutralMode(NeutralMode.Brake)
            configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, PID_PRIMARY, TIMEOUT_MS)
            inverted = INVERT_LEFT
            setSensorPhase(false)
            setStatusFramePeriod(StatusFrame.Status_2_Feedback0, 5, TIMEOUT_MS)
            configNeutralDeadband(NEUTRAL_DEADBAND, TIMEOUT_MS)
            configPeakOutputForward(+1.0, TIMEOUT_MS)
            configPeakOutputReverse(-1.0, TIMEOUT_MS)
        }

        //Configure back right TalonSRX to use Encoder and encoder from left talon to drive straight
        backRight.apply {
            set(ControlMode.PercentOutput, 0.0)
            setNeutralMode(NeutralMode.Brake)
            configRemoteFeedbackFilter(backLeft.deviceID, RemoteSensorSource.TalonSRX_SelectedSensor, REMOTE_0, TIMEOUT_MS)
            configSensorTerm(SensorTerm.Diff1, FeedbackDevice.RemoteSensor0, TIMEOUT_MS)
            configSensorTerm(SensorTerm.Diff0, FeedbackDevice.QuadEncoder, TIMEOUT_MS)
            configSelectedFeedbackSensor(FeedbackDevice.SensorDifference, PID_TURN, TIMEOUT_MS)

            //Scales the error by a coefficient calculated here:
            /**
             * Heading units should be scaled to ~4000 per 360 deg, due to the following limitations...
             * - Target param for aux PID1 is 18bits with a range of [-131072,+131072] units.
             * - Target for aux PID1 in motion profile is 14bits with a range of [-8192,+8192] units.
             *  ... so at 3600 units per 360', that ensures 0.1 degree precision in firmware closed-loop
             *  and motion profile trajectory points can range +-2 rotations.
             */
            configSelectedFeedbackCoefficient(TURN_TRAVEL_UNITS_PER_ROTATION / ENCODER_UNITS_PER_ROTATION, PID_TURN, TIMEOUT_MS)
            inverted = INVERT_RIGHT
            setSensorPhase(false)
            setStatusFramePeriod(StatusFrame.Status_12_Feedback1, 20, TIMEOUT_MS)
            setStatusFramePeriod(StatusFrame.Status_14_Turn_PIDF1, 20, TIMEOUT_MS)
            configNeutralDeadband(NEUTRAL_DEADBAND, TIMEOUT_MS)
            configPeakOutputForward(+1.0, TIMEOUT_MS)
            configPeakOutputReverse(-1.0)
            configGains(SLOT_TURNING, GAINS_TURNING, TIMEOUT_MS)
            configClosedLoopPeriod(0, CLOSED_LOOP_TIME_MS, TIMEOUT_MS)
            configClosedLoopPeriod(1, CLOSED_LOOP_TIME_MS, TIMEOUT_MS)
            configAuxPIDPolarity(AUX_PID_POLARITY, TIMEOUT_MS)
        }

        Robot.joystick.LeftLowerBumperButton.onPressed {
            setHeadingForDriveStraight()
        }
    }

    fun drive() {
        currentMaxSpeed = SmartDashboard.getNumber("CurrentMaxSpeed", MAX_MOTOR_SPEED)

        if(rotating) {
            if(targetAngle > 0) {
                SmartDashboard.putNumber("ROTATION", calculateRotation(Math.abs(backLeft.selectedSensorPosition - backRight.selectedSensorPosition).toDouble()))
                if(calculateRotation(Math.abs(backLeft.selectedSensorPosition - backRight.selectedSensorPosition).toDouble()) + calculateRotation(Math.abs(backLeft.selectedSensorVelocity - backRight.selectedSensorVelocity).toDouble()) > targetAngle) {
                    rotating = false
                } else {
                    driveSide(currentMaxSpeed * 0.75, -currentMaxSpeed * 0.75, Direction.HATCH_FORWARD)
                    return
                }
            }
        }
        else if(drivingDistance) {
            if(calculateDistance(Math.abs(backRight.selectedSensorPosition).toDouble()) + 1.5 * calculateDistance(Math.abs(backRight.selectedSensorVelocity).toDouble()) > targetDistance) {
                drivingDistance = false
            } else {
                driveStraight(currentMaxSpeed)
                return
            }
        } else if (Robot.joystick.povController != XboxMap.PovDirections.NULL) {
            //The POV pad on the joystick controls specialized driving maneuvers.

            when (Robot.joystick.povController) {
                XboxMap.PovDirections.UP, XboxMap.PovDirections.UP_RIGHT, XboxMap.PovDirections.UP_LEFT -> {
                    if (!povPressed) {
                        setHeadingForDriveStraight()
                    }

                    driveStraight(currentMaxSpeed * 0.85) //TODO tune
                }
                XboxMap.PovDirections.DOWN, XboxMap.PovDirections.DOWN_RIGHT, XboxMap.PovDirections.DOWN_LEFT -> {
                    if (!povPressed) {
                        setHeadingForDriveStraight()
                    }

                    driveStraight(-currentMaxSpeed * 0.85) //TODO tune
                }
                XboxMap.PovDirections.RIGHT, XboxMap.PovDirections.LEFT -> {
                    if (!povPressed) {
                        turnAngle(180.0)
                    }
                }
                XboxMap.PovDirections.NULL -> {}
            }
            povPressed = true
        } else if (Robot.joystick.LeftLowerBumperButton.get()) {
            //This will make the robot drive straight until it hits the tape on the ground
            if(!(Robot.leftLightSensor.get() || Robot.rightLightSensor.get())) {
                driveStraight(Robot.joystick.leftY * currentMaxSpeed)
            }
        } else {
            povPressed = false

            var leftY = Robot.joystick.leftY * currentMaxSpeed * .85
            var rightY = Robot.joystick.rightY * currentMaxSpeed * .85
            if (Robot.pneumatics.isHighGear()) {
                leftY *= 0.85
                rightY *= 0.85
            }

            //Reverse wheel base when direction is reversed.
            if (direction == Direction.HATCH_FORWARD) {
                driveSide(powerLeft = leftY, powerRight = rightY)
            } else {
                //Intentional swap
                driveSide(powerLeft = rightY, powerRight = leftY)
            }
        }
    }

    fun switchDirection() {
        direction = when (direction) {
            Direction.HATCH_FORWARD -> Direction.BOX_FORWARD
            Direction.BOX_FORWARD -> Direction.HATCH_FORWARD
        }
    }

    fun driveSide(powerLeft: Double, powerRight: Double, pathDirection: Direction = this.direction) {
        backLeft.set(ControlMode.PercentOutput, powerLeft * pathDirection.sign)
        backRight.set(ControlMode.PercentOutput, powerRight * pathDirection.sign)
        frontLeft.follow(backLeft)
        frontRight.follow(backRight)
    }

    private fun setHeadingForDriveStraight() {
        backRight.selectProfileSlot(SLOT_TURNING, PID_TURN)
        currentHeading = backRight.getSelectedSensorPosition(1).toDouble()
    }

    private fun driveStraight(power: Double) {
        backRight.set(ControlMode.PercentOutput, power * direction.sign, DemandType.AuxPID, currentHeading)
        backLeft.follow(backRight, FollowerType.AuxOutput1)
    }

    private fun calcRotation(encoderDiff: Double): Double {
        return (encoderDiff / ENCODER_UNITS_PER_ROTATION) * 360.0
    }

    private fun calcDistance(encoderUnits: Double): Double {
        return (encoderUnits / ENCODER_UNITS_PER_REVOLUTION) * WHEEL_DIAMETER * PI
    }

    fun getLeftEncoder(): Int {
        return backLeft.selectedSensorPosition
    }

    fun getRightEncoder(): Int {
        return backRight.selectedSensorPosition
    }

    fun resetEncoderCounts() {
        backLeft.sensorCollection.setQuadraturePosition(0, TIMEOUT_MS)
        backRight.sensorCollection.setQuadraturePosition(0, TIMEOUT_MS)
    }

    fun turnAngle(degrees: Double) {
        resetEncoderCounts()
        targetAngle = degrees
        rotating = true
    }

    private fun calculateRotation(encoderDiff: Double): Double {
        return (encoderDiff / ENCODER_UNITS_PER_ROTATION) * 360.0
    }

    private fun calculateDistance(encoderUnits: Double): Double {
        return (encoderUnits / 1440.0) * 6 * PI
    }

    fun driveDistance(inches: Double) {
        resetEncoderCounts()
        targetDistance = inches
        drivingDistance = true
    }

    override fun report() {
        //Robot direction
        SmartDashboard.putNumber("direction", direction.sign.toDouble())
        SmartDashboard.putNumber("heading", currentHeading)

        SmartDashboard.putNumber("Target angle", targetAngle)

        //Software-set max speed
        SmartDashboard.putNumber("CurrentMaxSpeed", currentMaxSpeed)

        //Encoder stuff
        SmartDashboard.putNumber("right_encoder_count", backRight.selectedSensorPosition.toDouble())
        SmartDashboard.putNumber("left_encoder_count", backLeft.selectedSensorPosition.toDouble())

        //Rotation values
        SmartDashboard.putNumber("right_encoder_rotations", backRight.selectedSensorPosition / 1440.0)
        SmartDashboard.putNumber("left_encoder_rotations", backLeft.selectedSensorPosition / 1440.0)

        SmartDashboard.putNumber("right_sensor_feedback", backRight.getSelectedSensorPosition(PID_TURN).toDouble())

        //Motor temperature
        SmartDashboard.putNumber("back_right_talon_temp", backRight.temperature)
        SmartDashboard.putNumber("back_left_talon_temp", backLeft.temperature)
        SmartDashboard.putNumber("front_left_talon_temp", frontLeft.temperature)
        SmartDashboard.putNumber("front_right_talon_temp", frontRight.temperature)

        //Motor inversions
        SmartDashboard.putBoolean("back_right_inverted", backRight.inverted)
        SmartDashboard.putBoolean("back_left_inverted", backLeft.inverted)
        SmartDashboard.putBoolean("front_left_inverted", frontLeft.inverted)
        SmartDashboard.putBoolean("front_right_inverted", frontRight.inverted)

        //Motor current
        SmartDashboard.putNumber("back_right_current", backRight.outputCurrent)
        SmartDashboard.putNumber("back_left_current", backLeft.outputCurrent)
        SmartDashboard.putNumber("front_left_current", frontLeft.outputCurrent)
        SmartDashboard.putNumber("front_right_current", frontRight.outputCurrent)

        //Motor voltage
        SmartDashboard.putNumber("back_right_voltage", backRight.motorOutputVoltage)
        SmartDashboard.putNumber("back_left_voltage", backLeft.motorOutputVoltage)
        SmartDashboard.putNumber("front_left_voltage", frontLeft.motorOutputVoltage)
        SmartDashboard.putNumber("front_right_voltage", frontRight.motorOutputVoltage)
    }

}

enum class Direction(val sign: Int) {
    HATCH_FORWARD(1),
    BOX_FORWARD(-1)
}
