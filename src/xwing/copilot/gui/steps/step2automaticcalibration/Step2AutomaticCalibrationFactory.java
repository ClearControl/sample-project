package xwing.copilot.gui.steps.step2automaticcalibration;

import javafx.scene.Node;
import xwing.copilot.CopilotDevice;
import xwing.copilot.gui.steps.StepFactoryBase;
import xwing.copilot.gui.steps.StepFactoryInterface;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * February 2018
 */
public class Step2AutomaticCalibrationFactory extends StepFactoryBase implements
                                                                      StepFactoryInterface
{
  public Step2AutomaticCalibrationFactory()
  {
    super("Step 2: Automatic calibration");
  }

  public Node getStep(CopilotDevice pCopilotDevice) {
    return new Step2AutomaticCalibration(pCopilotDevice);
  }
}
