package xwing.main;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import clearcl.ClearCL;
import clearcl.ClearCLContext;
import clearcl.ClearCLDevice;
import clearcl.backend.ClearCLBackends;
import clearcontrol.core.concurrent.thread.ThreadSleep;
import clearcontrol.core.configuration.MachineConfiguration;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.simulation.LightSheetMicroscopeSimulationDevice;
import clearcontrol.microscope.lightsheet.simulation.SimulationUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import xwing.XWingMicroscope;
import xwing.gui.XWingGui;

/**
 * Xwing main class
 *
 * @author royer
 */
public class XWingMain extends Application implements LoggingFeature
{
  static XWingMain instance = null;
  private boolean headless = false;

  public ClearCL getClearCL()
  {
    return mClearCL;
  }

  private ClearCL mClearCL;

  public static XWingMain getInstance()
  {
    if (instance == null)
    {
      launch();
    }
    return instance;
  }

  public XWingMain() {
    super();
  }

  public XWingMain(boolean headless) {
    headless = true;
  }

  private LightSheetMicroscope mLightSheetMicroscope;

  public LightSheetMicroscope getLightSheetMicroscope()
  {
    return mLightSheetMicroscope;
  }

  private static Alert sAlert;
  private static Optional<ButtonType> sResult;

  static final MachineConfiguration
      sMachineConfiguration =
      MachineConfiguration.get();

  public static void main(String[] args)
  {
    launch(args);
  }

  @Override public void start(Stage pPrimaryStage)
  {
    instance = this;
    if (headless) {
      return;
    }

    boolean l2DDisplay = true;
    boolean l3DDisplay = true;

    BorderPane lPane = new BorderPane();
    ImageView
        lImageView =
        new ImageView(new Image(XWingMicroscope.class.getResourceAsStream(
            "icon/xwing.png")));

    lImageView.fitWidthProperty().bind(pPrimaryStage.widthProperty());
    lImageView.fitHeightProperty().bind(pPrimaryStage.heightProperty());

    lPane.setCenter(lImageView);

    Scene scene = new Scene(lPane, 300, 300, Color.WHITE);
    pPrimaryStage.setScene(scene);
    pPrimaryStage.setX(0);
    pPrimaryStage.setY(0);
    pPrimaryStage.setTitle("XWing");
    pPrimaryStage.show();

    ButtonType lButtonReal = new ButtonType("Real");
    ButtonType lButtonRealWithOutStages = new ButtonType("Real (without stages)");
    ButtonType lButtonSimulation = new ButtonType("Simulation");
    ButtonType lButtonCancel = new ButtonType("Cancel");

    sAlert = new Alert(AlertType.CONFIRMATION);

    sAlert.setTitle("Dialog");
    sAlert.setHeaderText("Simulation or Real ?");
    sAlert.setContentText(
        "Choose whether you want to start in real or simulation mode");

    sAlert.getButtonTypes()
          .setAll(lButtonReal,
                  lButtonRealWithOutStages,
                  lButtonSimulation,
                  lButtonCancel);

    Platform.runLater(() -> {
      sResult = sAlert.showAndWait();
      Runnable lRunnable = () -> {
        if (sResult.get() == lButtonSimulation)
        {
          startXWing(true,
                     pPrimaryStage,
                     l2DDisplay,
                     l3DDisplay,
                     false);
        }
        else if (sResult.get() == lButtonReal)
        {
          startXWing(false,
                     pPrimaryStage,
                     l2DDisplay,
                     l3DDisplay,
                     true);
        }
        else if (sResult.get() == lButtonRealWithOutStages)
        {
          startXWing(false,
                     pPrimaryStage,
                     l2DDisplay,
                     l3DDisplay,
                     false);
        }
        else if (sResult.get() == lButtonCancel)
        {
          Platform.runLater(() -> pPrimaryStage.hide());
        }
      };

      Thread lThread = new Thread(lRunnable, "StartXWing");
      lThread.setDaemon(true);
      lThread.start();
    });

  }

  /**
   * Starts the microscope
   *
   * @param pSimulation   true
   * @param pPrimaryStage JFX primary stage
   * @param p2DDisplay    true: use 2D displays
   * @param p3DDisplay    true: use 3D displays
   */
  public XWingMicroscope startXWing(boolean pSimulation,
                                    Stage pPrimaryStage,
                                    boolean p2DDisplay,
                                    boolean p3DDisplay,
                                    boolean pUseStages)
  {
    int pNumberOfDetectionArms = 2;
    int pNumberOfLightSheets = 4;

    int lMaxStackProcessingQueueLength = 32;
    int lThreadPoolSize = 1;
    int lNumberOfControlPlanes = 8;

    try (ClearCL lClearCL = new ClearCL(ClearCLBackends.getBestBackend()))
    {
      for (ClearCLDevice lClearCLDevice : lClearCL.getAllDevices())
        info("OpenCl devices available: %s \n",
             lClearCLDevice.getName());

      ClearCLContext
          lStackFusionContext =
          lClearCL.getDeviceByName(sMachineConfiguration.getStringProperty(
              "clearcl.device.fusion",
              "")).createContext();

      info("Using device %s for stack fusion \n",
           lStackFusionContext.getDevice());

      XWingMicroscope
          lXWingMicroscope =
          new XWingMicroscope(lStackFusionContext,
                              lMaxStackProcessingQueueLength,
                              lThreadPoolSize);
      mLightSheetMicroscope = lXWingMicroscope;
      if (pSimulation)
      {
        ClearCLContext
            lSimulationContext =
            lClearCL.getDeviceByName(sMachineConfiguration.getStringProperty(
                "clearcl.device.simulation",
                "HD")).createContext();

        info("Using device %s for simulation (Simbryo) \n",
             lSimulationContext.getDevice());

        LightSheetMicroscopeSimulationDevice
            lSimulatorDevice =
            SimulationUtils.getSimulatorDevice(lSimulationContext,
                                               pNumberOfDetectionArms,
                                               pNumberOfLightSheets,
                                               2048,
                                               11,
                                               320,
                                               320,
                                               320,
                                               false);

        lXWingMicroscope.addSimulatedDevices(false,
                                             false,
                                             true,
                                             lSimulatorDevice);
      }
      else
      {
        lXWingMicroscope.addRealHardwareDevices(pNumberOfDetectionArms,
                                                pNumberOfLightSheets,
                                                pUseStages);
      }
      lXWingMicroscope.addStandardDevices(lNumberOfControlPlanes);

      //EDFImagingEngine
      //    lDepthOfFocusImagingEngine =
      //    new EDFImagingEngine(lStackFusionContext, lXWingMicroscope);
      //lXWingMicroscope.addDevice(0, lDepthOfFocusImagingEngine);


      info("Opening microscope devices...");
      if (lXWingMicroscope.open())
      {
        info("Starting microscope devices...");
        if (lXWingMicroscope.start())
        {
          if (pPrimaryStage != null)
          {
            XWingGui lXWingGui;

            info("Setting up XWing GUI...");
            lXWingGui =
                new XWingGui(lXWingMicroscope,
                             pPrimaryStage,
                             p2DDisplay,
                             p3DDisplay);
            lXWingGui.setup();
            info("Opening XWing GUI...");
            lXWingGui.open();

            lXWingGui.waitForVisible(true, 1L, TimeUnit.MINUTES);

            lXWingGui.connectGUI();
            lXWingGui.waitForVisible(false, null, null);

            lXWingGui.disconnectGUI();
            info("Closing XWing GUI...");
            lXWingGui.close();

            info("Stopping microscope devices...");
            lXWingMicroscope.stop();
            info("Closing microscope devices...");
            lXWingMicroscope.close();
           }
         else {
            mClearCL = lClearCL;
            return lXWingMicroscope;
          }
        }
        else
          severe("Not all microscope devices started!");
      }
      else
        severe("Not all microscope devices opened!");

      ThreadSleep.sleep(100, TimeUnit.MILLISECONDS);
    }

    if (pPrimaryStage != null)
    {
      System.exit(0);
    }
    return null;
  }

}
