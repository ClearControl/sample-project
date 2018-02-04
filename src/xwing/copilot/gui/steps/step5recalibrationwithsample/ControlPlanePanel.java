package xwing.copilot.gui.steps.step5recalibrationwithsample;

import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.gui.jfx.var.textfield.NumberVariableTextField;
import clearcontrol.microscope.lightsheet.LightSheetDOF;
import clearcontrol.microscope.lightsheet.adaptive.controlplanestate.gui.ControlPlaneStatePanel;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * February 2018
 */
public class ControlPlanePanel extends CustomGridPane
{
  InterpolatedAcquisitionState mInterpolatedAcquisitionState;
  int mControlPlaneIndex;

  int mRow = 0;
  int mPanelWidth = 60;

  public ControlPlanePanel(InterpolatedAcquisitionState pInterpolatedAcquisitionState, Variable<Integer> pControlPlaneIndexVariable, int pLightSheetIndex) {
    mInterpolatedAcquisitionState = pInterpolatedAcquisitionState;
    mControlPlaneIndex = pControlPlaneIndexVariable.get();

    pControlPlaneIndexVariable.addSetListener((oldValue, newValue) -> {
      mControlPlaneIndex = pControlPlaneIndexVariable.get();
    });

    double lDeltaZ = 1.0;
    double lDeltaA = 0.5 / 180.0 * Math.PI;

    addDOFButtons(LightSheetDOF.IZ, pControlPlaneIndexVariable, pLightSheetIndex, lDeltaZ);
    addDOFButtons(LightSheetDOF.IA, pControlPlaneIndexVariable, pLightSheetIndex, lDeltaA);

    this.setGap(0);
    this.setMaxWidth(mPanelWidth);
    this.setAlignment(Pos.TOP_CENTER);
  }

  private void addDOFButtons(LightSheetDOF pDOF, Variable<Integer> pControlPlaneIndexVariable, int pLightSheetIndex, double lDelta) {

    BoundedVariable<Double> lVariable =
        new BoundedVariable<Double>(pDOF.name(),
                                    mInterpolatedAcquisitionState.getInterpolationTables().get(pDOF,
                                                                                               mControlPlaneIndex,
                                                                                               pLightSheetIndex),
                                    -Double.MAX_VALUE,
                                    Double.MAX_VALUE,
                                    lDelta / 100);
    lVariable.addSetListener((oldValue, newValue) -> {
      mInterpolatedAcquisitionState.getInterpolationTables().set(
          pDOF,
          mControlPlaneIndex,
          pLightSheetIndex,
          newValue);
    });


    pControlPlaneIndexVariable.addSetListener((oldValue, newValue) -> {
      mControlPlaneIndex = pControlPlaneIndexVariable.get();
      lVariable.set(mInterpolatedAcquisitionState.getInterpolationTables().get(
          pDOF,
          mControlPlaneIndex,
          pLightSheetIndex));
    });



    NumberVariableTextField
        lField =
        new NumberVariableTextField(lVariable.getName(),
                                    lVariable);
    lField.getTextField().setMaxWidth(mPanelWidth);



    Button lPlusButton = new Button(pDOF.name() + "+");
    lPlusButton.setOnAction((e) -> {
      lVariable.set(mInterpolatedAcquisitionState.getInterpolationTables().get(pDOF,
                                                                               mControlPlaneIndex,
                                                                               pLightSheetIndex) + lDelta);
    });
    lPlusButton.setMaxWidth(mPanelWidth);

    Button lMinusButton = new Button(pDOF.name() + "-");
    lMinusButton.setOnAction((e) -> {
      lVariable.set(mInterpolatedAcquisitionState.getInterpolationTables().get(pDOF,
                                                                               mControlPlaneIndex,
                                                                               pLightSheetIndex) - lDelta);
    });
    lMinusButton.setMaxWidth(mPanelWidth);

    add(lPlusButton, 0, mRow);
    mRow++;
    add(lField.getTextField(), 0, mRow);
    mRow++;
    add(lMinusButton, 0, mRow);
    mRow++;
    add(new Label(" "), 0, mRow);
    mRow++;

  }
}
