package xwing.copilot.gui.steps.step3recalibrationwithsample;

import javafx.scene.Node;
import xwing.copilot.CopilotDevice;
import xwing.copilot.gui.steps.StepFactoryBase;
import xwing.copilot.gui.steps.StepFactoryInterface;
import xwing.copilot.gui.steps.step1manualcalibration.Step1ManualCalibration;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * February 2018
 */
public class Step3RecalibrationWithSampleFactory extends
                                                 StepFactoryBase implements
                                                                 StepFactoryInterface
{
  public Step3RecalibrationWithSampleFactory()
  {
    super("Step 3: Re-calibration with sample");
  }

  public Node getStep(CopilotDevice pCopilotDevice) {
    return new Step3RecalibrationWithSample(pCopilotDevice);
  }
}
