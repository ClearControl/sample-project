package xwing.copilot.gui.steps.step3samplemounting;

import javafx.scene.Node;
import xwing.copilot.CopilotDevice;
import xwing.copilot.gui.steps.StepFactoryBase;
import xwing.copilot.gui.steps.StepFactoryInterface;
import xwing.copilot.gui.steps.step2automaticcalibration.Step2AutomaticCalibration;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * February 2018
 */
public class Step3SampleMountingFactory extends StepFactoryBase implements
                                                                StepFactoryInterface
{
  public Step3SampleMountingFactory()
  {
    super("Step 3: Sample mounting");
  }

  public Node getStep(CopilotDevice pCopilotDevice) {
    return new Step3SampleMounting(pCopilotDevice);
  }
}
