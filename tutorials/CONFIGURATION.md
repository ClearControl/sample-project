## Configuring your microscope

Configuration part is just another important one. Depending your hardware of choice, you 
might want to use generated signals in various ways. To address this need, ClearControl 
provides a flexible way of configuration. Let's investigate the given sample configuration
to have a better understanding:


```$xslt
#RTlib machine configuration file

lookandfeel = metal

clearcl.device.fusion = TITAN 

device.serial.laser.omicron.0=COM4
device.serial.laser.cobolt.1=COM8

#For Nikon 40x 0.8 NA
#device.camera0.pixelsizenm  = 162.5

#For Nikon 25x 1.1 NA
device.camera0.pixelsizenm  = 260
```

In this part we configure the GPU type, laser connection ports and the camera pixel sizes.
<hr>

```
device.lsm.detection.D0.z.index = 0


device.lsm.lightsheet.I0.x.index  = 2
device.lsm.lightsheet.I0.y.index  = 3
device.lsm.lightsheet.I0.z.index  = 4
device.lsm.lightsheet.I0.b.index  = 5
device.lsm.lightsheet.I0.w.index  = 6
device.lsm.lightsheet.I0.la.index = 7
device.lsm.lightsheet.I0.t.index  = 15

device.lsm.lightsheet.I0.ld0.index = 8
device.lsm.lightsheet.I0.ld1.index = 9

device.lsm.switch.OpticalSwitch0.index = 10
device.lsm.switch.OpticalSwitch1.index = 11
device.lsm.switch.OpticalSwitch2.index = 12
device.lsm.switch.OpticalSwitch3.index = 13
```
In this part we configure the dedicated ports for the corresponding control target.
<hr>

```
# Bounds:

device.lsm.detection.D0.z.bounds = {"min":0, "max":1 }

device.lsm.lighsheet.I0.x.bounds = {"min":0, "max":1 }
device.lsm.lighsheet.I0.y.bounds = {"min":-1, "max":1 }
device.lsm.lighsheet.I0.z.bounds = {"min":-1, "max":1 }
device.lsm.lighsheet.I0.w.bounds = {"min":0, "max":1 }
device.lsm.lighsheet.I0.h.bounds = {"min":0, "max":1 }
device.lsm.lighsheet.I0.a.bounds = {"min":-90, "max":90 }
device.lsm.lighsheet.I0.b.bounds = {"min":-90, "max":90 }
device.lsm.lighsheet.I0.p.bounds = {"min":0, "max":0.5 }
device.lsm.lighsheet.I0.wp.bounds = {"min":-1, "max":1 }
device.lsm.lighsheet.I0.hp.bounds = {"min":-1, "max":1 }
```

In this part we configure the bound values for detection and illumination devices.
<hr>

```
# Functions:

device.lsm.detection.D0.z.f = {"a":1,"b":0 }

#device.lsm.detection.D0.z.f = {"a":-0.00264061262,"b":0.5 }

device.lsm.lighsheet.I0.x.f = {"a":1,"b":0 }
device.lsm.lighsheet.I0.y.f = {"a":1,"b":0 }
device.lsm.lighsheet.I0.z.f = {"a":1,"b":0 }
device.lsm.lighsheet.I0.w.f = {"a":1,"b":0 }
device.lsm.lighsheet.I0.h.f = {"a":1,"b":0 }
device.lsm.lighsheet.I0.a.f = {"a":1,"b":0 }
device.lsm.lighsheet.I0.b.f = {"a":1,"b":0 }
device.lsm.lighsheet.I0.p.f = {"a":0.5,"b":0 }
device.lsm.lighsheet.I0.wp.f = {"a":0,"b":0 }
device.lsm.lighsheet.I0.hp.f = {"a":0,"b":0 }

```
In this part we configure the coefficients for corresponding signalling functions.
<hr>