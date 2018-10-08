package grondag.hard_science.superblock.virtual;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.exotic_matter.block.SuperBlockStackHelper;
import grondag.exotic_matter.block.SuperBlockWorldAccess;
import grondag.exotic_matter.block.SuperModelBlock;
import grondag.exotic_matter.model.render.RenderLayout;
import grondag.exotic_matter.model.render.RenderLayoutProducer;
import grondag.exotic_matter.model.state.ISuperModelState;
import grondag.exotic_matter.model.varia.WorldLightOpacity;
import grondag.exotic_matter.network.PacketHandler;
import grondag.hard_science.HardScience;
import grondag.hard_science.network.client_to_server.PacketDestroyVirtualBlock;
import grondag.hard_science.superblock.placement.Build;
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
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

//TODO: seems to be missing placement logic, see methods in ExcavationMarker

public class VirtualBlock extends SuperModelBlock
{
    
    @Override
    public void registerItems(IForgeRegistry<Item> itemReg)
    {
        ItemBlock itemBlock = new VirtualItemBlock(this);
        itemBlock.setRegistryName(this.getRegistryName());
        itemReg.register(itemBlock);        
    }
    
    /**
     * Retrieves item stack that can be used to place a super block in place of
     * virtual block currently at position.  Null if no virtual block located there.
     */
    @Nullable
    public static ItemStack getSuperModelStack(World world, IBlockState blockState, BlockPos pos)
    {
        Block block = blockState.getBlock();
        if(!(block instanceof VirtualBlock)) return null;
        
        VirtualBlock vBlock = (VirtualBlock)block;
        
        ISuperModelState modelState = SuperBlockWorldAccess.access(world).getModelState(vBlock, pos, true);
        if(modelState == null) return null;
        
        TileEntity te = world.getTileEntity(pos);
        if(!(te instanceof VirtualTileEntity)) return null;
        VirtualTileEntity vte = (VirtualTileEntity)te;
        
        modelState.setStatic(true);
        
        SuperModelBlock smb = SuperModelBlock.findAppropriateSuperModelBlock(vte.getSubstance(), modelState);
        
        ItemStack result = smb.getSubItems().get(vBlock.getMetaFromState(blockState)).copy();
        
        SuperBlockStackHelper.setStackModelState(result, modelState);
        SuperBlockStackHelper.setStackLightValue(result, vte.getLightValue());
        SuperBlockStackHelper.setStackSubstance(result, vte.getSubstance());
        
        return result;
    }

    /**
     * Virtual blocks only vary by render mode
     */
    public static final VirtualBlock[] virtualBlocks = new VirtualBlock[RenderLayout.ALL_LAYOUTS.size()];
    
    public static VirtualBlock findAppropriateVirtualBlock(ISuperModelState modelState)
    {
        return virtualBlocks[modelState.getRenderLayout().ordinal];
    }

    public static void registerVirtualBlocks(Register<Block> event)
    {
        int virualBlockIndex = 0;
        
        for(RenderLayoutProducer renderLayout : RenderLayoutProducer.VALUES)
        {
            VirtualBlock.virtualBlocks[renderLayout.ordinal]
                    = (VirtualBlock) new VirtualBlock("virtual_block" + virualBlockIndex++, renderLayout)
                        .setUnlocalizedName("virtual_block").setCreativeTab(HardScience.tabMod); //all virtual blocks have same display name
            event.getRegistry().register(VirtualBlock.virtualBlocks[renderLayout.ordinal]);
        }        
    }
    
    public VirtualBlock(String blockName, RenderLayoutProducer renderLayout)
    {
        super(blockName, Material.AIR, renderLayout, WorldLightOpacity.TRANSPARENT, false, false);
    }
    
    @Override
    public void addCollisionBoxToList(@Nonnull IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull AxisAlignedBB entityBox, @Nonnull List<AxisAlignedBB> collidingBoxes,
            @Nullable Entity entityIn, boolean p_185477_7_)
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
    public boolean addHitEffects(@Nonnull IBlockState blockState, @Nonnull World world, @Nonnull RayTraceResult target, @Nonnull ParticleManager manager)
    {
        // no hit effects for virtual blocks
        return true;
    }

    @Override
    public boolean addLandingEffects(@Nonnull IBlockState state, @Nonnull WorldServer worldObj, @Nonnull BlockPos blockPosition, @Nonnull IBlockState iblockstate, @Nonnull EntityLivingBase entity,
            int numberOfParticles)
    {
        // no landing effects for virtual blocks
        return true;
    }
    
    @Override
    public boolean canBeConnectedTo(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EnumFacing facing)
    {
         return false;
    }
    
    @Override
    public boolean canBeReplacedByLeaves(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos)
    {
       return true;
    }

    @Override
    public boolean canConnectRedstone(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nullable EnumFacing side)
    {
        return false;
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull SpawnPlacementType type)
    {
        return false;
    }

    @Override
    public boolean canDropFromExplosion(@Nonnull Explosion explosionIn)
    {
        return false;
    }

    @Override
    public boolean canEntityDestroy(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull Entity entity)
    {
       return false;
    }

    @Override
    public boolean canEntitySpawn(@Nonnull IBlockState state, @Nonnull Entity entityIn)
    {
        return false;
    }
    
    @Override
    public boolean canPlaceTorchOnTop(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos)
    {
        return false;
    }
    
    @Override
    public boolean canSilkHarvest()
    {
       return false;
    }
    
    @Override
    public boolean canSilkHarvest(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityPlayer player)
    {
        return false;
    }
    
    @Override
    public @Nullable TileEntity createNewTileEntity(@Nonnull World worldIn, int meta)
    {
        return this.renderLayout().ordinal == 0
                ? new VirtualTileEntityTESR()
                : new VirtualTileEntity();
    }
    
    @Override
    public boolean doesSideBlockRendering(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EnumFacing face)
    {
        if(this.isVisible(state, world, pos))
        {
            // can only occlude other virtual blocks
            if(world.getBlockState(pos.offset(face)).getBlock() instanceof VirtualBlock)
            {
                return super.doesSideBlockRendering(state, world, pos, face);
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }
    
//
//    @Override
//    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face)
//    {
//        return false;
//    }
//    
//    public boolean doesSideBlockVirtualRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face)
//    {
//        return super.doesSideBlockRendering(state, world, pos, face);
//    }
    
    @Override
    public @Nullable PathNodeType getAiPathNodeType(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos)
    {
        return PathNodeType.OPEN;
    }

    @Override
    public @Nullable float[] getBeaconColorMultiplier(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull BlockPos beaconPos)
    {
        return null;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face)
    {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public float getBlockHardness(@Nonnull IBlockState blockState, @Nonnull World worldIn, @Nonnull BlockPos pos)
    {
        return 0;
    }

    @Override
    public @Nullable AxisAlignedBB getCollisionBoundingBox(@Nonnull IBlockState state, @Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos)
    {
        return Block.NULL_AABB;
    }

    @Override
    public List<ItemStack> getDrops(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull IBlockState state, int fortune)
    {
        return Collections.emptyList();
    }

    @Override
    public float getExplosionResistance(@Nonnull Entity exploder)
    {
        return 0;
    }

    @Override
    public float getExplosionResistance(@Nonnull World world, @Nonnull BlockPos pos, @Nullable Entity exploder, @Nonnull Explosion explosion)
    {
        return 0;
    }

    @Override
    public int getFlammability(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EnumFacing face)
    {
        return 0;
    }

    @Override
    public int getHarvestLevel(@Nonnull IBlockState state)
    {
        return 0;
    }

    @Override
    public @Nullable String getHarvestTool(@Nonnull IBlockState state)
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
    public int getLightValue(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos)
    {
        return 0;
    }

    @Override
    public MapColor getMapColor(@Nonnull IBlockState state, @Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos)
    {
        return MapColor.AIR;
    }

    /** Can't report AIR on client side or right click doesn't work to place blocks. */
    @Override
    public Material getMaterial(@Nonnull IBlockState state)
    {
        return  HardScience.proxy.allowCollisionWithVirtualBlocks(null) ? Material.STRUCTURE_VOID : Material.AIR;
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list)
    {
        // We only want to show one item for virtual blocks
        // Otherwise will spam creative search / JEI
        // All do the same thing in the end.
        if(this.renderLayout() == RenderLayout.SOLID_ONLY)
        {
            list.add(this.getSubItems().get(0));
        }
    }

    @Override
    public boolean getUseNeighborBrightness(IBlockState state)
    {
        return false;
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, @Nullable ItemStack stack)
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
    public boolean isNormalCube(@Nonnull IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return false;
    }

    // Problem with this is can’t cause chunk culling.
    // But can't have domain-dependent visibility without it.
    // Could probably work around by overriding shouldRefresh
    // and trapping the chunk load event and then setting
    // block state on client side each time - but is an egregious hack
    @Override
    public boolean isOpaqueCube(IBlockState blockState)
    {
        return false;
    }

    @Override
    public boolean isPassable(@Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos)
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
    public boolean isVirtuallySolid(BlockPos pos, EntityPlayer player)
    {
        //TODO: check for player visibility
        return true;
    }

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
    public boolean removedByPlayer(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player, boolean willHarvest)
    {
        if(world.isRemote)
        {
            // virtual blocks simply destroyed without ceremony
            world.setBlockState(pos, net.minecraft.init.Blocks.AIR.getDefaultState(), 11);
            
            // have to send to server also, because server will see block is air and ignore the digging packet it gets
            PacketHandler.CHANNEL.sendToServer(new PacketDestroyVirtualBlock(pos));
        }
        return true;
    }

    @SideOnly(Side.CLIENT)
    public boolean isVisible(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos)
    {
        TileEntity te = blockAccess.getTileEntity(pos);
        if (te == null) return false;
        return ((VirtualTileEntity)te).isVisible();
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
    {
        return isVisible(blockState, blockAccess, pos) 
                ? super.shouldSideBeRendered(blockState, blockAccess, pos, side)
                : false;
    }

    @Override
    public void onBlockExploded(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull Explosion explosion)
    {
        // not affected by explosions, because not really there
        // probably won't be called anyway, because material is air
    }
    
    @Override
    public void breakBlock(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state)
    {
        super.breakBlock(worldIn, pos, state);
        
        // disassociate with build
        if(!worldIn.isRemote)
        {
            TileEntity te = worldIn.getTileEntity(pos);
            if(te != null && te instanceof VirtualTileEntity)
            {
                Build build = Build.buildFromId(((VirtualTileEntity)te).buildID());
                
                if(build != null && build.isOpen())
                {
                    build.removePosition(pos);
                }
            }
        }
    }
}

