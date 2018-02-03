package clearcontrol.gui.jfx.custom.image;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * This class takes a 1, 2 or 3 channel ImagePlus and represents a JFX
 * Image with the same content.
 *
 * Deprecated because we shouldn't use ImageJ1 stuff (ImagePlus)
 *
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * February 2018
 */
@Deprecated
public class RGBImagePlusImage extends WritableImage
{
  public RGBImagePlusImage(ImagePlus pImagePlus)
  {
    super(pImagePlus.getWidth(), pImagePlus.getHeight());

    setImagePlus(pImagePlus);
  }

  public void setImagePlus(ImagePlus pImagePlus) {
    ImageProcessor lRedImageProcessor = null;
    ImageProcessor lGreenImageProcessor = null;
    ImageProcessor lBlueImageProcessor = null;

    double lMinRed = 0;
    double lMaxRed = 255;

    double lMinGreen = 0;
    double lMaxGreen = 255;
    double lMinBlue = 0;
    double lMaxBlue = 255;

    pImagePlus.setC(1);
    lRedImageProcessor = pImagePlus.getProcessor();
    lMinRed = pImagePlus.getDisplayRangeMin();
    lMaxRed = pImagePlus.getDisplayRangeMax();

    if (pImagePlus.getNChannels() > 1) {
      pImagePlus.setC(2);
      lGreenImageProcessor = pImagePlus.getProcessor();
      lMinGreen = pImagePlus.getDisplayRangeMin();
      lMaxGreen = pImagePlus.getDisplayRangeMax();
    }
    if (pImagePlus.getNChannels() > 2) {
      pImagePlus.setC(3);
      lBlueImageProcessor = pImagePlus.getProcessor();
      lMinBlue = pImagePlus.getDisplayRangeMin();
      lMaxBlue = pImagePlus.getDisplayRangeMax();
    }

    double lRed = 0;
    double lGreen = 0;
    double lBlue = 0;
    double lOpacity = 1.0;

    double lRangeRed = lMaxRed - lMinRed;
    double lRangeGreen = lMaxGreen - lMinGreen;
    double lRangeBlue = lMaxBlue - lMinBlue;


    int lWidth = Math.min(pImagePlus.getWidth(), (int)getWidth());
    int lHeight = Math.min(pImagePlus.getHeight(), (int)getHeight());

    PixelWriter lPixelWriter = getPixelWriter();

    for (int x = 0; x < lWidth; x++ ){
      for (int y = 0; y < lHeight; y++ )
      {
        lRed = (lRedImageProcessor.get(x, y) - lMinRed) / lRangeRed;
        if (lGreenImageProcessor != null) {
          lGreen = (lGreenImageProcessor.get(x, y) - lMinGreen) / lRangeGreen;
        }
        if (lBlueImageProcessor != null) {
          lBlue = (lBlueImageProcessor.get(x, y) - lMinBlue) / lRangeBlue;
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
