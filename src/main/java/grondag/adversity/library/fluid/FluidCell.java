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
    
    public LinkedList<FluidSimParticle> particles;
    
    public void addParticle(FluidSimParticle particle)
    {
        if(this.particles == null) this.particles = new LinkedList<FluidSimParticle>();
    }
}
