package grondag.adversity.library.model.quadfactory;

import java.util.ArrayList;

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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ArrayListMultimap;

import gnu.trove.map.hash.TLongObjectHashMap;
import grondag.adversity.Adversity;


public class CSGNode
{
    /**
     * RawQuads.
     */
    private List<RawQuad> quads;
    
    /**
     * Plane used for BSP.
     */
    private CSGPlane plane;
    
    /**
     * RawQuads in front of the plane.
     */
    private CSGNode front;
    
    /**
     * RawQuads in back of the plane.
     */
    private CSGNode back;

    /**
     * Constructor.
     *
     * Creates a BSP node consisting of the specified polygons.
     *
     * @param quadsIn polygons
     */
    public CSGNode(List<RawQuad> quadsIn) {
        this.quads = new LinkedList<RawQuad>();
        if (quadsIn != null) {
            this.build(quadsIn, true);
        }
    }

    /**
     * Constructor. Creates a node without polygons.
     */
    public CSGNode() {
        this(null);
    }

    @Override
    public CSGNode clone() {
        CSGNode node = new CSGNode();
        node.plane = this.plane == null ? null : this.plane.clone();
        node.front = this.front == null ? null : this.front.clone();
        node.back = this.back == null ? null : this.back.clone();
        node.quads = new LinkedList<RawQuad>();
        quads.forEach((RawQuad p) -> {
            node.quads.add(p.clone());
        });
        return node;
    }

    /**
     * Converts solid space to empty space and vice verca.
     */
    public void invert() {
        
        quads.forEach((quad) -> {
            quad.invert();
        });

        if (this.plane == null && !quads.isEmpty()) {
            this.plane = new CSGPlane(quads.get(0));
        } else if (this.plane == null && quads.isEmpty()) {
            throw new RuntimeException("Please fix me! I don't know what to do?");
        }

        this.plane.flip();

        if (this.front != null) {
            this.front.invert();
        }
        if (this.back != null) {
            this.back.invert();
        }
        CSGNode temp = this.front;
        this.front = this.back;
        this.back = temp;
    }

    /**
     * Recursively removes all polygons in the {@link polygons} list that are
     * contained within this BSP tree.
     *
     * <b>Note:</b> polygons are splitted if necessary.
     *
     * @param quads the polygons to clip
     *
     * @return the cliped list of polygons
     */
    private List<RawQuad> clipQuads(List<RawQuad> quads) {

        if (this.plane == null) {
            return new LinkedList<RawQuad>(quads);
        }

        List<RawQuad> frontP = new LinkedList<RawQuad>();
        List<RawQuad> backP = new LinkedList<RawQuad>();

        for (RawQuad quad : quads) {
            this.plane.splitQuad(quad, frontP, backP, frontP, backP);
        }
        if (this.front != null) {
            frontP = this.front.clipQuads(frontP);
        }
        if (this.back != null) {
            backP = this.back.clipQuads(backP);
        } else {
            backP = new LinkedList<RawQuad>();
        }

        frontP.addAll(backP);
        return frontP;
    }

    // Remove all polygons in this BSP tree that are inside the other BSP tree
    // `bsp`.
    /**
     * Removes all polygons in this BSP tree that are inside the specified BSP
     * tree ({@code bsp}).
     *
     * <b>Note:</b> polygons are split if necessary.
     *
     * @param bsp bsp that shall be used for clipping
     */
    public void clipTo(CSGNode bsp) {
        this.quads = bsp.clipQuads(this.quads);
        if (this.front != null) {
            this.front.clipTo(bsp);
        }
        if (this.back != null) {
            this.back.clipTo(bsp);
        }
    }

    /**
     * Returns a list of all polygons in this BSP tree.
     *
     * @return a list of all polygons in this BSP tree
     */
    public List<RawQuad> allRawQuads() {
        List<RawQuad> localRawQuads = new LinkedList<>(this.quads);
        if (this.front != null) {
            localRawQuads.addAll(this.front.allRawQuads());
//            polygons = Utils.concat(polygons, this.front.allRawQuads());
        }
        if (this.back != null) {
//            polygons = Utils.concat(polygons, this.back.allRawQuads());
            localRawQuads.addAll(this.back.allRawQuads());
        }

        return localRawQuads;
    }
    
    /**
     * Returns all quads in this tree recombined as much as possible.
     * Use instead of allRawQuads() for anything to be rendered.
     * Generally only useful on root node!
     * 
     * Will only work if build was called with initCSG parameter = true
     * during initialization of the tree because it uses the information populated then.
     * @return
     */
    public List<RawQuad> recombinedRawQuads()
    {
        TLongObjectHashMap<LinkedList<RawQuad>> ancestorBuckets = new TLongObjectHashMap<LinkedList<RawQuad>>();
        
        this.allRawQuads().forEach((quad) -> 
        {
            if(!ancestorBuckets.contains(quad.ancestorQuadID))
            {
                ancestorBuckets.put(quad.ancestorQuadID, new LinkedList<RawQuad>());
            }
            ancestorBuckets.get(quad.ancestorQuadID).add(quad);
        });
        
        LinkedList<RawQuad> retVal = new LinkedList<RawQuad>();
        ancestorBuckets.valueCollection().forEach((quadList) ->
        {
            retVal.addAll(recombine(quadList));
        });
        
        return retVal;
    }
//
//    private class CsgEdge
//    {
//        protected final long edgeId;
//        protected final int lowVertexId;
//        protected final int highVertexId;
//        protected final IdHolder quadIdHolder;
//        
//        private class IdHolder
//        {
//            protected long id;
//        }
//    }
//    
//    private class CsgVertexTracker
//    {
//        protected int getVertexId(Vertex vertexIn)
//        {
//            return 0;
//        }
//    }
    
    /**
     * Tries to combine two quads along the given edge. To join, all must be true:
     * 1) shared edge id
     * 2) vertexes in opposite order for each quad match each other
     * 3) quads are both inverted or both not inverted
     * 4) resulting quad has three or four vertices (Tri or Quad)
     * 5) resulting quad is convex
     * 
     * Returns null if quads cannot be joined.
     */
    private RawQuad joinCsgQuads(RawQuad aQuad, RawQuad bQuad, long lineID)
    {
        // quads must be same orientation to be joined
        if(aQuad.isInverted != bQuad.isInverted) return null;

        int aStartIndex = aQuad.findLineIndex(lineID);
        // shouldn't happen, but won't work if does
        if(aStartIndex == RawQuad.LINE_NOT_FOUND) return null;
        int aNextIndex = aStartIndex + 1 == aQuad.vertexCount() ? 0 : aStartIndex + 1;
        int aPrevIndex = aStartIndex == 0 ? aQuad.vertexCount() - 1 : aStartIndex - 1;

        int bStartIndex = bQuad.findLineIndex(lineID);
        // shouldn't happen, but won't work if does
        if(bStartIndex == RawQuad.LINE_NOT_FOUND) return null;
        int bNextIndex = bStartIndex + 1 == bQuad.vertexCount() ? 0 : bStartIndex + 1;
        int bPrevIndex = bStartIndex == 0 ? bQuad.vertexCount() - 1 : bStartIndex - 1;
        
        // confirm vertices on either end of vertex match
        if(!aQuad.getVertex(aStartIndex).isCsgEqual(bQuad.getVertex(bNextIndex))) return null;
        if(!aQuad.getVertex(aNextIndex).isCsgEqual(bQuad.getVertex(bStartIndex))) return null;

        ArrayList<Vertex> joinedVertex = new ArrayList<Vertex>(8);
        ArrayList<Long> joinedLineID = new ArrayList<Long>(8);
        
        for(int a = 0; a < aQuad.vertexCount(); a++)
        {
            if(a == aStartIndex)
            {
                //if vertex is on the same line as prev and next vertex, leave it out.
                if(aQuad.getLineID(aPrevIndex) != bQuad.getLineID(bNextIndex))
                {
                    joinedVertex.add(aQuad.getVertex(aStartIndex));
                    joinedLineID.add(bQuad.getLineID(bNextIndex));
                }
                
                // won't add the two bQuad vertexes in common with A, so only going to add one or two
                int b = bNextIndex + 1;
                if(b == bQuad.vertexCount()) b = 0;
                joinedVertex.add(bQuad.getVertex(b));
                joinedLineID.add(bQuad.getLineID(b));
                
                // second vertex added if quad B has four
                if(bQuad.vertexCount() == 4)
                {
                    b = b + 1;
                    if(b == bQuad.vertexCount()) b = 0;
                    joinedVertex.add(bQuad.getVertex(b));
                    joinedLineID.add(bQuad.getLineID(b));
                }
            }
            else if(a == aNextIndex)
            {
                //if vertex is on the same line as prev and next vertex, leave it out.
                if(aQuad.getLineID(aNextIndex) != bQuad.getLineID(bPrevIndex))
                {
                    joinedVertex.add(aQuad.getVertex(aNextIndex));
                    joinedLineID.add(aQuad.getLineID(aNextIndex));
                }
            }
            else
            {
                joinedVertex.add(aQuad.getVertex(a));
                joinedLineID.add(aQuad.getLineID(a));
           }
        }   
        
        // max size is quad
        if(joinedVertex.size() > 4 || joinedVertex.size() < 3) return null;
        
        // actually build the new quad!
        RawQuad joinedQuad = joinedVertex.size() == 3 ? new RawTri(aQuad) : new RawQuad(aQuad);
        joinedQuad.quadID = RawQuad.nextQuadID.getAndIncrement();
        for(int i = 0; i < joinedVertex.size(); i++)
        {
            joinedQuad.setVertex(i, joinedVertex.get(i));
            joinedQuad.setLineID(i, joinedLineID.get(i));
        }

        // must be convex
        if(!joinedQuad.isConvex()) return null;
        
        return joinedQuad;
        
    }
    
    private List<RawQuad> recombine(LinkedList<RawQuad> quadList)
    {
        if(quadList.getFirst().ancestorQuadID == RawQuad.IS_AN_ANCESTOR) return quadList;
        
        TLongObjectHashMap<RawQuad> quadMap = new TLongObjectHashMap<RawQuad>(quadList.size());
        TreeMap<Long, TreeMap<Long, Integer>> edgeMap = new TreeMap<Long, TreeMap<Long, Integer>>();
        
        for(RawQuad q : quadList) 
        {
            quadMap.put(q.quadID, q);
            for(int i = 0; i < q.vertexCount(); i++)
            {
                if(!edgeMap.containsKey(q.getLineID(i)))
                {
                    edgeMap.put(q.getLineID(i), new TreeMap<Long, Integer>());
                }
                edgeMap.get(q.getLineID(i)).put(q.quadID, i);
            }
        }
        
        boolean potentialMatchesRemain = true;
        while(potentialMatchesRemain)
        {
            potentialMatchesRemain = false;
            
            for(Long edgeKey : edgeMap.descendingKeySet())
            {
                TreeMap<Long, Integer> edgeQuadMap = edgeMap.get(edgeKey);
                
                if(edgeQuadMap.isEmpty()) break;
                
                Long[] edgeQuadIDs = edgeQuadMap.keySet().toArray(new Long[1]);
                if(edgeQuadIDs.length < 2) break;
                
                for(int i = 0; i < edgeQuadIDs.length - 1; i++)
                {
                    for(int j = i + 1; j < edgeQuadIDs.length; j++)
                    {
                        // Examining two quads that share an edge
                        // to determine if they can be combined.
                        
                        RawQuad iQuad = quadMap.get(edgeQuadIDs[i]);
                        RawQuad jQuad = quadMap.get(edgeQuadIDs[j]);
                        
                        if(iQuad == null || jQuad == null) break;
                        
                        RawQuad joined = joinCsgQuads(iQuad, jQuad, edgeKey);
                        
                        if(joined != null)
                        {    
                            potentialMatchesRemain = true;
                            
                            // remove quads from main map
                            quadMap.remove(iQuad.quadID);
                            quadMap.remove(jQuad.quadID);
                            
                            // remove quads from edge map
                            for(int n = 0; n < iQuad.vertexCount(); n++)
                            {
                                TreeMap<Long, Integer> removeMap = edgeMap.get(iQuad.getLineID(n));
                                removeMap.remove(iQuad.quadID);
                            }
                            
                            for(int n = 0; n < jQuad.vertexCount(); n++)
                            {
                                TreeMap<Long, Integer> removeMap = edgeMap.get(jQuad.getLineID(n));
                                removeMap.remove(jQuad.quadID);
                            }                            
                            
                            // add quad to main map
                            quadMap.put(joined.quadID, joined);
                            
                            // add quad to edge map
                            for(int n = 0; n < joined.vertexCount(); n++)
                            {
                                if(!edgeMap.containsKey(joined.getLineID(n)))
                                {
                                    edgeMap.put(joined.getLineID(n), new TreeMap<Long, Integer>());
                                }
                                edgeMap.get(joined.getLineID(n)).put(joined.quadID, n);
                            }                        
                        }
                    }
                }
            }
            
        }
        
        LinkedList<RawQuad> retVal = new LinkedList<RawQuad>();
        retVal.addAll(quadMap.valueCollection());
        return retVal;
            
        
    }
    
    /**
     * Build a BSP tree out of {@code polygons}. When called on an existing
     * tree, the new polygons are filtered down to the bottom of the tree and
     * become new nodes there. Each set of polygons is partitioned using the
     * first polygon (no heuristic is used to pick a good split).
     *
     * @param quadsIn polygons used to build the BSP
     */
    public final void build(List<RawQuad> quadsIn, boolean initCSG) {
        
        if (quadsIn.isEmpty()) return;

        if (this.plane == null) {
            this.plane = new CSGPlane(quadsIn.get(0));
        }

        List<RawQuad> frontP = new LinkedList<RawQuad>();
        List<RawQuad> backP = new LinkedList<RawQuad>();

        // parallel version does not work here
        quadsIn.forEach((quad) -> {
            this.plane.splitQuad(
                    initCSG ? quad.clone().initCsg() : quad.clone(), this.quads, this.quads, frontP, backP);
        });

        if (!frontP.isEmpty()) {
            if (this.front == null) {
                this.front = new CSGNode();
            }
            this.front.build(frontP, false);
        }
        if (!backP.isEmpty()) {
            if (this.back == null) {
                this.back = new CSGNode();
            }
            this.back.build(backP, false);
        }
    }
    
}
