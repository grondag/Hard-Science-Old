package grondag.hard_science.library.serialization;

import org.junit.Test;

import grondag.hard_science.superblock.model.shape.ModelShape;
import io.netty.buffer.UnpooledByteBufAllocator;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

public class SerializationHandlerTest
{
    
    private static class TestValue implements IFlexibleSerializer
    {
        private int number = -1;
        private String text = "new";
        private final String tagPrefix;
        
        private TestValue(String tagPrefix) { this.tagPrefix = tagPrefix; }
        
        @Override
        public void fromBytes(PacketBuffer pBuff)
        {
            this.number = pBuff.readInt();
            this.text = pBuff.readString(1024);
        }

        @Override
        public void toBytes(PacketBuffer pBuff)
        {
            pBuff.writeInt(number);
            pBuff.writeString(text);
        }

        @Override
        public void deserializeNBT(NBTTagCompound tag)
        {
            this.number = tag.getInteger(tagPrefix + "number");
            this.text = tag.getString(tagPrefix + "text");
            
        }

        @Override
        public void serializeNBT(NBTTagCompound tag)
        {
            tag.setInteger(tagPrefix + "number", number);
            tag.setString(tagPrefix + "text", text);
        }

        @Override
        public boolean fromBytesDetectChanges(PacketBuffer buf)
        {
            int oldNumber = this.number;
            String oldText = this.text;
            this.fromBytes(buf);
            return !(oldNumber == this.number && oldText.equals(this.text));
        }

        @Override
        public boolean deserializeNBTDetectChanges(NBTTagCompound tag)
        {
            int oldNumber = this.number;
            String oldText = this.text;
            this.deserializeNBT(tag);
            return !(oldNumber == this.number && oldText.equals(this.text));
        }
    }
    
    private static class TestSubject
    {
        
     
        
        public static final IntSerializer<TestSubject> COMMON_INTEGER = new IntSerializer<TestSubject>(false, "intCommon") 
        {
            @Override
            public int getValue(TestSubject target)
            {
                return target.intCommon;
            }

            @Override
            public void setValue(TestSubject target, int value)
            {
                target.intCommon = value;
            }
        };
        
        public static final IntSerializer<TestSubject> SERVER_INTEGER = new IntSerializer<TestSubject>(true, "intServer") 
        {
            @Override
            public int getValue(TestSubject target)
            {
                return target.intServer;
            }

            @Override
            public void setValue(TestSubject target, int value)
            {
                target.intServer = value;
            }
        };
        
        public static final EnumSerializer<TestSubject, ModelShape> COMMON_ENUM = new EnumSerializer<TestSubject, ModelShape>(false, "enumCommon", ModelShape.class) 
        {

            @Override
            public ModelShape getValue(TestSubject target)
            {
                return target.shapeCommon;
            }

            @Override
            public void setValue(TestSubject target, ModelShape value)
            {
                target.shapeCommon = value;
                target.changeDetected = true;
            }
            
        };
        
        public static final EnumSerializer<TestSubject, ModelShape> SERVER_ENUM = new EnumSerializer<TestSubject, ModelShape>(true, "enumServer", ModelShape.class) 
        {

            @Override
            public ModelShape getValue(TestSubject target)
            {
                return target.shapeServer;
            }

            @Override
            public void setValue(TestSubject target, ModelShape value)
            {
                target.shapeServer = value;
                target.changeDetected = true;
            }
            
        };
        
//        public static final SerializationManager<TestSubject> MANAGER = new SerializationManager<TestSubject>()
//                .addThen(COMMON_MEMBER)
//                .addThen(SERVER_MEMBER)
//                .addThen(COMMON_INTEGER)
//                .addThen(SERVER_INTEGER)
//                .addThen(COMMON_ENUM)
//                .addThen(SERVER_ENUM);
        
       
        private TestValue commonMember = new TestValue("common");
        private TestValue serverMember = new TestValue("server'");
        
        private int intServer = -1;
        private int intCommon = -1;
        private ModelShape shapeCommon = ModelShape.BOX;
        private ModelShape shapeServer = ModelShape.BOX;
        private boolean changeDetected = false;
        
    }

    @Test
    public void test()
    {
        TestSubject subject = new TestSubject();
        
        subject.commonMember.number = 42;
        subject.commonMember.text = "common";
        subject.intCommon = 42;
        subject.shapeCommon = ModelShape.DODECAHEDRON;
        
        subject.serverMember.number = 97;
        subject.serverMember.text = "server";
        subject.shapeServer = null;
        subject.intServer = 97;
        
        
//        PacketBuffer buff = new PacketBuffer(UnpooledByteBufAllocator.DEFAULT.buffer());
//        
////        TestSubject.MANAGER.toBytes(subject, buff);
//        
//        TestSubject target = new TestSubject();
//        
//        target.changeDetected = false;
//        TestSubject.MANAGER.fromBytes(target, buff);
//        
//        // packets should not include server-only values
//        assert target.commonMember.number == 42;
//        assert target.commonMember.text.equals("common");
//        assert target.intCommon == 42;
//        assert target.shapeCommon == ModelShape.DODECAHEDRON;
//        
//        assert target.serverMember.number == -1;
//        assert target.serverMember.text.equals("new");
//        assert target.intServer == -1;
//        assert target.shapeServer == ModelShape.BOX;
//        
//        assert target.changeDetected;
//        
//        // try now with tags
//        NBTTagCompound tag = new NBTTagCompound();
//        TestSubject.MANAGER.serializeNBT(subject, tag);
//        
//        target = new TestSubject();
//        
//        TestSubject.MANAGER.deserializeNBT(target, tag);
//        
//        assert target.commonMember.number == 42;
//        assert target.commonMember.text.equals("common");
//        assert target.intCommon == 42;
//        assert target.shapeCommon == ModelShape.DODECAHEDRON;
//        
//        assert target.serverMember.number == 97;
//        assert target.serverMember.text.equals("server");
//        assert target.intServer == 97;
//        assert target.shapeServer == null;
//        
//        assert target.changeDetected;
//        
//        // repeat but this time filter server-only tag
//        target = new TestSubject();
//        
//        TestSubject.MANAGER.deserializeNBT(target, SerializationManager.withoutServerTag(tag));
//        
//        assert target.commonMember.number == 42;
//        assert target.commonMember.text.equals("common");
//        assert target.intCommon == 42;
//        assert target.shapeCommon == ModelShape.DODECAHEDRON;
//        
//        // should not see server-only member change this time
//        assert target.serverMember.number == -1;
//        assert target.serverMember.text.equals("new");
//        assert target.intServer == -1;
//        assert target.shapeServer == ModelShape.BOX;
//        
//        assert target.changeDetected;
        
    }

}
