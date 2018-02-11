package xwing.copilot.gui.steps.step5recalibrationwithsample;

import javafx.scene.Node;
import xwing.copilot.CopilotDevice;
import xwing.copilot.gui.steps.StepFactoryBase;
import xwing.copilot.gui.steps.StepFactoryInterface;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * February 2018
 */
public class Step5RecalibrationWithSampleFactory extends
                                                 StepFactoryBase implements
                                                                 StepFactoryInterface
{
  public Step5RecalibrationWithSampleFactory()
  {
    super("Step 5: Re-calibration with sample");
  }

  public Node getStep(CopilotDevice pCopilotDevice) {
    return new Step5RecalibrationWithSample(pCopilotDevice);
  }
}
