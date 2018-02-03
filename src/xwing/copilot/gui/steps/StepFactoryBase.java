package xwing.copilot.gui.steps;

import clearcontrol.core.device.name.NameableInterface;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * February 2018
 */
public class StepFactoryBase implements NameableInterface
{
  private String mName;

  public StepFactoryBase(String pName) {
    mName = pName;
  }


  @Override public void setName(String pName)
  {
    mName = pName;
  }

  @Override public String getName()
  {
    return mName;
  }
}
