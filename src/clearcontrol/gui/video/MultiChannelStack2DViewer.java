package clearcontrol.gui.video;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.gui.jfx.custom.image.ImagePane;
import clearcontrol.gui.jfx.custom.image.RGBImagePlusImage;
import clearcontrol.gui.video.video2d.Stack2DDisplay;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.imglib2.StackToImgConverter;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import ij.plugin.RGBStackMerge;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Deprecated because we shouldnt imagej1 stuff (ImagePlus)
 *
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * February 2018
 */
@Deprecated
public class MultiChannelStack2DViewer extends CustomGridPane implements LoggingFeature
{
  List<Stack2DDisplay> mStack2DDisplays;

  ImagePane mImagePane;

  int minX = 0;
  int maxX = 0;
  int minY = 0;
  int maxY = 0;
  ImagePlus mImagePlus;

  double mCenterZoomRange = 1.0;

  public MultiChannelStack2DViewer(List<Stack2DDisplay> pStack2DDisplays) {
    mStack2DDisplays = pStack2DDisplays;
    refresh();
  }

  private ImagePlus getImagePlus()
  {
    ArrayList<ImagePlus> lImagePlusList = new ArrayList<ImagePlus>();
    for (Stack2DDisplay lStack2DDisplay : mStack2DDisplays) {
      StackInterface lStack = lStack2DDisplay.getLastViewedStack();
      if (lStack != null) {
        RandomAccessibleInterval
            lRandomAccessibleInterval = new StackToImgConverter(lStack).getRandomAccessibleInterval();
        ImagePlus lImagePlus = ImageJFunctions.wrap(lRandomAccessibleInterval, "temp");
        lImagePlus = new Duplicator().run(lImagePlus);
        if (lStack2DDisplay.isFlipX()) {
          IJ.run(lImagePlus, "Flip Horizontally", "");
        }
        lImagePlusList.add(lImagePlus);
      } else {
        warning("Stack was null.");
      }
    }


    ImagePlus[] lImagePlusArray = new ImagePlus[lImagePlusList.size()];
    lImagePlusList.toArray(lImagePlusArray);

    info("Showing " + lImagePlusList.size() + " images");
    ImagePlus fusedImage = RGBStackMerge.mergeChannels(lImagePlusArray, false);
    if (fusedImage != null)
    {
      //fusedImage.show();
      for (int c = 0; c < fusedImage.getNChannels(); c++) {
        fusedImage.setC(c + 1);
        IJ.run(fusedImage, "Enhance Contrast", "saturated=0.35");
      }
    } else {
      warning("Multichannel fusion result was null.");
    }
    mImagePlus = fusedImage;
    return fusedImage;
  }

  public void refresh() {
    ImagePlus lImagePlus = getImagePlus();
    if (Math.abs(mCenterZoomRange - 1.0) > 0.0001)
    {
      lImagePlus.setRoi(minX, minY, maxX - minX, maxY - minY);
      lImagePlus = new Duplicator().run(lImagePlus);
    }
    if (mImagePane == null) {
      if (mImagePlus != null)
      {
        maxX = mImagePlus.getWidth();
        maxY = mImagePlus.getHeight();
        mImagePane = new ImagePane(new RGBImagePlusImage(mImagePlus));
        this.add(mImagePane, 0, 0);
      }
    } else {
      mImagePane.setImage(new RGBImagePlusImage(lImagePlus));
    }
  }

  public void setCenterZoom(double pRelativeRange) {
    if (pRelativeRange > 1.0 || pRelativeRange < 0.0) {
      pRelativeRange = 1.0;
    }
    mCenterZoomRange = pRelativeRange;
    minX = (int)(pRelativeRange * mImagePlus.getWidth() / 2.0);
    minY = (int)(pRelativeRange * mImagePlus.getHeight() / 2.0);

    maxX = mImagePlus.getWidth() - minX;
    maxY = mImagePlus.getHeight() - minY;

    refresh();
  }

  public double getCenterZoom() {
    return mCenterZoomRange;
  }
}

