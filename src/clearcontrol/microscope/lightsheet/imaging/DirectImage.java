package clearcontrol.microscope.lightsheet.imaging;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.LightSheetMicroscopeQueue;
import clearcontrol.microscope.lightsheet.component.lightsheet.si.BinaryStructuredIlluminationPattern;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import clearcontrol.stack.OffHeapPlanarStack;
import clearcontrol.stack.imglib2.StackToImgConverter;
import ij.ImagePlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.view.Views;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * February 2018
 */
public class DirectImage extends DirectImageBase implements LoggingFeature
{



  public DirectImage(LightSheetMicroscope pLightSheetMicroscope) {
    super(pLightSheetMicroscope);
  }

  @Override protected boolean configureQueue(LightSheetMicroscopeQueue pQueue)
  {
    if (mInterpolatedAcquisitionState == null)
    {
      pQueue.setIZ(mLightSheetIndex, mIlluminationZ);
    }
    pQueue.setDZ(mDetectionArmIndex, mDetectionZ);

    pQueue.setC(mDetectionArmIndex, true);
    pQueue.addCurrentStateToQueue();
    return true;
  }
}
