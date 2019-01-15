# Robot11
FRC Team 4450 2018 Robot Control program.

This is the 2018 competition robot control program created by the Olympia Robotics Federation (FRC Team 4450).
Operates the robot "Odyssey" for FRC game "FIRST POWER UP".

## Instructions to setup development environment for eclipse
1) Follow the instructions [here](http://wpilib.screenstepslive.com/s/4485/m/13809/l/599681-installing-eclipse-c-java) to setup the JDK, Eclipse, and the FRC plugins for eclipse. Do not install the C++ portions.
2) Clone this repository
3) Import the project into eclipse as an existing gradle project.
4) Edit the build.gradle file for the dependency line:
	compile "com.github.ORF-4450:RobotLib:v2.3" 
	and change the version of RobotLib to the current version available
	on Jitpack.com for ORF-4450.
5) If you change the version, right click on the project in eclipse and select Gradle/Refresh Gradle Project.

### If RobotLib gets an update:
1) Do steps 4 & 5 above with the new version number.
****************************************************************************************************************
Version 11.4

*	Extensive after season testing of new ideas.
*	Pathfinder.
*	Velocity motor control.
*	Using PID loops onboard Talon SRX controllers.

R. Corn
June-September 2018

Version 11.3

*	As of completion of clone robot.

R. Corn
March 16, 2018

Version 11.2

*	As of the end of Mt. Vernon week 1 competition.

R. Corn
March 5, 2018

Version 11.1

* 	Update to work with 2018 plugins.

S. Flo
January 4, 2018

Version 11.0

*	Cloned from Robot10. 2017 specific code removed, cleaned up in preparation for 2018 season.

S. Flo
October 1, 2017
