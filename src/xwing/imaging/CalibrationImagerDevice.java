package xwing.imaging;

import clearcontrol.core.device.task.PeriodicLoopTaskDevice;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.microscope.lightsheet.imaging.DirectImage;
import clearcontrol.scripting.engine.ScriptingEngine;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.view.Views;
import xwing.XWingMicroscope;

import java.util.concurrent.TimeUnit;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * February 2018
 */
public class CalibrationImagerDevice extends PeriodicLoopTaskDevice implements
                                                                    LoggingFeature
{
  RandomAccessibleInterval<UnsignedShortType>[][] images;

  RandomAccessibleInterval<UnsignedShortType> mImageC0L01;
  RandomAccessibleInterval<UnsignedShortType> mImageC0L23;
  RandomAccessibleInterval<UnsignedShortType> mImageC1L01;
  RandomAccessibleInterval<UnsignedShortType> mImageC1L23;

  XWingMicroscope mXWingMicroscope;
  Variable<Double> mZVariable;

  private int mImageSize = 2048;

  public CalibrationImagerDevice(XWingMicroscope pXWingMicroscope,
                                 Variable<Double> pZVariable)
  {
    super("XWing Calibration Imager", 5000.0, TimeUnit.MILLISECONDS);
    mXWingMicroscope = pXWingMicroscope;

    images =
        new RandomAccessibleInterval[pXWingMicroscope.getNumberOfDetectionArms()][pXWingMicroscope
            .getNumberOfLightSheets()];
    mZVariable = pZVariable;
  }

  private int counter = 0;

  @Override public boolean loop()
  {
    info("Start loop");

    counter++;
    if (counter % 4 == 0)
    {
      mImageC0L01 = acquireDualLightSheetImage(0, 1, 0);
    }
    if (counter % 4 == 1)
    {
      mImageC0L23 = acquireDualLightSheetImage(2, 3, 0);
    }
    if (counter % 4 == 2)
    {
      mImageC1L01 = acquireDualLightSheetImage(0, 1, 1);
    }
    if (counter % 4 == 3)
    {
      mImageC1L23 = acquireDualLightSheetImage(2, 3, 1);
    }
    info("End loop");
    return true;
  }

  private RandomAccessibleInterval<UnsignedShortType> acquireDualLightSheetImage(
      int pLightSheetIndex1,
      int pLightSheetIndex2,
      int pDetectionArmIndex)
  {
    info("Acquire! D"
         + pDetectionArmIndex
         + " L"
         + pLightSheetIndex1
         + pLightSheetIndex2);
    DirectImage lDirectImage = new DirectImage(mXWingMicroscope);
    lDirectImage.setDetectionZ(mZVariable.get());
    lDirectImage.setIlluminationZ(mZVariable.get());
    lDirectImage.setDetectionArmIndex(pDetectionArmIndex);
    lDirectImage.setLightSheetIndex(pLightSheetIndex1);
    lDirectImage.setImageHeight(mImageSize);
    lDirectImage.setImageWidth(mImageSize);
    RandomAccessibleInterval<UnsignedShortType>
        lLightSheetImage1 =
        lDirectImage.getRandomAccessibleInterval();

    lDirectImage.setLightSheetIndex(pLightSheetIndex2);
    RandomAccessibleInterval<UnsignedShortType>
        lLightSheetImage2 =
        lDirectImage.getRandomAccessibleInterval();
    images[pDetectionArmIndex][pLightSheetIndex1] = lLightSheetImage1;

    RandomAccessibleInterval<UnsignedShortType>
        pDualChannelImage =
        Views.concatenate(2, lLightSheetImage1, lLightSheetImage2);
    images[pDetectionArmIndex][pLightSheetIndex2] = lLightSheetImage2;

    return pDualChannelImage;
  }

  public RandomAccessibleInterval<UnsignedShortType> getImageC0L01()
  {
    return mImageC0L01;
  }

  public RandomAccessibleInterval<UnsignedShortType> getImageC0L23()
  {
    return mImageC0L23;
  }

  public RandomAccessibleInterval<UnsignedShortType> getImageC1L01()
  {
    return mImageC1L01;
  }

  public RandomAccessibleInterval<UnsignedShortType> getImageC1L23()
  {
    return mImageC1L23;
  }

  public void zoomIn()
  {
    if (mImageSize / 2 > 64)
    {
      mImageSize = mImageSize / 2;
    }
    else
    {
      mImageSize = 64;
    }
  }

  public void zoomOut()
  {
    if (mImageSize * 2 < 2048)
    {
      mImageSize = mImageSize * 2;
    }
    else
    {
      mImageSize = 2048;
    }
  }

  public RandomAccessibleInterval<UnsignedShortType> getImage(int pDetectionArmIndex,
                                                              int pLightSheetIndex)
  {
    return images[pDetectionArmIndex][pLightSheetIndex];
  }

  public void setZVariable(Variable<Double> pZVariable)
  {
    this.mZVariable = pZVariable;
  }

  public boolean isStopRequested()
  {
    return ScriptingEngine.isCancelRequestedStatic()
           || getStopSignalVariable().get();
  }

}
