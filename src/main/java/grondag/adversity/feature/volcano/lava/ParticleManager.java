package grondag.adversity.feature.volcano.lava;


import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.google.common.collect.ComparisonChain;

import grondag.adversity.Adversity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class ParticleManager
{
    private final static String NBT_SAVE_DATA_TAG = "particleManager";
    private final static int NBT_SAVE_DATA_WIDTH = 5;
    
    private final ConcurrentHashMap<BlockPos, ParticleInfo> map = new ConcurrentHashMap<BlockPos, ParticleInfo>(512);
            
    public void clear()
    {
        map.clear();
    }
    
    public int size()
    {
        return map.size();
    }
   
    public void addLavaForParticle(LavaSimulator sim, BlockPos pos, int fluidAmount)
    {
        ParticleInfo particle = map.get(pos);
        
        if(particle == null)
        {
            particle = new ParticleInfo(sim.getTickIndex(), pos, fluidAmount);
            map.put(pos, particle);
//            Adversity.log.info("ParticleManager added new particle @" + particle.pos.toString() + " with amount=" + particle.getFluidUnits());
        }
        else
        {
            particle.addFluid(fluidAmount);
//            Adversity.log.info("ParticleManager updated particle @" + particle.pos.toString() + " with amount=" + particle.getFluidUnits() + " added " + fluidAmount);
        }
    
    }
    
    /** returns a collection of eligible particles up the max count given */
    public Collection<EntityLavaParticle> pollEligible(LavaSimulator sim, int maxCount)
    {
        if(map.isEmpty()) return null;
        
        //TODO: make age limit configurable
        int firstEligibleTick = sim.getTickIndex() - 4;
        
        // wait until full size or at age limit
        return map.values().parallelStream()
                .filter(p -> p.tickCreated >= firstEligibleTick || p.fluidUnits >= LavaCell.FLUID_UNITS_PER_BLOCK)
                .sorted(new Comparator<ParticleInfo>() {

                    @Override
                    public int compare(ParticleInfo o1, ParticleInfo o2)
                    {
                        return ComparisonChain.start()
                                .compare(o2.fluidUnits, o1.fluidUnits)
                                .compare(o1.tickCreated, o2.tickCreated)
                                .result();
                    }})
                .sequential()
                .limit(maxCount)
                .map(p -> new EntityLavaParticle(sim.worldBuffer.realWorld, p.fluidUnits, new Vec3d(p.pos.getX() + 0.5, p.pos.getY() + 0.4, p.pos.getZ() + 0.5), Vec3d.ZERO))
                .collect(Collectors.toList());
    }
    
    public static class ParticleInfo
    {
        public final int tickCreated;
        private int fluidUnits = 0;
        public final BlockPos pos;
        
        private ParticleInfo(int tickCreated, BlockPos pos, int fluidUnits)
        {
            this.tickCreated = tickCreated;
            this.pos = pos;
            this.fluidUnits = fluidUnits;
        }
        
        private void addFluid(int fluidUnitsIn)
        {
            this.fluidUnits += fluidUnitsIn;
        }
        
        public int getFluidUnits()
        {
            return this.fluidUnits;
        }
    }

    public void readFromNBT(NBTTagCompound nbt)
    {
        this.map.clear();
    
        int[] saveData = nbt.getIntArray(NBT_SAVE_DATA_TAG);

        //confirm correct size
        if(saveData == null || saveData.length % NBT_SAVE_DATA_WIDTH != 0)
        {
            Adversity.log.warn("Invalid save data loading lava entity state buffer. Lava entities may have been lost.");
            return;
        }

        int i = 0;

        while(i < saveData.length)
        {
            ParticleInfo p = new ParticleInfo(saveData[i++], new BlockPos(saveData[i++], saveData[i++], saveData[i++]), saveData[i++]);
            
            //protect against duplicate position weirdness in save data
            if(!map.containsKey(p.pos))
            {
                map.put(p.pos, p);
            }
        }

        Adversity.log.info("Loaded " + map.size() + " lava entities.");
    }

    public void writeToNBT(NBTTagCompound nbt)
    {
        Adversity.log.info("Saving " + map.size() + " lava entities.");
        
        int[] saveData = new int[map.size() * NBT_SAVE_DATA_WIDTH];
        int i = 0;

        for(ParticleInfo p: map.values())
        {
            saveData[i++] = p.tickCreated;
            saveData[i++] = p.pos.getX();
            saveData[i++] = p.pos.getY();
            saveData[i++] = p.pos.getZ();
            saveData[i++] = p.fluidUnits;
        }       

        nbt.setIntArray(NBT_SAVE_DATA_TAG, saveData);

    }
}
