package grondag.hard_science.virtualblock;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import grondag.hard_science.HardScience;
import grondag.hard_science.init.ModBlocks;
import grondag.hard_science.network.ModMessages;
import grondag.hard_science.network.client_to_server.PacketDestroyVirtualBlock;
import grondag.hard_science.superblock.block.SuperBlock;
import grondag.hard_science.superblock.block.SuperModelBlock;
import grondag.hard_science.superblock.model.state.BlockRenderMode;
import grondag.hard_science.superblock.model.state.WorldLightOpacity;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class VirtualBlock extends SuperModelBlock
{
    
//    public static final SoundType VIRTUAL_BLOCK_SOUND = new SoundType(6.0F, 2.0F, SoundEvents.BLOCK_CLOTH_BREAK, SoundEvents.BLOCK_CLOTH_STEP, SoundEvents.BLOCK_CLOTH_PLACE, SoundEvents.BLOCK_CLOTH_HIT, SoundEvents.BLOCK_CLOTH_FALL);

    public VirtualBlock(String blockName)
    {
        super(blockName, Material.AIR, BlockRenderMode.TESR, WorldLightOpacity.TRANSPARENT, false, false);
    }
    
    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes,
            Entity entityIn, boolean p_185477_7_)
    {
        if(HardScience.proxy.allowCollisionWithVirtualBlocks(worldIn) && entityIn == null)
        {
            super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, p_185477_7_);
        }
        else
        {
            return;
        }
    }

    @Override
    public boolean addHitEffects(IBlockState blockState, World world, RayTraceResult target, ParticleManager manager)
    {
        // no hit effects for virtual blocks
        return true;
    }

    @Override
    public boolean addLandingEffects(IBlockState state, WorldServer worldObj, BlockPos blockPosition, IBlockState iblockstate, EntityLivingBase entity,
            int numberOfParticles)
    {
        // no landing effects for virtual blocks
        return true;
    }
    
    @Override
    public boolean canBeConnectedTo(IBlockAccess world, BlockPos pos, EnumFacing facing)
    {
         return false;
    }
    
    @Override
    public boolean canBeReplacedByLeaves(IBlockState state, IBlockAccess world, BlockPos pos)
    {
       return true;
    }

    @Override
    public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
    {
        return false;
    }

    @Override
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, SpawnPlacementType type)
    {
        return false;
    }

    @Override
    public boolean canDropFromExplosion(Explosion explosionIn)
    {
        return false;
    }

    @Override
    public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity)
    {
       return false;
    }

    @Override
    public boolean canEntitySpawn(IBlockState state, Entity entityIn)
    {
        return false;
    }
    
    @Override
    public boolean canPlaceTorchOnTop(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return false;
    }
    
    @Override
    public boolean canSilkHarvest()
    {
       return false;
    }
    
    @Override
    public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player)
    {
        return false;
    }
    
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new VirtualBlockTileEntity();
    }

    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face)
    {
        return false;
    }
    
    public boolean doesSideBlockVirtualRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face)
    {
        return super.doesSideBlockRendering(state, world, pos, face);
    }
    
    @Override
    public PathNodeType getAiPathNodeType(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return PathNodeType.OPEN;
    }

    @Override
    public float[] getBeaconColorMultiplier(IBlockState state, World world, BlockPos pos, BlockPos beaconPos)
    {
        return null;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face)
    {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public float getBlockHardness(IBlockState blockState, World worldIn, BlockPos pos)
    {
        return 0;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        return Block.NULL_AABB;
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
    {
        return Collections.emptyList();
    }

    @Override
    public float getExplosionResistance(Entity exploder)
    {
        return 0;
    }

    @Override
    public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion)
    {
        return 0;
    }

    @Override
    public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face)
    {
        return 0;
    }

    @Override
    public int getHarvestLevel(IBlockState state)
    {
        return 0;
    }

    @Override
    public String getHarvestTool(IBlockState state)
    {
        return null;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune)
    {
        return Items.AIR;
    }

    @Override
    public int getLightOpacity(IBlockState state)
    {
        return 0;
    }

    @Override
    public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return 0;
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return 0;
    }

    @Override
    public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        return MapColor.AIR;
    }

    /** Can't report AIR on client side or right click doesn't work to place blocks. */
    @Override
    public Material getMaterial(IBlockState state)
    {
        return  HardScience.proxy.allowCollisionWithVirtualBlocks(null) ? Material.STRUCTURE_VOID : Material.AIR;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState iBlockState)
    {
      return EnumBlockRenderType.INVISIBLE;
    }

//     @Override
//    public SoundType getSoundType()
//    {
//        return VIRTUAL_BLOCK_SOUND;
//    }

//    @Override
//    public SoundType getSoundType(IBlockState state, World world, BlockPos pos, Entity entity)
//    {
//        return VIRTUAL_BLOCK_SOUND;
//    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list)
    {
        list.add(this.getSubItems().get(0));
    }

    @Override
    public boolean getUseNeighborBrightness(IBlockState state)
    {
        return false;
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack stack)
    {
        // no drops!
    }

    @Override
    public boolean isBeaconBase(IBlockAccess worldObj, BlockPos pos, BlockPos beacon)
    {
        return false;
    }

    @Override
    public boolean isBlockNormalCube(IBlockState blockState)
    {
        return false;
    }

    @Override
    public boolean isBurning(IBlockAccess world, BlockPos pos)
    {
        return false;
    }

    @Override
    public boolean isCollidable()
    {
        return HardScience.proxy.allowCollisionWithVirtualBlocks(null);
    }

    @Override
    public boolean isFlammable(IBlockAccess world, BlockPos pos, EnumFacing face)
    {
        return false;
    }

    @Override
    public boolean isFullBlock(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState blockState)
    {
        return false;
    }

    @Override
    public boolean isPassable(IBlockAccess worldIn, BlockPos pos)
    {
        return true;
    }

    @Override
    public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos)
    {
        return true;
    }

    @Override
    public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side)
    {
        return false;
    }

    @Override
    public boolean isTranslucent(IBlockState state)
    {
        return true;
    }

    @Override
    public boolean isVirtual() { return true; }
    
    @Override
    public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn)
    {
        // nothing
    }

    @Override
    public int quantityDropped(IBlockAccess world, BlockPos pos, IBlockState state)
    {
        return 0;
    }

    @Override
    public int quantityDropped(IBlockState state, int fortune, Random random)
    {
        return 0;
    }

    @Override
    public int quantityDropped(Random random)
    {
        return 0;
    }

    @Override
    public int quantityDroppedWithBonus(int fortune, Random random)
    {
        return 0;
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
    {
        if(world.isRemote)
        {
            // virtual blocks simply destroyed without ceremony
            world.setBlockState(pos, net.minecraft.init.Blocks.AIR.getDefaultState(), 11);
            
            // have to send to server also, because server will see block is air and ignore the digging packet it gets
            ModMessages.INSTANCE.sendToServer(new PacketDestroyVirtualBlock(pos));
        }
        return true;
    }

    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
    {

        if(this.getSubstance(blockState, blockAccess, pos).isTranslucent)
        {
            BlockPos otherPos = pos.offset(side);
            IBlockState otherBlockState = blockAccess.getBlockState(otherPos);
            Block block = otherBlockState.getBlock();
            if(block instanceof SuperBlock)
            {
                SuperBlock sBlock = (SuperBlock)block;
                if(((SuperBlock) block).getSubstance(otherBlockState, blockAccess, otherPos).isTranslucent)
                {
                    ModelState myModelState = this.getModelStateAssumeStateIsCurrent(blockState, blockAccess, pos, false);
                    ModelState otherModelState = sBlock.getModelStateAssumeStateIsCurrent(otherBlockState, blockAccess, otherPos, false);
                    // for transparent blocks, want blocks with same apperance and species to join
                    return (myModelState.hasSpecies() && myModelState.getSpecies() != otherModelState.getSpecies())
                            || !myModelState.doShapeAndAppearanceMatch(otherModelState);

                }
            }
        }
        
        AxisAlignedBB axisalignedbb = blockState.getBoundingBox(blockAccess, pos);

        switch (side)
        {
            case DOWN:

                if (axisalignedbb.minY > 0.0D)
                {
                    return true;
                }

                break;
            case UP:

                if (axisalignedbb.maxY < 1.0D)
                {
                    return true;
                }

                break;
            case NORTH:

                if (axisalignedbb.minZ > 0.0D)
                {
                    return true;
                }

                break;
            case SOUTH:

                if (axisalignedbb.maxZ < 1.0D)
                {
                    return true;
                }

                break;
            case WEST:

                if (axisalignedbb.minX > 0.0D)
                {
                    return true;
                }

                break;
            case EAST:

                if (axisalignedbb.maxX < 1.0D)
                {
                    return true;
                }
        }

        BlockPos otherPos = pos.offset(side);
        IBlockState otherBlockState = blockAccess.getBlockState(otherPos);
        if(otherBlockState.getBlock() == ModBlocks.virtual_block)
        {
            return !((VirtualBlock)ModBlocks.virtual_block).doesSideBlockVirtualRendering(otherBlockState, blockAccess, otherPos, side.getOpposite());
        }
        else
        {
            return !blockAccess.getBlockState(pos.offset(side)).doesSideBlockRendering(blockAccess, pos.offset(side), side.getOpposite());
        }
    }
    
    
    
}

