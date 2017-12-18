package grondag.hard_science.superblock.placement.spec;

import org.lwjgl.opengl.GL11;

import grondag.hard_science.library.world.IBlockRegion;
import grondag.hard_science.library.world.SingleBlockRegion;
import grondag.hard_science.library.world.WorldHelper;
import grondag.hard_science.simulator.domain.Domain;
import grondag.hard_science.simulator.domain.DomainManager;
import grondag.hard_science.simulator.storage.jobs.IWorldTask;
import grondag.hard_science.simulator.storage.jobs.Job;
import grondag.hard_science.simulator.storage.jobs.RequestPriority;
import grondag.hard_science.simulator.storage.jobs.tasks.ExcavationTask;
import grondag.hard_science.superblock.placement.Build;
import grondag.hard_science.superblock.placement.BuildManager;
import grondag.hard_science.superblock.placement.PlacementHandler;
import grondag.hard_science.superblock.placement.PlacementPosition;
import grondag.hard_science.superblock.placement.PlacementPreviewRenderMode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SingleBuilder extends SingleStackBuilder
{
    public SingleBuilder(ItemStack placedStack, EntityPlayer player, PlacementPosition pPos)
    {
        super(placedStack, player, pPos);
    }

    @Override
    protected boolean doValidate()
    {
        if(this.player.world.isOutsideBuildHeight(this.pPos.inPos)) return false;

        if(this.isExcavation)
        {
            return !this.player.world.isAirBlock(this.pPos.inPos);
        }
        else
        {
            if(WorldHelper.isBlockReplaceable(this.player.world, this.pPos.inPos, false))
            {
                this.outputStack = PlacementHandler.cubicPlacementStack(this);
                return true;
            }
            else
            {
                return false;
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected void drawSelection(Tessellator tessellator, BufferBuilder bufferBuilder)
    {
        // NOOP - selection mode not meaningful for a single-block region
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected void drawPlacement(Tessellator tessellator, BufferBuilder bufferBuilder, PlacementPreviewRenderMode previewMode)
    {
        switch(previewMode)
        {
        case EXCAVATE:
            
            AxisAlignedBB box = new AxisAlignedBB(this.pPos.inPos);

            // draw edges without depth to show extent of region
            GlStateManager.disableDepth();
            GlStateManager.glLineWidth(2.0F);
            bufferBuilder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
            RenderGlobal.drawBoundingBox(bufferBuilder, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, previewMode.red, previewMode.green, previewMode.blue, 1f);
            tessellator.draw();
            
            // draw sides with depth to better show what parts are unobstructed
            GlStateManager.enableDepth();
            bufferBuilder.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
            RenderGlobal.addChainedFilledBoxVertices(bufferBuilder, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, previewMode.red, previewMode.green, previewMode.blue, 0.4f);
            tessellator.draw();
            
            break;
        case PLACE:
            this.drawPlacementPreview(tessellator, bufferBuilder);
            break;
            
        case SELECT:
        case OBSTRUCTED:
        default:
            break;
        
        }
    }

    public IWorldTask worldTask(EntityPlayerMP player)
    {
        if(this.isExcavation)
        {
            return new IWorldTask()
            {
                private boolean isDone = false;

                @Override
                public int runInServerTick(int maxOperations)
                {
                    this.isDone = true;

                    World world = player.world;

                    BlockPos pos = SingleBuilder.this.pPos.inPos;
                    if(pos == null) return 1;

                    // is the position inside the world?
                    if(world.isOutsideBuildHeight(pos)) return 1;

                    IBlockState blockState = world.getBlockState(pos);

                    // is the block at the position affected
                    // by this excavation?
                    if(SingleBuilder.this.effectiveFilterMode.shouldAffectBlock(
                            blockState, 
                            world, 
                            pos, 
                            SingleBuilder.this.placedStack(),
                            SingleBuilder.this.isVirtual))
                    {
                        Job job = new Job(RequestPriority.MEDIUM, player);
                        job.setDimensionID(world.provider.getDimension());
                        job.addTask(new ExcavationTask(pos));
                        Domain domain = DomainManager.instance().getActiveDomain(player);
                        if(domain != null)
                        {
                            domain.jobManager.addJob(job);
                        }
                    }
                    return 2;
                }

                @Override
                public boolean isDone()
                {
                    return this.isDone;
                }
            };
        }
        else
        {
            // Placement world task places virtual blocks in the currently active build
            return new IWorldTask()
            {
                private boolean isDone = false;

                @Override
                public int runInServerTick(int maxOperations)
                {
                    this.isDone = true;

                    Build build = BuildManager.getActiveBuildForPlayer(player);
                    if(build == null || !build.isOpen())
                    {                        
                        String chatMessage = I18n.translateToLocal("placement.message.no_build");
                        player.sendMessage(new TextComponentString(chatMessage));
                        return 1;
                    }
                    
                    World world = player.world;

                    BlockPos pos = SingleBuilder.this.pPos.inPos;
                    if(pos == null) return 1;

                    // is the position inside the world?
                    if(world.isOutsideBuildHeight(pos)) return 1;

                    IBlockState blockState = world.getBlockState(pos);

                    // is the block at the position affected
                    // by this excavation?
                    if(SingleBuilder.this.effectiveFilterMode.shouldAffectBlock(
                            blockState, 
                            world, 
                            pos, 
                            SingleBuilder.this.placedStack(),
                            SingleBuilder.this.isVirtual))
                    {
                        PlacementHandler.placeVirtualBlock(world, SingleBuilder.this.outputStack, player, pos, build);
                        return 5;
                    }
                    return 3;
                }

                @Override
                public boolean isDone()
                {
                    return this.isDone;
                }
            };
        }
    }

    @Override
    public IBlockRegion region()
    {
        return new SingleBlockRegion(this.pPos.inPos);
    }
}