package grondag.hard_science.simulator.base;

import org.junit.Test;

import grondag.exotic_matter.simulator.persistence.AssignedNumber;
import grondag.exotic_matter.simulator.persistence.AssignedNumbersAuthority;
import net.minecraft.nbt.NBTTagCompound;

public class AssignedNumbersAuthorityTest
{

    @Test
    public void test()
    {
        AssignedNumbersAuthority subject = new AssignedNumbersAuthority();
        
        assert subject.newNumber(AssignedNumber.DOMAIN) == 1000;
        assert subject.newNumber(AssignedNumber.DOMAIN) == 1001;
        assert subject.newNumber(AssignedNumber.BUILD) == 1000;
        assert subject.newNumber(AssignedNumber.BUILD) == 1001;
        assert subject.newNumber(AssignedNumber.DOMAIN) == 1002;
        
        NBTTagCompound tag = new NBTTagCompound();
        
        subject.serializeNBT(tag);
        
        assert subject.newNumber(AssignedNumber.DOMAIN) == 1003;
        assert subject.newNumber(AssignedNumber.BUILD) == 1002;
        assert subject.newNumber(AssignedNumber.TASK) == 1000;
        
        subject.deserializeNBT(tag);
        
        assert subject.newNumber(AssignedNumber.DOMAIN) == 1003;
        assert subject.newNumber(AssignedNumber.BUILD) == 1002;
        assert subject.newNumber(AssignedNumber.TASK) == 1000;
        
        subject.deserializeNBT(new NBTTagCompound()); 
        
        assert subject.newNumber(AssignedNumber.DOMAIN) == 1000;
        assert subject.newNumber(AssignedNumber.BUILD) == 1000;
        assert subject.newNumber(AssignedNumber.TASK) == 1000;
    }

}