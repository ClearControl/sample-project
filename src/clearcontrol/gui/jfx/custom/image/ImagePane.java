package clearcontrol.gui.jfx.custom.image;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

/**
 *
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * January 2018
 */
public class ImagePane extends Pane
{
  GraphicsContext mGraphicsContext;
  int mWidth;
  int mHeight;

  public ImagePane(int pWidth, int pHeight) {
    super(new Canvas(pWidth, pHeight));
    mWidth = pWidth;
    mHeight = pHeight;

    mGraphicsContext = ((Canvas)this.getChildren().get(0)).getGraphicsContext2D();
  }

  public ImagePane(int pWidth, int pHeight, Image pImage) {
    this(pWidth, pHeight);
    setImage(pImage);
  }

  public ImagePane(Image pImage) {
    this((int)pImage.getWidth(), (int)pImage.getHeight());
    setImage(pImage);
  }
  /*
  public GraphicsContext getGraphicsContext() {
    return mGraphicsContext;
  }
  */

  public void setImage(Image pImage){
    mGraphicsContext.drawImage(pImage, 0, 0, mWidth, mHeight);
  }
}
