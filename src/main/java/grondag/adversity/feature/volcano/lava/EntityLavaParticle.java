package grondag.adversity.feature.volcano.lava;

import java.util.List;

import grondag.adversity.Adversity;
import grondag.adversity.niceblock.NiceBlockRegistrar;
import grondag.adversity.niceblock.base.IFlowBlock;
import grondag.adversity.simulator.Simulator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockWall;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityLavaParticle extends Entity
{
    private static int nextParticleID;

    private final int id;

    private static final String TAG_AMOUNT = "amt";

    private float renderScale;

    private static final DataParameter<Integer> FLUID_AMOUNT = EntityDataManager.<Integer>createKey(EntityLavaParticle.class, DataSerializers.VARINT);

    @Override
    public int hashCode()
    {
        return this.id;
    }

    protected EntityLavaParticle(World world, int amount, Vec3d position, Vec3d velocity)
    {
        this(world, amount);
//        if(!world.isRemote) Adversity.log.info("EntityLavaParticle amount=" + amount + " @" + position.toString());
        this.setPosition(position.xCoord, position.yCoord, position.zCoord);


        this.motionX = velocity.xCoord;
        this.motionY = velocity.yCoord;
        this.motionZ = velocity.zCoord;

    }

    public EntityLavaParticle(World world)
    {
        this(world, LavaCell.FLUID_UNITS_PER_BLOCK);
//        if(!world.isRemote) Adversity.log.info("EntityLavaParticle no params");
    }

    public EntityLavaParticle(World world, int amount)
    {
        super(world);
//        if(!world.isRemote) Adversity.log.info("EntityLavaParticle amount=" + amount);
        this.id = nextParticleID++;
        if(!world.isRemote)
        {
            this.dataManager.set(FLUID_AMOUNT, Integer.valueOf(amount)); 
        }
        this.forceSpawn = true;
        this.updateAmountDependentData();
    }

    private void updateAmountDependentData()
    {
        float unitAmout = (float)this.dataManager.get(FLUID_AMOUNT).intValue() / LavaCell.FLUID_UNITS_PER_BLOCK;

        // Give bounding box same volume as model, but small enough to fit through a one block space and not too small to interact
        float edgeLength = (float) Math.min(0.8, Math.max(0.1, Math.pow(unitAmout, 0.3333333333333)));
        this.setSize(edgeLength, edgeLength);

        /**
         * Is essentially the diameter of a sphere with volume = amount.
         */
        this.renderScale = (float) (2 * Math.pow(unitAmout * 3 / (Math.PI * 4), 1F/3F));

        //        Adversity.log.info("Particle @" + this.getPosition().toString() + " has edgeLength=" + edgeLength + "  and scale=" + renderScale);
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key)
    {
        super.notifyDataManagerChange(key);

        //resize once we have our amount
        if (FLUID_AMOUNT.equals(key) && this.worldObj.isRemote)
        {
            this.updateAmountDependentData();
        }
    }



    /**
     * Sets throwable heading based on an entity that's throwing it
     */
    public void setHeadingFromThrower(Entity entityThrower, float rotationPitchIn, float rotationYawIn, float pitchOffset, float velocity, float inaccuracy)
    {
        float f = -MathHelper.sin(rotationYawIn * 0.017453292F) * MathHelper.cos(rotationPitchIn * 0.017453292F);
        float f1 = -MathHelper.sin((rotationPitchIn + pitchOffset) * 0.017453292F);
        float f2 = MathHelper.cos(rotationYawIn * 0.017453292F) * MathHelper.cos(rotationPitchIn * 0.017453292F);
        this.setThrowableHeading((double)f, (double)f1, (double)f2, velocity, inaccuracy);
        this.motionX += entityThrower.motionX;
        this.motionZ += entityThrower.motionZ;

        if (!entityThrower.onGround)
        {
            this.motionY += entityThrower.motionY;
        }
    }

    /**
     * Similar to setArrowHeading, it's point the throwable entity to a x, y, z direction.
     */
    public void setThrowableHeading(double x, double y, double z, float velocity, float inaccuracy)
    {
        float f = MathHelper.sqrt_double(x * x + y * y + z * z);
        x = x / (double)f;
        y = y / (double)f;
        z = z / (double)f;
        x = x + this.rand.nextGaussian() * 0.007499999832361937D * (double)inaccuracy;
        y = y + this.rand.nextGaussian() * 0.007499999832361937D * (double)inaccuracy;
        z = z + this.rand.nextGaussian() * 0.007499999832361937D * (double)inaccuracy;
        x = x * (double)velocity;
        y = y * (double)velocity;
        z = z * (double)velocity;
        this.motionX = x;
        this.motionY = y;
        this.motionZ = z;
        float f1 = MathHelper.sqrt_double(x * x + z * z);
        this.rotationYaw = (float)(MathHelper.atan2(x, z) * (180D / Math.PI));
        this.rotationPitch = (float)(MathHelper.atan2(y, (double)f1) * (180D / Math.PI));
        this.prevRotationYaw = this.rotationYaw;
        this.prevRotationPitch = this.rotationPitch;
    }

    public float getScale()
    {
        return this.renderScale;
    }

    @Override
    public void onUpdate()
    {
        //    Adversity.log.info("onUpdate id=" + this.id + " starting x,y,z=" + this.getPositionVector().toString());

        //TODO: remove
        if(this.isDead) 
            Adversity.log.info("Dead entity derp");

        super.onUpdate();

        if(this.ticksExisted > 600)
        {
            Adversity.log.info("Too long. What's up?");
        }



        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        this.motionY -= 0.03999999910593033D;

        this.moveEntity(this.motionX, this.motionY, this.motionZ);

        this.motionX *= 0.9800000190734863D;
        this.motionY *= 0.9800000190734863D;
        this.motionZ *= 0.9800000190734863D;

        if (!this.worldObj.isRemote & this.onGround)
        {
//            Adversity.log.info("particle landing @" + this.getPosition().toString() + " amount=" + this.dataManager.get(FLUID_AMOUNT).floatValue());

            Simulator.instance.getFluidTracker().addLava(this.getPosition(), this.dataManager.get(FLUID_AMOUNT).intValue());
            //            this.worldObj.setBlockState(this.getPosition(), NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK.getDefaultState());
            this.shouldDie = true;
            this.setDead();
        }

        //TODO: handle webs, tree leaves and other destructable blocks


        //        Adversity.log.info("onUpdate id=" + this.id + " ending x,y,z=" + this.getPositionVector().toString());

    }

    //TODO: remove, was for debug

    private boolean shouldDie = false;
    @Override
    public void setDead()
    {
        if(!this.worldObj.isRemote & !this.shouldDie)
        {
            Adversity.log.info("unintended particle death");
        }
        super.setDead();
    }

    /**
     * Tries to move the entity towards the specified location.
     */
    @Override
    public void moveEntity(double x, double y, double z)
    {
        this.worldObj.theProfiler.startSection("move");

        //shouldn't actually happen because web will be set to air before we collide, but just in case...
        if (this.isInWeb)
        {
            this.isInWeb = false;
            this.worldObj.setBlockToAir(this.getPosition());
        }


        AxisAlignedBB targetBox = this.getEntityBoundingBox().addCoord(x, y, z);

        this.destroyCollidingDisplaceableBlocks(targetBox);

        double startingX = x;
        double startingY = y;
        double startingZ = z;

        List<AxisAlignedBB> blockCollisions = this.worldObj.getCollisionBoxes(targetBox);

        //TODO: entity collisions and damage
        //            AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
        int i = 0;

        for (int j = blockCollisions.size(); i < j; ++i)
        {
            y = ((AxisAlignedBB)blockCollisions.get(i)).calculateYOffset(this.getEntityBoundingBox(), y);
        }

        this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, y, 0.0D));
        //            boolean i_ = this.onGround || startingY != y && startingY < 0.0D;
        int j4 = 0;

        for (int k = blockCollisions.size(); j4 < k; ++j4)
        {
            x = ((AxisAlignedBB)blockCollisions.get(j4)).calculateXOffset(this.getEntityBoundingBox(), x);
        }

        this.setEntityBoundingBox(this.getEntityBoundingBox().offset(x, 0.0D, 0.0D));
        j4 = 0;

        for (int k4 = blockCollisions.size(); j4 < k4; ++j4)
        {
            z = ((AxisAlignedBB)blockCollisions.get(j4)).calculateZOffset(this.getEntityBoundingBox(), z);
        }

        this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, 0.0D, z));

        this.worldObj.theProfiler.endSection();
        this.worldObj.theProfiler.startSection("rest");
        this.resetPositionToBB();
        this.isCollidedHorizontally = startingX != x || startingZ != z;
        this.isCollidedVertically = startingY != y;
        //        this.onGround = this.isCollidedVertically && startingY < 0.0D;
        //having negative Y offset is not determinative because tops of blocks can
        //force us up even if we aren't really fully on top of them.
        this.isCollided = this.isCollidedHorizontally || this.isCollidedVertically;
        j4 = MathHelper.floor_double(this.posX);
        int l4 = MathHelper.floor_double(this.posY - 0.20000000298023224D);
        int i5 = MathHelper.floor_double(this.posZ);
        BlockPos blockpos = new BlockPos(j4, l4, i5);
        IBlockState iblockstate = this.worldObj.getBlockState(blockpos);
        this.onGround = this.isCollidedVertically && !LavaTerrainHelper.canLavaDisplace(iblockstate);

        //this is very crude, but if we are vertically collided but not resting on top of the ground
        //re-center on our block pos so that we have a better chance to fall down
        if(this.isCollidedVertically && !this.onGround)
        {
            this.setEntityBoundingBox(this.getEntityBoundingBox().offset(this.posX - (blockpos.getX() +0.5), 0.0D, this.posZ - (blockpos.getZ() +0.5)));
            this.resetPositionToBB();
        }

        if (iblockstate.getMaterial() == Material.AIR)
        {
            BlockPos blockpos1 = blockpos.down();
            IBlockState iblockstate1 = this.worldObj.getBlockState(blockpos1);
            Block block1 = iblockstate1.getBlock();

            if (block1 instanceof BlockFence || block1 instanceof BlockWall || block1 instanceof BlockFenceGate)
            {
                iblockstate = iblockstate1;
                blockpos = blockpos1;
            }
        }

        //TODO: use it or get rid of it?
        this.updateFallState(y, this.onGround, iblockstate, blockpos);

        //because lava is sticky, want to stop all horizontal motion once collided
        if (startingX != x || startingZ != z)
        {
            this.motionX = 0.0D;
            this.motionZ = 0.0D;
        }

        //        if (startingX != x)
        //        {
        //            this.motionX = 0.0D;
        //        }
        //
        //        if (startingZ != z)
        //        {
        //            this.motionZ = 0.0D;
        //        }



        Block block = iblockstate.getBlock();

        if (startingY != y)
        {
            block.onLanded(this.worldObj, this);
        }

        //TODO: is needed?
        try
        {
            this.doBlockCollisions();
        }
        catch (Throwable throwable)
        {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Checking entity block collision");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity being checked for collision");
            this.addEntityCrashInfo(crashreportcategory);
            throw new ReportedException(crashreport);
        }


        this.worldObj.theProfiler.endSection();

    }

    private void destroyCollidingDisplaceableBlocks(AxisAlignedBB bb)
    {
        int i = MathHelper.floor_double(bb.minX);
        int j = MathHelper.ceiling_double_int(bb.maxX);
        int k = MathHelper.floor_double(bb.minY);
        int l = MathHelper.ceiling_double_int(bb.maxY);
        int i1 = MathHelper.floor_double(bb.minZ);
        int j1 = MathHelper.ceiling_double_int(bb.maxZ);
        BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();

        for (int k1 = i; k1 < j; ++k1)
        {
            for (int l1 = k; l1 < l; ++l1)
            {
                for (int i2 = i1; i2 < j1; ++i2)
                {
                    blockpos$pooledmutableblockpos.setPos(k1, l1, i2);
                    IBlockState state = worldObj.getBlockState(blockpos$pooledmutableblockpos);
                    if(state.getMaterial() != Material.AIR && !IFlowBlock.isFlowFiller(state.getBlock()) && LavaTerrainHelper.canLavaDisplace(state))
                    {
                        this.worldObj.destroyBlock(blockpos$pooledmutableblockpos.toImmutable(), true);
                    }
                }
            }
        }
        blockpos$pooledmutableblockpos.release();
    }


    @Override
    protected void readEntityFromNBT(NBTTagCompound compound)
    {
        this.dataManager.set(FLUID_AMOUNT, Integer.valueOf(compound.getInteger(TAG_AMOUNT)));
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound)
    {
        compound.setInteger(TAG_AMOUNT, this.dataManager.get(FLUID_AMOUNT).intValue());
    }

    @Override
    protected void entityInit()
    {
        this.dataManager.register(FLUID_AMOUNT, LavaCell.FLUID_UNITS_PER_BLOCK);
    }
    
    @SideOnly(Side.CLIENT)
    public int getBrightnessForRender(float partialTicks)
    {
        return 15728880;
    }

}
