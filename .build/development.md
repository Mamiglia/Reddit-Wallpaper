Here's a list of useful information of practices I do when developing this software.

### Java Version
At the moment I use AdoptOpenJDK 11.0.10.9 for development. In any case it works fine with any AdoptOpenJDK 11.0.x, and it should work with any version of Java 11.x (not tested) 

### Libraries
Apart from the standard Java Libraries, I use the following External Libraries:
- h2 database: com.h2database:h2:1.4.200
- com.formdev:flatlaf:1.1.2
- org.json:json:20210307
- jna-5.8.0

### Artifacts  
I generate two jars for this project:
- Reddit-Wallpaper.jar
  - Output of Reddit-Wallpaper Package
  - Libraries mentioned before
  - This is the main jar the one actually running the program
- Installer.jar
  - Output of Installer package
 
### Releases
The actual exe release is the result of the Launch4j program that bundles the aforementioned jars with the Java version I use. The config file for this program is [RW_launch4j.xml](https://github.com/Mamiglia/Reddit-Wallpaper/tree/main/.build/RW_launch4j.xml)
