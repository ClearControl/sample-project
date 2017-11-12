# X-Scope #

This README would normally document whatever steps are necessary to get your application up and running.

### How to build the project? ###

* Developing XWing
To develop XWing using Eclipse or Intellij, just import the projects gradle.build file in your IDE.

* Building XWing-Executable
Go to the command line, navigate to the folder where this file is located and run:

gradlew jfxnative

Afterwards, you will find an XWing.exe in this sub directory:

build\jfx\native\XWing

Go to this folder and fix the following (temporary) issues:
* Delete CoreMem-0.4.3.jar
* Delete ClearCL-0.5.2.jar
* Copy over the "kernels" folders from fastfuse/registration and fastfuse/tasks to app/kernels
