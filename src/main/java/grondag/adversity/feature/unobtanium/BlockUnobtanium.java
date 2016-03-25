package com.grondag.adversity.feature.unobtanium;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.grondag.adversity.Adversity;
import com.grondag.adversity.Config;
import com.grondag.adversity.Config.Unobtanium;
import com.grondag.adversity.deprecate.OddBlock;

public class BlockUnobtanium extends OddBlock {

	// This is the max value any block can have before it is destroyed - can take 7 hits.
	public static final int		DAMAGE_MAX		= 7;
	private static final int	BITMASK_DAMAGE	= 0x7;

	public BlockUnobtanium(String unlocalizedName, Material material) {
		super(unlocalizedName, material);
		this.setBlockTextureName(Adversity.MODID + ":unob_white_dmg0_0");
		this.setHarvestLevel("pickaxe", 3);
		this.setStepSound(soundTypeMetal);
		this.needsRandomTick = Config.Unobtanium.selfRepairChance > 0;
	}

	@Override
	public boolean canCreatureSpawn(EnumCreatureType type, IBlockAccess world, int x, int y, int z) {
		if (Config.Unobtanium.allowMobSpawning)
			return super.canCreatureSpawn(type, world, x, y, z);
		else
			return false;

	}

	@Override
	public boolean canDropFromExplosion(Explosion p_149659_1_) {
		return true;
	}

	@Override
	protected boolean canSilkHarvest() {
		return Config.Unobtanium.allowHarvest && Config.Unobtanium.allowSilkTouch;
	}

	@Override
	public boolean canSilkHarvest(World world, EntityPlayer player, int x, int y, int z, int metadata) {
		return Config.Unobtanium.allowHarvest && Config.Unobtanium.allowSilkTouch;
	}

	@Override
	public boolean canHarvestBlock(EntityPlayer player, int meta) {
		return Config.Unobtanium.allowHarvest;
	}

	@Override
	public Item getItemDropped(int meta, Random random, int fortune) {
		return com.grondag.adversity.feature.unobtanium.Unobtanium.itemUnobtaniumRubble;
	}

	@Override
	public int quantityDropped(int meta, int fortune, Random random) {
		return Math.max(1, (4 + random.nextInt(4) + fortune - (meta & BITMASK_DAMAGE)) / 2);
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random random) {
		if (!world.isRemote) {
			final double r = Math.random();
			if (r < Config.Unobtanium.selfRepairChance) {
				final int meta = world.getBlockMetadata(x, y, z);
				if ((meta & BITMASK_DAMAGE) > 0) {
					if (Config.Unobtanium.selfRepairFailedBlocks || (meta & BITMASK_DAMAGE) < DAMAGE_MAX) {
						world.setBlockMetadataWithNotify(x, y, z, meta - 1, 3);
						if (Config.Unobtanium.logging) {
							Adversity.log.info(String.format("Unobtanium block self repaired at %d %d %d.", x, y, z));
						}
					}
				}
			}
		}
	}

	@Override
	public float getBlockHardness(World world, int x, int y, int z) {
		if (Config.Unobtanium.canBeDamaged) {
			final int damage = world.getBlockMetadata(x, y, z) & BITMASK_DAMAGE;
			return (float) (Config.Unobtanium.startingHardness * Math.pow(Config.Unobtanium.hardnessDamageFactor,
					damage));
		} else
			return Config.Unobtanium.startingHardness;
	}

	@Override
	public float getExplosionResistance(Entity entity) {
		// no way to know specific resistance in this case, so just return max
		if (Config.Unobtanium.logging) {
			Adversity.log.info("getExplosionResistance(Entity) from "
					+ (entity == null ? "unknown entity" : entity.getClass().getSimpleName()) + " result="
					+ Unobtanium.intactBlastResistance);
		}

		return Unobtanium.intactBlastResistance;

	}

	@Override
	public boolean canEntityDestroy(IBlockAccess world, int x, int y, int z, Entity entity) {

		final int meta = world.getBlockMetadata(x, y, z);

		// only failed blocks can be destroyed
		final boolean destroyFlag = Config.Unobtanium.canBeDamaged && (meta & BITMASK_DAMAGE) == DAMAGE_MAX;
		boolean damageFlag = false;

		// intact blocks still have a chance to be damaged
		if (Config.Unobtanium.canBeDamaged && entity != null
				&& Math.random() <= Config.Unobtanium.destroyedFailureChance) {
			entity.worldObj.setBlockMetadataWithNotify(x, y, z, meta + 1, 3);
			damageFlag = true;
		}

		if (Config.Unobtanium.logging) {
			Adversity.log.info(String.format("canEntityDestroy at %d %d %d from %s result=%s", x, y, z,
					entity == null ? "unknown entity" : entity.getClass().getSimpleName(), destroyFlag ? "allowed"
							: damageFlag ? "denied & took damage" : "denied & took no damage"));
		}

		return destroyFlag;
	}

	@Override
	public float getExplosionResistance(Entity entity, World world, int x, int y, int z, double explosionX,
			double explosionY, double explosionZ) {

		final int meta = world.getBlockMetadata(x, y, z);

		// if block has fully failed, always handled like a normal block
		if ((meta & BITMASK_DAMAGE) == DAMAGE_MAX) {
			if (Config.Unobtanium.logging) {
				Adversity.log.info(String.format(
						"getExploisionResistance from %s for failed block at %d %d %d returned %d",
						entity == null ? "unknown entity" : entity.getClass().getSimpleName(), x, y, z,
								Config.Unobtanium.failedBlastResistance));
			}
			return Config.Unobtanium.failedBlastResistance;
		}

		if (Config.Unobtanium.canBeDamaged && Math.random() <= Config.Unobtanium.resistanceFailureChance) {
			world.setBlockMetadataWithNotify(x, y, z, meta + 1, 3);
			if (Config.Unobtanium.logging) {
				Adversity.log.info(String.format("getExploisionResistance from %s damaged intact block at %d %d %d",
						entity == null ? "unknown entity" : entity.getClass().getSimpleName(), x, y, z));
			}
		}

		// unless we were in full failure at start, we always absorb the full blast
		return Config.Unobtanium.intactBlastResistance;

	}

	// TODO need a custom getRenderType even though is standard render so that ReactorCraft does not
	// explode us and turn us into a flying block
	// see https://github.com/ReikaKalseki/ReactorCraft/blob/master/Auxiliary/HydrogenExplosion.java

	@Override
	public void onBlockExploded(World world, int x, int y, int z, Explosion explosion) {
		final int meta = world.getBlockMetadata(x, y, z);
		String message = "";

		if (Config.Unobtanium.logging) {
			message = String.format("onBlockExploded at %d %d %d from %s", x, y, z,
					explosion != null ? explosion.toString() : "unidentified explosion");
		}

		if ((meta & BITMASK_DAMAGE) == DAMAGE_MAX) {
			super.onBlockExploded(world, x, y, z, explosion);
			if (Config.Unobtanium.logging) {
				Adversity.log.info(message + " Result: block exploded");
			}
		} else if (Math.random() <= Config.Unobtanium.explodedFailureChance) {
			world.setBlockMetadataWithNotify(x, y, z, meta + 1, 3);
			if (Config.Unobtanium.logging) {
				Adversity.log.info(message + " Result: block damaged");
			}
		} else if (Config.Unobtanium.logging) {
			Adversity.log.info(message + " Result: no damage");
		}

	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
		if (!Unobtanium.allowFire && y < 256 && world.getBlock(x, y + 1, z) == Blocks.fire) {
			world.setBlock(x, y + 1, z, Blocks.air);
		}
	}

	@Override
	public void registerBlockIcons(IIconRegister reg) {

		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 8; j++) {
				this.icons[i * 16 + j] = reg.registerIcon(Adversity.MODID + ":unob_white_dmg" + j + "_" + i);
				this.icons[i * 16 + j + 8] = reg.registerIcon(Adversity.MODID + ":unob_gray_dmg" + j + "_" + i);
			}

		}
	}

	@Override
	public int getBlockColor() {
		// TODO Auto-generated method stub
		return super.getBlockColor();
	}

	@Override
	public int getRenderColor(int p_149741_1_) {
		// TODO Auto-generated method stub
		return super.getRenderColor(p_149741_1_);
	}

	@Override
	public int colorMultiplier(IBlockAccess p_149720_1_, int p_149720_2_, int p_149720_3_, int p_149720_4_) {
		// TODO Auto-generated method stub
		return 0xFF0505;
	}

}
