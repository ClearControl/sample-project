package xwing.gui;

import clearcontrol.microscope.gui.halcyon.MicroscopeNodeType;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.gui.LightSheetMicroscopeGUI;
import javafx.stage.Stage;
import xwing.adaptive.AdaptiveZInstruction;
import xwing.adaptive.gui.AdaptiveZSchedulerPanel;
import xwing.copilot.CopilotDevice;
import xwing.copilot.gui.CopilotDevicePanel;
import xwing.fastimage.FastImageDevice;
import xwing.fastimage.gui.FastImageDevicePanel;

/**
 * XWing microscope GUI
 *
 * @author royer
 */
public class XWingGui extends LightSheetMicroscopeGUI
{

  /**
   * Instantiates XWing microscope GUI
   * 
   * @param pLightSheetMicroscope
   *          microscope
   * @param pPrimaryStage
   *          JFX primary stage
   * @param p2DDisplay
   *          2D display
   * @param p3DDisplay
   *          3D display
   */
  public XWingGui(LightSheetMicroscope pLightSheetMicroscope,
                  Stage pPrimaryStage,
                  boolean p2DDisplay,
                  boolean p3DDisplay)
  {
    super(pLightSheetMicroscope,
          pPrimaryStage,
          p2DDisplay,
          p3DDisplay);
    addGroovyScripting("lsm");
    addJythonScripting("lsm");

    addPanelMappingEntry(AdaptiveZInstruction.class,
            AdaptiveZSchedulerPanel.class,
            MicroscopeNodeType.AdaptiveOptics);


    addPanelMappingEntry(FastImageDevice.class, FastImageDevicePanel.class, MicroscopeNodeType.Acquisition);

    addPanelMappingEntry(CopilotDevice.class, CopilotDevicePanel.class, MicroscopeNodeType.Other);

  }

}
