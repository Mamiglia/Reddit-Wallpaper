Here's a list of useful information of practices I do when developing this software.

### Java Version
At the moment I use AdoptOpenJDK 11.0.10.9 for development. In any case it works fine with any AdoptOpenJDK 11.0.x, and it should work with any version of Java 11.x (not tested) 

### Libraries
Apart from the standard Java Libraries, I use the following External Libraries:
- h2 database: com.h2database:h2:1.4.200
- styling: com.formdev:flatlaf:1.1.2
- org.json:json:20210307
- jna-5.8.0

### Artifacts  
I generate two jars for this project:
- Reddit-Wallpaper.jar
  - Output of Reddit-Wallpaper Package
  - Libraries mentioned before
  - This is the main jar the one actually running the program
- Installer.jar //no longer needed!
  - Output of Installer package
 
### Releases
The actual exe release is generated thanks to [install4j](https://www.ej-technologies.com/products/install4j/overview.html) that gave me a free open-source license (thanks!).
The config file for install4j is [RW.install4j](https://github.com/Mamiglia/Reddit-Wallpaper/blob/main/.build/Install4j/RW.install4j)
