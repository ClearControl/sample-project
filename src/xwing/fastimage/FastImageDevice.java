package xwing.fastimage;

import clearcontrol.core.device.VirtualDevice;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.LightSheetMicroscopeQueue;
import clearcontrol.stack.OffHeapPlanarStack;
import clearcontrol.stack.imglib2.StackToImgConverter;
import ij.ImageJ;
import ij.plugin.Duplicator;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import xwing.XWingMicroscope;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * February 2018
 */
public class FastImageDevice extends VirtualDevice
{
  private LightSheetMicroscope mLightSheetMicroscope;

  private BoundedVariable<Integer> mDetectionArmIndex;
  private BoundedVariable<Integer> mIlluminationArmIndex;
  private BoundedVariable<Double> mDetectionArmZ;
  private BoundedVariable<Double> mIlluminationArmZ;
  private BoundedVariable<Double> mExposureTimeInSeconds;
  private BoundedVariable<Double> mTransitionTimeInSeconds;
  private BoundedVariable<Integer> mImageSize;
  private BoundedVariable<Integer> mNumberOfImages;

  /**
   * Instanciates a virtual device with a given name
   */
  public FastImageDevice(LightSheetMicroscope pLightSheetMicroscope)
  {
    super("Fast imager");
    mLightSheetMicroscope = pLightSheetMicroscope;

    BoundedVariable<Number>
        lZVariable = pLightSheetMicroscope.getDetectionArm(0).getZVariable();

    mDetectionArmIndex = new BoundedVariable<Integer>("Detection arm index", 0, 0, mLightSheetMicroscope.getNumberOfDetectionArms() - 1);
    mIlluminationArmIndex= new BoundedVariable<Integer>("Illumination arm index", 0, 0, mLightSheetMicroscope.getNumberOfLightSheets() - 1);
    mDetectionArmZ = new BoundedVariable<Double>("Detection arm Z", 0.0, lZVariable.getMin().doubleValue(), lZVariable.getMax().doubleValue(), 0.01);
    mIlluminationArmZ = new BoundedVariable<Double>("Illumination arm Z", 0.0, lZVariable.getMin().doubleValue(), lZVariable.getMax().doubleValue(), 0.01);;
    mExposureTimeInSeconds = new BoundedVariable<Double>("Exposure time", 0.01, 0.0, Double.MAX_VALUE, 0.00000000001);
    mTransitionTimeInSeconds = new BoundedVariable<Double>("Transition time", 0.1, 0.0, Double.MAX_VALUE, 0.00000000001);
    mImageSize = new BoundedVariable<Integer>("Image size (256, 512, 1024, 2048)", 512, 0, 2048);
    mNumberOfImages = new BoundedVariable<Integer>("Number of images", 10, 1, Integer.MAX_VALUE);
  }

  public boolean execute() {

    // Retrieve a queue from the scope to configure what to do in which order
    LightSheetMicroscopeQueue lQueue = mLightSheetMicroscope.requestQueue();
    lQueue.clearQueue();

    // set general imaging properties
    lQueue.setFullROI();
    lQueue.setCenteredROI(mImageSize.get(), mImageSize.get());
    lQueue.setExp(mExposureTimeInSeconds.get());

    // reset everything
    for (int i = 0; i < mLightSheetMicroscope.getNumberOfLightSheets(); i++)
    {
      lQueue.setI(i, false);
    }

    // go to the desired position, move light sheet to zero
    lQueue.setI(mIlluminationArmIndex.get(), true);
    lQueue.setIX(mIlluminationArmIndex.get(), 0);
    lQueue.setIY(mIlluminationArmIndex.get(), 0);
    lQueue.setDZ(mDetectionArmIndex.get(), mDetectionArmZ.get());
    lQueue.setC(mDetectionArmIndex.get(), false);
    lQueue.addCurrentStateToQueue();

    // acquire the actual images
    for (int i = 0; i < mNumberOfImages.get(); i++)
    {
      lQueue.setIZ(mIlluminationArmIndex.get(), mIlluminationArmZ.get());
      lQueue.setDZ(mDetectionArmIndex.get(), mDetectionArmZ.get());

      lQueue.setC(mDetectionArmIndex.get(), true);
      lQueue.addCurrentStateToQueue();
    }

    // finish configuration
    lQueue.setTransitionTime(mTransitionTimeInSeconds.get());
    lQueue.finalizeQueue();

    // execute imaging
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

    // retrieve resulting image
    OffHeapPlanarStack lResultImage =
        (OffHeapPlanarStack) mLightSheetMicroscope.getCameraStackVariable(mDetectionArmIndex.get())
                                                  .get();

    // convert the image to imagej format
    StackToImgConverter lStackToImgConverter = new StackToImgConverter(lResultImage);
    RandomAccessibleInterval<UnsignedShortType> lRAI = lStackToImgConverter.getRandomAccessibleInterval();

    // show image
    showImageJ();
    new Duplicator().run(ImageJFunctions.wrap(lRAI, "temp")).show();

    return true;
  }


  private static ImageJ sImageJ ;
  private void showImageJ() {
    if (sImageJ == null)
      sImageJ = new ImageJ();
    if (!sImageJ.isVisible())
      sImageJ.setVisible(true);
  }

  public BoundedVariable<Double> getDetectionArmZ()
  {
    return mDetectionArmZ;
  }

  public BoundedVariable<Double> getIlluminationArmZ()
  {
    return mIlluminationArmZ;
  }

  public BoundedVariable<Double> getExposureTimeInSeconds()
  {
    return mExposureTimeInSeconds;
  }

  public BoundedVariable<Double> getTransitionTimeInSeconds()
  {
    return mTransitionTimeInSeconds;
  }

  public BoundedVariable<Integer> getDetectionArmIndex()
  {
    return mDetectionArmIndex;
  }

  public BoundedVariable<Integer> getIlluminationArmIndex()
  {
    return mIlluminationArmIndex;
  }

  public BoundedVariable<Integer> getImageSize()
  {
    return mImageSize;
  }

  public BoundedVariable<Integer> getNumberOfImages()
  {
    return mNumberOfImages;
  }
}
