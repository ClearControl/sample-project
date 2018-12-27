# Sample Microscope

Sample microscope project for ClearControl environment. This project is
the basis for all ClearControl tutorials.

## Get the project
```bash
git clone https://github.com/ClearControl/sample-project
cd sample-project 
```

## Build
You can build the project with or without gradle.

With gradle:
```bash
gradle build
```

Without gradle:
```bash
./gradlew build
```

## Configure
This part is significant. If this is your first time with ClearControl 
start with creating a folder `.clearcontrol` under the your user file 
on Windows OS ( currently we only have support for the Windows due to driver 
dependencies we have ), i.e. `C:\Users\YOURUSERNAME\.clearcontrol`.

Then create a new file in that folder with name `configuration.txt`. This
file will be your main configuration file for your connected hardware.
You can copy the content of `configuration.txt.hide` file as a starting
point. For more details on writing configuration can be found 
[here](tutorials/CONFIGURATION.md).

## Run

Run the `SampleMain` class as an application. Then a window will pop up
with several options to start the software:

- options image here

Then depending on your choice main control window will be loaded:

- loaded software image here

.<hr> 
For more check the further detailed [tutorials](tutorials/README.md).