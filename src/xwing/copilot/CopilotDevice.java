package xwing.copilot;

import clearcontrol.core.device.VirtualDevice;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.devices.lasers.LaserDeviceInterface;
import clearcontrol.gui.jfx.var.checkbox.VariableCheckBox;
import clearcontrol.microscope.lightsheet.component.detection.DetectionArmInterface;
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

  public BoundedVariable<Number> getZVariable() {
    return getXWingMicroscope().getDevice(DetectionArmInterface.class, 0).getZVariable();
  }
}
