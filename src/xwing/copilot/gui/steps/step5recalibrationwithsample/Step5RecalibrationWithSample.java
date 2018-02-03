package xwing.copilot.gui.steps.step5recalibrationwithsample;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.gui.jfx.custom.image.CachedImagePaneRefreshFeature;
import clearcontrol.gui.jfx.custom.image.ImagePane;
import clearcontrol.gui.jfx.custom.image.RGBImgImage;
import clearcontrol.gui.jfx.var.textfield.NumberVariableTextField;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.util.Duration;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
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

  private int mImagePaneSize = 256;

  private CalibrationImagerDevice mCalibrationImagerDevice;

  private BoundedVariable<Integer> mCameraChoice;
  BoundedVariable<Double> mZVariable;

  public Step5RecalibrationWithSample(CopilotDevice pCopilotDevice)
  {
    int lRow = 0;
    mCopilotDevice = pCopilotDevice;

    mCameraChoice =
        new BoundedVariable<Integer>("Camera choice",
                                     0,
                                     0,
                                     pCopilotDevice.getXWingMicroscope()
                                                   .getNumberOfDetectionArms());

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

    mCalibrationImagerDevice =
        pCopilotDevice.getXWingMicroscope()
                      .getDevice(CalibrationImagerDevice.class, 0);

    // Introductory text
    {
      String
          lIntroductionText =
          "Recalibration might be neccessary after a sample has been put in the chamber. Follow the instructions below to recalibrate imaging for each control plane.";

      Label lLabel = new Label(lIntroductionText);
      add(lLabel, 0, lRow, 4, 1);
      lRow++;
    }

    {
      LaserIcon lLaserIcon = new LaserIcon(75, 75);
      add(lLaserIcon, 0, lRow, 1, 3);
      //lRow++;

      Button lLaserOnButton = new Button("Turn laser on");
      lLaserOnButton.setOnAction((a) -> {
        mCopilotDevice.imagingLaserOn();
      });
      add(lLaserOnButton, 1, lRow);

      Button lLaserOffButton = new Button("Turn all lasers off");
      lLaserOffButton.setClip(new LaserIcon(25, 25));
      lLaserOffButton.setOnAction((a) -> {
        mCopilotDevice.allLasersOff();
      });
      add(lLaserOffButton, 2, lRow);
      lRow++;

      Button lLaserFullPowerButton = new Button("Laser mild power");
      lLaserFullPowerButton.setOnAction((a) -> {
        mCopilotDevice.imagingLaserMildPower();
      });
      add(lLaserFullPowerButton, 1, lRow);
      lRow++;
    }

    {
      add(new Label("Camera"), 0, lRow);
      NumberVariableTextField
          lField =
          new NumberVariableTextField(mCameraChoice.getName(),
                                      mCameraChoice);
      add(lField.getTextField(), 1, lRow);
      lRow++;
    }

    {
      Button lToggleAutoRefreshTimer = new Button("Imaging on/off");
      lToggleAutoRefreshTimer.setOnAction((a) -> {
        toggleAutoRefreshTimer();
      });
      add(lToggleAutoRefreshTimer, 1, lRow);
      lRow++;
    }

    mTopLeftImagePane =
        new ImagePane(mImagePaneSize, mImagePaneSize);
    mTopRightImagePane =
        new ImagePane(mImagePaneSize, mImagePaneSize);
    mBottomLeftImagePane =
        new ImagePane(mImagePaneSize, mImagePaneSize);
    mBottomRightImagePane =
        new ImagePane(mImagePaneSize, mImagePaneSize);
    mTopImagePane = new ImagePane(mImagePaneSize / 2, mImagePaneSize);
    mLeftImagePane = new ImagePane(mImagePaneSize, mImagePaneSize / 2);
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
    lImagesGridPane.add(mTopLeftImagePane, 0, 0);
    lImagesGridPane.add(mTopImagePane, 1, 0);
    lImagesGridPane.add(mTopRightImagePane, 2, 0);
    lImagesGridPane.add(mLeftImagePane, 0, 1);
    lImagesGridPane.add(mRightImagePane, 2, 1);
    lImagesGridPane.add(mBottomLeftImagePane, 0, 2);
    lImagesGridPane.add(mBottomImagePane, 1, 2);
    lImagesGridPane.add(mBottomRightImagePane, 2, 2);

    lScrollPane.setContent(lImagesGridPane);

    this.add(lScrollPane, 0, lRow, 4, 1);
    lRow++;
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
                           Intervals.createMinSize(0, 0, 0,
                                                   1024, 1024, 1),
                           Intervals.createMinSize(0,
                                                   1024,
                                                   0,
                                                   1024,
                                                   1024,
                                                   1) };

      Interval lTopOverlapInterval = Intervals.createMinSize(768, 0, 0, 512, 1024, 1);


      Interval lBottomOverlapInterval = Intervals.createMinSize(768, 1024, 0, 512, 1024, 1);
      Interval lLeftOverlapInterval = Intervals.createMinSize(0, 768, 0, 1024, 512, 1);
      Interval lRightOverlapInterval = Intervals.createMinSize(1024, 768, 0, 1024, 512, 1);

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


                info("lTopOverlapInterval image " + lTopOverlapInterval.max(0) + "/" + lTopOverlapInterval.max(1));
                info("lBottomOverlapInterval image " + lBottomOverlapInterval.max(0) + "/" + lBottomOverlapInterval.max(1));
                info("lLeftOverlapInterval image " + lLeftOverlapInterval.max(0) + "/" + lLeftOverlapInterval.max(1));
                info("lRightOverlapInterval image " + lRightOverlapInterval.max(0) + "/" + lRightOverlapInterval.max(1));

                info("lTopOverlapInterval a image " + lTopOverlapInterval.max(0) + "/" + lTopOverlapInterval.max(1));

                cachedRefreshDualChannelImagePane(mTopImagePane,
                                                  mCalibrationImagerDevice
                                                      .getImage(
                                                          mCameraChoice
                                                              .get(), 1),
                                                  mCalibrationImagerDevice
                                                      .getImage(
                                                          mCameraChoice
                                                              .get(), 2),
                                                  lTopOverlapInterval
                );
                info("lTopOverlapInterval b image " + lTopOverlapInterval.max(0) + "/" + lTopOverlapInterval.max(1));

                cachedRefreshDualChannelImagePane(mBottomImagePane,
                                                  mCalibrationImagerDevice
                                                      .getImage(
                                                          mCameraChoice
                                                              .get(), 0),
                                                  mCalibrationImagerDevice
                                                      .getImage(
                                                          mCameraChoice
                                                              .get(), 3),
                                                  lBottomOverlapInterval
                );

                cachedRefreshDualChannelImagePane(mLeftImagePane,
                                                  mCalibrationImagerDevice
                                                      .getImage(
                                                          mCameraChoice
                                                              .get(), 2),
                                                  mCalibrationImagerDevice
                                                      .getImage(
                                                          mCameraChoice
                                                              .get(), 3),
                                                  lLeftOverlapInterval
                );

                cachedRefreshDualChannelImagePane(mRightImagePane,
                                                  mCalibrationImagerDevice
                                                      .getImage(
                                                          mCameraChoice
                                                              .get(), 0),
                                                  mCalibrationImagerDevice
                                                      .getImage(
                                                          mCameraChoice
                                                              .get(), 1),
                                                  lRightOverlapInterval
                );
              }

            });
          })); mTimeline.setCycleCount(Animation.INDEFINITE);
      mTimeline.play();
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
    if (pRAI1 == null || pRAI2 == null) {
      return;
    }
    if (((!mCache1.containsKey(pImagePane))
         || mCache1.get(pImagePane) != pRAI1)
        || ((!mCache2.containsKey(pImagePane))
            || mCache2.get(pImagePane) != pRAI2))
    {
      mCache1.put(pImagePane, pRAI1);
      mCache2.put(pImagePane, pRAI2);


      info("pInterval image " + pInterval.max(0) + "/" + pInterval.max(1));

      RandomAccessibleInterval<UnsignedShortType>
          lCroppedRAI1 =
          pRAI1;
      if (pRAI1.min(0) != pInterval.min(0)
          || pRAI1.max(0) != pInterval.max(0)
          || pRAI1.min(1) != pInterval.min(1)
          || pRAI1.max(1) != pInterval.max(1))
      {
        lCroppedRAI1 = Views.interval(pRAI1, pInterval);

        info("lCroppedRAI1 image " + lCroppedRAI1.max(0) + "/" + lCroppedRAI1.max(1));
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
        info("lCroppedRAI2 image " + lCroppedRAI2.max(0) + "/" + lCroppedRAI2.max(1));

      }

      RandomAccessibleInterval<UnsignedShortType>
          lDualChannelImage =
          Views.concatenate(2, lCroppedRAI1, lCroppedRAI2);

      info("Dualchannel image " + lDualChannelImage.max(0) + "/" + lDualChannelImage.max(1));

      pImagePane.setImage(new RGBImgImage<UnsignedShortType>(
          lDualChannelImage));
    }
  }

}
