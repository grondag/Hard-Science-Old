package grondag.hard_science.simulator.wip;

import org.junit.Test;

import grondag.hard_science.simulator.wip.DomainManager.Domain;
import grondag.hard_science.simulator.wip.DomainManager.Domain.DomainUser;
import grondag.hard_science.simulator.wip.DomainManager.Priveledge;
import net.minecraft.nbt.NBTTagCompound;

public class DomainManagerTest
{

    @Test
    public void test()
    {
        
        Domain d1 = DomainManager.INSTANCE.createDomain();
        d1.setName("first");
        d1.setSecurityEnabled(true);
        
        DomainUser bob = d1.addUser("Bob");
        bob.setPriveledges(Priveledge.ACCESS_INVENTORY, Priveledge.ADD_NODE, Priveledge.REMOVE_NODE, Priveledge.ADMIN);
        bob.grantPriveledge(Priveledge.ADMIN, false);
        bob.grantPriveledge(Priveledge.REMOVE_NODE, false);
        
        DomainUser sally = d1.addUser("Sally");
        DomainManager.INSTANCE.setSaveDirty(false);
        sally.grantPriveledge(Priveledge.ADMIN, true);
        assert(DomainManager.INSTANCE.isSaveDirty());
        
        Domain d2 = DomainManager.INSTANCE.createDomain();
        d2.setName("second");
        d2.setSecurityEnabled(false);
        
        DomainUser pat = d2.addUser("pat");
        
        assert(DomainManager.INSTANCE.isSaveDirty());
        NBTTagCompound tag = DomainManager.INSTANCE.serializeNBT();
        
        DomainManager.INSTANCE.clear();
        assert DomainManager.INSTANCE.getAllDomains().size() == 0;
        
        DomainManager.INSTANCE.deserializeNBT(tag);
        
        d1 = DomainManager.INSTANCE.getDomain(1);
        assert(d1.getName() == "first");
        assert(d1.isSecurityEnabled());
        
        bob = d1.findUser("Bob");
        assert(bob.hasPriveledge(Priveledge.ACCESS_INVENTORY));
        assert(bob.hasPriveledge(Priveledge.ADD_NODE));
        assert(!bob.hasPriveledge(Priveledge.REMOVE_NODE));
        assert(!bob.hasPriveledge(Priveledge.ADMIN));
        
        sally = d1.findUser("Sally");
        assert(sally.hasPriveledge(Priveledge.ACCESS_INVENTORY));
        assert(sally.hasPriveledge(Priveledge.ADD_NODE));
        assert(sally.hasPriveledge(Priveledge.REMOVE_NODE));
        assert(sally.hasPriveledge(Priveledge.ADMIN));
        
        d2 = DomainManager.INSTANCE.getDomain(2);
        assert(d2.getName() == "second");
        assert(!d2.isSecurityEnabled());
        
        pat = d2.findUser("pat");
        assert(pat.hasPriveledge(Priveledge.ACCESS_INVENTORY));
        assert(pat.hasPriveledge(Priveledge.ADD_NODE));
        assert(pat.hasPriveledge(Priveledge.REMOVE_NODE));
        assert(pat.hasPriveledge(Priveledge.ADMIN));
        
        assert DomainManager.INSTANCE.createDomain().getId() == 3;
    }

}
