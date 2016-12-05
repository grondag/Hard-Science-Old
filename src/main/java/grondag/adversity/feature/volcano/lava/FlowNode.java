package grondag.adversity.feature.volcano.lava;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.minecraft.world.World;

public abstract class FlowNode
{    
    
    private TreeSet<FlowNode> inputs;
    private TreeSet<FlowNode> outputs;
    
    private boolean isBlocked = true;
    private boolean isCutOff = true;
    
    private float level = 0;
    
    public static final float MINIMUM_LEVEL_INCREMENT = 1F/12F;
    public static final int DEFAULT_BLOCKED_RETRY_COUNT = 3;
    
    public abstract List<FlowNode> flow(LavaManager2 lavaManager, World world);
    public abstract Set<LavaCell> getCells();
    
    /** can this flow accept lava at its current vertical level? */
    public abstract boolean canAcceptAtCurrentLevel();
    
    
    /**
     * Add self to tracking list at creation time
     */
    public FlowNode(LavaManager2 lavaManager)
    {
        lavaManager.registerFlow(this);
    }
    
    public boolean isCellAnInput(LavaCell cell)
    {
        for(FlowNode input: this.getInputs())
        {
            if(input.getCells().contains(cell)) return true;
        }
        return false;
    }
    
    public boolean isCellAnOutput(LavaCell cell)
    {
        for(FlowNode output: this.getOutputs())
        {
            if(output.getCells().contains(cell)) return true;
        }
        return false;
    }
    
    //FLOW TRACKING
    public void addInput(FlowNode inputNode)
    {
        inputs.add(inputNode);
        inputNode.outputs.add(this);
        this.checkForCutOff();
        inputNode.checkForBlocked();
    }
    
    public void removeInput(FlowNode inputNode)
    {
        inputs.remove(inputNode);
        inputNode.outputs.remove(this);
        this.checkForCutOff();
        inputNode.checkForBlocked();
    }
    
    public void addOutput(FlowNode outputNode)
    {
        outputs.add(outputNode);
        outputNode.inputs.add(this);
        this.checkForBlocked();
        outputNode.checkForCutOff();
    }
    
    public void removeOutput(FlowNode outputNode)
    {
        outputs.remove(outputNode);
        outputNode.inputs.remove(this);
        this.checkForBlocked();
        outputNode.checkForCutOff();
    }
    
    public void removeLinks()
    {
        for(FlowNode output: outputs)
        {
            output.inputs.remove(this);
            output.checkForCutOff();
        }
        outputs.clear();
        this.isBlocked = true;
        
        for(FlowNode input: inputs)
        {
            input.outputs.remove(this);
            input.checkForBlocked();
        }
        inputs.clear();
        this.isCutOff = true;        
    }
    
    public void transferLinksTo(FlowNode other)
    {
        other.outputs.addAll(outputs);
        for(FlowNode output: outputs)
        {
            output.inputs.add(other);
            output.checkForCutOff();
        }
        outputs.clear();
        this.isBlocked = true;
        
        other.inputs.addAll(inputs);
        for(FlowNode input: inputs)
        {
            input.outputs.add(other);
            input.checkForBlocked();
        }
        inputs.clear();
        this.isCutOff = true;
        
    }
    
    public Set<FlowNode> getOutputs()
    {
        return Collections.unmodifiableSet(outputs);
    }
    
    public Set<FlowNode> getInputs()
    {
        return Collections.unmodifiableSet(inputs);
    }
    
    //BLOCKAGE TRACKING
    
    public boolean isCutOff()
    {
        return this.isCutOff;
    }
    
    public boolean isBlocked()
    {
        return this.isBlocked;
    }
    
    protected void setCutOff(boolean isCutOff)
    {
        if(isCutOff != this.isCutOff)
        {
            this.isCutOff = isCutOff;
            notifyAllOutputsToCheckForCutoff();
        }
    }
    
    protected void setBlocked(boolean isBlocked)
    {
        if(isBlocked != this.isBlocked)
        {
            this.isBlocked = isBlocked;
            notifyAllInputsToCheckForBlocked();
        }
    }
    
    protected void notifyAllInputsToCheckForBlocked()
    {
        if(inputs.isEmpty()) return;
        
        for(FlowNode input : inputs)
        {
            input.checkForBlocked();
        }
    }
    
    protected void notifyAllOutputsToCheckForCutoff()
    {
        if(outputs.isEmpty()) return;
        
        for(FlowNode output : outputs)
        {
            output.checkForCutOff();
        }
    }
    
    /** returns true if status changed and outputs were notified */
    protected boolean checkForCutOff()
    {
        boolean shouldBeCutOff = true;
        
        if(!this.inputs.isEmpty())
        {
            for(FlowNode input : this.inputs)
            {
                if(!input.isCutOff() && input.getLeve() >= this.minimumLevelAccepted())
                {
                    shouldBeCutOff = false;
                    break;
                }
            }
        }
        
        if(shouldBeCutOff != this.isBlocked())
        {
            setCutOff(shouldBeCutOff);
            return true;
        }
        else
        {
            return false;
        }
    }
    
    /** returns true if status changed and inputs were notified */
    protected boolean checkForBlocked()
    {
        boolean shouldBeBlocked = true;
        
        if(!this.outputs.isEmpty())
        {
            for(FlowNode output : this.outputs)
            {
                if(!output.isBlocked() && this.getLeve() >= output.minimumLevelAccepted())
                {
                    shouldBeBlocked = false;
                    break;
                }
            }
        }
        
        if(shouldBeBlocked != this.isBlocked())
        {
            setBlocked(shouldBeBlocked);
            return true;
        }
        else
        {
            return false;
        }
    }
    
    /** Vertical flow level provided to output nodes */
    public float getLeve()
    {
        return this.level;
    }
    
    /** Updates our vertical flow level and notifies inputs and outputs to update flow satus */
    protected void setLevel(float newLevel)
    {
        if(newLevel != this.level)
        {
            this.level = newLevel;
            
            if(!this.checkForBlocked())
            {
                this.notifyAllInputsToCheckForBlocked();
            }
            
            if(!this.checkForCutOff())
            {
                this.notifyAllOutputsToCheckForCutoff();
            }
        }
    }
    
    /** 
     * Vertical level that must be offered for this node to accept more fluid.
     * If this node can expand outwards will generally be equal to flows existing level, up to 1.0
     * If this node can only expand up, will be equal to current level + minimum increment. (1/12)
     */
    public float minimumLevelAccepted()
    {
        if(this.canAcceptAtCurrentLevel())
        {
            return this.getLeve();
        }
        else
        {
            return this.getLeve() + MINIMUM_LEVEL_INCREMENT;
        }
    }
}
