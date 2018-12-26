package sample.main;

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
import sample.SampleMicroscope;
import sample.gui.SampleGui;

/**
 * Sample main class
 *
 * @author royer
 */
public class SampleMain extends Application implements LoggingFeature
{
  static SampleMain instance = null;
  private boolean headless = false;

  public ClearCL getClearCL()
  {
    return mClearCL;
  }

  private ClearCL mClearCL;

  public static SampleMain getInstance()
  {
    if (instance == null)
    {
      launch();
    }
    return instance;
  }

  public SampleMain() {
    super();
  }

  public SampleMain(boolean headless) {
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
        new ImageView(new Image(SampleMicroscope.class.getResourceAsStream(
            "icon/cc_logo.jpg")));

    lImageView.fitWidthProperty().bind(pPrimaryStage.widthProperty());
    lImageView.fitHeightProperty().bind(pPrimaryStage.heightProperty());

    lPane.setCenter(lImageView);

    Scene scene = new Scene(lPane, 300, 300, Color.WHITE);
    pPrimaryStage.setScene(scene);
    pPrimaryStage.setX(0);
    pPrimaryStage.setY(0);
    pPrimaryStage.setTitle("ClearControl");
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
          startSample(true,
                     pPrimaryStage,
                     l2DDisplay,
                     l3DDisplay,
                     false);
        }
        else if (sResult.get() == lButtonReal)
        {
          startSample(false,
                     pPrimaryStage,
                     l2DDisplay,
                     l3DDisplay,
                     true);
        }
        else if (sResult.get() == lButtonRealWithOutStages)
        {
          startSample(false,
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

      Thread lThread = new Thread(lRunnable, "StartSample");
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
  public SampleMicroscope startSample(boolean pSimulation,
                                      Stage pPrimaryStage,
                                      boolean p2DDisplay,
                                      boolean p3DDisplay,
                                      boolean pUseStages)
  {
    int pNumberOfDetectionArms = 1;
    int pNumberOfLightSheets = 1;

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

      SampleMicroscope
              lSampleMicroscope =
          new SampleMicroscope(lStackFusionContext,
                              lMaxStackProcessingQueueLength,
                              lThreadPoolSize);
      mLightSheetMicroscope = lSampleMicroscope;
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

        lSampleMicroscope.addSimulatedDevices(false,
                                             false,
                                             true,
                                             lSimulatorDevice);
      }
      else
      {
        lSampleMicroscope.addRealHardwareDevices(pNumberOfDetectionArms,
                                                pNumberOfLightSheets,
                                                pUseStages);
      }
      lSampleMicroscope.addStandardDevices(lNumberOfControlPlanes);

      //EDFImagingEngine
      //    lDepthOfFocusImagingEngine =
      //    new EDFImagingEngine(lStackFusionContext, lSampleMicroscope);
      //lSampleMicroscope.addDevice(0, lDepthOfFocusImagingEngine);


      info("Opening microscope devices...");
      if (lSampleMicroscope.open())
      {
        info("Starting microscope devices...");
        if (lSampleMicroscope.start())
        {
          if (pPrimaryStage != null)
          {
            SampleGui lSampleGui;

            info("Setting up Sample GUI...");
            lSampleGui =
                new SampleGui(lSampleMicroscope,
                             pPrimaryStage,
                             p2DDisplay,
                             p3DDisplay);
            lSampleGui.setup();
            info("Opening Sample GUI...");
            lSampleGui.open();

            lSampleGui.waitForVisible(true, 1L, TimeUnit.MINUTES);

            lSampleGui.connectGUI();
            lSampleGui.waitForVisible(false, null, null);

            lSampleGui.disconnectGUI();
            info("Closing Sample GUI...");
            lSampleGui.close();

            info("Stopping microscope devices...");
            lSampleMicroscope.stop();
            info("Closing microscope devices...");
            lSampleMicroscope.close();
           }
         else {
            mClearCL = lClearCL;
            return lSampleMicroscope;
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
