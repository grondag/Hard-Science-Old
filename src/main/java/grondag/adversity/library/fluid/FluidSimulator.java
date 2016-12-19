package grondag.adversity.library.fluid;

import java.util.Collections;
import java.util.LinkedList;

import javax.vecmath.Vector3f;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class FluidSimulator
{
    
    public final static int MAX_PARTICLES = 10000;
    public final static float GRAVITY = -9.8F;
    
//    public final static float RESTING_DENSITY = 1000F;

//    kernel=0.04f;
//    mass=0.02f;
//
//    world_size.x=0.64f;
//    world_size.y=0.64f;
//    world_size.z=0.64f;
//    cell_size=kernel;
//    grid_size.x=(uint)ceil(world_size.x/cell_size);
//    grid_size.y=(uint)ceil(world_size.y/cell_size);
//    grid_size.z=(uint)ceil(world_size.z/cell_size);
//    tot_cell=grid_size.x*grid_size.y*grid_size.z;

//    gravity.x=0.0f; 
//    gravity.y=-6.8f;
//    gravity.z=0.0f;
//    wall_damping=-0.5f;

//    gas_constant=1.0f;
//    viscosity=6.5f;
//    time_step=0.003f;
//    surf_norm=6.0f;
//    surf_coe=0.1f;
//
//    poly6_value=315.0f/(64.0f * PI * pow(kernel, 9));;
//    spiky_value=-45.0f/(PI * pow(kernel, 6));
//    visco_value=45.0f/(PI * pow(kernel, 6));
//
//    grad_poly6=-945/(32 * PI * pow(kernel, 9));
//    lplc_poly6=-945/(8 * PI * pow(kernel, 9));
//
//    kernel_2=kernel*kernel;
//    self_dens=mass*poly6_value*pow(kernel, 6);
//    self_lplc_color=lplc_poly6*mass*kernel_2*(0-3/4*kernel_2);
//
//    mem=(Particle *)malloc(sizeof(Particle)*max_particle);
//    cell=(Particle **)malloc(sizeof(Particle *)*tot_cell);
//
//    sys_running=0;
    
    /** 
     * Particle sizes are expressed in terms of fluid units
     */
    public static final int PARTICLES_PER_BLOCK = 8;
    
    /** kg / m^3, or equivalently, mass for one block */
    public final float fluidDensity;
    
    public final float massPerParticle;
    
    private FluidParticle[] particles;
    
    private FluidParticle[] cellParticles;
    
    /**
     * How much fluid cell can contain
     * Range is 0 to PARTICLES_PER_BLOCK
     */  
    public byte [] cellCapacity;
    
    private int particleCount = 0;
    
    public final int minX;
    public final int maxX;
    public final int minZ;
    public final int maxZ;
    public final int xLength;
    public final int zLength;
    public final int cellCount;
    
    @SuppressWarnings("unchecked")
    public FluidSimulator(float fluidDensity, int minX, int minZ, int maxX, int maxZ)
    {
        this.minX = minX;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxZ = maxZ;
        this.xLength = maxX - minZ + 1;
        this.zLength = maxZ - minZ + 1;
        this.fluidDensity = fluidDensity;
        this.massPerParticle = fluidDensity / PARTICLES_PER_BLOCK;
        
        this.particles = new FluidParticle[MAX_PARTICLES];
        this.cellCount = this.xLength * 256 * this.zLength;
        this.cellCapacity = new byte[cellCount];
        this.cellParticles = new FluidParticle[cellCount];
    }
    
    private int getCellIndex(int x, int y, int z)
    {
        return ((((x - this.minX) * this.zLength) + (z - this.minZ)) << 8) | y;
    }
    
    public void setFluidCapacity(BlockPos pos, float capacity)
    {
        cellCapacity[getCellIndex(pos.getX(), pos.getY(), pos.getZ())]
                = (byte) Math.round(capacity * PARTICLES_PER_BLOCK);
    }
    
    public void addFluidParticle(Vec3d position, Vec3d velocity)
    {
        if(this.particleCount >= MAX_PARTICLES) return;
        
        FluidParticle newParticle = new FluidParticle(position, velocity, this);
        newParticle.index = this.particleCount;
        particles[this.particleCount] = newParticle;
        this.particleCount++;
    }
    
    
    private void mapParticlesToCells()
    {
        
   
//        Particle *p;
//        uint hash;
//
//        for(uint i=0; i<tot_cell; i++)
//        {
//            cell[i]=NULL;
//        }
//
//        for(uint i=0; i<num_particle; i++)
//        {
//            p=&(mem[i]);
//            hash=calc_cell_hash(calc_cell_pos(p->pos));
//
//            if(cell[hash] == NULL)
//            {
//                p->next=NULL;
//                cell[hash]=p;
//            }
//            else
//            {
//                p->next=cell[hash];
//                cell[hash]=p;
//            }
//        }
    }
//    
//    int3 SPHSystem::calc_cell_pos(float3 p)
//    {
//        int3 cell_pos;
//        cell_pos.x = int(floor((p.x) / cell_size));
//        cell_pos.y = int(floor((p.y) / cell_size));
//        cell_pos.z = int(floor((p.z) / cell_size));
//
//        return cell_pos;
//    }
//
//    uint SPHSystem::calc_cell_hash(int3 cell_pos)
//    {
//        if(cell_pos.x<0 || cell_pos.x>=(int)grid_size.x || cell_pos.y<0 || cell_pos.y>=(int)grid_size.y || cell_pos.z<0 || cell_pos.z>=(int)grid_size.z)
//        {
//            return (uint)0xffffffff;
//        }
//
//        cell_pos.x = cell_pos.x & (grid_size.x-1);  
//        cell_pos.y = cell_pos.y & (grid_size.y-1);  
//        cell_pos.z = cell_pos.z & (grid_size.z-1);  
//
//        return ((uint)(cell_pos.z))*grid_size.y*grid_size.x + ((uint)(cell_pos.y))*grid_size.x + (uint)(cell_pos.x);
//    }
    
    public void doStep()
    {
        mapParticlesToCells();
//        comp_dens_pres();
//        comp_force_adv();
//        advection();
    }

}


