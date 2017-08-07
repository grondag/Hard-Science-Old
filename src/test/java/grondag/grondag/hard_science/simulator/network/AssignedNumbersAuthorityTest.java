package grondag.hard_science.simulator.network;

import org.junit.Test;

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
        
        subject.writeToNBT(tag);
        
        assert subject.generateNewNumber(AssignedNumber.DOMAIN) == 4;
        assert subject.generateNewNumber(AssignedNumber.NETWORK) == 3;
        assert subject.generateNewNumber(AssignedNumber.ENDPOINT) == 1;
        
        subject.readFromNBT(tag);
        
        assert subject.generateNewNumber(AssignedNumber.DOMAIN) == 4;
        assert subject.generateNewNumber(AssignedNumber.NETWORK) == 3;
        assert subject.generateNewNumber(AssignedNumber.ENDPOINT) == 1;
        
        subject.readFromNBT(new NBTTagCompound()); 
        
        assert subject.generateNewNumber(AssignedNumber.DOMAIN) == 1;
        assert subject.generateNewNumber(AssignedNumber.NETWORK) == 1;
        assert subject.generateNewNumber(AssignedNumber.ENDPOINT) == 1;
    }

}
