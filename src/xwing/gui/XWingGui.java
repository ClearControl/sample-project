package xwing.gui;

import clearcontrol.devices.stages.BasicThreeAxisStageInterface;
import clearcontrol.devices.stages.kcube.gui.BasicThreeAxesStagePanel;
import clearcontrol.microscope.gui.halcyon.MicroscopeNodeType;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.gui.LightSheetMicroscopeGUI;
import javafx.stage.Stage;
import clearcontrol.devices.stages.kcube.impl.KCubeDevice;
import clearcontrol.devices.stages.kcube.gui.KCubePane;
import xwing.adaptive.AdaptiveZScheduler;
import xwing.adaptive.gui.AdaptiveZSchedulerPanel;

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

    addPanelMappingEntry(KCubeDevice.class,
                         KCubePane.class,
                         MicroscopeNodeType.Stage);

    addPanelMappingEntry(BasicThreeAxisStageInterface.class,
                         BasicThreeAxesStagePanel.class,
                         MicroscopeNodeType.Stage);

    addPanelMappingEntry(AdaptiveZScheduler.class,
                         AdaptiveZSchedulerPanel.class,
                         MicroscopeNodeType.AdaptiveOptics);

  }

}
