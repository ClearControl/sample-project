package xwing.copilot.gui.steps.step1manualcalibration;

import javafx.scene.Node;
import xwing.XWingMicroscope;
import xwing.copilot.CopilotDevice;
import xwing.copilot.gui.steps.StepFactoryBase;
import xwing.copilot.gui.steps.StepFactoryInterface;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * February 2018
 */
public class Step1ManualCalibrationFactory extends StepFactoryBase implements
                                           StepFactoryInterface
{
  public Step1ManualCalibrationFactory()
  {
    super("Step 1: Manual calibration");
  }

  public Node getStep(CopilotDevice pCopilotDevice) {
    return new Step1ManualCalibration(pCopilotDevice);
  }


}
