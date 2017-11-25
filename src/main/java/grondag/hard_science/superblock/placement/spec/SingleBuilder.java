package grondag.hard_science.superblock.placement.spec;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.library.world.WorldHelper;
import grondag.hard_science.simulator.base.DomainManager;
import grondag.hard_science.simulator.base.DomainManager.Domain;
import grondag.hard_science.simulator.base.jobs.IWorldTask;
import grondag.hard_science.simulator.base.jobs.Job;
import grondag.hard_science.simulator.base.jobs.RequestPriority;
import grondag.hard_science.simulator.base.jobs.tasks.ExcavationTask;
import grondag.hard_science.superblock.placement.PlacementHandler;
import grondag.hard_science.superblock.placement.PlacementPosition;
import grondag.hard_science.superblock.placement.PlacementPreviewRenderMode;
import grondag.hard_science.superblock.placement.spec.SingleStackPlacementSpec.SingleStackEntry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

class SingleBuilder extends SingleStackBuilder
        {

            protected SingleBuilder(ItemStack placedStack, EntityPlayer player, PlacementPosition pPos)
            {
                super(placedStack, player, pPos);
            }

            @Override
            protected AbstractPlacementSpec buildSpec()
            {
                SinglePlacementSpec result = new SinglePlacementSpec(this, PlacementHandler.cubicPlacementStack(this));
                result.playerName = this.player.getName();
                
                SingleStackEntry entry 
                    = result.new SingleStackEntry(0, this.pPos.inPos);
                result.entries = ImmutableList.of(entry);
                return result;
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
                    return WorldHelper.isBlockReplaceable(this.player.world, this.pPos.inPos, false);
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
                this.drawPlacementPreview(tessellator, bufferBuilder);
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
                            SinglePlacementSpec spec = (SinglePlacementSpec) buildSpec();
                            this.isDone = true;
                            
                            World world = spec.location.world();
                            
                            if(spec.entries.isEmpty()) return 1;
                            
                            BlockPos pos = spec.entries.get(0).pos();
                            if(pos == null) return 1;
                                
                            // is the position inside the world?
                            if(world.isOutsideBuildHeight(pos)) return 1;
                                
                            IBlockState blockState = world.getBlockState(pos);
                                
                            // is the block at the position affected
                            // by this excavation?
                            if(spec.filterMode().shouldAffectBlock(
                                    blockState, 
                                    world, 
                                    pos, 
                                    spec.sourceStack(),
                                    spec.isVirtual))
                            {
                            
                                Job job = new Job(RequestPriority.MEDIUM, player, spec);
                                job.addTask(new ExcavationTask(spec.entries.get(0)));
                                Domain domain = DomainManager.INSTANCE.getActiveDomain(player);
                                if(domain != null)
                                {
                                    domain.JOB_MANAGER.addJob(job);
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
                            SinglePlacementSpec spec = (SinglePlacementSpec) buildSpec();
                            
                            this.isDone = true;
                            
                            World world = spec.location.world();
                            
                            if(spec.entries.isEmpty()) return 1;
                            
                            BlockPos pos = spec.entries.get(0).pos();
                            if(pos == null) return 1;
                                
                            // is the position inside the world?
                            if(world.isOutsideBuildHeight(pos)) return 1;
                                
                            IBlockState blockState = world.getBlockState(pos);
                                
                            // is the block at the position affected
                            // by this excavation?
                            if(spec.filterMode().shouldAffectBlock(
                                    blockState, 
                                    world, 
                                    pos, 
                                    spec.sourceStack(),
                                    spec.isVirtual))
                            {
                                //TODO: set virtual block build/domain
//                                Domain domain = DomainManager.INSTANCE.getActiveDomain(player);
                                PlacementHandler.placeVirtualBlock(world, spec.sourceStack(), player, pos);
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
        }