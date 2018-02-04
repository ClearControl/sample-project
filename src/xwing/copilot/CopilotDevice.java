package xwing.copilot;

import clearcontrol.core.device.VirtualDevice;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.devices.lasers.LaserDeviceInterface;
import clearcontrol.gui.jfx.var.checkbox.VariableCheckBox;
import clearcontrol.microscope.lightsheet.calibrator.CalibrationEngine;
import clearcontrol.microscope.lightsheet.component.detection.DetectionArmInterface;
import clearcontrol.microscope.lightsheet.component.lightsheet.LightSheetInterface;
import xwing.XWingMicroscope;
import xwing.copilot.gui.steps.StepFactoryInterface;
import xwing.main.XWingMain;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * February 2018
 */
public class CopilotDevice extends VirtualDevice
{
  XWingMicroscope mXWingMicroscope;
  ArrayList<StepFactoryInterface> mStepFactoryInterfaceList;


  private final int mCalibrationLaserWaveLength = 488;
  private final int mImagingLaserWaveLength = 594;



  /**
   * INstanciates a virtual device with a given name
   */
  public CopilotDevice(XWingMicroscope pXWingMicroscope, ArrayList<StepFactoryInterface> pStepFactoryInterfaceList)
  {
    super("Copilot");
    mXWingMicroscope = pXWingMicroscope;
    mStepFactoryInterfaceList = pStepFactoryInterfaceList;
  }

  public XWingMicroscope getXWingMicroscope()
  {
    return mXWingMicroscope;
  }

  public ArrayList<StepFactoryInterface> getStepFactoryInterfaceList()
  {
    return mStepFactoryInterfaceList;
  }

  public LaserDeviceInterface getCalibrationLaser()
  {
    List<LaserDeviceInterface>
      lLaserList = mXWingMicroscope.getDevices(LaserDeviceInterface.class);

    for(LaserDeviceInterface lLaserDeviceInterface : lLaserList) {
      if (lLaserDeviceInterface.getWavelengthInNanoMeter() == mCalibrationLaserWaveLength) {
        return lLaserDeviceInterface;
      }
    }
    return null;
  }

  public LaserDeviceInterface getImagingLaser()
  {
    List<LaserDeviceInterface>
        lLaserList = mXWingMicroscope.getDevices(LaserDeviceInterface.class);

    for(LaserDeviceInterface lLaserDeviceInterface : lLaserList) {
      if (lLaserDeviceInterface.getWavelengthInNanoMeter() == mImagingLaserWaveLength) {
        return lLaserDeviceInterface;
      }
    }
    return null;
  }


  public BoundedVariable<Number> getZVariable() {
    return getXWingMicroscope().getDevice(DetectionArmInterface.class, 0).getZVariable();
  }


  public void calibrationLaserOn(){
    LaserDeviceInterface lLaser = getCalibrationLaser();
    lLaser.setLaserOn(true);
    lLaser.setLaserPowerOn(true);
  }
  public void calibrationLaserOff(){
    LaserDeviceInterface lLaser = getCalibrationLaser();
    lLaser.setLaserOn(false);
    lLaser.setLaserPowerOn(false);
  }

  public void calibrationLaserFullPower() {
    LaserDeviceInterface lLaser = getCalibrationLaser();
    lLaser.setTargetPowerInPercent(100);
  }

  public void calibrationLaserZeroPower() {
    LaserDeviceInterface lLaser = getCalibrationLaser();
    lLaser.setTargetPowerInPercent(0);
  }

  public void allLightSheetsHeightZero()
  {
    XWingMicroscope lXWingMicroscope = getXWingMicroscope();
    for (LightSheetInterface lLightSheetInterface : lXWingMicroscope.getDevices(LightSheetInterface.class)) {
      lLightSheetInterface.getHeightVariable().set(lLightSheetInterface.getHeightVariable().getMin());
    }
  }

  public void allLightSheetsFullHeight() {
    XWingMicroscope lXWingMicroscope = getXWingMicroscope();
    for (LightSheetInterface lLightSheetInterface : lXWingMicroscope.getDevices(LightSheetInterface.class)) {
      lLightSheetInterface.getHeightVariable().set(lLightSheetInterface.getHeightVariable().getMax());
    }
  }

  double mMicroscopeZ = 0;
  public void setMicroscopeZ(double pZ) {
    XWingMicroscope lXWingMicroscope = getXWingMicroscope();

    for (int l = 0; l < lXWingMicroscope.getNumberOfLightSheets(); l++) {
      lXWingMicroscope.getLightSheet(l).getZVariable().set(pZ);
    }

    for (int d = 0; d < lXWingMicroscope.getNumberOfDetectionArms(); d++) {
      lXWingMicroscope.getDetectionArm(d).getZVariable().set(pZ);
    }

    mMicroscopeZ = pZ;
  }

  public void imagingLaserOn()
  {
    LaserDeviceInterface lLaser = getImagingLaser();
    lLaser.setLaserOn(true);
    lLaser.setLaserPowerOn(true);
  }

  public void imagingLaserOff()
  {
    LaserDeviceInterface lLaser = getImagingLaser();
    lLaser.setLaserOn(false);
    lLaser.setLaserPowerOn(false);
  }

  public void imagingLaserMildPower()
  {
    LaserDeviceInterface lLaser = getImagingLaser();
    lLaser.setTargetPowerInPercent(100);
  }

  public void allLasersOff() {

    List<LaserDeviceInterface>
        lLaserList = mXWingMicroscope.getDevices(LaserDeviceInterface.class);

    for(LaserDeviceInterface lLaserDeviceInterface : lLaserList) {
      lLaserDeviceInterface.setTargetPowerInPercent(0);
      lLaserDeviceInterface.setLaserPowerOn(false);
      lLaserDeviceInterface.setLaserOn(false);
    }

  }

  public void startCalibration()
  {
    CalibrationEngine lCalibrationEngine = getXWingMicroscope().getDevice(CalibrationEngine.class, 0);
    lCalibrationEngine.getCalibrateZVariable().set(true);
    lCalibrationEngine.getCalibrateZWithSampleVariable().set(false);
    lCalibrationEngine.getCalibrateAVariable().set(true);
    lCalibrationEngine.getCalibrateWVariable().set(false);
    lCalibrationEngine.getCalibrateXYVariable().set(false);
    lCalibrationEngine.getCalibrateHPVariable().set(false);
    lCalibrationEngine.getCalibratePVariable().set(false);
    lCalibrationEngine.getCalibrateWPVariable().set(false);
    lCalibrationEngine.startTask();
  }


  public void startCalibrationWithSample()
  {
    CalibrationEngine lCalibrationEngine = getXWingMicroscope().getDevice(CalibrationEngine.class, 0);
    lCalibrationEngine.getCalibrateZVariable().set(false);
    lCalibrationEngine.getCalibrateZWithSampleVariable().set(false);
    lCalibrationEngine.getCalibrateAVariable().set(false);
    lCalibrationEngine.getCalibrateWVariable().set(false);
    lCalibrationEngine.getCalibrateXYVariable().set(false);
    lCalibrationEngine.getCalibrateHPVariable().set(false);
    lCalibrationEngine.getCalibratePVariable().set(true);
    lCalibrationEngine.getCalibrateWPVariable().set(false);
    lCalibrationEngine.startTask();
  }
}
