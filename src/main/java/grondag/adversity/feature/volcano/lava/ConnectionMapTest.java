package grondag.adversity.feature.volcano.lava;

import static org.junit.Assert.*;

import java.util.HashSet;

import org.junit.Test;

public class ConnectionMapTest
{

    private static class DirtyThing
    {
        public boolean dirt;
        
        public void setDirty()
        {
            
        }
    }
    
    private static class ReallyDirtyThing extends DirtyThing
    {
 
        
        @Override
        public void setDirty()
        {
            this.dirt = true;
        }
    }
    
    HashSet<DirtyThing> dirtyThings = new HashSet<DirtyThing>();
    
    DirtyThing t1 = new ReallyDirtyThing();
    DirtyThing t2 = new ReallyDirtyThing();
    DirtyThing t3 = new ReallyDirtyThing();
    DirtyThing t4 = new ReallyDirtyThing();
    DirtyThing t5 = new ReallyDirtyThing();
    DirtyThing t6 = new ReallyDirtyThing();
    
    
    DirtyThing nt1 = new DirtyThing();
    DirtyThing nt2 = new DirtyThing();
    DirtyThing nt3 = new DirtyThing();
    DirtyThing nt4 = new DirtyThing();
    DirtyThing nt5 = new DirtyThing();
    DirtyThing nt6 = new DirtyThing();
    
    
    int b1 = 0;
    int b2 = 0;
    int b3 = 0;
    int b4 = 0;
    int b5 = 0;
    int b6 = 0;
    
    @Test
    public void test()
    {
        dirtyThings.add(new ReallyDirtyThing());
        dirtyThings.add(new ReallyDirtyThing());
        dirtyThings.add(new ReallyDirtyThing());
        dirtyThings.add(new ReallyDirtyThing());
        
        long startTime;

        
        for(int j = 0; j < 10; j++)
        {
            long hashTime = 0;
            long nullcheckTime = 0;
            long dummyTime = 0;
            
            for(int i = 0;  i < 10000000; i++)
            {
                startTime = System.nanoTime();
                dirtyThings.forEach(t -> t.setDirty());
                hashTime += System.nanoTime() - startTime;
                
                startTime = System.nanoTime();
//                t1.dirt = true;
//                t2.dirt = true;
//                t3.dirt = true;
//                t4.dirt = true;
//                t5.dirt = true;
//                t6.dirt = true;
                t1.setDirty();
                t2.setDirty();
                t3.setDirty();
                t4.setDirty();
                t5.setDirty();
                t6.setDirty();
                dummyTime += System.nanoTime() - startTime;
                
                startTime = System.nanoTime();
                nt1.setDirty();
                nt2.setDirty();
                nt3.setDirty();
                nt4.setDirty();
                nt5.setDirty();
                nt6.setDirty();
//                if(nt1 != null) nt1.dirt = true;
//                if(nt2 != null) nt2.dirt = true;
//                if(nt3 != null) nt3.dirt = true;
//                if(nt4 != null) nt4.dirt = true;
//                if(nt5 != null) nt5.dirt = true;
//                if(nt6 != null) nt6.dirt = true;
                nullcheckTime += System.nanoTime() - startTime;
            }
            
            System.out.println("hashTime=" + hashTime);
            System.out.println("dummyTime=" + dummyTime);
            System.out.println("nullcheckTime=" + nullcheckTime);
        }
    }

}
