package xwing.copilot.gui.steps.step4powercalibrationwithsample;

import javafx.scene.Node;
import xwing.copilot.CopilotDevice;
import xwing.copilot.gui.steps.StepFactoryBase;
import xwing.copilot.gui.steps.StepFactoryInterface;
import xwing.copilot.gui.steps.step2automaticcalibration.Step2AutomaticCalibration;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * February 2018
 */
public class Step4PowerCalibrationWithSampleFactory extends
                                                    StepFactoryBase implements
                                                                    StepFactoryInterface
{
  public Step4PowerCalibrationWithSampleFactory()
  {
    super("Step 4: Power calibration with sample");
  }

  public Node getStep(CopilotDevice pCopilotDevice) {
    return new Step4PowerCalibrationWithSample(pCopilotDevice);
  }
}
