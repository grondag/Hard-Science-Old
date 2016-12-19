package grondag.adversity.library.fluid;

import java.util.LinkedList;

public class FluidCell
{
    public float density;
    
//    /**
//     * How much fluid cell can contain
//     * Range is 0 to PARTICLES_PER_BLOCK
//     */  
//    public byte capacity;
    
    public LinkedList<FluidParticle> particles;
    
    public void addParticle(FluidParticle particle)
    {
        if(this.particles == null) this.particles = new LinkedList<FluidParticle>();
    }
}
