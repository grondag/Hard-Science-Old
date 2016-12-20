package grondag.adversity.feature.volcano.lava;

import java.util.List;

import grondag.adversity.Adversity;
import grondag.adversity.feature.volcano.LavaManager;
import grondag.adversity.niceblock.NiceBlockRegistrar;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockWall;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class FluidParticle extends Entity
{
    private static int nextParticleID;

    private final float amount;
    private final int id;
    private double positionX;
    private double positionY;
    private double positionZ;
    private double velocityX;
    private double velocityY;
    private double velocityZ;

    private final FluidTracker tracker;


    @Override
    public int hashCode()
    {
        return this.id;
    }

    protected FluidParticle(FluidTracker tracker, float amount, Vec3d position, Vec3d velocity)
    {
        this(tracker.world, amount);
        this.setPosition(position.xCoord, position.yCoord, position.zCoord);


        this.motionX = velocity.xCoord;
        this.motionY = velocity.yCoord;
        this.motionZ = velocity.zCoord;

        //        this.positionX = position.xCoord;
        //        this.positionY = position.yCoord;
        //        this.positionZ = position.zCoord;
        //        
        //        this.velocityX = velocity.xCoord;
        //        this.velocityY = velocity.yCoord;
        //        this.velocityZ = velocity.zCoord;

        tracker.allParticles.add(this);
    }

    public FluidParticle(World world)
    {
        this(world, 1);
    }

    public FluidParticle(World world, float amount)
    {
        super(world);
        this.id = nextParticleID++;
        if(FluidTracker.egregiousHack == null)
        {
            FluidTracker.egregiousHack = new FluidTracker(world);
        }
        this.tracker = FluidTracker.egregiousHack;
        this.amount = amount;
        this.forceSpawn = true;
        this.setSize(1F, 1F);
    }

    public void doStep(double seconds)
    {
        double blockX = Math.floor(this.positionX);
        double blockY = Math.floor(this.positionY);
        double blockZ = Math.floor(this.positionZ);

        this.velocityY -= seconds * 9.8;
        this.positionX += this.velocityX * seconds;
        this.positionY += this.velocityY * seconds;
        this.positionZ += this.velocityZ * seconds;

        if(blockX != Math.floor(this.positionX)
                || blockY != Math.floor(this.positionY)
                || blockZ != Math.floor(this.positionZ))
        {
            tracker.movedParticles.add(this);
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

    public float getAmount()
    {
        return this.amount;
    }

    //    public Vec3d getPosition()
    //    {
    //        return new Vec3d(this.positionX, this.positionY, this.positionZ);
    //    }


    //    @Override
    //    public BlockPos getPosition()
    //    {
    //        return new BlockPos(this.positionX, this.positionY, this.positionZ);
    //    }



    @Override
    protected void entityInit()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void onUpdate()
    {
        //    Adversity.log.info("onUpdate id=" + this.id + " starting x,y,z=" + this.getPositionVector().toString());

        super.onUpdate();


        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        this.motionY -= 0.03999999910593033D;

        this.noClip = this.pushOutOfBlocks(this.posX, (this.getEntityBoundingBox().minY + this.getEntityBoundingBox().maxY) / 2.0D, this.posZ);
        this.moveEntity(this.motionX, this.motionY, this.motionZ);

        this.motionX *= 0.9800000190734863D;
        this.motionY *= 0.9800000190734863D;
        this.motionZ *= 0.9800000190734863D;

        if (!this.worldObj.isRemote & !LavaManager.canDisplace(this.worldObj.getBlockState(this.getPosition().down())))
        {
            this.worldObj.setBlockState(this.getPosition(), NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK.getDefaultState());
            this.setDead();
        }

        //TODO: handle webs, tree leaves and other destructable blocks
        

        //        Adversity.log.info("onUpdate id=" + this.id + " ending x,y,z=" + this.getPositionVector().toString());

    }
    
    /**
     * Tries to move the entity towards the specified location.
     */
    @Override
    public void moveEntity(double x, double y, double z)
    {
        if (this.noClip)
        {
            this.setEntityBoundingBox(this.getEntityBoundingBox().offset(x, y, z));
            this.resetPositionToBB();
        }
        else
        {
            this.worldObj.theProfiler.startSection("move");

            //shouldn't actually happen because web will be set to air before we collide, but just in case...
            if (this.isInWeb)
            {
                this.isInWeb = false;
                this.worldObj.setBlockToAir(this.getPosition());
            }
            

            AxisAlignedBB targetBox = this.getEntityBoundingBox().addCoord(x, y, z);
//            if(!this.worldObj.isRemote)
//            {
                this.destroyCollidingDisplaceableBlocks(targetBox);
//            }
            
            double startingX = x;
            double startingY = y;
            double startingZ = z;
           
            List<AxisAlignedBB> list1 = this.worldObj.getCollisionBoxes(this, targetBox);
//            AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
            int i = 0;

            for (int j = list1.size(); i < j; ++i)
            {
                y = ((AxisAlignedBB)list1.get(i)).calculateYOffset(this.getEntityBoundingBox(), y);
            }

            this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, y, 0.0D));
//            boolean i_ = this.onGround || startingY != y && startingY < 0.0D;
            int j4 = 0;

            for (int k = list1.size(); j4 < k; ++j4)
            {
                x = ((AxisAlignedBB)list1.get(j4)).calculateXOffset(this.getEntityBoundingBox(), x);
            }

            this.setEntityBoundingBox(this.getEntityBoundingBox().offset(x, 0.0D, 0.0D));
            j4 = 0;

            for (int k4 = list1.size(); j4 < k4; ++j4)
            {
                z = ((AxisAlignedBB)list1.get(j4)).calculateZOffset(this.getEntityBoundingBox(), z);
            }

            this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, 0.0D, z));

            this.worldObj.theProfiler.endSection();
            this.worldObj.theProfiler.startSection("rest");
            this.resetPositionToBB();
            this.isCollidedHorizontally = startingX != x || startingZ != z;
            this.isCollidedVertically = startingY != y;
            this.onGround = this.isCollidedVertically && startingY < 0.0D;
            this.isCollided = this.isCollidedHorizontally || this.isCollidedVertically;
            j4 = MathHelper.floor_double(this.posX);
            int l4 = MathHelper.floor_double(this.posY - 0.20000000298023224D);
            int i5 = MathHelper.floor_double(this.posZ);
            BlockPos blockpos = new BlockPos(j4, l4, i5);
            IBlockState iblockstate = this.worldObj.getBlockState(blockpos);

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

            this.updateFallState(y, this.onGround, iblockstate, blockpos);

            if (startingX != x)
            {
                this.motionX = 0.0D;
            }

            if (startingZ != z)
            {
                this.motionZ = 0.0D;
            }

            Block block = iblockstate.getBlock();

            if (startingY != y)
            {
                block.onLanded(this.worldObj, this);
            }

    
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

                    if(!worldObj.isAirBlock(blockpos$pooledmutableblockpos) && LavaManager.canDisplace(worldObj.getBlockState(blockpos$pooledmutableblockpos)))
                    {
                        this.worldObj.destroyBlock(blockpos$pooledmutableblockpos.toImmutable(), true);
                    }
                }
            }
        }
        blockpos$pooledmutableblockpos.release();
    }
    
    @Override
    public void onEntityUpdate()
    {
//        Adversity.log.info("onEntityUpdate id=" + this.id);
        // TODO Auto-generated method stub
        super.onEntityUpdate();
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound)
    {
        // TODO Auto-generated method stub

    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setDead()
    {
        Adversity.log.info("setDead id=" + this.id);
        // TODO Auto-generated method stub
        super.setDead();
    }

    @Override
    protected void kill()
    {
        // TODO Auto-generated method stub
        Adversity.log.info("kill id=" + this.id);
        super.kill();
    }

    @Override
    public boolean shouldRenderInPass(int pass)
    {
        // TODO Auto-generated method stub
        return super.shouldRenderInPass(pass);
    }


}
