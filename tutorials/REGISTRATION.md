## Registering hardware components

Various types of devices are needed for custom microscopes and ClearControl
is supporting as many different device as possible. Moreover, it is easy
to implement wrappers for drivers of non-supported devices by ClearControl.


### `startSample()` method in the `SampleMain` class
In the method `startSample()`, microscope object is created, initialized and returned.
In the context of our example this is a `SampleMicroscope` object. This method is the 
starting point the register your hardware devices. 

First, you need to know the number of detection arms and the number of lightsheets
your microscope has. Then you need to set these numbers in the beginning of the method, 
on line-183 and line-184 respectively.
```java
int pNumberOfDetectionArms = 1;
int pNumberOfLightSheets = 1;
```
Next, one can observe two important function calls in the `startSample()`, find more about
those functions below.

### `addStandardDevices()`
This is a default method needed to use ClearControl. You can find the implementation 
in `SampleMicroscope` class in this sample project. It is advised to not manipulate it
unless you want to manipulate the way ClearControl works.

### `addRealHardwareDevices()`
One needs to add the real hardware devices to be able to use from ClearControl. This method
demonstrates how to that for the sample microscope in this project. An significant concept
 used in this method is creating separate contexts for different device types. Try to keep
 this convention. For example to add cameras it is given as:

```java
// Setting up cameras:
{
    for (int c = 0; c < pNumberOfDetectionArms; c++)
    {
        StackCameraDeviceInterface<?> lCamera =
                                              HamStackCamera.buildWithExternalTriggering(c);

        lCamera.getStackWidthVariable().set(lDefaultStackWidth);
        lCamera.getStackHeightVariable().set(lDefaultStackHeight);
        lCamera.getExposureInSecondsVariable().set(0.010);

        addDevice(c, lCamera);
    }
}
```

In this method, you can also find many commented parts which we believe would be useful for
readers. 
