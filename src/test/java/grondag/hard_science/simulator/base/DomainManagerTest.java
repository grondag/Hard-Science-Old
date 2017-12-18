package grondag.hard_science.simulator.base;

import org.junit.Test;

import grondag.hard_science.simulator.domain.Domain;
import grondag.hard_science.simulator.domain.DomainManager;
import grondag.hard_science.simulator.domain.Privilege;
import grondag.hard_science.simulator.domain.DomainUser;
import net.minecraft.nbt.NBTTagCompound;

public class DomainManagerTest
{

    @Test
    public void test()
    {
        DomainManager dm = DomainManager.instance();
        dm.loadNew();
        
        Domain d1 = dm.createDomain();
        d1.setName("first");
        d1.setSecurityEnabled(true);
        
        DomainUser bob = d1.addUser("Bob");
        bob.setPrivileges(Privilege.ACCESS_INVENTORY, Privilege.ADD_NODE, Privilege.REMOVE_NODE, Privilege.ADMIN);
        bob.grantPrivilege(Privilege.ADMIN, false);
        bob.grantPrivilege(Privilege.REMOVE_NODE, false);
        
        DomainUser sally = d1.addUser("Sally");
        dm.setSaveDirty(false);
        sally.grantPrivilege(Privilege.ADMIN, true);
        assert(dm.isSaveDirty());
        
        Domain d2 = dm.createDomain();
        d2.setName("second");
        d2.setSecurityEnabled(false);
        
        DomainUser pat = d2.addUser("pat");
        
        assert(dm.isSaveDirty());
        NBTTagCompound tag = dm.serializeNBT();
        
        dm.unload();
        dm.deserializeNBT(tag);
        
        d1 = dm.getDomain(1000);
        assert(d1.getName() == "first");
        assert(d1.isSecurityEnabled());
        
        bob = d1.findUser("Bob");
        assert(bob.hasPrivilege(Privilege.ACCESS_INVENTORY));
        assert(bob.hasPrivilege(Privilege.ADD_NODE));
        assert(!bob.hasPrivilege(Privilege.REMOVE_NODE));
        assert(!bob.hasPrivilege(Privilege.ADMIN));
        
        sally = d1.findUser("Sally");
        assert(sally.hasPrivilege(Privilege.ACCESS_INVENTORY));
        assert(sally.hasPrivilege(Privilege.ADD_NODE));
        assert(sally.hasPrivilege(Privilege.REMOVE_NODE));
        assert(sally.hasPrivilege(Privilege.ADMIN));
        
        d2 = dm.getDomain(1001);
        assert(d2.getName() == "second");
        assert(!d2.isSecurityEnabled());
        
        pat = d2.findUser("pat");
        assert(pat.hasPrivilege(Privilege.ACCESS_INVENTORY));
        assert(pat.hasPrivilege(Privilege.ADD_NODE));
        assert(pat.hasPrivilege(Privilege.REMOVE_NODE));
        assert(pat.hasPrivilege(Privilege.ADMIN));
        
        assert dm.createDomain().getId() == 1002;
    }

}