package frc.robot.subsystems

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import frc.robot.Robot
import frc.robot.commands.RunFeederCommand
import frc.robot.maps.XboxMap
import frc.robot.utilties.ReportableSubsystem

class Feeder: ReportableSubsystem() {

    override fun initDefaultCommand() {
        defaultCommand = RunFeederCommand()
    }

//    private val feederMotor = Spark(FEEDER_MOTOR_PORT)
//    private val secondaryFeederMotor = Spark(FEEDER_MOTOR_2_PORT)

    fun runFeeder() {
        when(Robot.joystick.povController) {
            XboxMap.PovDirections.UP, XboxMap.PovDirections.UP_RIGHT, XboxMap.PovDirections.UP_LEFT -> {
//                feederMotor.set(-MAX_MOTOR_SPEED * 0.4)
//                secondaryFeederMotor.set(-MAX_MOTOR_SPEED * 0.4)
            }
            XboxMap.PovDirections.DOWN, XboxMap.PovDirections.DOWN_RIGHT, XboxMap.PovDirections.DOWN_LEFT -> {
//                feederMotor.set(MAX_MOTOR_SPEED * 0.4)
//                secondaryFeederMotor.set(MAX_MOTOR_SPEED * 0.4)
            }
            else -> {
//                feederMotor.set(0.0)
            }
        }
    }

    override fun report() {
        SmartDashboard.putString("pov", Robot.joystick.povController.toString())
//        SmartDashboard.putBoolean("feedermotor_isalive", feederMotor.isAlive)
//        SmartDashboard.putBoolean("feedermotor_safetyenabled", feederMotor.isSafetyEnabled)
        SmartDashboard.putBoolean("Physical Feeder Enabled", false)
    }

}
