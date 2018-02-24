package xwing.copilot.gui.steps.step1manualcalibration;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.gui.jfx.custom.image.CachedImagePaneRefreshFeature;
import clearcontrol.gui.jfx.custom.image.ImagePane;
import clearcontrol.gui.jfx.custom.image.RGBImgImage;
import clearcontrol.gui.jfx.var.slider.VariableSlider;
import clearcontrol.gui.jfx.var.textfield.NumberVariableTextField;
import clearcontrol.microscope.lightsheet.imaging.DirectImageStack;
import clearcontrol.stack.OffHeapPlanarStack;
import clearcontrol.stack.imglib2.StackToImgConverter;
import de.mpicbg.rhaase.spimcat.postprocessing.fijiplugins.imagemath.Sampler;
import de.mpicbg.rhaase.spimcat.postprocessing.fijiplugins.projection.ArgMaxProjection;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.util.Duration;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import xwing.copilot.CopilotDevice;
import xwing.icon.LaserIcon;
import xwing.imaging.CalibrationImagerDevice;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * February 2018
 */
public class Step1ManualCalibration extends CustomGridPane implements
                                                           LoggingFeature,
                                                           CachedImagePaneRefreshFeature
{
  private CopilotDevice mCopilotDevice;

  int mImagePaneSize = 300;

  private CalibrationImagerDevice mCalibrationImagerDevice;
  private ImagePane mC0L01ImagePane;
  private ImagePane mC0L23ImagePane;
  private ImagePane mC1L01ImagePane;
  private ImagePane mC1L23ImagePane;

  ImagePane mTopMaximumProjectionImagePane;
  ImagePane mSideMaximumProjectionImagePane;
  ImagePane mFrontMaximumProjectionImagePane;


  BoundedVariable<Double> lZVariable;

  public Step1ManualCalibration(CopilotDevice pCopilotDevice)
  {
    setAlignment(Pos.TOP_LEFT);

    int lRow = 0;
    mCopilotDevice = pCopilotDevice;

    // Introductory text
    {
      String
          lIntroductionText =
          "Initial manual calibration will allow you to align focus light sheets and align cameras to each other. Follow the buttons and instructions from top to bottom.";

      Label lLabel = new Label(lIntroductionText);
      lLabel.setWrapText(true);
      lLabel.setMaxWidth(150);
      add(lLabel, 0, lRow);
      lRow++;
    }

    // turn laser on
    {
      Button lLaserOnButton = new Button("Turn calibration laser on");
      lLaserOnButton.setMaxWidth(Double.MAX_VALUE);
      lLaserOnButton.setGraphic(new LaserIcon(25, 25));
      lLaserOnButton.setOnAction((a) -> {
        mCopilotDevice.calibrationLaserOn();
      });
      add(lLaserOnButton, 0, lRow);
      lRow++;

      Button lLaserFullPowerButton = new Button("Laser full power");
      lLaserFullPowerButton.setMaxWidth(Double.MAX_VALUE);
      lLaserFullPowerButton.setOnAction((a) -> {
        mCopilotDevice.calibrationLaserFullPower();
      });
      add(lLaserFullPowerButton, 0, lRow);
      lRow++;

      Button lLightSheetHeightZeroButton = new Button("All light sheets height = 0");
      lLightSheetHeightZeroButton.setMaxWidth(Double.MAX_VALUE);
      lLightSheetHeightZeroButton.setOnAction((a) -> {
        mCopilotDevice.allLightSheetsHeightZero();
      });
      add(lLightSheetHeightZeroButton, 0, lRow);
      lRow++;
    }


    {
      BoundedVariable<Number> lScopeZVariable = mCopilotDevice.getZVariable();

      double lMin = lScopeZVariable.getMin().doubleValue();
      double lMax = lScopeZVariable.getMax().doubleValue();

      lZVariable =
          new BoundedVariable<Double>("Z",
                                      (lMin + lMax) / 2.0,
                                      lMin,
                                      lMax,
                                      1.0);

      mCalibrationImagerDevice =
          pCopilotDevice.getXWingMicroscope()
                        .getDevice(CalibrationImagerDevice.class, 0);
      //new CalibrationImagerDevice(pCopilotDevice.getXWingMicroscope(), lZVariable);

      final VariableSlider<Double> lSlider =
          new VariableSlider<Double>(lZVariable.getName(),
                                     lZVariable,
                                     lZVariable.getMinVariable(),
                                     lZVariable.getMaxVariable(),
                                     lZVariable.getGranularityVariable(),
                                     10.0);

      lSlider.getSlider().setPrefWidth(400);
      lSlider.getSlider().setMinWidth(400);
      lSlider.getSlider().setMaxWidth(Double.MAX_VALUE);

      GridPane.setHgrow(lSlider.getSlider(), Priority.ALWAYS);

      add(lSlider.getLabel(), 1, 0);
      add(lSlider.getSlider(), 2, 0);
      add(lSlider.getTextField(), 3, 0);

      lZVariable.addSetListener((oldValue, newValue) -> {
        mCopilotDevice.setMicroscopeZ(newValue);
      });
    }

    {
      CustomGridPane lGridPane = new CustomGridPane();
      /*ArrayList<Stack2DDisplay> lDisplayList = pCopilotDevice.getXWingMicroscope().getDevices(Stack2DDisplay.class);

      mMultiChannelStack2DViewer = new MultiChannelStack2DViewer(lDisplayList);
      mMultiChannelStack2DViewer.setMaxHeight(400);
      mMultiChannelStack2DViewer.setMaxWidth(400);
      this.add(mMultiChannelStack2DViewer, 0, lRow);*/

      lGridPane.add(new Label("C0 L0/L1"), 0, 0);
      lGridPane.add(new Label("C0 L2/L3"), 1, 0);
      lGridPane.add(new Label("C1 L0/L1"), 2, 0);
      lGridPane.add(new Label("C1 L2/L3"), 3, 0);

      mC0L01ImagePane = new ImagePane(mImagePaneSize, mImagePaneSize);
      lGridPane.add(mC0L01ImagePane, 0, 1);
      mC0L23ImagePane = new ImagePane(mImagePaneSize, mImagePaneSize);
      lGridPane.add(mC0L23ImagePane, 1, 1);
      mC1L01ImagePane = new ImagePane(mImagePaneSize, mImagePaneSize);
      lGridPane.add(mC1L01ImagePane, 2, 1);
      mC1L23ImagePane = new ImagePane(mImagePaneSize, mImagePaneSize);
      lGridPane.add(mC1L23ImagePane, 3, 1);

      {
        Button lRequestImages = new Button("Request images");
        lRequestImages.setMaxWidth(Double.MAX_VALUE);
        lRequestImages.setOnAction((a) -> {
          mCalibrationImagerDevice.loop();
          mCalibrationImagerDevice.loop();
          mCalibrationImagerDevice.loop();
          mCalibrationImagerDevice.loop();
          cachedImagePaneRefresh(mC0L01ImagePane, mCalibrationImagerDevice.getImageC0L01());
          cachedImagePaneRefresh(mC0L23ImagePane, mCalibrationImagerDevice.getImageC0L23());
          cachedImagePaneRefresh(mC1L01ImagePane, mCalibrationImagerDevice.getImageC1L01());
          cachedImagePaneRefresh(mC1L23ImagePane, mCalibrationImagerDevice.getImageC1L23());
        });
        lGridPane.add(lRequestImages, 0, 2);

        Button lZoomInButton = new Button("Zoom in");
        lZoomInButton.setMaxWidth(Double.MAX_VALUE);
        lZoomInButton.setOnAction((a) -> {
          zoomIn();
          lRequestImages.getOnAction().handle(null);
        });
        lGridPane.add(lZoomInButton, 1, 2);

        Button lZoomOutButton = new Button("Zoom out");
        lZoomOutButton.setMaxWidth(Double.MAX_VALUE);
        lZoomOutButton.setOnAction((a) -> {
          zoomOut();
          lRequestImages.getOnAction().handle(null);
        });
        lGridPane.add(lZoomOutButton, 2, 2);
      }

      {
        Separator lSeparator = new Separator();
        lSeparator.setOrientation(Orientation.HORIZONTAL);
        lGridPane.add(lSeparator, 0, 3, 4, 1);
      }


      buildMaximumProjectionPane(lGridPane);


      this.add(lGridPane, 1, 2, 3, 20);
    }

    {
      Separator lSeparator = new Separator();
      lSeparator.setOrientation(Orientation.HORIZONTAL);
      add(lSeparator, 0, lRow);
      lRow++;
    }

    {
      Label lLabel = new Label(
          "After aligning the crosses, reset the lasers using the following buttons and continue to the next step.");
      lLabel.setWrapText(true);
      lLabel.setMaxWidth(150);
      add(lLabel,0, lRow, 4, 1);
      lRow++;
    }

    {
      Button lLaserOffButton = new Button("Turn all lasers off");
      lLaserOffButton.setMaxWidth(Double.MAX_VALUE);
      lLaserOffButton.setGraphic(new LaserIcon(25, 25));
      lLaserOffButton.setOnAction((a) -> {
        mCopilotDevice.allLasersOff();
      });
      add(lLaserOffButton, 0, lRow);
      lRow++;

      Button lLaserFullPowerButton = new Button("Calibration laser zero power");
      lLaserFullPowerButton.setMaxWidth(Double.MAX_VALUE);
      lLaserFullPowerButton.setOnAction((a) -> {
        mCopilotDevice.calibrationLaserZeroPower();
      });
      add(lLaserFullPowerButton, 0, lRow);
      lRow++;

      Button lLightSheetHeightZeroButton = new Button("All light sheets to full height");
      lLightSheetHeightZeroButton.setMaxWidth(Double.MAX_VALUE);
      lLightSheetHeightZeroButton.setOnAction((a) -> {
        mCopilotDevice.allLightSheetsFullHeight();
      });
      add(lLightSheetHeightZeroButton, 0, lRow);
      lRow++;


    }
  }

  private void buildMaximumProjectionPane(CustomGridPane lGridPane)
  {

    BoundedVariable<Number> lZVariable = mCopilotDevice.getZVariable();
    int lNumberOfSteps = 128;
    double lStepZDistance = (lZVariable.getMax().doubleValue() - lZVariable.getMin().doubleValue()) / lNumberOfSteps;

    lGridPane.add(new Label("Front view"), 0, 4);
    lGridPane.add(new Label("Top view"), 1, 4);
    lGridPane.add(new Label("Side view"), 2, 4);

    mFrontMaximumProjectionImagePane = new ImagePane(mImagePaneSize, mImagePaneSize);
    lGridPane.add(mFrontMaximumProjectionImagePane, 0, 5);
    mTopMaximumProjectionImagePane = new ImagePane(mImagePaneSize, mImagePaneSize);
    lGridPane.add(mTopMaximumProjectionImagePane, 1, 5);
    mSideMaximumProjectionImagePane = new ImagePane(mImagePaneSize, mImagePaneSize);
    lGridPane.add(mSideMaximumProjectionImagePane, 2, 5);

    BoundedVariable<Integer> lDetectionArmVariable = new BoundedVariable<Integer>("", 0, 0, mCopilotDevice.getXWingMicroscope().getNumberOfDetectionArms());
    BoundedVariable<Integer> lIlluminationArmVariable = new BoundedVariable<Integer>("", 0, 0, mCopilotDevice.getXWingMicroscope().getNumberOfLightSheets());
    BoundedVariable<Double> lExposureTimeVariable = new BoundedVariable<Double>("", 0.05, 0.0, Double.MAX_VALUE, 0.01);


    lGridPane.add(new Label("Illumination arm"), 0, 6);
    lGridPane.add(new NumberVariableTextField<Integer>("", lIlluminationArmVariable), 1, 6);

    lGridPane.add(new Label("Detection arm"), 0, 7);
    lGridPane.add(new NumberVariableTextField<Integer>("", lDetectionArmVariable), 1, 7);

    lGridPane.add(new Label("Exposure time (sec)"), 0, 8);
    lGridPane.add(new NumberVariableTextField<Double>("", lExposureTimeVariable), 1, 8);

    Variable<RandomAccessibleInterval<UnsignedShortType>> lLastStackVariable = new Variable<RandomAccessibleInterval<UnsignedShortType>>("", null);

    EventHandler<ActionEvent> lEventHandler = new EventHandler<ActionEvent>()
    {
      @Override public void handle(ActionEvent event)
      {
        DirectImageStack lDirectImageStack = new DirectImageStack(mCopilotDevice.getXWingMicroscope());
        lDirectImageStack.setExposureTimeInSeconds(lExposureTimeVariable.get());
        lDirectImageStack.setImageWidth(2048);
        lDirectImageStack.setImageHeight(2048);
        lDirectImageStack.setLightSheetIndex(lIlluminationArmVariable.get());
        lDirectImageStack.setDetectionArmIndex(lDetectionArmVariable.get());
        lDirectImageStack.setDetectionZ(lZVariable.getMin().doubleValue());
        lDirectImageStack.setIlluminationZ(lZVariable.getMin().doubleValue());
        lDirectImageStack.setNumberOfRequestedImages(lNumberOfSteps);
        lDirectImageStack.setIlluminationZStepDistance(lStepZDistance);
        lDirectImageStack.setDetectionZStepDistance(lStepZDistance);
        OffHeapPlanarStack lStack = lDirectImageStack.acquire();
        RandomAccessibleInterval<UnsignedShortType> lRAI = new StackToImgConverter<UnsignedShortType>(lStack).getRandomAccessibleInterval();
        lLastStackVariable.set(lRAI);
        info("Stack before resampling: " + lRAI.dimension(0) + "/" + lRAI.dimension(1) + "/" + lRAI.dimension(2));

        lRAI = new Sampler(lRAI, new double[]{
            ((double)mImagePaneSize) / 2048.0,
            ((double)mImagePaneSize) / 2048.0,
            ((double)mImagePaneSize) / ((double)lNumberOfSteps)
        }).getSampledImage();
        info("Stack after resampling: " + lRAI.dimension(0) + "/" + lRAI.dimension(1) + "/" + lRAI.dimension(2));

        {
          RandomAccessibleInterval<UnsignedShortType>
              lRotatedRAI =
              Views.permute(lRAI, 1, 2);
          ArgMaxProjection<UnsignedShortType>
              lArgMaxProjection =
              new ArgMaxProjection<>(lRotatedRAI);
          RandomAccessibleInterval<FloatType>
              lProjectedRAI =
              lArgMaxProjection.getMaxProjection();
          RGBImgImage<FloatType> lRGBImage = new RGBImgImage<>(lProjectedRAI);
          mTopMaximumProjectionImagePane.setImage(lRGBImage);
        }

        {
          RandomAccessibleInterval<UnsignedShortType>
              lRotatedRAI =
              Views.permute(lRAI, 0, 2);
          ArgMaxProjection<UnsignedShortType>
              lArgMaxProjection =
              new ArgMaxProjection<>(lRotatedRAI);
          RandomAccessibleInterval<FloatType>
              lProjectedRAI =
              lArgMaxProjection.getMaxProjection();
          RGBImgImage<FloatType> lRGBImage = new RGBImgImage<>(lProjectedRAI);
          mSideMaximumProjectionImagePane.setImage(lRGBImage);
        }


        {
          ArgMaxProjection<UnsignedShortType>
              lArgMaxProjection =
              new ArgMaxProjection<>(lRAI);
          RandomAccessibleInterval<FloatType>
              lProjectedRAI =
              lArgMaxProjection.getMaxProjection();
          RGBImgImage<FloatType> lRGBImage = new RGBImgImage<>(lProjectedRAI);
          mFrontMaximumProjectionImagePane.setImage(lRGBImage);
        }
      }
    };


    Button lRequestProjectionImagesButton = new Button("Request images");
    lRequestProjectionImagesButton.setOnAction(lEventHandler);
    lGridPane.add(lRequestProjectionImagesButton, 0, 9);


    Button lShowStackButton = new Button("Show last stack");
    lShowStackButton.setOnAction((e)->{
      if (lLastStackVariable.get() == null) {
        lEventHandler.handle(null);
      }
      mCopilotDevice.showImageJ();
      //new Duplicator().run(ImageJFunctions.wrap(lLastStackVariable.get(), "")).show();
      ImageJFunctions.wrap(lLastStackVariable.get(), "").show();
    });
    lGridPane.add(lShowStackButton, 1, 9);

  }

  Timeline mTimeline = null;


  private synchronized void toggleAutoRefreshTimer() {
    if (mTimeline != null) {
      mTimeline.stop();
      mTimeline = null;
      mCalibrationImagerDevice.stopTask();
    } else {
      mTimeline = new Timeline(new KeyFrame(Duration.millis(500), (ae) -> {
        cachedImagePaneRefresh(mC0L01ImagePane, mCalibrationImagerDevice.getImageC0L01());
        cachedImagePaneRefresh(mC0L23ImagePane, mCalibrationImagerDevice.getImageC0L23());
        cachedImagePaneRefresh(mC1L01ImagePane, mCalibrationImagerDevice.getImageC1L01());
        cachedImagePaneRefresh(mC1L23ImagePane, mCalibrationImagerDevice.getImageC1L23());

      }));
      mTimeline.setCycleCount(Animation.INDEFINITE);
      mTimeline.play();

      mCalibrationImagerDevice.setInterpolatedAcquisitionState(null);
      mCalibrationImagerDevice.setZVariable(lZVariable);
      mCalibrationImagerDevice.startTask();
    }
  }

  private void zoomIn() {
    mCalibrationImagerDevice.zoomIn();
  }

  private void zoomOut() {
    mCalibrationImagerDevice.zoomOut();
  }

}
