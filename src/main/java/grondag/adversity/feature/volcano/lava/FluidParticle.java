package grondag.adversity.feature.volcano.lava;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

public class FluidParticle extends EntityThrowable
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
        super(tracker.world, position.xCoord, position.yCoord, position.zCoord);
        this.tracker = tracker;
        this.amount = amount;
        this.id = nextParticleID++;
        this.positionX = position.xCoord;
        this.positionY = position.yCoord;
        this.positionZ = position.zCoord;
        
        this.velocityX = velocity.xCoord;
        this.velocityY = velocity.yCoord;
        this.velocityZ = velocity.zCoord;
        
        tracker.allParticles.add(this);
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
    
    public float getAmount()
    {
        return this.amount;
    }
    
//    public Vec3d getPosition()
//    {
//        return new Vec3d(this.positionX, this.positionY, this.positionZ);
//    }
    

    @Override
    public BlockPos getPosition()
    {
        return new BlockPos(this.positionX, this.positionY, this.positionZ);
    }

    @Override
    protected void onImpact(RayTraceResult result)
    {
        if (!this.worldObj.isRemote && result.typeOfHit ==  RayTraceResult.Type.BLOCK)
        {
           FluidCell cell = tracker.getCell(result.getBlockPos());
           if(cell.canAcceptFluidDirectly(tracker))
           {
               tracker.addLava(result.getBlockPos(), amount);
               this.setDead();
           }
        }
    }
}
