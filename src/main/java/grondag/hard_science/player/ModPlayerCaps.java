package grondag.hard_science.player;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;

public class ModPlayerCaps implements ICapabilityProvider, INBTSerializable<NBTTagCompound>
{
    @CapabilityInject(ModPlayerCaps.class)
    public static Capability<ModPlayerCaps> CAP_INSTANCE = null;
    
    /**
     *  True if player is holding down the placement modifier key.  Not persisted.
     */
    private boolean isPlacementModifierOn;
    
    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing)
    {
        return capability == CAP_INSTANCE;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, EnumFacing facing)
    {
        return capability == CAP_INSTANCE ? (T) this : null;
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        return new NBTTagCompound();
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        
    }

    public boolean isPlacementModifierOn()
    {
        return isPlacementModifierOn;
    }

    public void setPlacementModifierOn(boolean isPlacementModifierOn)
    {
        this.isPlacementModifierOn = isPlacementModifierOn;
    }

    public static boolean isPlacementModifierOn(EntityPlayer player)
    {
        if(player != null)
        {
            ModPlayerCaps caps = player.getCapability(ModPlayerCaps.CAP_INSTANCE, null);
            if(caps != null)
            {
                return caps.isPlacementModifierOn();
            }
        }
        return false;
    }
    
    public static void setPlacementModifierOn(EntityPlayer player, boolean isOn)
    {
        if(player != null)
        {
            ModPlayerCaps caps = player.getCapability(ModPlayerCaps.CAP_INSTANCE, null);
            if(caps != null)
            {
                caps.setPlacementModifierOn(isOn);
            }
        }
        
    }
}
