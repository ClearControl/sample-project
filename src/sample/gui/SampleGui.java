package sample.gui;

import clearcontrol.microscope.gui.halcyon.MicroscopeNodeType;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.gui.LightSheetMicroscopeGUI;
import javafx.stage.Stage;
import sample.adaptive.AdaptiveZInstruction;
import sample.adaptive.gui.AdaptiveZSchedulerPanel;

/**
 * XWing microscope GUI
 *
 * @author royer
 */
public class SampleGui extends LightSheetMicroscopeGUI
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
  public SampleGui(LightSheetMicroscope pLightSheetMicroscope,
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


  }

}
