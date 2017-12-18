package grondag.hard_science.machines;

import grondag.hard_science.simulator.storage.ItemStorage;

public class SmartChestMachine extends ItemStorage
{
    @Override
    public boolean hasOnOff()
    {
        return false;
    }

    @Override
    public boolean hasRedstoneControl()
    {
        return false;
    }
   
}
