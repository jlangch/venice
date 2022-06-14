# Setting Up Eclipse IDE

## Gradle project configuration for Eclipse IDE

1.  Checkout Project
2.  Move to your projects path (where `gradlew` is found)
3.  Run clean: `./gradlew cleaneclipse`
4.  Refresh your Project in Eclipse
5.  Import the Gradle project `./gradlew eclipse`
6.  Refresh your Project in Eclipse

Repeat step 5) and 6) whenever the Gradle build changes


## Loding Code Style Preferencies

* Import the preferencies file: `doc/eclipse/eclipse-codestyle-prefs.epf`
 
    1. Navigate to Global Eclipse/Preferencies
    2. Import the file
  
  
* Activate the Code style preferencies for the Venice project

    1. Navigate to Venice Properties/Java Code Style/Formatter  
    2. Enable Project specific settings
    3. Activate the profile `Eclipse [venice]`