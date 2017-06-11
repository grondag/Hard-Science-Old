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
import java.util.concurrent.atomic.AtomicLong;

import net.minecraft.util.math.Vec3d;

public class CSGPlane
{
    protected static AtomicLong nextInsideLineID = new AtomicLong(1);
    protected static AtomicLong nextOutsideLineID = new AtomicLong(-1);
    
    /**
     * Normal vector.
     */
    public Vec3d normal;
    /**
     * Distance to origin.
     */
    public double dist;
    
    protected long lineID = nextInsideLineID.getAndIncrement();

    /**
     * Constructor. Creates a new plane defined by its normal vector and the
     * distance to the origin.
     *
     * @param normal plane normal
     * @param dist distance from origin
     */
    public CSGPlane(Vec3d normal, double dist) {
        this.normal = normal;
        this.dist = dist;
    }

    public CSGPlane(RawQuad quad)
    {
        this.normal = quad.getFaceNormal();
        this.dist = normal.dotProduct(quad.getVertex(0));    
    }
    
    @Override
    public CSGPlane clone() {
        return new CSGPlane(new Vec3d(normal.xCoord, normal.yCoord, normal.zCoord), dist);
    }

    /**
     * Flips this plane.
     */
    public void flip() {
        normal = normal.scale(-1);
        dist = -dist;
    }

    /**
     * Splits a {@link Polygon} by this plane if needed. After that it puts the
     * polygons or the polygon fragments in the appropriate lists
     * ({@code front}, {@code back}). Coplanar polygons go into either
     * {@code coplanarFront}, {@code coplanarBack} depending on their
     * orientation with respect to this plane. Polygons in front or back of this
     * plane go into either {@code front} or {@code back}.
     *
     * @param quad polygon to split
     * @param coplanarFront "coplanar front" polygons
     * @param coplanarBack "coplanar back" polygons
     * @param front front polygons
     * @param back back polgons
     */
            
    public void splitQuad(
        RawQuad quad,
        List<RawQuad> coplanarFront,
        List<RawQuad> coplanarBack,
        List<RawQuad> front,
        List<RawQuad> back) 
    {
        final int COPLANAR = 0;
        final int FRONT = 1;
        final int BACK = 2;
        final int SPANNING = 3;
        
        // Classify each point as well as the entire polygon into one of the above
        // four classes.
        int polygonType = 0;
        int types[] = new int[quad.getVertexCount()];
        for (int i = 0; i < quad.getVertexCount(); i++) {
            double t = this.normal.dotProduct(quad.getVertex(i)) - this.dist;
            
            int type = (t < -QuadFactory.EPSILON) ? BACK : (t > QuadFactory.EPSILON) ? FRONT : COPLANAR;
            polygonType |= type;
            types[i] = type;
        }
        
        // Put the polygon in the correct list, splitting it when necessary.
        switch (polygonType) {
            case COPLANAR:
                //System.out.println(" -> coplanar");
                (this.normal.dotProduct(quad.getFaceNormal()) > 0 ? coplanarFront : coplanarBack).add(quad);
                break;
            case FRONT:
                //System.out.println(" -> front");
                front.add(quad);
                break;
            case BACK:
                //System.out.println(" -> back");
                back.add(quad);
                break;
            case SPANNING:
                
                List<Vertex> frontVertex = new ArrayList<Vertex>(quad.getVertexCount()+1);
                List<Vertex> backVertex = new ArrayList<Vertex>(quad.getVertexCount()+1);
                List<Long> frontLineID = new ArrayList<Long>(quad.getVertexCount()+1);
                List<Long> backLineID = new ArrayList<Long>(quad.getVertexCount()+1);
                for (int i = 0; i < quad.getVertexCount(); i++) {
                    int j = (i + 1) % quad.getVertexCount();
                    int iType = types[i];
                    int jType = types[j];
                    Vertex iVertex = quad.getVertex(i);
                    Vertex jVertex = quad.getVertex(j);
                    Long iLineID = quad.getLineID(i);
                    
                    if (iType != BACK) {
                        frontVertex.add(iVertex);
                        // if we are splitting at an existing vertex need to use split line
                        // if the next vertex is not going into this list
                        frontLineID.add(iType == COPLANAR && jType == BACK ? this.lineID : iLineID);
                    }
                    if (iType != FRONT) {
                        backVertex.add(iType != BACK ? iVertex.clone() : iVertex);
                        // if we are splitting at an existing vertex need to use split line
                        // if the next vertex is not going into this list
                        backLineID.add(iType == COPLANAR && jType == FRONT ? this.lineID : iLineID);
                    }
                    // Line for interpolated vertex depends on what the next vertex is for this side (front/back).
                    // If the next vertex will be included in this side, we are starting the line connecting
                    // next vertex with previous vertex and should use line from prev. vertex
                    // If the next vertex will NOT be included in this side, we are starting the split line.

                    if ((iType | jType) == SPANNING) {
                        double t = (this.dist - this.normal.dotProduct(iVertex)) / this.normal.dotProduct(jVertex.subtract(iVertex));
                        Vertex v = iVertex.interpolate(jVertex, t);
                        
                        frontVertex.add(v);
                        frontLineID.add(jType != FRONT ? this.lineID : iLineID);
                        
                        backVertex.add(v.clone());
                        backLineID.add(jType != BACK ? this.lineID : iLineID);
                    }
                }
                if (frontVertex.size() >= 3) {
                    RawQuad frontQuad = new RawQuad(quad, frontVertex.size());
                    frontQuad.ancestorQuadID = quad.getAncestorQuadIDForDescendant();
                    
                    for(int i = 0; i < frontVertex.size(); i++)
                    {
                        frontQuad.setVertex(i, frontVertex.get(i));
                        frontQuad.setLineID(i, frontLineID.get(i));
                    }
                    
                    // avoid generate zero face normals due to very small polys
                    frontQuad.setFaceNormal(quad.getFaceNormal());
                    front.add(frontQuad);
                }

                if (backVertex.size() >= 3) {
                    RawQuad backQuad = new RawQuad(quad, backVertex.size());
                    backQuad.ancestorQuadID = quad.getAncestorQuadIDForDescendant();

                    for(int i = 0; i < backVertex.size(); i++)
                    {
                        backQuad.setVertex(i, backVertex.get(i));
                        backQuad.setLineID(i, backLineID.get(i));
                    }
                    
                    // avoid generate zero face normals due to very small polys
                    backQuad.setFaceNormal(quad.getFaceNormal());
                    back.add(backQuad);               
                }
                break;
        }
    }
}
