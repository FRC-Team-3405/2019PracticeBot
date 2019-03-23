/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot

import edu.wpi.first.wpilibj.Joystick
import edu.wpi.first.wpilibj.TimedRobot
import edu.wpi.first.wpilibj.command.Scheduler
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import frc.robot.commands.FollowPathCommand
import frc.robot.commands.buttons.*
import frc.robot.maps.JoystickMap
import frc.robot.maps.RobotMap
import frc.robot.maps.XboxMap
import frc.robot.subsystems.*
import frc.robot.utilties.onPressed

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
class Robot : TimedRobot() {
    private var autoSelected: String? = null
    private val mchooser = SendableChooser<String>()
    private var currentPathCommand: FollowPathCommand? = null

    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    override fun robotInit() {
        Robot.gyroscope.initGryo()
        Robot.gyroscope.calibrate()

        //Register permanent button commands
        joystick.RightBumperButton.onPressed(ShiftHighGearCommand())
        joystick.LeftBumperButton.onPressed(ShiftLowGearCommand())
        joystick.NineButton.onPressed(GrabHatchPanelCommand())
        joystick.TenButton.onPressed(ReleaseHatchPanelCommand())
        joystick.LeftLowerBumperButton.onPressed(SwitchDirectionCommand())
        //Reserved for DriveTrain: joystick.RightLowerBumperButton
        //Reserved for Feeder: joystick.povController

        joystick.XButton.onPressed {
            val path = SmartDashboard.getString("path", "")
            if (path.isNotEmpty() && currentPathCommand?.isRunning != true) {
                currentPathCommand = FollowPathCommand(path)
                currentPathCommand?.start()
                println("Started following path $path")
                SmartDashboard.putString("current_path", path)
            } else if (currentPathCommand?.isRunning == true) {
                currentPathCommand?.cancel()
                println("Cancelled path $path")
                SmartDashboard.putString("current_path", null)
            }
        }
    }

    /**
     * This function is called every robot packet, no matter the mode.
     */
    override fun robotPeriodic() {
        SmartDashboard.updateValues()
        Scheduler.getInstance().run()
    }

    /**
     * This function is called at the beginning of the sandstorm period.
     */
    override fun autonomousInit() {
        autoSelected = mchooser.selected
        println("Auto selected: " + autoSelected!!)
    }

    /**
     * This function is called periodically during the sandstorm period.
     */
    override fun autonomousPeriodic() {

    }

    /**
     * This function is called at the end of the sandstorm period.
     */
    override fun teleopInit() {

    }

    /**
     * This function is called periodically during operator control.
     */
    override fun teleopPeriodic() {

    }

    /**
     * This function is run when test is first started.
     */
    override fun testInit() {

    }

    /**
     * This function is called periodically during test mode.
     */
    override fun testPeriodic() {

    }

    /**
     * This function is run when robot is first disabled.
     */
    override fun disabledInit() {

    }

    /**
     * This function is called periodically while disabled.
     */
    override fun disabledPeriodic() {

    }

    /**
     * Static members of the robot (Subsystems)
     */
    companion object {
        //Control Joysticks
        val joystick = XboxMap.Controller(Joystick(RobotMap.MAIN_JOYSTICK_PORT))
        val secondaryJoystick = JoystickMap.Controller(Joystick(RobotMap.SECONDARY_JOYSTICK_PORT))

        //Drive subsystems
        val driveTrain = DriveTrain()

        //Sensor subsystems
        val builtInAccelerometer = Accelerometer()
        val gyroscope = Gyroscope()

        //Pneumatics subsystems
        val pneumatics = Pneumatics()

        //Elevator subsystems
        val elevator = Elevator()

        //Box subsystems
        val box = Box()

        //Feeder subsystems
        val feeder = Feeder()

        //Autonomous Control Subsystem
//        val autonomousControl = AutonomousControl()

        //Power
//        val pdp = PowerDistributionPanel(0)
    }
}
