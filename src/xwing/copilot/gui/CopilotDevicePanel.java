package xwing.copilot.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import xwing.XWingMicroscope;
import xwing.copilot.CopilotDevice;
import xwing.copilot.gui.steps.StepFactoryInterface;

import java.util.ArrayList;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * February 2018
 */
public class CopilotDevicePanel extends CustomGridPane
{
  XWingMicroscope mXWingMicroscope;
  ArrayList<StepFactoryInterface> mStepFactoryInterfaceList;
  CopilotDevice mCopilotDevice;

  public CopilotDevicePanel(CopilotDevice pCopilotDevice)
  {
    mCopilotDevice = pCopilotDevice;
    mXWingMicroscope = pCopilotDevice.getXWingMicroscope();
    mStepFactoryInterfaceList = pCopilotDevice.getStepFactoryInterfaceList();

    initializeStepTabs();
  }

  private void initializeStepTabs()
  {

    int lRow = 0;
    {
      TabPane lTabPane = new TabPane();
      add(lTabPane, 0, lRow, 4, 1);

      ArrayList<StepFactoryInterface>
          lStepList = mStepFactoryInterfaceList;

      for (StepFactoryInterface lStepFactoryInterface : lStepList)
      {
        // Single zernike editor
        Tab lStepTab = new Tab(lStepFactoryInterface.getName());
        Node lNode = lStepFactoryInterface.getStep(mCopilotDevice);
        lStepTab.setContent(lNode);
        lTabPane.getTabs().add(lStepTab);
      }
      lRow++;
    }
    //this.add(lTabPane, 0, lRow);

    /*
    {

      Button lPrevButton = new Button("Prev");
      this.add(lPrevButton, 2, lRow);

      Button lNextButton = new Button("Next");
      this.add(lNextButton, 3, lRow);

      lRow++;
    }
    */
  }
}
