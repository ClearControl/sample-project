package xwing.copilot.gui.steps.step2automaticcalibration;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.calibrator.CalibrationEngine;
import clearcontrol.microscope.lightsheet.configurationstate.gui.ConfigurationStatePanel;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import xwing.copilot.CopilotDevice;
import xwing.icon.LaserIcon;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * February 2018
 */
public class Step2AutomaticCalibration extends CustomGridPane implements
                                                              LoggingFeature
{
  private CopilotDevice mCopilotDevice;

  public Step2AutomaticCalibration(CopilotDevice pCopilotDevice)
  {
    setAlignment(Pos.TOP_LEFT);

    int lRow = 0;
    mCopilotDevice = pCopilotDevice;

    // Introductory text
    {
      String
          lIntroductionText =
          "Automatic calibration takes about 20 minutes. Turn the laser on and start calibration. If all steps succeeded, continue.";

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


      Button lLaserFullPowerButton = new Button("Laser full power");
      lLaserFullPowerButton.setMaxWidth(Double.MAX_VALUE);
      lLaserFullPowerButton.setOnAction((a) -> {
        mCopilotDevice.calibrationLaserFullPower();
      });
      add(lLaserFullPowerButton, 0, lRow);
      lRow++;
    }

    {
      Button
          lLightSheetHeightZeroButton =
          new Button("All light sheets height = 0");
      lLightSheetHeightZeroButton.setMaxWidth(Double.MAX_VALUE);
      lLightSheetHeightZeroButton.setOnAction((a) -> {
        mCopilotDevice.allLightSheetsHeightZero();
      });
      add(lLightSheetHeightZeroButton, 0, lRow);
      lRow++;
    }

    {
      Button lCalibrateButton = new Button("Start Calibration");
      lCalibrateButton.setMaxWidth(Double.MAX_VALUE);
      lCalibrateButton.setOnAction((a) -> {
        mCopilotDevice.startCalibration();
      });
      add(lCalibrateButton, 0, lRow);
      lRow++;
    }


    {
      Separator lSeparator = new Separator();
      lSeparator.setOrientation(Orientation.HORIZONTAL);
      add(lSeparator, 0, lRow);
      lRow++;
    }

    {
      add(new Label("Wait"), 0, lRow);
      lRow++;
    }

    {
      CalibrationEngine
          lCalibrationEngine =
          pCopilotDevice.getXWingMicroscope()
                        .getDevice(CalibrationEngine.class, 0);

      ConfigurationStatePanel
          lConfigurationStatePanel =
          new ConfigurationStatePanel(lCalibrationEngine.getModuleList(),
                                      lCalibrationEngine.getLightSheetMicroscope()
                                                        .getNumberOfLightSheets());

      TitledPane
          lTitledPane =
          new TitledPane("Calibration state",
                         lConfigurationStatePanel);
      lTitledPane.setAnimated(false);
      lTitledPane.setExpanded(true);
      GridPane.setColumnSpan(lTitledPane, 3);
      add(lTitledPane, 1, 0, 4, 10);
      lRow++;
    }


    {
      Separator lSeparator = new Separator();
      lSeparator.setOrientation(Orientation.HORIZONTAL);
      add(lSeparator, 0, lRow);
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

      Button lLaserFullPowerButton = new Button("Laser zero power");
      lLaserFullPowerButton.setMaxWidth(Double.MAX_VALUE);
      lLaserFullPowerButton.setOnAction((a) -> {
        mCopilotDevice.calibrationLaserZeroPower();
      });
      add(lLaserFullPowerButton, 0, lRow);
      lRow++;

      Button lLightSheetFullHeightButton =
          new Button("All light sheets to full height");
      lLightSheetFullHeightButton.setMaxWidth(Double.MAX_VALUE);
      lLightSheetFullHeightButton.setOnAction((a) -> {
        mCopilotDevice.allLightSheetsFullHeight();
      });
      add(lLightSheetFullHeightButton, 0, lRow);
      lRow++;
    }

  }
}
