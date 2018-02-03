package clearcontrol.gui.jfx.custom.image;

import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.view.Views;

import java.util.HashMap;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * February 2018
 */
public interface CachedImagePaneRefreshFeature
{
  public static HashMap<ImagePane, RandomAccessibleInterval<UnsignedShortType>>
      mImageCache = new HashMap<>();

  public default void cachedImagePaneRefresh(ImagePane pImagePane, RandomAccessibleInterval<UnsignedShortType> pRAI, Interval pInterval)
  {
    if (pRAI != null)
    {
      if ((!mImageCache.containsKey(pImagePane)) || mImageCache.get(pImagePane) != pRAI)
      {
        mImageCache.put(pImagePane, pRAI);

        RandomAccessibleInterval<UnsignedShortType> pCroppedRAI = pRAI;
        if (pInterval != pRAI) {
          pCroppedRAI = Views.interval(pRAI, pInterval);
        }

        pImagePane.setImage(new RGBImgImage<UnsignedShortType>(pCroppedRAI));
      }
    }
  }

  public default void cachedImagePaneRefresh(ImagePane pImagePane, RandomAccessibleInterval<UnsignedShortType> pRAI) {
    cachedImagePaneRefresh(pImagePane, pRAI, pRAI);
  }

}
