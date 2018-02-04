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
public class DirectImage implements LoggingFeature
{
  // input
  LightSheetMicroscope mLightSheetMicroscope;

  double mIlluminationZ = 0;
  double mDetectionZ = 0;

  int mLightSheetIndex = 0;
  int mDetectionArmIndex = 0;

  double mExposureTimeInSeconds = 0.01;

  private int mImageWidth = 512;
  private int mImageHeight = 512;

  // output
  OffHeapPlanarStack mResultImage = null;

  public DirectImage(LightSheetMicroscope pLightSheetMicroscope) {
    mLightSheetMicroscope = pLightSheetMicroscope;
  }

  private synchronized boolean image() {

    if (mResultImage != null) {
      return true;
    }

    LightSheetMicroscopeQueue lQueue = mLightSheetMicroscope.requestQueue();
    lQueue.clearQueue();
    // lQueue.zero();

    if (mInterpolatedAcquisitionState != null) {
      mInterpolatedAcquisitionState.applyAcquisitionStateAtZ(lQueue, mAcquisitionZ);
      info("Imaging at I " + lQueue.getIZ(mLightSheetIndex)  + " instead of " + mIlluminationZ );
    }

    lQueue.setFullROI();
    lQueue.setCenteredROI(mImageWidth, mImageHeight);

    lQueue.setExp(mExposureTimeInSeconds);

    // reset everything
    for (int i = 0; i < mLightSheetMicroscope.getNumberOfLightSheets(); i++)
    {
      lQueue.setI(i, false);
    }


    lQueue.setI(mLightSheetIndex, true);
    lQueue.setIX(mLightSheetIndex, 0);
    lQueue.setIY(mLightSheetIndex, 0);
    /*mQueue.setIPattern(i,
                       0,
                       new BinaryStructuredIlluminationPattern());
    mQueue.setIPattern(i,
                       0,
                       new BinaryStructuredIlluminationPattern());
 */

    //if (mInterpolatedAcquisitionState == null)
    //{
    lQueue.setDZ(mDetectionArmIndex, mDetectionZ);
    //}
    lQueue.setC(mDetectionArmIndex, false);

    lQueue.addCurrentStateToQueue();

    // acquire the actual image
    if (mInterpolatedAcquisitionState == null)
    {
      lQueue.setIZ(mLightSheetIndex, mIlluminationZ);
    }
    lQueue.setDZ(mDetectionArmIndex, mDetectionZ);

    lQueue.setC(mDetectionArmIndex, true);
    lQueue.addCurrentStateToQueue();

    lQueue.setTransitionTime(0.1);

    lQueue.finalizeQueue();

    mLightSheetMicroscope.useRecycler("adaptation", 1, 4, 4);
    final Boolean lPlayQueueAndWait;
    try
    {
      lPlayQueueAndWait = mLightSheetMicroscope.playQueueAndWaitForStacks(lQueue,
                                                      100 + lQueue.getQueueLength(),
                                                                          TimeUnit.SECONDS);
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
      return false;
    }
    catch (ExecutionException e)
    {
      e.printStackTrace();
      return false;
    }
    catch (TimeoutException e)
    {
      e.printStackTrace();
      return false;
    }

    if (!lPlayQueueAndWait)
    {
      return false;
    }

    mResultImage =
        (OffHeapPlanarStack) mLightSheetMicroscope.getCameraStackVariable(mDetectionArmIndex)
                                                  .get();

    return true;
  }

  public OffHeapPlanarStack getImage() {
    image();
    return mResultImage;
  }

  public RandomAccessibleInterval<UnsignedShortType> getRandomAccessibleInterval() {
    image();
    if (mResultImage == null) {
      return null;
    }

    StackToImgConverter<UnsignedShortType> lStackToImgConverter = new StackToImgConverter<UnsignedShortType>(mResultImage);
    RandomAccessibleInterval<UnsignedShortType> img = lStackToImgConverter.getRandomAccessibleInterval();

    return img;
  }

  public void invalidate() {
    mResultImage = null;
  }


  public void setIlluminationZ(double pIlluminationZ) {
    invalidate();
    mInterpolatedAcquisitionState = null;
    mIlluminationZ = pIlluminationZ;
  }
  public void setDetectionZ(double pDetectionZ) {
    invalidate();
    mDetectionZ = pDetectionZ;
  }

  public void setLightSheetIndex(int pLightSheetIndex)
  {
    invalidate();
    this.mLightSheetIndex = pLightSheetIndex;
  }

  public void setDetectionArmIndex(int pDetectionArmIndex)
  {
    invalidate();
    this.mDetectionArmIndex = pDetectionArmIndex;
  }

  public void setImageHeight(int pImageHeight)
  {
    this.mImageHeight = pImageHeight;
  }

  public void setImageWidth(int pImageWidth) {
    this.mImageWidth = pImageWidth;
  }

  InterpolatedAcquisitionState mInterpolatedAcquisitionState;
  Double mAcquisitionZ;

  public void applyInterpolatedAcquisitionState(
      InterpolatedAcquisitionState pInterpolatedAcquisitionState,
      Double pAcquisitionZ)
  {
    invalidate();
    mInterpolatedAcquisitionState = pInterpolatedAcquisitionState;
    mAcquisitionZ = pAcquisitionZ;
  }
}
