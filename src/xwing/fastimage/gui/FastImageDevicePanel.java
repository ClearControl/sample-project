package xwing.fastimage.gui;

import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.gui.jfx.var.textfield.NumberVariableTextField;
import javafx.scene.control.Button;
import xwing.fastimage.FastImageDevice;


/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * February 2018
 */
public class FastImageDevicePanel extends CustomGridPane
{
  int mRow=0;

  public FastImageDevicePanel(FastImageDevice pFastImageDevice) {
    addDoubleVariableTextField(pFastImageDevice.getDetectionArmZ());
    addDoubleVariableTextField(pFastImageDevice.getIlluminationArmZ());
    addDoubleVariableTextField(pFastImageDevice.getExposureTimeInSeconds());
    addDoubleVariableTextField(pFastImageDevice.getTransitionTimeInSeconds());
    addIntegerVariableTextField(pFastImageDevice.getDetectionArmIndex());
    addIntegerVariableTextField(pFastImageDevice.getIlluminationArmIndex());
    addIntegerVariableTextField(pFastImageDevice.getImageSize());
    addIntegerVariableTextField(pFastImageDevice.getNumberOfImages());

    Button lStartButton = new Button("Start");
    lStartButton.setOnAction((e)->{
      pFastImageDevice.execute();
    });
    add(lStartButton, 0, mRow);
    mRow++;
  }

  private void addDoubleVariableTextField(BoundedVariable<Double> pVariable) {
    NumberVariableTextField<Double>
        lField = new NumberVariableTextField<Double>(pVariable.getName(), pVariable);
    this.add(lField.getLabel(), 0, mRow);
    this.add(lField.getTextField(), 1, mRow);
    mRow++;
  }

  private void addIntegerVariableTextField(BoundedVariable<Integer> pVariable) {
    NumberVariableTextField<Integer> lField = new NumberVariableTextField<Integer>(pVariable.getName(), pVariable);
    this.add(lField.getLabel(), 0, mRow);
    this.add(lField.getTextField(), 1, mRow);
    mRow++;
  }


}
