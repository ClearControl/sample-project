package clearcontrol.gui.jfx.custom.image;

import clearcontrol.core.log.LoggingFeature;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.stats.ComputeMinMax;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * February 2018
 */
public class RGBImgImage<T extends RealType<T>> extends WritableImage implements
                                                                      LoggingFeature
{
  public RGBImgImage(RandomAccessibleInterval<T> pRAI)
  {
    super((int)pRAI.dimension(0), (int)pRAI.dimension(1));

    setRandomAccessibleInterval(pRAI);
  }

  public void setRandomAccessibleInterval(RandomAccessibleInterval<T> pRAI) {
    RandomAccess<T> lRedRA = null;
    RandomAccess<T> lGreenRA = null;
    RandomAccess<T> lBlueRA = null;

    T lMinValue = pRAI.randomAccess().get().copy();
    T lMaxValue = pRAI.randomAccess().get().copy();

    ComputeMinMax<T> lComputeMinMax = new ComputeMinMax<T>(Views.iterable(pRAI), lMinValue, lMaxValue);
    lComputeMinMax.process();

    double lMin = lMinValue.getRealDouble();
    double lMax = lMaxValue.getRealDouble();

    if (pRAI.numDimensions() == 2)
    {
      lRedRA = pRAI.randomAccess();
    } else if (pRAI.numDimensions() == 3) {
      lRedRA = Views.hyperSlice(pRAI, 2, 0).randomAccess();
      if (pRAI.dimension(2) > 1)
      {
        lGreenRA = Views.hyperSlice(pRAI, 2, 1).randomAccess();
        if (pRAI.dimension(2) > 2)
        {
          lBlueRA = Views.hyperSlice(pRAI, 2, 2).randomAccess();
        }
      }
    } else {
      warning("Wrong number of dimensions");
      return;
    }

    double lRed = 0;
    double lGreen = 0;
    double lBlue = 0;
    double lOpacity = 1.0;

    double lRange = lMax - lMin;


    int lWidth = Math.min((int)pRAI.dimension(0), (int)getWidth());
    int lHeight = Math.min((int)pRAI.dimension(1), (int)getHeight());

    PixelWriter lPixelWriter = getPixelWriter();

    long[] position = new long[2];

    for (int x = 0; x < lWidth; x++ ){
      position[0] = pRAI.min(0) + x;
      for (int y = 0; y < lHeight; y++ )
      {
        position[1] = pRAI.min(1) + y;

        lRedRA.setPosition(position);

        lRed = (lRedRA.get().getRealDouble() - lMin) / lRange;
        if (lGreenRA != null) {
          lGreenRA.setPosition(position);
          lGreen = (lGreenRA.get().getRealDouble() - lMin) / lRange;
        }
        if (lBlueRA != null) {
          lBlueRA.setPosition(position);
          lBlue = (lBlueRA.get().getRealDouble() - lMin) / lRange;
        }
        if (lRed < 0) {
          lRed = 0;
        }
        if (lGreen < 0) {
          lGreen = 0;
        }
        if (lBlue < 0) {
          lBlue = 0;
        }

        if (lRed > 1) {
          lRed = 1;
        }
        if (lGreen > 1) {
          lGreen = 1;
        }
        if (lBlue > 1) {
          lBlue = 1;
        }
        Color color = new Color(lRed, lGreen, lBlue, lOpacity);
        lPixelWriter.setColor(x, y, color);
      }
    }
  }
}
