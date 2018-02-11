package xwing.copilot.gui.steps.step4automaticcalibrationwithsample;

import javafx.scene.Node;
import xwing.copilot.CopilotDevice;
import xwing.copilot.gui.steps.StepFactoryBase;
import xwing.copilot.gui.steps.StepFactoryInterface;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * February 2018
 */
public class Step4AutomaticCalibrationWithSampleFactory extends
                                                    StepFactoryBase implements
                                                                    StepFactoryInterface
{
  public Step4AutomaticCalibrationWithSampleFactory()
  {
    super("Step 4: Automatic calibration with sample");
  }

  public Node getStep(CopilotDevice pCopilotDevice) {
    return new Step4AutomaticCalibrationWithSample(pCopilotDevice);
  }
}
