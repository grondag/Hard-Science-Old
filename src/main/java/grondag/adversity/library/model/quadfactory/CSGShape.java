package grondag.adversity.library.model.quadfactory;
/**
 * Portions reproduced or adapted from JCSG.
 * Copyright 2014-2014 Michael Hoffer <info@michaelhoffer.de>. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY Michael Hoffer <info@michaelhoffer.de> "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL Michael Hoffer <info@michaelhoffer.de> OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of Michael Hoffer
 * <info@michaelhoffer.de>.
 */

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import grondag.adversity.Adversity;
import grondag.adversity.library.Useful;

public class CSGShape extends LinkedList<RawQuad>
{

    /**
     * 
     */
    private static final long serialVersionUID = 796007237565914078L;

    public CSGShape(List<RawQuad> quads)
    {
        super(quads);
    }
    

    public CSGShape()
    {
        super();
    }
    
    public CSGShape clone()
    {
        Stream<RawQuad> quadStream;

        if (this.size() > 200) {
            quadStream = this.parallelStream();
        } else {
            quadStream = this.stream();
        }

        return new CSGShape(quadStream.
                map((RawQuad p) -> p.clone()).collect(Collectors.toList()));
    }
    
    public CSGShape initCsg()
    {
        this.forEach((q) -> q.initCsg());
        return this;
    }
    
    /**
     * Randomly recolors all the quads as an aid to debugging.
     */
    public void recolor()
    {
        Stream<RawQuad> quadStream;

        if (this.size() > 200) {
            quadStream = this.parallelStream();
        } else {
            quadStream = this.stream();
        }

        quadStream.forEach((RawQuad quad) -> quad.recolor((Useful.SALT_SHAKER.nextInt(0x1000000) & 0xFFFFFF) | 0xFF000000));
    }
    
    /**
     * Return a new CSG solid representing the union of this csg and the
     * specified csg.
     *
     * <b>Note:</b> Neither this csg nor the specified csg are weighted.
     *
     * <blockquote><pre>
     *    A.union(B)
     *
     *    +-------+            +-------+
     *    |       |            |       |
     *    |   A   |            |       |
     *    |    +--+----+   =   |       +----+
     *    +----+--+    |       +----+       |
     *         |   B   |            |       |
     *         |       |            |       |
     *         +-------+            +-------+
     * </pre></blockquote>
     *
     *
     * @param csg other csg
     *
     * @return union of this csg and the specified csg
     */
//    public CSG union(CSG csg) {
//
//        switch (getOptType()) {
//            case CSG_BOUND:
//                return _unionCSGBoundsOpt(csg);
//            case POLYGON_BOUND:
//                return _unionPolygonBoundsOpt(csg);
//            default:
////                return _unionIntersectOpt(csg);
//                return _unionNoOpt(csg);
//        }
//    }
    
    /**
     * Return a new CSG solid representing the intersection of this csg and the
     * specified csg.
     *
     * <b>Note:</b> Neither this csg nor the specified csg are weighted.
     *
     * <blockquote><pre>
     *     A.intersect(B)
     *
     *     +-------+
     *     |       |
     *     |   A   |
     *     |    +--+----+   =   +--+
     *     +----+--+    |       +--+
     *          |   B   |
     *          |       |
     *          +-------+
     * }
     * </pre></blockquote>
     *
     * @param csg other csg
     * @return intersection of this csg and the specified csg
     */
    public CSGShape intersect(CSGShape other)
    {
        CSGNode a = new CSGNode(this);
        CSGNode b = new CSGNode(other);
        a.invert();
        b.clipTo(a);
        b.invert();
        a.clipTo(b);
        b.clipTo(a);
        a.build(b.allRawQuads());
        a.invert();
        CSGShape retVal = new CSGShape(a.recombinedRawQuads());
        Adversity.log.info("raw count " + a.allRawQuads().size() + "   combined count " + retVal.size());

        return retVal;
    }

}