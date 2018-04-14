package xwing;

import clearcl.ClearCLContext;
import clearcontrol.core.variable.Variable;
import clearcontrol.devices.cameras.StackCameraDeviceInterface;
import clearcontrol.devices.cameras.devices.hamamatsu.HamStackCamera;
import clearcontrol.devices.lasers.devices.cobolt.CoboltLaserDevice;
import clearcontrol.devices.lasers.devices.omicron.OmicronLaserDevice;
import clearcontrol.devices.lasers.schedulers.LaserOnOffScheduler;
import clearcontrol.devices.lasers.schedulers.LaserPowerScheduler;
import clearcontrol.devices.signalamp.devices.srs.SIM900MainframeDevice;
import clearcontrol.devices.signalamp.devices.srs.SIM983ScalingAmplifierDevice;
import clearcontrol.devices.signalgen.devices.nirio.NIRIOSignalGenerator;
import clearcontrol.devices.stages.BasicThreeAxesStageInterface;
import clearcontrol.devices.stages.StageType;
import clearcontrol.devices.stages.devices.tst.TSTStageDevice;
import clearcontrol.devices.stages.hub.StageHubDevice;
import clearcontrol.devices.stages.kcube.impl.KCubeDevice;
import clearcontrol.devices.stages.kcube.impl.KCubeThreeAxesStageDevice;
import clearcontrol.devices.stages.kcube.scheduler.BasicThreeAxesStageScheduler;
import clearcontrol.devices.stages.kcube.sim.SimulatedThreeAxesStageDevice;
import clearcontrol.microscope.lightsheet.adaptive.modules.*;
import clearcontrol.microscope.lightsheet.adaptive.schedulers.AdaptationScheduler;
import clearcontrol.microscope.lightsheet.component.detection.DetectionArm;
import clearcontrol.microscope.lightsheet.component.lightsheet.LightSheet;
import clearcontrol.microscope.lightsheet.component.opticalswitch.LightSheetOpticalSwitch;
import clearcontrol.microscope.lightsheet.signalgen.LightSheetSignalGeneratorDevice;
import clearcontrol.microscope.lightsheet.simulation.LightSheetMicroscopeSimulationDevice;
import clearcontrol.microscope.lightsheet.simulation.SimulatedLightSheetMicroscope;
import clearcontrol.devices.stages.kcube.impl.KCubeDeviceFactory;
import xwing.adaptive.AdaptiveZScheduler;
import xwing.copilot.CopilotDevice;
import xwing.copilot.gui.steps.StepFactoryInterface;
import xwing.copilot.gui.steps.step1manualcalibration.Step1ManualCalibrationFactory;
import xwing.copilot.gui.steps.step2automaticcalibration.Step2AutomaticCalibrationFactory;
import xwing.copilot.gui.steps.step3samplemounting.Step3SampleMountingFactory;
import xwing.copilot.gui.steps.step4automaticcalibrationwithsample.Step4AutomaticCalibrationWithSampleFactory;
import xwing.copilot.gui.steps.step5recalibrationwithsample.Step5RecalibrationWithSampleFactory;
import xwing.fastimage.FastImageDevice;
import xwing.imaging.CalibrationImagerDevice;
import xwing.multicolor.MultiChannelScheduler;

import java.util.ArrayList;

/**
 * XWing microscope
 *
 * @author royer
 */
public class XWingMicroscope extends SimulatedLightSheetMicroscope
{

  /**
   * Instantiates an XWing microscope
   * 
   * @param pStackFusionContext
   *          ClearCL context for stack fusion
   * @param pMaxStackProcessingQueueLength
   *          max stack processing queue length
   * @param pThreadPoolSize
   *          thread pool size
   */
  public XWingMicroscope(ClearCLContext pStackFusionContext,
                         int pMaxStackProcessingQueueLength,
                         int pThreadPoolSize)
  {
    super("XWing",
          pStackFusionContext,
          pMaxStackProcessingQueueLength,
          pThreadPoolSize);

  }

  /**
   * Assembles the microscope
   * 
   * @param pNumberOfDetectionArms
   *          number of detection arms
   * @param pNumberOfLightSheets
   *          number of lightsheets
   */
  public void addRealHardwareDevices(int pNumberOfDetectionArms,
                                     int pNumberOfLightSheets, boolean pUseStages)
  {
    long lDefaultStackWidth = 1024;
    long lDefaultStackHeight = 2048;

    if (pUseStages)
    {
      KCubeDeviceFactory lKCubeDeviceFactory = KCubeDeviceFactory.getInstance();
      addDevice(0, lKCubeDeviceFactory);
      KCubeDevice lXStage = lKCubeDeviceFactory.createKCubeDevice(26000278, "XKCubeStage"); // XWing stage X-axis
      KCubeDevice lYStage = lKCubeDeviceFactory.createKCubeDevice(26000298, "YKCubeStage"); // XWing stage Y-axis
      KCubeDevice lZStage = lKCubeDeviceFactory.createKCubeDevice(26000299, "ZKCubeStage"); // XWing stage Z-axis
      addDevice(0, lXStage);
      addDevice(0, lYStage);
      addDevice(0, lZStage);

      BasicThreeAxesStageInterface lBasicThreeAxesStageInterface = new KCubeThreeAxesStageDevice("Stage", lXStage, lYStage, lZStage);
      addDevice(0, lBasicThreeAxesStageInterface);

      BasicThreeAxesStageScheduler lBasicThreeAxesStageScheduler = new BasicThreeAxesStageScheduler(lBasicThreeAxesStageInterface);
      addDevice(0, lBasicThreeAxesStageScheduler);

      addDevice(0, lKCubeDeviceFactory.createKCubeDevice(26000303, "Illumination0BAngleKCubeStage")); // XWing LS0 beta angle
      addDevice(0, lKCubeDeviceFactory.createKCubeDevice(26000309, "Illumination1BAngleKCubeStage")); // XWing LS1 beta angle
      addDevice(0, lKCubeDeviceFactory.createKCubeDevice(26000317, "Illumination2BAngleKCubeStage")); // XWing LS2 beta angle
      addDevice(0, lKCubeDeviceFactory.createKCubeDevice(26000318, "Illumination3BAngleKCubeStage")); // XWing LS3 beta angle


    }




    // Setting up lasers:
    {
      final OmicronLaserDevice lLaserDevice488 =
                                               new OmicronLaserDevice(0);
      addDevice(0, lLaserDevice488);

      addDevice(0, new LaserPowerScheduler(lLaserDevice488, 0.0));
      addDevice(0, new LaserPowerScheduler(lLaserDevice488, 1.0));
      addDevice(0, new LaserPowerScheduler(lLaserDevice488, 5.0));
      addDevice(0, new LaserPowerScheduler(lLaserDevice488, 10.0));
      addDevice(0, new LaserPowerScheduler(lLaserDevice488, 20.0));
      addDevice(0, new LaserPowerScheduler(lLaserDevice488, 50.0));
      addDevice(0, new LaserPowerScheduler(lLaserDevice488, 100.0));

      addDevice(0, new LaserOnOffScheduler(lLaserDevice488, true));
      addDevice(0, new LaserOnOffScheduler(lLaserDevice488, false));



      final CoboltLaserDevice lLaserDevice594 =
                                              new CoboltLaserDevice("Mambo",
                                                                    100,
                                                                    1);
      addDevice(1, lLaserDevice594);/**/

      addDevice(0, new LaserPowerScheduler(lLaserDevice594, 0.0));
      addDevice(0, new LaserPowerScheduler(lLaserDevice594, 1.0));
      addDevice(0, new LaserPowerScheduler(lLaserDevice594, 5.0));
      addDevice(0, new LaserPowerScheduler(lLaserDevice594, 10.0));
      addDevice(0, new LaserPowerScheduler(lLaserDevice594, 20.0));
      addDevice(0, new LaserPowerScheduler(lLaserDevice594, 50.0));
      addDevice(0, new LaserPowerScheduler(lLaserDevice594, 100.0));

      addDevice(0, new LaserOnOffScheduler(lLaserDevice594, true));
      addDevice(0, new LaserOnOffScheduler(lLaserDevice594, false));

    }

    // Setting up Stage:
    if (false)
    {
      TSTStageDevice lTSTStageDevice = new TSTStageDevice();

      StageHubDevice lStageHubDevice =
                                     new StageHubDevice("XYZR Stage",
                                                        StageType.XYZR);

      lStageHubDevice.addDOF("X", lTSTStageDevice, 0);
      lStageHubDevice.addDOF("Y", lTSTStageDevice, 1);
      lStageHubDevice.addDOF("Z", lTSTStageDevice, 2);

      addDevice(0, lStageHubDevice);
    }

    // Setting up cameras:
    if (true)
    {
      for (int c = 0; c < pNumberOfDetectionArms; c++)
      {
        StackCameraDeviceInterface<?> lCamera =
                                              HamStackCamera.buildWithExternalTriggering(c);

        lCamera.getStackWidthVariable().set(lDefaultStackWidth);
        lCamera.getStackHeightVariable().set(lDefaultStackHeight);
        lCamera.getExposureInSecondsVariable().set(0.010);

        // lCamera.getStackVariable().addSetListener((o,n)->
        // {System.out.println("camera output:"+n);} );

        addDevice(c, lCamera);
      }
    }

    // Scaling Amplifier:
    if (false)
    {
      final SIM900MainframeDevice lSIM900MainframeDevice =
                                                         new SIM900MainframeDevice("COM1");

      final SIM983ScalingAmplifierDevice lScalingAmp =
                                                     new SIM983ScalingAmplifierDevice(lSIM900MainframeDevice,
                                                                                      4);
      addDevice(0, lSIM900MainframeDevice);
      addDevice(0, lScalingAmp);

    }

    // Adding signal Generator:
    LightSheetSignalGeneratorDevice lLSSignalGenerator;
    {
      NIRIOSignalGenerator lNIRIOSignalGenerator =
                                                 new NIRIOSignalGenerator();
      lLSSignalGenerator =
                         LightSheetSignalGeneratorDevice.wrap(lNIRIOSignalGenerator,
                                                              true);
      // addDevice(0, lNIRIOSignalGenerator);
      addDevice(0, lLSSignalGenerator);
    }

    // Setting up detection arms:
    {
      for (int c = 0; c < pNumberOfDetectionArms; c++)
      {
        final DetectionArm lDetectionArm = new DetectionArm("D" + c);
        lDetectionArm.getPixelSizeInMicrometerVariable().set(0.26);

        addDevice(c, lDetectionArm);
      }
    }

    // Setting up lightsheets:
    {

      for (int l = 0; l < pNumberOfLightSheets; l++)
      {
        final LightSheet lLightSheet =
                                     new LightSheet("I" + l,
                                                    9.4,
                                                    getNumberOfLaserLines());
        addDevice(l, lLightSheet);
      }
    }

    // syncing exposure between cameras and lightsheets, as well as camera image
    // height:
    {
      for (int l = 0; l < pNumberOfLightSheets; l++)
        for (int c = 0; c < pNumberOfDetectionArms; c++)
        {
          StackCameraDeviceInterface<?> lCamera =
                                                getDevice(StackCameraDeviceInterface.class,
                                                          c);
          LightSheet lLightSheet = getDevice(LightSheet.class, l);

          lCamera.getExposureInSecondsVariable()
                 .sendUpdatesTo(lLightSheet.getEffectiveExposureInSecondsVariable());

          lCamera.getStackHeightVariable()
                 .sendUpdatesTo(lLightSheet.getImageHeightVariable());

        }
    }

    // Setting up lightsheets selector
    {
      LightSheetOpticalSwitch lLightSheetOpticalSwitch =
                                                       new LightSheetOpticalSwitch("OpticalSwitch",
                                                                                   pNumberOfLightSheets);

      addDevice(0, lLightSheetOpticalSwitch);
    }

    {
      //addDevice(0, new TSTStageDevice());
    }
  }

  @Override
  public void addSimulatedDevices(boolean pDummySimulation,
                                  boolean pXYZRStage,
                                  boolean pSharedLightSheetControl,
                                  LightSheetMicroscopeSimulationDevice pSimulatorDevice)
  {
    super.addSimulatedDevices(pDummySimulation, pXYZRStage, pSharedLightSheetControl, pSimulatorDevice);


    {
      //KCubeDeviceFactory lKCubeDeviceFactory = KCubeDeviceFactory.getInstance();
      //addDevice(0, lKCubeDeviceFactory);
      //addDevice(0, lKCubeDeviceFactory.createKCubeDevice(26000318, "I3B")); // XWing LS3 beta angle

      BasicThreeAxesStageInterface lBasicThreeAxesStageInterface = new SimulatedThreeAxesStageDevice();

      addDevice(0, lBasicThreeAxesStageInterface);

      BasicThreeAxesStageScheduler lBasicThreeAxesStageScheduler = new BasicThreeAxesStageScheduler(lBasicThreeAxesStageInterface);
      addDevice(0, lBasicThreeAxesStageScheduler);


    }

    // setup adaptators
    //{
    //  AdaptiveZScheduler lAdaptiveZScheduler = new AdaptiveZScheduler();
    //  addDevice(0, lAdaptiveZScheduler);
    //}

  }

  @Override
  public void addStandardDevices(int pNumberOfControlPlanes) {
    super.addStandardDevices(pNumberOfControlPlanes);


    // setup adaptators/schedulers
    {
      AdaptiveZScheduler lAdaptiveZScheduler = new AdaptiveZScheduler();
      addDevice(0, lAdaptiveZScheduler);
    }

    {
      AdaptationScheduler lAdaptationScheduler = new AdaptationScheduler("Adaptation: Focus Z",
                                                                         AdaptationZ.class);
      addDevice(0, lAdaptationScheduler);
    }
    {
      AdaptationScheduler lAdaptationScheduler = new AdaptationScheduler("Adaptation: Focus Z with manual detection arm selection",
                                                                         AdaptationZManualDetectionArmSelection.class);
      addDevice(0, lAdaptationScheduler);
    }
    {
      AdaptationScheduler lAdaptationScheduler = new AdaptationScheduler("Adaptation: Focus Z with sliding window detection arm selection",
                                                                         AdaptationZSlidingWindowDetectionArmSelection.class);
      addDevice(0, lAdaptationScheduler);
    }
    {
      AdaptationScheduler lAdaptationScheduler = new AdaptationScheduler("Adaptation: Lightsheet angle alpha",
                                                                         AdaptationA.class);
      addDevice(0, lAdaptationScheduler);
    }
    {
      AdaptationScheduler lAdaptationScheduler = new AdaptationScheduler("Adaptation: Power",
                                                                         AdaptationP.class);
      addDevice(0, lAdaptationScheduler);
    }
    {
      AdaptationScheduler lAdaptationScheduler = new AdaptationScheduler("Adaptation: Lightsheet X position",
                                                                         AdaptationX.class);
      addDevice(0, lAdaptationScheduler);
    }

    {
      MultiChannelScheduler lMultiChannelScheduler = new MultiChannelScheduler();
      addDevice(0, lMultiChannelScheduler);
    }

    // initialize copilot
    {
      this.getNumberOfLightSheets();
      CalibrationImagerDevice
          lCalibrationImagerDevice = new CalibrationImagerDevice(this, new Variable<Double>("", 0.0));
      addDevice(0, lCalibrationImagerDevice);

      ArrayList<StepFactoryInterface> lCopilotStepList = new ArrayList<StepFactoryInterface>();
      lCopilotStepList.add(new Step1ManualCalibrationFactory());
      lCopilotStepList.add(new Step2AutomaticCalibrationFactory());
      lCopilotStepList.add(new Step3SampleMountingFactory());
      lCopilotStepList.add(new Step4AutomaticCalibrationWithSampleFactory());
      lCopilotStepList.add(new Step5RecalibrationWithSampleFactory());

      addDevice(0, new CopilotDevice(this, lCopilotStepList));
    }

    addDevice(0, new FastImageDevice(this));
  }

}
