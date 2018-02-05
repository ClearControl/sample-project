package xwing.copilot.gui.steps.step5recalibrationwithsample;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.VariableSetListener;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.gui.jfx.custom.image.CachedImagePaneRefreshFeature;
import clearcontrol.gui.jfx.custom.image.ImagePane;
import clearcontrol.gui.jfx.custom.image.RGBImgImage;
import clearcontrol.gui.jfx.var.checkbox.VariableCheckBox;
import clearcontrol.gui.jfx.var.textfield.NumberVariableTextField;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import ij.ImageJ;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.util.Duration;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import xwing.copilot.CopilotDevice;
import xwing.icon.LaserIcon;
import xwing.imaging.CalibrationImagerDevice;

import java.util.HashMap;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * February 2018
 */
public class Step5RecalibrationWithSample extends CustomGridPane implements
                                                                 LoggingFeature,
                                                                 CachedImagePaneRefreshFeature
{
  private CopilotDevice mCopilotDevice;

  ImagePane mTopLeftImagePane;
  ImagePane mTopRightImagePane;
  ImagePane mBottomLeftImagePane;
  ImagePane mBottomRightImagePane;
  ImagePane mTopImagePane;
  ImagePane mLeftImagePane;
  ImagePane mBottomImagePane;
  ImagePane mRightImagePane;


  int mTopLeftLightSheet = 2;
  int mTopRightLightSheet = 1;
  int mBottomLeftLightSheet = 3;
  int mBottomRightightSheet = 0;

  private int mImagePaneSize = 300;

  private CalibrationImagerDevice mCalibrationImagerDevice;

  private BoundedVariable<Integer> mCameraChoice;
  private BoundedVariable<Integer> mControlPlaneIndexVariable;
  BoundedVariable<Double> mZVariable;

  private InterpolatedAcquisitionState mInterpolatedAcqusitionState;
  Variable<Boolean> mInterpolatedAcquisitionVariable;

  public Step5RecalibrationWithSample(CopilotDevice pCopilotDevice)
  {
    setAlignment(Pos.TOP_LEFT);

    int lRow = 0;
    mCopilotDevice = pCopilotDevice;
    mInterpolatedAcqusitionState =
        (InterpolatedAcquisitionState) (pCopilotDevice.getXWingMicroscope()
                                                      .getAcquisitionStateManager()
                                                      .getCurrentState());

    mCameraChoice =
        new BoundedVariable<Integer>("Camera choice",
                                     0,
                                     0,
                                     pCopilotDevice.getXWingMicroscope()
                                                   .getNumberOfDetectionArms()
                                     - 1);

    mControlPlaneIndexVariable =
        new BoundedVariable<Integer>("Control plane index",
                                     0,
                                     0,
                                     mInterpolatedAcqusitionState.getNumberOfControlPlanes()
                                     - 1);

    mControlPlaneIndexVariable.addSetListener(new VariableSetListener<Integer>()
    {
      @Override public void setEvent(Integer pCurrentValue,
                                     Integer pNewValue)
      {
        double
            z =
            mInterpolatedAcqusitionState.getControlPlaneZ(pNewValue);
        mZVariable.set(z);
      }
    });

    BoundedVariable<Number>
        lScopeZVariable =
        mCopilotDevice.getZVariable();
    double lMin = lScopeZVariable.getMin().doubleValue();
    double lMax = lScopeZVariable.getMax().doubleValue();
    mZVariable =
        new BoundedVariable<Double>("Z",
                                    (lMin + lMax) / 2.0,
                                    lMin,
                                    lMax,
                                    1.0);

    // indirectly set Z variable
    mControlPlaneIndexVariable.set(mInterpolatedAcqusitionState.getNumberOfControlPlanes()
                                   / 2);

    mCalibrationImagerDevice =
        pCopilotDevice.getXWingMicroscope()
                      .getDevice(CalibrationImagerDevice.class, 0);

    mInterpolatedAcquisitionVariable =
        new Variable<Boolean>("Interpolated acquisition", true);
    mInterpolatedAcquisitionVariable.addSetListener((oldValue, newValue) -> {
      if (newValue)
      {
        mCalibrationImagerDevice.setInterpolatedAcquisitionState(
            mInterpolatedAcqusitionState);
      }
      else
      {
        mCalibrationImagerDevice.setInterpolatedAcquisitionState(null);
      }
    });
    mInterpolatedAcquisitionVariable.set(true);

    // Introductory text
    {
      String
          lIntroductionText =
          "Recalibration might be neccessary after a sample has been put in the chamber. Follow the instructions below to recalibrate imaging for each control plane.";

      Label lLabel = new Label(lIntroductionText);
      lLabel.setWrapText(true);
      lLabel.setMaxWidth(150);
      add(lLabel, 0, lRow);
      lRow++;
    }

    {
      Button lLaserOnButton = new Button("Turn imaging laser on");
      lLaserOnButton.setMaxWidth(Double.MAX_VALUE);
      lLaserOnButton.setGraphic(new LaserIcon(25, 25));
      lLaserOnButton.setOnAction((a) -> {
        mCopilotDevice.imagingLaserOn();
      });
      add(lLaserOnButton, 0, lRow);
      lRow++;

      Button lLaserMildPowerButton = new Button("Laser mild power");
      lLaserMildPowerButton.setMaxWidth(Double.MAX_VALUE);
      lLaserMildPowerButton.setOnAction((a) -> {
        mCopilotDevice.imagingLaserMildPower();
      });
      lLaserMildPowerButton.setMaxWidth(150);
      add(lLaserMildPowerButton, 0, lRow);
      lRow++;
    }

    {
      Separator lSeparator = new Separator();
      lSeparator.setOrientation(Orientation.HORIZONTAL);
      add(lSeparator, 0, lRow);
      lRow++;
    }

    {
      Label
          lLabel =
          new Label("Use these tools to configure the view");
      lLabel.setWrapText(true);
      lLabel.setMaxWidth(150);
      add(lLabel, 0, lRow);
      lRow++;
    }

    {
      Button lToggleAutoRefreshTimer = new Button("Imaging on/off");
      lToggleAutoRefreshTimer.setMaxWidth(Double.MAX_VALUE);
      lToggleAutoRefreshTimer.setOnAction((a) -> {
        toggleAutoRefreshTimer();
      });
      lToggleAutoRefreshTimer.setMaxWidth(150);
      add(lToggleAutoRefreshTimer, 0, lRow);
      lRow++;
    }

    {
      add(new VariableCheckBox(mInterpolatedAcquisitionVariable.getName(),
                               mInterpolatedAcquisitionVariable),
          0,
          lRow);
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

    }

    CustomGridPane lViewPositionGridPane = new CustomGridPane();
    lViewPositionGridPane.setGap(0);
    {
      lViewPositionGridPane.add(new Label("Camera"), 0, 0, 3, 1);

      NumberVariableTextField
          lField =
          new NumberVariableTextField(mCameraChoice.getName(),
                                      mCameraChoice);
      lField.getTextField().setMaxWidth(25);
      lViewPositionGridPane.add(lField.getTextField(), 2, 1);

      Button lCamera0Button = new Button("Front");
      lCamera0Button.setOnAction((a) -> {
        mCameraChoice.set(0);
      });
      lCamera0Button.setAlignment(Pos.CENTER);
      lCamera0Button.setMaxWidth(Double.MAX_VALUE);
      lViewPositionGridPane.add(lCamera0Button, 0, 1, 2, 1);

      Button lCamera1Button = new Button("Back");
      lCamera1Button.setOnAction((a) -> {
        mCameraChoice.set(1);
      });

      lCamera1Button.setAlignment(Pos.CENTER);
      lCamera1Button.setMaxWidth(Double.MAX_VALUE);
      lViewPositionGridPane.add(lCamera1Button, 3, 1, 2, 1);
    }

    {
      lViewPositionGridPane.add(new Label("Control plane"),
                                0,
                                2,
                                3,
                                1);
      NumberVariableTextField
          lField =
          new NumberVariableTextField(mControlPlaneIndexVariable.getName(),
                                      mControlPlaneIndexVariable);
      lField.getTextField().setMaxWidth(25);
      lViewPositionGridPane.add(lField.getTextField(), 2, 3);

      Button lToFirstControlPlane = new Button("|<");
      lToFirstControlPlane.setOnAction((a) -> {
        mControlPlaneIndexVariable.set(0);
      });
      lViewPositionGridPane.add(lToFirstControlPlane, 0, 3);

      Button lToPreviousControlPlane = new Button("<");
      lToPreviousControlPlane.setOnAction((a) -> {
        if (mControlPlaneIndexVariable.get()
            > mControlPlaneIndexVariable.getMin())
        {
          mControlPlaneIndexVariable.set(mControlPlaneIndexVariable.get()
                                         - 1);
        }
      });
      lViewPositionGridPane.add(lToPreviousControlPlane, 1, 3);

      Button lToNextControlPlane = new Button(">");
      lToNextControlPlane.setOnAction((a) -> {
        if (mControlPlaneIndexVariable.get()
            < mControlPlaneIndexVariable.getMax())
        {
          mControlPlaneIndexVariable.set(mControlPlaneIndexVariable.get()
                                         + 1);
        }
      });
      lViewPositionGridPane.add(lToNextControlPlane, 3, 3);

      Button lToLastControlPlane = new Button(">|");
      lToLastControlPlane.setOnAction((a) -> {
        mControlPlaneIndexVariable.set(mControlPlaneIndexVariable.getMax());
      });
      lViewPositionGridPane.add(lToLastControlPlane, 4, 3);
    }

    {
      lViewPositionGridPane.add(new Label("Z"), 0, 4, 3, 1);
      NumberVariableTextField
          lField =
          new NumberVariableTextField(mZVariable.getName(),
                                      mZVariable);
      lField.getTextField().setMaxWidth(75);
      lViewPositionGridPane.add(lField.getTextField(), 1, 5, 3, 1);

    }

    mTopLeftImagePane = new ImagePane(mImagePaneSize, mImagePaneSize);
    mTopRightImagePane =
        new ImagePane(mImagePaneSize, mImagePaneSize);
    mBottomLeftImagePane =
        new ImagePane(mImagePaneSize, mImagePaneSize);
    mBottomRightImagePane =
        new ImagePane(mImagePaneSize, mImagePaneSize);
    mTopImagePane = new ImagePane(mImagePaneSize / 2, mImagePaneSize);
    mLeftImagePane =
        new ImagePane(mImagePaneSize, mImagePaneSize / 2);
    mBottomImagePane =
        new ImagePane(mImagePaneSize / 2, mImagePaneSize);
    mRightImagePane =
        new ImagePane(mImagePaneSize, mImagePaneSize / 2);

    mTopLeftImagePane.setMinHeight(mImagePaneSize);
    mTopLeftImagePane.setMinWidth(mImagePaneSize);
    mTopImagePane.setMinWidth(mImagePaneSize / 2);
    mLeftImagePane.setMinHeight(mImagePaneSize / 2);
    mBottomRightImagePane.setMinHeight(mImagePaneSize);
    mBottomRightImagePane.setMinWidth(mImagePaneSize);

    ScrollPane lScrollPane = new ScrollPane();

    CustomGridPane lImagesGridPane = new CustomGridPane();
    lImagesGridPane.setGap(10);
    lImagesGridPane.add(mTopLeftImagePane, 1, 0);
    lImagesGridPane.add(mTopImagePane, 2, 0);
    lImagesGridPane.add(mTopRightImagePane, 3, 0);
    lImagesGridPane.add(mLeftImagePane, 1, 1);
    lImagesGridPane.add(mRightImagePane, 3, 1);
    lImagesGridPane.add(mBottomLeftImagePane, 1, 2);
    lImagesGridPane.add(mBottomImagePane, 2, 2);
    lImagesGridPane.add(mBottomRightImagePane, 3, 2);

    ControlPlanePanel
        lTopLeftPanel =
        new ControlPlanePanel(mInterpolatedAcqusitionState,
                              mControlPlaneIndexVariable,
                              mTopLeftLightSheet);
    lTopLeftPanel.setOpenExternallyAction((a)->{
      showImageJ();
      ImageJFunctions.wrap(mCalibrationImagerDevice
                               .getImage(
                                   mCameraChoice
                                       .get(),
                                   mTopLeftLightSheet), "C" + mCameraChoice + "L" + mTopLeftLightSheet).show();
    });

    ControlPlanePanel
        lBottomLeftPanel =
        new ControlPlanePanel(mInterpolatedAcqusitionState,
                              mControlPlaneIndexVariable,
                              mBottomLeftLightSheet);
    lBottomLeftPanel.setOpenExternallyAction((a)->{
      showImageJ();
      ImageJFunctions.wrap(mCalibrationImagerDevice
                               .getImage(
                                   mCameraChoice
                                       .get(),
                                   mBottomLeftLightSheet), "C" + mCameraChoice + "L" + mBottomLeftLightSheet).show();
    });

    ControlPlanePanel
        lTopRightPanel =
        new ControlPlanePanel(mInterpolatedAcqusitionState,
                              mControlPlaneIndexVariable,
                              mTopRightLightSheet);
    lTopRightPanel.setOpenExternallyAction((a)->{
      showImageJ();
      ImageJFunctions.wrap(mCalibrationImagerDevice
                               .getImage(
                                   mCameraChoice
                                       .get(),
                                   mTopRightLightSheet), "C" + mCameraChoice + "L" + mTopRightLightSheet).show();
    });

    ControlPlanePanel
        lBottomRightPanel =
        new ControlPlanePanel(mInterpolatedAcqusitionState,
                              mControlPlaneIndexVariable,
                              mBottomRightightSheet);
    lBottomRightPanel.setOpenExternallyAction((a)->{
      showImageJ();
      ImageJFunctions.wrap(mCalibrationImagerDevice
                             .getImage(
                                 mCameraChoice
                                     .get(),
                                 mBottomRightightSheet), "C" + mCameraChoice + "L" + mBottomRightightSheet).show();
  });

    lImagesGridPane.add(lTopLeftPanel, 0, 0);
    lImagesGridPane.add(lBottomLeftPanel, 0, 2);
    lImagesGridPane.add(lTopRightPanel, 4, 0);
    lImagesGridPane.add(lBottomRightPanel, 4, 2);

    lImagesGridPane.add(lViewPositionGridPane, 2, 1);

    lScrollPane.setContent(lImagesGridPane);

    lScrollPane.setMinWidth(2.5 * mImagePaneSize + 20 + 50);
    this.add(lScrollPane, 1, 0, 1, 15);

  }

  Timeline mTimeline = null;

  private synchronized void toggleAutoRefreshTimer()
  {
    if (mTimeline != null)
    {
      mTimeline.stop();
      mTimeline = null;
      mCalibrationImagerDevice.stopTask();
    }
    else
    {
      // the order here corresponds to the spatial orientation of lightsheets at the actual XWing microscope
      ImagePane[]
          lListOfImagePanes =
          new ImagePane[] { mBottomRightImagePane,
                            mTopRightImagePane,
                            mTopLeftImagePane,
                            mBottomLeftImagePane };

      // for every light sheet we cut out the part where the light sheet should enter the sample

      Interval[]
          lCornerImageIntervals =
          new Interval[] { Intervals.createMinSize(1024,
                                                   1024,
                                                   0,
                                                   1024,
                                                   1024,
                                                   1),
                           Intervals.createMinSize(1024,
                                                   0,
                                                   0,
                                                   1024,
                                                   1024,
                                                   1),
                           Intervals.createMinSize(0,
                                                   0,
                                                   0,
                                                   1024,
                                                   1024,
                                                   1),
                           Intervals.createMinSize(0,
                                                   1024,
                                                   0,
                                                   1024,
                                                   1024,
                                                   1) };

      Interval
          lTopOverlapInterval =
          Intervals.createMinSize(768, 0, 0, 512, 1024, 1);

      Interval
          lBottomOverlapInterval =
          Intervals.createMinSize(768, 1024, 0, 512, 1024, 1);
      Interval
          lLeftOverlapInterval =
          Intervals.createMinSize(0, 768, 0, 1024, 512, 1);
      Interval
          lRightOverlapInterval =
          Intervals.createMinSize(1024, 768, 0, 1024, 512, 1);

      mTimeline =
          new Timeline(new KeyFrame(Duration.millis(500), (ae) -> {
            Platform.runLater(new Runnable()
            {
              @Override public void run()
              {
                for (int i = 0; i
                                < Integer.min(mCopilotDevice.getXWingMicroscope()
                                                            .getNumberOfLightSheets(),
                                              lListOfImagePanes.length); i++)
                {
                  RandomAccessibleInterval<UnsignedShortType>
                      lImage =
                      mCalibrationImagerDevice.getImage(mCameraChoice.get(),
                                                        i);

                  cachedImagePaneRefresh(lListOfImagePanes[i],
                                         lImage,
                                         lCornerImageIntervals[i]);

                }

                cachedRefreshDualChannelImagePane(mTopImagePane,
                                                  mCalibrationImagerDevice
                                                      .getImage(
                                                          mCameraChoice
                                                              .get(),
                                                          mTopLeftLightSheet),
                                                  mCalibrationImagerDevice
                                                      .getImage(
                                                          mCameraChoice
                                                              .get(),
                                                          mTopRightLightSheet),
                                                  lTopOverlapInterval);

                cachedRefreshDualChannelImagePane(mBottomImagePane,
                                                  mCalibrationImagerDevice
                                                      .getImage(
                                                          mCameraChoice
                                                              .get(),
                                                          mBottomRightightSheet),
                                                  mCalibrationImagerDevice
                                                      .getImage(
                                                          mCameraChoice
                                                              .get(),
                                                          mBottomLeftLightSheet),
                                                  lBottomOverlapInterval);

                cachedRefreshDualChannelImagePane(mLeftImagePane,
                                                  mCalibrationImagerDevice
                                                      .getImage(
                                                          mCameraChoice
                                                              .get(),
                                                          mBottomLeftLightSheet),
                                                  mCalibrationImagerDevice
                                                      .getImage(
                                                          mCameraChoice
                                                              .get(),
                                                          mTopLeftLightSheet),
                                                  lLeftOverlapInterval);

                cachedRefreshDualChannelImagePane(mRightImagePane,
                                                  mCalibrationImagerDevice
                                                      .getImage(
                                                          mCameraChoice
                                                              .get(),
                                                          0),
                                                  mCalibrationImagerDevice
                                                      .getImage(
                                                          mCameraChoice
                                                              .get(),
                                                          1),
                                                  lRightOverlapInterval);
              }

            });
          }));
      mTimeline.setCycleCount(Animation.INDEFINITE);
      mTimeline.play();

      if (mInterpolatedAcquisitionVariable.get())
      {
        mCalibrationImagerDevice.setInterpolatedAcquisitionState(
            mInterpolatedAcqusitionState);
      }
      else
      {
        mCalibrationImagerDevice.setInterpolatedAcquisitionState(null);
      }

      mCalibrationImagerDevice.setZVariable(mZVariable);
      mCalibrationImagerDevice.startTask();
    }
  }

  private HashMap<ImagePane, RandomAccessibleInterval>
      mCache1 =
      new HashMap<>();
  private HashMap<ImagePane, RandomAccessibleInterval>
      mCache2 =
      new HashMap<>();

  private void cachedRefreshDualChannelImagePane(ImagePane pImagePane,
                                                 RandomAccessibleInterval<UnsignedShortType> pRAI1,
                                                 RandomAccessibleInterval<UnsignedShortType> pRAI2,
                                                 Interval pInterval)
  {
    if (pRAI1 == null || pRAI2 == null)
    {
      return;
    }
    if (((!mCache1.containsKey(pImagePane))
         || mCache1.get(pImagePane) != pRAI1)
        || ((!mCache2.containsKey(pImagePane))
            || mCache2.get(pImagePane) != pRAI2))
    {
      mCache1.put(pImagePane, pRAI1);
      mCache2.put(pImagePane, pRAI2);

      RandomAccessibleInterval<UnsignedShortType>
          lCroppedRAI1 =
          pRAI1;
      if (pRAI1.min(0) != pInterval.min(0)
          || pRAI1.max(0) != pInterval.max(0)
          || pRAI1.min(1) != pInterval.min(1)
          || pRAI1.max(1) != pInterval.max(1))
      {
        lCroppedRAI1 = Views.interval(pRAI1, pInterval);
      }
      RandomAccessibleInterval<UnsignedShortType>
          lCroppedRAI2 =
          pRAI2;
      if (pRAI2.min(0) != pInterval.min(0)
          || pRAI2.max(0) != pInterval.max(0)
          || pRAI2.min(1) != pInterval.min(1)
          || pRAI2.max(1) != pInterval.max(1))
      {
        lCroppedRAI2 = Views.interval(pRAI2, pInterval);
      }

      RandomAccessibleInterval<UnsignedShortType>
          lDualChannelImage =
          Views.concatenate(2, lCroppedRAI1, lCroppedRAI2);

      pImagePane.setImage(new RGBImgImage<UnsignedShortType>(
          lDualChannelImage));
    }
  }

  private static ImageJ sImageJ ;
  private void showImageJ() {
    if (sImageJ == null)
      sImageJ = new ImageJ();
    if (!sImageJ.isVisible())
      sImageJ.setVisible(true);
  }

}
