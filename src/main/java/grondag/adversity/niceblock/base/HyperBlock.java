// TODO: Fix

//package grondag.adversity.niceblock.base;
//
//import java.util.List;
//import java.util.Random;
//
//import grondag.adversity.Adversity;
//import grondag.adversity.Config;
//import grondag.adversity.feature.unobtanium.Unobtanium;
//import grondag.adversity.niceblock.support.BaseMaterial;
//import net.minecraft.block.Block;
//import net.minecraft.block.material.Material;
//import net.minecraft.block.state.IBlockState;
//import net.minecraft.entity.Entity;
//import net.minecraft.entity.EntityLiving.SpawnPlacementType;
//import net.minecraft.entity.EnumCreatureType;
//import net.minecraft.entity.player.EntityPlayer;
//import net.minecraft.init.Blocks;
//import net.minecraft.item.Item;
//import net.minecraft.item.ItemStack;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.world.Explosion;
//import net.minecraft.world.IBlockAccess;
//import net.minecraft.world.World;
//
//public class HyperBlock extends NiceBlockPlus
//{
//
//	public HyperBlock(BlockModelHelper blockModelHelper, BaseMaterial material, String styleName) {
//		super(blockModelHelper, material, styleName);
//		this.needsRandomTick = Config.Unobtanium.selfRepairChance > 0;
//	}
//
//	
//	// This is the max value any block can have before it is destroyed - can take 7 hits.
//	public static final int		DAMAGE_MAX		= 99;
//
//	@Override
//	public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, SpawnPlacementType type)
//	{
//		if(this.material != BaseMaterial.HYPERSTONE) return super.canCreatureSpawn(state, world, pos, type);
//		return Config.Unobtanium.allowMobSpawning ? super.canCreatureSpawn(state, world, pos, type) : false;
//	}
//
//	@Override
//	public boolean canDropFromExplosion(Explosion explosionIn)
//	{
//		if(this.material != BaseMaterial.HYPERSTONE) return super.canDropFromExplosion(explosionIn);
//		return true;
//	}
//
//	@SuppressWarnings("deprecation")
//	@Override
//	protected boolean canSilkHarvest()
//	{
//		if(this.material != BaseMaterial.HYPERSTONE) return super.canSilkHarvest();
//		return Config.Unobtanium.allowHarvest && Config.Unobtanium.allowSilkTouch;
//	}
//
//	@Override
//	public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player)
//	{
//		if(this.material != BaseMaterial.HYPERSTONE) return super.canSilkHarvest(world, pos, state, player);
//		return Config.Unobtanium.allowHarvest && Config.Unobtanium.allowSilkTouch;
//	}
//
//
//	@Override
//	public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player)
//	{
//		if(this.material != BaseMaterial.HYPERSTONE) return super.canHarvestBlock(world, pos, player);
//		return Config.Unobtanium.allowHarvest;
//	}
//
//	
//	@Override
//	public Item getItemDropped(IBlockState state, Random rand, int fortune)
//	{
//		if(this.material != BaseMaterial.HYPERSTONE) return super.getItemDropped(state, rand, fortune);
//		return Unobtanium.itemUnobtaniumRubble;
//	}
//
//	@Override
//    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
//    {
//		if(this.material != BaseMaterial.HYPERSTONE) return super.getDrops(world, pos, state, fortune);
//		
//        List<ItemStack> retVal = new java.util.ArrayList<ItemStack>();
//
//        NiceTileEntity niceTE = (NiceTileEntity)world.getTileEntity(pos);
//        if(niceTE != null)
//        {
//        	int dropCount = Math.round(9 * (100 - niceTE.getDamage()) / 100);
//        	 retVal.add(new ItemStack(Unobtanium.itemUnobtaniumRubble, dropCount, this.damageDropped(state)));
//        }
//        return retVal;
//    }
//    
//	
//	@Override
//	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
//		if (!world.isRemote) {
//			final double r = Math.random();
//			if (r < Config.Unobtanium.selfRepairChance) {
//				final int meta = world.getBlockMetadata(pos);
//				if ((meta & BITMASK_DAMAGE) > 0) {
//					if (Config.Unobtanium.selfRepairFailedBlocks || (meta & BITMASK_DAMAGE) < DAMAGE_MAX) {
//						world.setBlockMetadataWithNotify(pos, meta - 1, 3);
//						if (Config.Unobtanium.logging) {
//							Adversity.log.info(String.format("Unobtanium block self repaired at %d %d %d.", x, y, z));
//						}
//					}
//				}
//			}
//		}
//	}
//
//	@Override
//	public float getBlockHardness(World world, int x, int y, int z) {
//		if (Config.Unobtanium.canBeDamaged) {
//			final int damage = world.getBlockMetadata(x, y, z) & BITMASK_DAMAGE;
//			return (float) (Config.Unobtanium.startingHardness * Math.pow(Config.Unobtanium.hardnessDamageFactor,
//					damage));
//		} else
//			return Config.Unobtanium.startingHardness;
//	}
//
//	@Override
//	public float getExplosionResistance(Entity entity) {
//		// no way to know specific resistance in this case, so just return max
//		if (Config.Unobtanium.logging) {
//			Adversity.log.info("getExplosionResistance(Entity) from "
//					+ (entity == null ? "unknown entity" : entity.getClass().getSimpleName()) + " result="
//					+ Unobtanium.intactBlastResistance);
//		}
//
//		return Unobtanium.intactBlastResistance;
//
//	}
//
//	@Override
//	public boolean canEntityDestroy(IBlockAccess world, int x, int y, int z, Entity entity) {
//
//		final int meta = world.getBlockMetadata(x, y, z);
//
//		// only failed blocks can be destroyed
//		final boolean destroyFlag = Config.Unobtanium.canBeDamaged && (meta & BITMASK_DAMAGE) == DAMAGE_MAX;
//		boolean damageFlag = false;
//
//		// intact blocks still have a chance to be damaged
//		if (Config.Unobtanium.canBeDamaged && entity != null
//				&& Math.random() <= Config.Unobtanium.destroyedFailureChance) {
//			entity.worldObj.setBlockMetadataWithNotify(x, y, z, meta + 1, 3);
//			damageFlag = true;
//		}
//
//		if (Config.Unobtanium.logging) {
//			Adversity.log.info(String.format("canEntityDestroy at %d %d %d from %s result=%s", x, y, z,
//					entity == null ? "unknown entity" : entity.getClass().getSimpleName(), destroyFlag ? "allowed"
//							: damageFlag ? "denied & took damage" : "denied & took no damage"));
//		}
//
//		return destroyFlag;
//	}
//
//	@Override
//	public float getExplosionResistance(Entity entity, World world, int x, int y, int z, double explosionX,
//			double explosionY, double explosionZ) {
//
//		final int meta = world.getBlockMetadata(x, y, z);
//
//		// if block has fully failed, always handled like a normal block
//		if ((meta & BITMASK_DAMAGE) == DAMAGE_MAX) {
//			if (Config.Unobtanium.logging) {
//				Adversity.log.info(String.format(
//						"getExploisionResistance from %s for failed block at %d %d %d returned %d",
//						entity == null ? "unknown entity" : entity.getClass().getSimpleName(), x, y, z,
//								Config.Unobtanium.failedBlastResistance));
//			}
//			return Config.Unobtanium.failedBlastResistance;
//		}
//
//		if (Config.Unobtanium.canBeDamaged && Math.random() <= Config.Unobtanium.resistanceFailureChance) {
//			world.setBlockMetadataWithNotify(x, y, z, meta + 1, 3);
//			if (Config.Unobtanium.logging) {
//				Adversity.log.info(String.format("getExploisionResistance from %s damaged intact block at %d %d %d",
//						entity == null ? "unknown entity" : entity.getClass().getSimpleName(), x, y, z));
//			}
//		}
//
//		// unless we were in full failure at start, we always absorb the full blast
//		return Config.Unobtanium.intactBlastResistance;
//
//	}
//
//	@Override
//	public void onBlockExploded(World world, int x, int y, int z, Explosion explosion) {
//		final int meta = world.getBlockMetadata(x, y, z);
//		String message = "";
//
//		if (Config.Unobtanium.logging) {
//			message = String.format("onBlockExploded at %d %d %d from %s", x, y, z,
//					explosion != null ? explosion.toString() : "unidentified explosion");
//		}
//
//		if ((meta & BITMASK_DAMAGE) == DAMAGE_MAX) {
//			super.onBlockExploded(world, x, y, z, explosion);
//			if (Config.Unobtanium.logging) {
//				Adversity.log.info(message + " Result: block exploded");
//			}
//		} else if (Math.random() <= Config.Unobtanium.explodedFailureChance) {
//			world.setBlockMetadataWithNotify(x, y, z, meta + 1, 3);
//			if (Config.Unobtanium.logging) {
//				Adversity.log.info(message + " Result: block damaged");
//			}
//		} else if (Config.Unobtanium.logging) {
//			Adversity.log.info(message + " Result: no damage");
//		}
//
//	}
//
//	
//	@Override
//	public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {
//		if (!Unobtanium.allowFire && pos.getY() < 256 && neighborBlock == Blocks.fire) {
//			world.setBlock(x, y + 1, z, Blocks.air);
//		};
//	}
//}
