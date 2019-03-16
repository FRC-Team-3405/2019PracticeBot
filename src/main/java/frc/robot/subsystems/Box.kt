package frc.robot.subsystems

import com.ctre.phoenix.motorcontrol.can.VictorSPX
import edu.wpi.first.wpilibj.Spark
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import frc.robot.utilties.ReportableSubsystem

class Box: ReportableSubsystem() {
    override fun initDefaultCommand() {
//        defaultCommand = RunBoxCommand()
    }

//    private val boxRotatorMotor = VictorSPX(BOX_MOTOR_PORT)
//    private val beltMotor = Spark(BOX_BELT_MOTOR_PORT)

    override fun report() {
        SmartDashboard.putBoolean("Box Enabled", false)
    }
}