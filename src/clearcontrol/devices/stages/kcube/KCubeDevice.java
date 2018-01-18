package clearcontrol.devices.stages.kcube;

import aptj.APTJDevice;
import aptj.APTJExeption;
import clearcontrol.core.device.task.TaskDevice;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.gui.jfx.custom.visualconsole.VisualConsoleInterface;

/**
 *
 *
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG
 * (http://mpi-cbg.de) December 2017
 */
public class KCubeDevice extends TaskDevice
                            implements VisualConsoleInterface,
                                       LoggingFeature
{
  long mSerialId;
  APTJDevice mKCubeAPTJDevice = null;

  /**
   * The constructor is package private because you are supposed to
   * get new instances from the KCubeFactory.
   *
   * @param pKCubeDevice
   */
  KCubeDevice(APTJDevice pKCubeDevice, String pName)
  {
    super(pName + " (Thorlabs K-cube, " + pKCubeDevice.getSerialNumber() + ") test");
    mKCubeAPTJDevice = pKCubeDevice;
    mSerialId = mKCubeAPTJDevice.getSerialNumber();


    //System.out.println("APTLibrary.APTInit();");
    //APTLibrary.
  //  System.out.println("APTLibrary.InitHWDevice(" + mSerialId + ");");
//    APTLibrary.InitHWDevice(mSerialId);
    //System.out.println("B");
/*
    try
    {
      mKCubeAPTJDevice = mAPTJLibrary.createDeviceFromSerialNumber(mSerialId);

      System.out.println(mKCubeAPTJDevice.getCurrentPosition());
    }
    catch (APTJExeption aptjExeption)
    {
      aptjExeption.printStackTrace();
    }*/
  }

  @Override
  public void run()
  {

  }

  public double getMinPosition() {
    if (mKCubeAPTJDevice != null)   {
      return mKCubeAPTJDevice.getMinPosition();
    }
    return Double.NaN;
  }
  public double getMaxPosition() {
    if (mKCubeAPTJDevice != null)   {
      return mKCubeAPTJDevice.getMaxPosition();
    }
    return Double.NaN;
  }
  public double getCurrentPosition() {
    if (mKCubeAPTJDevice != null)   {
      try
      {
        return mKCubeAPTJDevice.getCurrentPosition();
      }
      catch (APTJExeption aptjExeption)
      {
        aptjExeption.printStackTrace();
      }
    }
    return Double.NaN;
  }

  public void moveBy(double pStep) {
    double lNewPosition = getCurrentPosition() + pStep;
    if (lNewPosition > getMaxPosition() || lNewPosition < getMinPosition()) {
      warning("The KCube controlled motor " + mSerialId + " cannot be moved to position " + lNewPosition + ", it would be out of [" + getMinPosition() + ", " + getMaxPosition() + "]");
      return;
    }


    info("Moving KCube " + mSerialId + " by " + pStep);
    try
    {
      mKCubeAPTJDevice.moveBy(pStep);
    }
    catch (APTJExeption aptjExeption)
    {
      aptjExeption.printStackTrace();
    }
  }



  public void moveTo(double pPosition) {
    info("Moving KCube " + mSerialId + " to " + pPosition);
    try
    {
      mKCubeAPTJDevice.moveBy(pPosition);
    }
    catch (APTJExeption aptjExeption)
    {
      aptjExeption.printStackTrace();

    }
  }


}
