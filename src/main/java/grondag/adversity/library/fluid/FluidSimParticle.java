package grondag.adversity.library.fluid;

import javax.vecmath.Vector3f;

import net.minecraft.util.math.Vec3d;

public class FluidSimParticle
{
    private static int nextParticleID = 0;
    
    protected byte fluidUnits;
    protected float mass;
    
    protected Vec3d position;
    protected Vec3d velocity;
    
    protected Vec3d acceleration;
    protected Vec3d ev;
    
    protected float pressure;
    protected float density;
    
    protected FluidSimParticle nextParticle;
    
    //TODO: not sure if this or if index is needed
    public final int id;
    public int index;
    
    public FluidSimParticle(Vec3d position, Vec3d velocity, FluidSimulator sim)
    {
        this.position = position;
        this.velocity = velocity;
        this.fluidUnits = (byte) fluidUnits;
        this.id = nextParticleID++;
        this.acceleration = new Vec3d(0, 0, 0);
        this.ev = new Vec3d(0, 0, 0);

        this.pressure = 0.0F;
        this.nextParticle = null;
        this.density = sim.fluidDensity;

    }
}
