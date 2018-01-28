package grondag.hard_science.library;

import org.junit.Test;

import grondag.hard_science.crafting.base.SingleParameterModel;
import grondag.hard_science.crafting.base.SingleParameterModel.Result;
import grondag.hard_science.crafting.base.SingleParameterModel.ResultBuilder;

public class SingleParameterModelTest
{
    @Test
    public void test()
    {
        SingleParameterModel thing = new SingleParameterModel();
        Object v1 = new Object();
        thing.createInput(v1, 1);
        Object v2 = new Object();
        thing.createInput(v2, 2);
        Object v3 = new Object();
        thing.createOutput(v3, 0.5);
        
        ResultBuilder builder = thing.builder();
        builder.limitInput(v1, 100);
        
        Result r = builder.build();
        assert r.inputValue(v1) == 100;
        assert r.inputValue(v2) == 200;
        assert r.outputValue(v3) == 50;
        
        builder = thing.builder();
        builder.ensureOutput(v3, 100);
        
        r = builder.build();
        assert r.inputValue(v1) == 200;
        assert r.inputValue(v2) == 400;
        assert r.outputValue(v3) == 100;
        
        builder = thing.builder();
        builder.limitInput(v1, 100);
        builder.limitInput(v2, 100);
        
        r = builder.build();
        assert r.inputValue(v1) == 50;
        assert r.inputValue(v2) == 100;
        assert r.outputValue(v3) == 25;
    }

}
