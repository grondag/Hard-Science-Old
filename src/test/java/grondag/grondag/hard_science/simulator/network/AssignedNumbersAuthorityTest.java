package grondag.hard_science.simulator.network;

import org.junit.Test;

import grondag.hard_science.simulator.wip.AssignedNumber;
import grondag.hard_science.simulator.wip.AssignedNumbersAuthority;
import net.minecraft.nbt.NBTTagCompound;

public class AssignedNumbersAuthorityTest
{

    @Test
    public void test()
    {
        AssignedNumbersAuthority subject = AssignedNumbersAuthority.INSTANCE;
        
        assert subject.newNumber(AssignedNumber.DOMAIN) == 1;
        assert subject.newNumber(AssignedNumber.DOMAIN) == 2;
        assert subject.newNumber(AssignedNumber.NETWORK) == 1;
        assert subject.newNumber(AssignedNumber.NETWORK) == 2;
        assert subject.newNumber(AssignedNumber.DOMAIN) == 3;
        
        NBTTagCompound tag = new NBTTagCompound();
        
        subject.serializeNBT(tag);
        
        assert subject.newNumber(AssignedNumber.DOMAIN) == 4;
        assert subject.newNumber(AssignedNumber.NETWORK) == 3;
        assert subject.newNumber(AssignedNumber.ENDPOINT) == 1;
        
        subject.deserializeNBT(tag);
        
        assert subject.newNumber(AssignedNumber.DOMAIN) == 4;
        assert subject.newNumber(AssignedNumber.NETWORK) == 3;
        assert subject.newNumber(AssignedNumber.ENDPOINT) == 1;
        
        subject.deserializeNBT(new NBTTagCompound()); 
        
        assert subject.newNumber(AssignedNumber.DOMAIN) == 1;
        assert subject.newNumber(AssignedNumber.NETWORK) == 1;
        assert subject.newNumber(AssignedNumber.ENDPOINT) == 1;
    }

}
