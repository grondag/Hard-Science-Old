package grondag.adversity.library.render;

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
import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import com.google.common.collect.ImmutableList;

import grondag.adversity.library.varia.Useful;

public class QuadFactory
{
    public static final double EPSILON = 0.0000001;


    public static final List<BakedQuad> EMPTY_QUAD_LIST = new ImmutableList.Builder<BakedQuad>().build();


    public static int shadeColor(int color, float shade, boolean glOrder)
    {
        int red = (int) (shade * 255f * ((color >> 16 & 0xFF) / 255f));
        int green = (int) (shade * 255f * ((color >> 8 & 0xFF) / 255f));
        int blue = (int) (shade * 255f * ((color & 0xFF) / 255f));
        int alpha = color >> 24 & 0xFF;

        return glOrder ? red  | green << 8 | blue << 16 | alpha << 24 : red << 16 | green << 8 | blue | alpha << 24;
    }
    
    /** arguments are assumed to be ARGB */
    public static int multiplyColor(int color1, int color2)
    {
        int red = ((color1 >> 16) & 0xFF) * ((color2 >> 16) & 0xFF) / 0xFF;
        int green = ((color1 >> 8) & 0xFF) * ((color2 >> 8) & 0xFF) / 0xFF;
        int blue = (color1 & 0xFF) * (color2 & 0xFF) / 0xFF;
        int alpha = ((color1 >> 24) & 0xFF) * ((color2 >> 24) & 0xFF) / 0xFF;

        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    public static List<RawQuad> makeBox(AxisAlignedBB box, RawQuad template)
    {
        List<RawQuad> retVal = new ArrayList<RawQuad>(6);
        
        RawQuad quad = new RawQuad(template);
        quad.setupFaceQuad(EnumFacing.UP, 1 - box.maxX, box.minZ, 1 - box.minX, box.maxZ, 1 - box.maxY, EnumFacing.SOUTH);
        
//        quad.tag = "UP";
        
        retVal.add(quad);

        quad = new RawQuad(template);
        quad.setupFaceQuad(EnumFacing.DOWN, box.minX, box.minZ, box.maxX, box.maxZ, box.minY, EnumFacing.SOUTH);
        retVal.add(quad);

        //-X
        quad = new RawQuad(template);
        quad.setupFaceQuad(EnumFacing.WEST, box.minZ, box.minY, box.maxZ, box.maxY, box.minX, EnumFacing.UP);
        retVal.add(quad);
        
        //+X
        quad = new RawQuad(template);
        quad.setupFaceQuad(EnumFacing.EAST, 1 - box.maxZ, box.minY, 1 - box.minZ, box.maxY, 1 - box.maxX, EnumFacing.UP);
        retVal.add(quad);
        
        //-Z
        quad = new RawQuad(template);
        quad.setupFaceQuad(EnumFacing.NORTH, 1 - box.maxX, box.minY, 1 - box.minX, box.maxY, box.minZ, EnumFacing.UP);
        retVal.add(quad);
        
        //+Z
        quad = new RawQuad(template);
        quad.setupFaceQuad(EnumFacing.SOUTH, box.minX, box.minY, box.maxX, box.maxY, 1 - box.maxZ, EnumFacing.UP);
        retVal.add(quad);
        
        

        return retVal;
    }
    
    // NOTE: this is a prototype implementation
    // It's fine for smaller objects, but would likely generate excess polys for big shapes after CSG operations.
    // Also needs better/different texture handling for top and bottom when face diameter is > 1.
    // Will probably need separate version for creating axis-aligned cylinders and cones.  
    // Also needs a parameter for minimum slices to reduce poly count on small model parts when appropriate.
    // Right now minimum is fixed at 12.
    public static List<RawQuad> makeCylinder(Vec3d start, Vec3d end, double startRadius, double endRadius, RawQuad template)
    {
        double circumference = Math.PI * Math.max(startRadius, endRadius) * 2;
        int textureSlices = (int) Math.max(1, Math.round(circumference));
        int polysPerTextureSlice = 1;
        while(textureSlices * polysPerTextureSlice < 12) polysPerTextureSlice++;
        int polySlices = textureSlices * polysPerTextureSlice;

        double length = start.distanceTo(end);
        int raySlices = (int) Math.ceil(length);
        
        final Vec3d axisZ = end.subtract(start).normalize();
        boolean isY = (Math.abs(axisZ.yCoord) > 0.5);
        final Vec3d axisX = new Vec3d(isY ? 1 : 0, !isY ? 1 : 0, 0)
                .crossProduct(axisZ).normalize();
        final Vec3d axisY = axisX.crossProduct(axisZ).normalize();
        RawQuad top = new RawQuad(template, polySlices);
        RawQuad bottom = new RawQuad(template, polySlices);
        
        List<RawQuad> results = new ArrayList<RawQuad>(48);

        for (int i = 0; i < polySlices; i++) {
            double t0 = i / (double) polySlices, t1 = (i + 1) / (double) polySlices;

            for(int j = 0; j < raySlices; j++ )
            {
                double rayLength = Math.min(1,  length - j);
                Vec3d centerStart = start.add(axisZ.scale(j));
                Vec3d centerEnd = start.add(axisZ.scale(j + rayLength));
                
                double quadStartRadius = Useful.linearInterpolate(startRadius, endRadius, (double) j / raySlices );
                double quadEndRadius = Useful.linearInterpolate(startRadius, endRadius, Math.min(1, (double) (j + 1) / raySlices ));

                double uStart = ((double) (i % polysPerTextureSlice) / polysPerTextureSlice);
                double u0 = 16.0 * uStart;
                double u1 = 16.0 * (uStart + 1.0 / polysPerTextureSlice);
                double v0 = 0;
                double v1 = 16.0 * rayLength;
                
                Vec3d n0 = cylNormal(axisX, axisY, t1);
                Vec3d n1= cylNormal(axisX, axisY, t0);
 
                
                RawQuad newQuad = new RawQuad(template);
                
                newQuad.setVertex(0, new Vertex(centerStart.add(n0.scale(quadStartRadius)), u0, v0, template.color, n0));
                newQuad.setVertex(1, new Vertex(centerStart.add(n1.scale(quadStartRadius)), u1, v0, template.color, n1));
                newQuad.setVertex(2, new Vertex(centerEnd.add(n1.scale(quadEndRadius)), u1, v1, template.color, n1));
                newQuad.setVertex(3, new Vertex(centerEnd.add(n0.scale(quadEndRadius)), u0, v1, template.color, n0));
                results.add(newQuad);
                
                if(j == 0 || j == raySlices - 1)
                {
                    double angle = t0 * Math.PI * 2;
                    double u = 8.0 + Math.cos(angle) * 8.0;
                    double v = 8.0 + Math.sin(angle) * 8.0;

                    if(j == 0)
                    {    
                        bottom.setVertex(i, new Vertex(centerStart.add(n0.scale(quadStartRadius)), u, v, template.color, null));                
                    }
                    if(j == raySlices - 1)
                    {
                        top.setVertex(polySlices - i - 1, new Vertex(centerEnd.add(n0.scale(quadEndRadius)), u, v, template.color, null));
                    }
                }
            }
        
        }

        results.addAll(top.toQuads());
        results.addAll(bottom.toQuads());
        return results;
    }

    private static Vec3d cylNormal(Vec3d axisX, Vec3d axisY, double slice) {
            double angle = slice * Math.PI * 2;
            return axisX.scale(Math.cos(angle)).add(axisY.scale(Math.sin(angle)));
    }
    
    
    /**
     * Makes a regular icosahedron, which is a very close approximation to a sphere for most purposes.
     * Loosely based on http://blog.andreaskahler.com/2009/06/creating-icosphere-mesh-in-code.html
     */
    public static List<RawQuad> makeIcosahedron(Vec3d center, double radius, RawQuad template) 
    {
        /** vertex scale */
        double s = radius  / (2 * Math.sin(2 * Math.PI / 5));
        
        Vec3d[] vertexes = new Vec3d[12];
        
        // create 12 vertices of a icosahedron
        double t = s * (1.0 + Math.sqrt(5.0)) / 2.0;
        int vi = 0;
        
        vertexes[vi++] = new Vec3d(-s,  t,  0).add(center);
        vertexes[vi++] = new Vec3d( s,  t,  0).add(center);
        vertexes[vi++] = new Vec3d(-s, -t,  0).add(center);
        vertexes[vi++] = new Vec3d( s, -t,  0).add(center);
        
        vertexes[vi++] = new Vec3d( 0, -s,  t).add(center);
        vertexes[vi++] = new Vec3d( 0,  s,  t).add(center);
        vertexes[vi++] = new Vec3d( 0, -s, -t).add(center);
        vertexes[vi++] = new Vec3d( 0,  s, -t).add(center);
        
        vertexes[vi++] = new Vec3d( t,  0, -s).add(center);
        vertexes[vi++] = new Vec3d( t,  0,  s).add(center);
        vertexes[vi++] = new Vec3d(-t,  0, -s).add(center);
        vertexes[vi++] = new Vec3d(-t,  0,  s).add(center);

        Vec3d[] normals = new Vec3d[12];
        for(int i = 0; i < 12; i++)
        {
            normals[i] = vertexes[i].subtract(center).normalize();
        }
        
        // create 20 triangles of the icosahedron
        List<RawQuad> results = new ArrayList<RawQuad>(20);


        // 5 faces around point 0
        results.add(makeIcosahedronFace(0, 11, 5, vertexes, normals, template));
        results.add(makeIcosahedronFace(0, 5, 1, vertexes, normals, template));
        results.add(makeIcosahedronFace(0, 1, 7, vertexes, normals, template));
        results.add(makeIcosahedronFace(0, 7, 10, vertexes, normals, template));
        results.add(makeIcosahedronFace(0, 10, 11, vertexes, normals, template));

        // 5 adjacent faces 
        results.add(makeIcosahedronFace(1, 5, 9, vertexes, normals, template));
        results.add(makeIcosahedronFace(5, 11, 4, vertexes, normals, template));
        results.add(makeIcosahedronFace(11, 10, 2, vertexes, normals, template));
        results.add(makeIcosahedronFace(10, 7, 6, vertexes, normals, template));
        results.add(makeIcosahedronFace(7, 1, 8, vertexes, normals, template));

        // 5 faces around point 3
        results.add(makeIcosahedronFace(3, 9, 4, vertexes, normals, template));
        results.add(makeIcosahedronFace(3, 4, 2, vertexes, normals, template));
        results.add(makeIcosahedronFace(3, 2, 6, vertexes, normals, template));
        results.add(makeIcosahedronFace(3, 6, 8, vertexes, normals, template));
        results.add(makeIcosahedronFace(3, 8, 9, vertexes, normals, template));

        // 5 adjacent faces 
        results.add(makeIcosahedronFace(4, 9, 5, vertexes, normals, template));
        results.add(makeIcosahedronFace(2, 4, 11, vertexes, normals, template));
        results.add(makeIcosahedronFace(6, 2, 10, vertexes, normals, template));
        results.add(makeIcosahedronFace(8, 6, 7, vertexes, normals, template));
        results.add(makeIcosahedronFace(9, 8, 1, vertexes, normals, template));
  
        return results;
    }
    
    private static RawQuad makeIcosahedronFace(int p1, int p2, int p3, Vec3d[] points, Vec3d[] normals, RawQuad template)
    {
        RawQuad newQuad = new RawQuad(template, 3);
        
        newQuad.setVertex(0, new Vertex(points[p1], 0, 0, template.color, normals[p1]));
        newQuad.setVertex(1, new Vertex(points[p2], 1, 0, template.color, normals[p2]));
        newQuad.setVertex(2, new Vertex(points[p3], 1, 1, template.color, normals[p3]));

        // used for testing
//        newQuad.recolor((Useful.SALT_SHAKER.nextInt(0x1000000) & 0xFFFFFF) | 0xFF000000);
        
        return newQuad;
    }
    
    //    private static int[] vertexToInts(double x, double y, double z, double u, double v, int color, TextureAtlasSprite sprite)
    //    {
    //
    //        return new int[] { Float.floatToRawIntBits((float) x), Float.floatToRawIntBits((float) y), Float.floatToRawIntBits((float) z), color,
    //                Float.floatToRawIntBits(sprite.getInterpolatedU(u)), Float.floatToRawIntBits(sprite.getInterpolatedV(v)), 0 };
    //    }


}