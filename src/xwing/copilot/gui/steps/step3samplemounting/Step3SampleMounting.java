package xwing.copilot.gui.steps.step3samplemounting;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.VariableSetListener;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.devices.stages.BasicThreeAxesStageInterface;
import clearcontrol.devices.stages.kcube.gui.BasicThreeAxesStagePanel;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.gui.jfx.var.textfield.NumberVariableTextField;
import clearcontrol.microscope.lightsheet.state.ControlPlaneLayout;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import xwing.copilot.CopilotDevice;
import xwing.icon.LaserIcon;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * February 2018
 */
public class Step3SampleMounting extends CustomGridPane implements
                                                        LoggingFeature
{
  private final InterpolatedAcquisitionState
      mInterpolatedAcqusitionState;
  CopilotDevice mCopilotDevice;

  // Control planes should not start/end directly with the sample; they are supposed to liea at least n pixel within the sample.
  int mControlPlaneZBorder = 20;

  public Step3SampleMounting(CopilotDevice pCopilotDevice) {

    setAlignment(Pos.TOP_LEFT);

    mCopilotDevice = pCopilotDevice;


    mInterpolatedAcqusitionState = (InterpolatedAcquisitionState)(pCopilotDevice.getXWingMicroscope()
                                                                                .getAcquisitionStateManager().getCurrentState());

    int lRow = 0;


    BoundedVariable<Number> lRemoteZVariable = mCopilotDevice.getXWingMicroscope().getDetectionArm(0).getZVariable();


    BoundedVariable<Double> lMinZVariable = new BoundedVariable<Double>("MinZ",
                                                                        lRemoteZVariable.getMin().doubleValue(),
                                                                        lRemoteZVariable.getMin().doubleValue(),
                                                                        lRemoteZVariable.getMax().doubleValue(),
                                                                        lRemoteZVariable.getGranularity().doubleValue());


    BoundedVariable<Double> lMaxZVariable = new BoundedVariable<Double>("MaxZ",
                                                                        lRemoteZVariable.getMax().doubleValue(),
                                                                        lRemoteZVariable.getMin().doubleValue(),
                                                                        lRemoteZVariable.getMax().doubleValue(),
                                                                        lRemoteZVariable.getGranularity().doubleValue());

    VariableSetListener<Double> lVariableListener = new VariableSetListener<Double>()
    {
      @Override public void setEvent(Double pCurrentValue,
                                     Double pNewValue)
      {
        double lMin = lMinZVariable.get();
        double lMax = lMaxZVariable.get();

        mInterpolatedAcqusitionState.getStackZLowVariable().set(lMin);
        mInterpolatedAcqusitionState.getStackZHighVariable().set(lMax);


        if (lMax - lMin > mControlPlaneZBorder * 2) {
          lMin = lMin + mControlPlaneZBorder;
          lMax = lMax - mControlPlaneZBorder;
        }

        mInterpolatedAcqusitionState.setupControlPlanes(mInterpolatedAcqusitionState.getNumberOfControlPlanes(),
                           lMin,
                           lMax,
                           ControlPlaneLayout.LinearWithDoubledCenter);
      }
    };
    lMinZVariable.addSetListener(lVariableListener);
    lMaxZVariable.addSetListener(lVariableListener);

    {
      String
          lIntroductionText =
          "Turn all lasers off, mount your sample and enter the Z range the sample is located in.";

      Label lLabel = new Label(lIntroductionText);
      lLabel.setWrapText(true);
      lLabel.setMaxWidth(150);
      add(lLabel, 0, lRow);
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




    {
      BasicThreeAxesStageInterface
          lStage = mCopilotDevice.getXWingMicroscope().getDevice(BasicThreeAxesStageInterface.class, 0);
      if (lStage != null)
      {
        BasicThreeAxesStagePanel lStagePanel = new BasicThreeAxesStagePanel(lStage);
        add(lStagePanel, 1, 1, 3, 10);
      }
    }

    {
      String
          lIntroductionText =
          "In which Z range is the sample located?";

      Label lLabel = new Label(lIntroductionText);
      lLabel.setWrapText(true);
      lLabel.setMaxWidth(150);
      add(lLabel, 0, lRow);
      lRow++;
    }


    {
      add(new Label(lMinZVariable.getName()), 0, lRow);
      lRow++;

      NumberVariableTextField
          lField =
          new NumberVariableTextField(lMinZVariable.getName(),
                                      lMinZVariable);
      lField.getTextField().setMaxWidth(Double.MAX_VALUE);
      add(lField.getTextField(), 0, lRow);
      lRow++;
    }

    {
      add(new Label(lMaxZVariable.getName()), 0, lRow);
      lRow++;

      NumberVariableTextField
          lField =
          new NumberVariableTextField(lMaxZVariable.getName(),
                                      lMaxZVariable);
      lField.getTextField().setMaxWidth(Double.MAX_VALUE);
      add(lField.getTextField(), 0, lRow);
      lRow++;
    }

  }
}
