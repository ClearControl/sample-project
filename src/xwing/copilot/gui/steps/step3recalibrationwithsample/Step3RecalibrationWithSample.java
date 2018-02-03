package xwing.copilot.gui.steps.step3recalibrationwithsample;

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
import javafx.util.Duration;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.util.Intervals;
import xwing.copilot.CopilotDevice;
import xwing.icon.LaserIcon;
import xwing.imaging.CalibrationImagerDevice;

import java.util.Random;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * February 2018
 */
public class Step3RecalibrationWithSample extends CustomGridPane implements
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

  private int mImagePaneWidth = 256;
  private int mImagePaneHeight = 256;

  private CalibrationImagerDevice mCalibrationImagerDevice;

  private BoundedVariable<Integer> mCameraChoice;
  BoundedVariable<Double> mZVariable;

  public Step3RecalibrationWithSample(CopilotDevice pCopilotDevice)
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
        new ImagePane(mImagePaneWidth, mImagePaneHeight);
    mTopRightImagePane =
        new ImagePane(mImagePaneWidth, mImagePaneHeight);
    mBottomLeftImagePane =
        new ImagePane(mImagePaneWidth, mImagePaneHeight);
    mBottomRightImagePane =
        new ImagePane(mImagePaneWidth, mImagePaneHeight);
    mTopImagePane = new ImagePane(mImagePaneWidth, mImagePaneHeight);
    mLeftImagePane = new ImagePane(mImagePaneWidth, mImagePaneHeight);
    mBottomImagePane =
        new ImagePane(mImagePaneWidth, mImagePaneHeight);
    mRightImagePane =
        new ImagePane(mImagePaneWidth, mImagePaneHeight);

    CustomGridPane lImagesGridPane = new CustomGridPane();
    lImagesGridPane.add(mTopLeftImagePane, 0, 0);
    lImagesGridPane.add(mTopImagePane, 1, 0);
    lImagesGridPane.add(mTopRightImagePane, 2, 0);
    lImagesGridPane.add(mLeftImagePane, 0, 1);
    lImagesGridPane.add(mRightImagePane, 2, 1);
    lImagesGridPane.add(mBottomLeftImagePane, 0, 2);
    lImagesGridPane.add(mBottomImagePane, 1, 2);
    lImagesGridPane.add(mBottomRightImagePane, 2, 2);
    this.add(lImagesGridPane, 0, lRow, 4, 1);
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
      ImagePane[] lListOfImagePanes = new ImagePane[] {
          mBottomRightImagePane,
          mTopRightImagePane,
          mTopLeftImagePane,
          mBottomLeftImagePane };

      // for every light sheet we cut out the part where the light sheet should enter the sample

      Interval[] lIntervals = new Interval[] {
          Intervals.createMinSize(1024,1024, 1024, 1024),
          Intervals.createMinSize(1024,0, 1024, 1024),
          Intervals.createMinSize(0,0, 1024, 1024),
          Intervals.createMinSize(0,1024, 1024, 1024)
      };

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

                  cachedImagePaneRefresh(lListOfImagePanes[i], lImage);

                }
              }

            });
          }));
      mTimeline.setCycleCount(Animation.INDEFINITE);
      mTimeline.play();
      mCalibrationImagerDevice.setZVariable(mZVariable);
      mCalibrationImagerDevice.startTask();
    }
  }

}
