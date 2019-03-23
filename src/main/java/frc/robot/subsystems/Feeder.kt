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
    private var feederEnabled = false

    fun runFeeder() {
        if(feederEnabled) {
            //feederMotor.set(-MAX_MOTOR_SPEED * 0.4)
            //secondaryFeederMotor.set(-MAX_MOTOR_SPEED * 0.4)
        } else {
            //feederMotor.set(0.0)
            //secondaryFeederMotor.set(0.0)
        }
    }

    fun toggleFeeder() {
        feederEnabled = !feederEnabled
    }

    fun isFeederEnabled() = feederEnabled

    override fun report() {
        SmartDashboard.putString("pov", Robot.joystick.povController.toString())
//        SmartDashboard.putBoolean("feedermotor_isalive", feederMotor.isAlive)
//        SmartDashboard.putBoolean("feedermotor_safetyenabled", feederMotor.isSafetyEnabled)
        SmartDashboard.putBoolean("Feeder Enabled", feederEnabled)
    }

}
