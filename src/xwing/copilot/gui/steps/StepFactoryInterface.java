package xwing.copilot.gui.steps;

import clearcontrol.core.device.name.NameableInterface;
import javafx.scene.Node;
import xwing.XWingMicroscope;
import xwing.copilot.CopilotDevice;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * February 2018
 */
public interface StepFactoryInterface extends NameableInterface
{
  Node getStep(CopilotDevice pCopilotDevice);
}
