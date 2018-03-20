package grondag.hard_science.player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.exotic_matter.serialization.IReadWriteNBT;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class ModPlayerCaps implements ICapabilityProvider, IReadWriteNBT
{
    @CapabilityInject(ModPlayerCaps.class)
    public static Capability<ModPlayerCaps> CAP_INSTANCE = null;
    
    public static enum ModifierKey
    {
        CTRL_KEY,
        ALT_KEY;
        
        public final int flag;
        
        private ModifierKey()
        {
           this.flag = 1 << this.ordinal(); 
        }
    }

    /**
     *  True if player is holding down the placement modifier key.  Not persisted.
     */
    private int modifierKeyFlags;

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing)
    {
        return capability == CAP_INSTANCE;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Nullable
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing)
    {
        return capability == CAP_INSTANCE ? (T) this : null;
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        
    }

    public boolean isModifierKeyPressed(ModifierKey key)
    {
        return (this.modifierKeyFlags & key.flag) != 0;
    }

    public void setPlacementModifierFlags(int keyFlags)
    {
        this.modifierKeyFlags = keyFlags;
    }

    public static boolean isModifierKeyPressed(EntityPlayer player, ModifierKey key)
    {
        if(player != null)
        {
            ModPlayerCaps caps = player.getCapability(ModPlayerCaps.CAP_INSTANCE, null);
            if(caps != null)
            {
                return caps.isModifierKeyPressed(key);
            }
        }
        return false;
    }
    
    public static void setPlacementModifierFlags(EntityPlayer player, int keyFlags)
    {
        if(player != null)
        {
            ModPlayerCaps caps = player.getCapability(ModPlayerCaps.CAP_INSTANCE, null);
            if(caps != null)
            {
                caps.setPlacementModifierFlags(keyFlags);
            }
        }
        
    }

}
