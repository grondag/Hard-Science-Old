package grondag.hard_science.simulator.base;

import org.junit.Test;

import grondag.hard_science.simulator.base.AssignedNumber;
import grondag.hard_science.simulator.base.AssignedNumbersAuthority;
import net.minecraft.nbt.NBTTagCompound;

public class AssignedNumbersAuthorityTest
{

    @Test
    public void test()
    {
        AssignedNumbersAuthority subject = new AssignedNumbersAuthority();
        
        assert subject.newNumber(AssignedNumber.DOMAIN) == 1000;
        assert subject.newNumber(AssignedNumber.DOMAIN) == 1001;
        assert subject.newNumber(AssignedNumber.NETWORK) == 1000;
        assert subject.newNumber(AssignedNumber.NETWORK) == 1001;
        assert subject.newNumber(AssignedNumber.DOMAIN) == 1002;
        
        NBTTagCompound tag = new NBTTagCompound();
        
        subject.serializeNBT(tag);
        
        assert subject.newNumber(AssignedNumber.DOMAIN) == 1003;
        assert subject.newNumber(AssignedNumber.NETWORK) == 1002;
        assert subject.newNumber(AssignedNumber.ENDPOINT) == 1000;
        
        subject.deserializeNBT(tag);
        
        assert subject.newNumber(AssignedNumber.DOMAIN) == 1003;
        assert subject.newNumber(AssignedNumber.NETWORK) == 1002;
        assert subject.newNumber(AssignedNumber.ENDPOINT) == 1000;
        
        subject.deserializeNBT(new NBTTagCompound()); 
        
        assert subject.newNumber(AssignedNumber.DOMAIN) == 1000;
        assert subject.newNumber(AssignedNumber.NETWORK) == 1000;
        assert subject.newNumber(AssignedNumber.ENDPOINT) == 1000;
    }

}