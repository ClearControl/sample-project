package clearcontrol.gui.jfx.custom.image;

import clearcontrol.core.log.LoggingFeature;
import de.mpicbg.rhaase.spimcat.postprocessing.fijiplugins.imageanalysis.statistics.Average;
import de.mpicbg.rhaase.spimcat.postprocessing.fijiplugins.imageanalysis.statistics.StandardDeviation;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import net.imglib2.IterableInterval;
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

    T lMinRedValue = pRAI.randomAccess().get().copy();
    T lMaxRedValue = pRAI.randomAccess().get().copy();
    T lMinGreenValue = pRAI.randomAccess().get().copy();
    T lMaxGreenValue = pRAI.randomAccess().get().copy();
    T lMinBlueValue = pRAI.randomAccess().get().copy();
    T lMaxBlueValue = pRAI.randomAccess().get().copy();

    if (pRAI.numDimensions() == 2)
    {
      lRedRA = pRAI.randomAccess();
      computeViewRangeMinMax(Views.iterable(pRAI),
                               lMinRedValue,
                               lMaxRedValue);

    } else if (pRAI.numDimensions() == 3) {
      lRedRA = Views.hyperSlice(pRAI, 2, 0).randomAccess();
      computeViewRangeMinMax(Views.iterable(Views.hyperSlice(pRAI,
                                                               2,
                                                               0)),
                               lMinRedValue,
                               lMaxRedValue);
      if (pRAI.dimension(2) > 1)
      {
        lGreenRA = Views.hyperSlice(pRAI, 2, 1).randomAccess();
        computeViewRangeMinMax(Views.iterable(Views.hyperSlice(pRAI,
                                                                 2,
                                                                 1)),
                                 lMinGreenValue,
                                 lMaxGreenValue);
        if (pRAI.dimension(2) > 2)
        {
          lBlueRA = Views.hyperSlice(pRAI, 2, 2).randomAccess();
          computeViewRangeMinMax(Views.iterable(Views.hyperSlice(pRAI,
                                                                   2,
                                                                   2)),
                                   lMinBlueValue,
                                   lMaxBlueValue);
        }
      }
    } else {
      warning("Wrong number of dimensions");
      return;
    }


    double lMinRed = lMinRedValue.getRealDouble();
    double lMaxRed = lMaxRedValue.getRealDouble();
    double lMinGreen = lMinGreenValue.getRealDouble();
    double lMaxGreen = lMaxGreenValue.getRealDouble();
    double lMinBlue = lMinBlueValue.getRealDouble();
    double lMaxBlue = lMaxBlueValue.getRealDouble();

    info("lMinRed" + lMinRed);
    info("lMaxRed" + lMaxRed);
    info("lMinGreen" + lMinGreen);
    info("lMaxGreen" + lMaxGreen);

    lMinRed = 100;
    lMinGreen = 100;
    lMaxRed = 130;
    lMaxGreen = 130;

    double lRed = 0;
    double lGreen = 0;
    double lBlue = 0;
    double lOpacity = 1.0;

    double lRedRange = lMaxRed - lMinRed;
    double lGreenRange = lMaxGreen - lMinGreen;
    double lBlueRange = lMaxBlue - lMinBlue;


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

        lRed = (lRedRA.get().getRealDouble() - lMinRed) / lRedRange;
        if (lGreenRA != null) {
          lGreenRA.setPosition(position);
          lGreen = (lGreenRA.get().getRealDouble() - lMinGreen) / lGreenRange;
        }
        if (lBlueRA != null) {
          lBlueRA.setPosition(position);
          lBlue = (lBlueRA.get().getRealDouble() - lMinBlue) / lBlueRange;
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

  private void computeViewRangeMinMax(IterableInterval<T> pRAI, T pMin, T pMax) {
    Average<T> lAverage = new Average<T>(pRAI);
    double lAverageValue = lAverage.getAverage();
    StandardDeviation<T> lStdDev = new StandardDeviation<T>(pRAI, lAverage);
    double lStdDevValue = lStdDev.getStandardDevation();
    info("determined avg: " + lAverage);
    info("determined std: " + lStdDevValue);
    pMin.setReal(lAverageValue - 2 * lStdDevValue );
    pMax.setReal(lAverageValue + 2 * lStdDevValue );
  }
}
