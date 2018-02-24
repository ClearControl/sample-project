package xwing.multicolor;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.devices.lasers.LaserDeviceInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscopeInterface;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerInterface;

import java.util.ArrayList;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * January 2018
 */
public class MultiChannelScheduler extends SchedulerBase implements
                                                         SchedulerInterface,
                                                         LoggingFeature
{
  private LightSheetMicroscopeInterface mLightSheetMicroscope;

  boolean mInitialized = false;
  ArrayList<Double> mLaserPower = new ArrayList<Double>();

  /**
   * INstanciates a virtual device with a given name
   *
   */
  public MultiChannelScheduler()
  {
    super("Multichannel scheduler");
  }


  @Override public boolean initialize()
  {
    mTimePointCount = -1;
    return true;
  }

  int mTimePointCount = -1;
  @Override public boolean enqueue(long pTimePoint)
  {

    if (mMicroscope instanceof LightSheetMicroscopeInterface) {
      mLightSheetMicroscope = (LightSheetMicroscopeInterface) mMicroscope;
    } else {
      warning("Error: I only support lightsheet microscopes!");
      return false;
    }

    ArrayList<LaserDeviceInterface> lLaserList =
        mLightSheetMicroscope.getDevices(LaserDeviceInterface.class);

    if (!mInitialized) {
      for (LaserDeviceInterface lLaserDeviceInterface : lLaserList) {
        mLaserPower.add(lLaserDeviceInterface.getTargetPowerInPercent());
      }
      mInitialized = true;
    }

    mTimePointCount++;

    if (lLaserList.size() == 2) {
      LaserDeviceInterface lLaser1 = lLaserList.get(0);
      LaserDeviceInterface lLaser2 = lLaserList.get(1);

      if (mTimePointCount % 2 == 0) {
        lLaser1.setLaserPowerOn(true);
        lLaser1.setLaserOn(true);
        lLaser1.setTargetPowerInPercent(mLaserPower.get(0));
        lLaser2.setLaserPowerOn(false);
        lLaser2.setLaserOn(false);
      } else {
        lLaser1.setLaserPowerOn(false);
        lLaser1.setLaserOn(false);
        lLaser2.setLaserPowerOn(true);
        lLaser2.setLaserOn(true);
        lLaser1.setTargetPowerInPercent(mLaserPower.get(1));
      }
    } else {
      warning("Error: Wrong number of lasers!!");
      return false;
    }
    return false;
  }
}
