package grondag.hard_science.simulator.network;

import org.junit.Test;

import grondag.hard_science.simulator.scratch.AssignedNumber;
import grondag.hard_science.simulator.scratch.AssignedNumbersAuthority;
import net.minecraft.nbt.NBTTagCompound;

public class AssignedNumbersAuthorityTest
{

    @Test
    public void test()
    {
        AssignedNumbersAuthority subject = AssignedNumbersAuthority.INSTANCE;
        
        assert subject.generateNewNumber(AssignedNumber.DOMAIN) == 1;
        assert subject.generateNewNumber(AssignedNumber.DOMAIN) == 2;
        assert subject.generateNewNumber(AssignedNumber.NETWORK) == 1;
        assert subject.generateNewNumber(AssignedNumber.NETWORK) == 2;
        assert subject.generateNewNumber(AssignedNumber.DOMAIN) == 3;
        
        NBTTagCompound tag = new NBTTagCompound();
        
        subject.serializeNBT(tag);
        
        assert subject.generateNewNumber(AssignedNumber.DOMAIN) == 4;
        assert subject.generateNewNumber(AssignedNumber.NETWORK) == 3;
        assert subject.generateNewNumber(AssignedNumber.ENDPOINT) == 1;
        
        subject.deserializeNBT(tag);
        
        assert subject.generateNewNumber(AssignedNumber.DOMAIN) == 4;
        assert subject.generateNewNumber(AssignedNumber.NETWORK) == 3;
        assert subject.generateNewNumber(AssignedNumber.ENDPOINT) == 1;
        
        subject.deserializeNBT(new NBTTagCompound()); 
        
        assert subject.generateNewNumber(AssignedNumber.DOMAIN) == 1;
        assert subject.generateNewNumber(AssignedNumber.NETWORK) == 1;
        assert subject.generateNewNumber(AssignedNumber.ENDPOINT) == 1;
    }

}
