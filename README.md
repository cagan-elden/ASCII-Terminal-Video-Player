# ASCII Terminal Video-Player (Finished)
This is an ASCII terminal video player built with Java and Maven for project-management. With this program you can turn any `.mp4` into computer-comprehensible video by `sobel-filters` and play them on your terminal or powershell.

## Setup
To set up the repository on your device, run the following command on your terminal:

```bash
git clone https://github.com/cagan-elden/ASCII-Terminal-Video-Player
```

## How to Use?
To upload the video, create a new directory called `vidSrc` in `demo`, drag your video into the directory and rename it `sample.mp4`. If your video is in a format such as `Bad Apple`, you can rename it `sobel.mp4`.

Unless the video is in black 'n white format such as `Bad Apple`, it is recommended to turn it into a computer-comprehensible version by `sobel-filters`. By default, Maven runs `Player.java` which plays videos on your terminal.

If you want to play a normal video, turn it into `sobel-format` by accessing `demo/pom.xml`. You can change the file to execute when Maven is executed by changing `Player.java` to `App.java`, if you want to play a video keep it `Player.java`:

```xml
<plugin>
  <groupId>org.codehaus.mojo</groupId>
  <artifactId>exec-maven-plugin</artifactId>
  <version>3.1.0</version>
  <configuration>
    <mainClass>com.example.Player</mainClass>
  </configuration>
</plugin>
```

To activate the script after configuring `pom.xml`, run:

```bash
mvn clean compile exec:java
```

## Configuration
There are parts in the repo where I commented "Don't touch". Unless you know what you are doing, it is not recommended to change those parts.
