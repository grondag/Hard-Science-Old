package grondag.hard_science.machines.matbuffer;

public enum BulkBufferPurpose
{
    FUEL,
    PRIMARY_OUTPUT,
    PRIMARY_INPUT,
    
    // value used during deserialization to indicate bad read
    INVALID
}
