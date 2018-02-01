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

  /**
   * INstanciates a virtual device with a given name
   *
   */
  public MultiChannelScheduler()
  {
    super("Multichannel scheduler");
  }

  @Override public boolean doExperiment(long pTimePoint)
  {
    if (mMicroscope instanceof LightSheetMicroscopeInterface) {
      mLightSheetMicroscope = (LightSheetMicroscopeInterface) mMicroscope;
    } else {
      warning("Error: I only support lightsheet microscopes!");
      return false;
    }

    ArrayList<LaserDeviceInterface> lLaserList =
        mLightSheetMicroscope.getDevices(LaserDeviceInterface.class);

    if (lLaserList.size() == 2) {
      LaserDeviceInterface lLaser1 = lLaserList.get(0);
      LaserDeviceInterface lLaser2 = lLaserList.get(1);

      if (pTimePoint % 2 == 0) {
        lLaser1.setLaserPowerOn(true);
        lLaser2.setLaserPowerOn(false);
      } else {
        lLaser1.setLaserPowerOn(false);
        lLaser2.setLaserPowerOn(true);
      }
    } else {
      warning("Error: Wrong number of lasers!!");
      return false;
    }
    return false;
  }
}
