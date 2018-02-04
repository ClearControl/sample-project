package xwing.copilot.gui.steps.step1manualcalibration;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.devices.lasers.LaserDeviceInterface;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.gui.jfx.custom.image.CachedImagePaneRefreshFeature;
import clearcontrol.gui.jfx.custom.image.ImagePane;
import clearcontrol.gui.jfx.custom.image.RGBImgImage;
import clearcontrol.gui.jfx.var.slider.VariableSlider;
import clearcontrol.microscope.lightsheet.component.lightsheet.LightSheetInterface;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.util.Duration;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import xwing.XWingMicroscope;
import xwing.copilot.CopilotDevice;
import xwing.icon.LaserIcon;
import xwing.imaging.CalibrationImagerDevice;

import java.util.HashMap;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * February 2018
 */
public class Step1ManualCalibration extends CustomGridPane implements
                                                           LoggingFeature,
                                                           CachedImagePaneRefreshFeature
{
  private CopilotDevice mCopilotDevice;

  private CalibrationImagerDevice mCalibrationImagerDevice;
  private ImagePane mC0L01ImagePane;
  private ImagePane mC0L23ImagePane;
  private ImagePane mC1L01ImagePane;
  private ImagePane mC1L23ImagePane;

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
      add(lLabel, 0, lRow, 4, 1);
      lRow++;
    }

    // turn laser on
    {
      Button lLaserOnButton = new Button("Turn laser on");
      lLaserOnButton.setGraphic(new LaserIcon(25, 25));
      lLaserOnButton.setOnAction((a) -> {
        mCopilotDevice.calibrationLaserOn();
      });
      add(lLaserOnButton, 1, lRow);

      Button lLaserOffButton = new Button("Turn all lasers off");
      lLaserOffButton.setGraphic(new LaserIcon(25, 25));
      lLaserOffButton.setOnAction((a) -> {
        mCopilotDevice.allLasersOff();
      });
      add(lLaserOffButton, 2, lRow);
      lRow++;

      Button lLaserFullPowerButton = new Button("Laser full power");
      lLaserFullPowerButton.setOnAction((a) -> {
        mCopilotDevice.calibrationLaserFullPower();
      });
      add(lLaserFullPowerButton, 1, lRow);
      lRow++;

      Button lLightSheetHeightZeroButton = new Button("All light sheets height = 0");
      lLightSheetHeightZeroButton.setOnAction((a) -> {
        mCopilotDevice.allLightSheetsHeightZero();
      });
      add(lLightSheetHeightZeroButton, 1, lRow);
      lRow++;
    }

    {
      Separator lSeparator = new Separator();
      lSeparator.setOrientation(Orientation.HORIZONTAL);
      GridPane.setColumnSpan(lSeparator, 3);
      add(lSeparator, 0, lRow);
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

      add(lSlider.getLabel(), 0, lRow);
      add(lSlider.getSlider(), 1, lRow);
      add(lSlider.getTextField(), 2, lRow);

      lZVariable.addSetListener((oldValue, newValue) -> {
        mCopilotDevice.setMicroscopeZ(newValue);
      });
      lRow++;
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

      mC0L01ImagePane = new ImagePane(200, 200);
      lGridPane.add(mC0L01ImagePane, 0, 1);
      mC0L23ImagePane = new ImagePane(200, 200);
      lGridPane.add(mC0L23ImagePane, 1, 1);
      mC1L01ImagePane = new ImagePane(200, 200);
      lGridPane.add(mC1L01ImagePane, 2, 1);
      mC1L23ImagePane = new ImagePane(200, 200);
      lGridPane.add(mC1L23ImagePane, 3, 1);
      this.add(lGridPane, 0, lRow, 4, 1);
      lRow++;

      Button lToggleAutoRefreshTimer = new Button("Imaging on/off");
      lToggleAutoRefreshTimer.setOnAction((a) -> {
        toggleAutoRefreshTimer();
      });
      add(lToggleAutoRefreshTimer, 1, lRow);
      lRow++;

      Button lZoomInButton = new Button("Zoom in");
      lZoomInButton.setOnAction((a) -> {
        zoomIn();
      });
      add(lZoomInButton, 1, lRow);
      lRow++;

      Button lZoomOutButton = new Button("Zoom out");
      lZoomOutButton.setOnAction((a) -> {
        zoomOut();
      });
      add(lZoomOutButton, 1, lRow);
      lRow++;
    }

    {
      add(new Label(
          "After aligning the crosses, reset the lasers using the following buttons and continue to the next step."),0, lRow, 4, 1);
      lRow++;
    }

    {
      Button lLaserOffButton = new Button("Turn all lasers off");
      lLaserOffButton.setGraphic(new LaserIcon(25, 25));
      lLaserOffButton.setOnAction((a) -> {
        mCopilotDevice.allLasersOff();
      });
      add(lLaserOffButton, 1, lRow);
      lRow++;

      Button lLaserFullPowerButton = new Button("Calibration laser zero power");
      lLaserFullPowerButton.setOnAction((a) -> {
        mCopilotDevice.calibrationLaserZeroPower();
      });
      add(lLaserFullPowerButton, 1, lRow);
      lRow++;

      Button lLightSheetHeightZeroButton = new Button("All light sheets to full height");
      lLightSheetHeightZeroButton.setOnAction((a) -> {
        mCopilotDevice.allLightSheetsFullHeight();
      });
      add(lLightSheetHeightZeroButton, 1, lRow);
      lRow++;


    }
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
