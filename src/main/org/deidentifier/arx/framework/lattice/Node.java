/*
 * ARX: Powerful Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.arx.framework.lattice;

import java.util.Arrays;

import org.deidentifier.arx.metric.InformationLoss;

/**
 * The Class Node.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class Node {

    /**
     * All privacy criteria are fulfilled
     */
    public static final int    PROPERTY_ANONYMOUS            = 1 << 0;

    /**
     * Not all privacy criteria are fulfilled
     */
    public static final int    PROPERTY_NOT_ANONYMOUS        = 1 << 1;

    /**
     * A k-anonymity sub-criterion is fulfilled
     */
    public static final int    PROPERTY_K_ANONYMOUS          = 1 << 2;

    /**
     * A k-anonymity sub-criterion is not fulfilled
     */
    public static final int    PROPERTY_NOT_K_ANONYMOUS      = 1 << 3;

    /**
     * The transformation results in insufficient utility
     */
    public static final int    PROPERTY_INSUFFICIENT_UTILITY = 1 << 4;

    /**
     * The transformation has been checked explicitly
     */
    public static final int    PROPERTY_CHECKED              = 1 << 5;

    /**
     * A snapshot for this transformation must be created if it fits the size
     * limits, regardless of whether it triggers the storage condition
     */
    public static final int    PROPERTY_FORCE_SNAPSHOT       = 1 << 6;

    /**
     * This node has already been visited during the second phase
     */
    public static final int    PROPERTY_VISITED              = 1 << 7;

    /**
     * Marks nodes for which the search algorithm guarantees to never check any
     * of its successors
     */
    public static final int    PROPERTY_SUCCESSORS_PRUNED    = 1 << 8;

    /**
     * We have already fired an event for this node
     */
    public static final int    PROPERTY_EVENT_FIRED          = 1 << 9;

    /** The id. */
    public final int           id;

    /** Set of properties */
    private int                properties;

    /** The predecessors. */
    private Node[]             predecessors;

    /** The level. */
    private int                level;

    /** The information loss. */
    private InformationLoss<?> informationLoss;

    /** The lower bound. */
    private InformationLoss<?> lowerBound;

    /** The transformation. */
    private int[]              transformation;

    /** The upwards. */
    private Node[]             successors;

    /** The down index. */
    private int                preIndex;

    /** The up index. */
    private int                sucIndex;

    /** Associated data */
    private Object             data;

    /**
     * Instantiates a new node.
     */
    public Node(int id) {
        this.id = id;
        this.informationLoss = null;
        this.preIndex = 0;
        this.sucIndex = 0;
        this.properties = 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) { return true; }
        if (obj == null) { return false; }
        if (getClass() != obj.getClass()) { return false; }
        final Node other = (Node) obj;
        if (!Arrays.equals(transformation, other.transformation)) { return false; }
        return true;
    }

    /** Associated data*/
    public Object getData() {
        return data;
    }

    /**
     * Returns the information loss
     * 
     * @return
     */
    public InformationLoss<?> getInformationLoss() {
        return informationLoss;
    }

    /**
     * Returns the level
     * 
     * @return
     */
    public int getLevel() {
        return level;
    }

    /**
     * @return the lowerBound
     */
    public InformationLoss<?> getLowerBound() {
        return lowerBound;
    }

    /**
     * Returns the predecessors
     * 
     * @return
     */
    public Node[] getPredecessors() {
        return predecessors;
    }

    /**
     * Returns the successors
     * 
     * @return
     */
    public Node[] getSuccessors() {
        return successors;
    }

    /**
     * Returns the transformation
     * 
     * @return
     */
    public int[] getTransformation() {
        return transformation;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(transformation);
    }

    /**
     * Returns whether the node has the given property
     * @param property
     * @return
     */
    public boolean hasProperty(int property){
        return (properties & property) == property;
    }

    /** Associated data*/
    public void setData(Object data) {
        this.data = data;
    }

    /**
     * Sets the transformation
     * 
     * @param transformation
     */
    public void setTransformation(int[] transformation, int level) {
        this.transformation = transformation;
        this.level = level;
    }

    /**
     * Sets the information loss
     * 
     * @param informationLoss
     */
    protected void setInformationLoss(final InformationLoss<?> informationLoss) {
        if (this.informationLoss == null) {
            this.informationLoss = informationLoss;
        }
    }

    /**
     * Sets the information loss
     * 
     * @param informationLoss
     */
    protected void setLowerBound(final InformationLoss<?> lowerBound) {
        if (this.lowerBound == null) {
            this.lowerBound = lowerBound;
        }
    }

    
    /**
     * Sets the predecessors
     * 
     * @param nodes
     */
    protected void setPredecessors(Node[] nodes) {
        predecessors = nodes;
    }

    /**
     * Sets the given property
     * @param property
     * @return
     */
    protected void setProperty(int property){
        properties |= property;
    }
  
    /**
     * Sets the successors
     * 
     * @param nodes
     */
    protected void setSuccessors(Node[] nodes) {
        successors = nodes;
    }

    /**
     * Adds a predecessor
     * 
     * @param predecessor
     */
    void addPredecessor(Node predecessor) {
        predecessors[preIndex++] = predecessor;
    }

    /**
     * Adds a successor
     * 
     * @param successor
     */
    void addSuccessor(Node successor) {
        successors[sucIndex++] = successor;
    }
}
