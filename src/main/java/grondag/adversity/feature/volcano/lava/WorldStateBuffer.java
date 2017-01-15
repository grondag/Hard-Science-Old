package grondag.adversity.feature.volcano.lava;

import java.util.Collection;
import java.util.HashMap;

import org.apache.commons.lang3.tuple.Pair;

import grondag.adversity.Adversity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;

public class WorldStateBuffer implements IBlockAccess
{
    public final World realWorld;
    
    private final HashMap<BlockPos, Pair<BlockPos, IBlockState>> pendingUpdates = new HashMap<BlockPos, Pair<BlockPos, IBlockState>>();
    
    public WorldStateBuffer(World worldIn)
    {
        this.realWorld = worldIn;
    }
    
    public IBlockState getBlockState(BlockPos pos)
    {
        
        Pair<BlockPos, IBlockState> entry = pendingUpdates.get(pos);
        
        if(entry == null)
        {
            return this.realWorld.getBlockState(pos);
        }
        else
        {
            return entry.getRight();
        }
    }
    
    public void setBlockState(BlockPos pos, IBlockState state)
    {
        this.pendingUpdates.put(pos, Pair.of(pos, state));
    }
    
    /** 
     * Makes the updates in the game world for up to chunkCount chunks.
     * Returns the number of blocks updated.
     * 
     */
    public int applyBlockUpdates(int chunkCount)
    {
        int count = this.pendingUpdates.size();
        for(Pair<BlockPos, IBlockState> pair : this.pendingUpdates.values())
        {
            this.realWorld.setBlockState(pair.getLeft(), pair.getRight(), 3);
        }
        this.pendingUpdates.clear();
        return count;
    }
    
    public void addUpdates(Collection<Pair<BlockPos, IBlockState>> updates)
    {
        for(Pair<BlockPos, IBlockState> pair : updates)
        {
            this.pendingUpdates.put(pair.getLeft(), pair);
        }
    }

    @Override
    public boolean isAirBlock(BlockPos pos)
    {
        return this.getBlockState(pos).getBlock().isAir(this.getBlockState(pos), this, pos);
    }
    
    // FOLLOWING ARE UNSUPPORTED
    
    @Override
    public TileEntity getTileEntity(BlockPos pos)
    {
        return this.realWorld.getTileEntity(pos);
    }

    @Override
    public int getCombinedLight(BlockPos pos, int lightValue)
    {
        return this.realWorld.getCombinedLight(pos, lightValue);
    }

    @Override
    public Biome getBiome(BlockPos pos)
    {
        return this.realWorld.getBiome(pos);
    }

    @Override
    public int getStrongPower(BlockPos pos, EnumFacing direction)
    {
        return this.realWorld.getStrongPower(pos);
    }

    @Override
    public WorldType getWorldType()
    {
        return this.realWorld.getWorldType();
    }

    @Override
    public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default)
    {
        return this.realWorld.isSideSolid(pos, side, _default);
    }
    
}
