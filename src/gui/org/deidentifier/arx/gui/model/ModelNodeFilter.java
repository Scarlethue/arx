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

package org.deidentifier.arx.gui.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.deidentifier.arx.ARXLattice;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXLattice.Anonymity;
import org.deidentifier.arx.ARXResult;

/**
 * This class implements a filter for a generalization lattice
 * @author Fabian Prasser
 */
public class ModelNodeFilter implements Serializable {

    /** SVUID*/
    private static final long    serialVersionUID   = 5451641489562102719L;

    /** The anonymity properties allowed*/
    private final Set<Anonymity> anonymity          = new HashSet<Anonymity>();
    /** The generalization levels allowed*/
    private Set<Integer>[]       generalizations    = null;
    /** The initial number of nodes*/
    private int                  maxNumNodesInitial = 0;
    /** Bound for min information loss*/
    private double               minInformationLoss = 0d;
    /** Bound for max information loss*/
    private double               maxInformationLoss = 1d;

    /**
     * Creates a new instance
     * @param maxLevels
     * @param maxNumNodesInitial
     */
    @SuppressWarnings("unchecked")
    public ModelNodeFilter(final int[] maxLevels, final int maxNumNodesInitial) {
        this.maxNumNodesInitial = maxNumNodesInitial;
        this.generalizations = new Set[maxLevels.length];
        for (int i = 0; i < generalizations.length; i++) {
            generalizations[i] = new HashSet<Integer>();
        }
    }

    /**
     * Allows transformations with any information loss to pass the filter
     */
    public void allowAllInformationLoss() {
        minInformationLoss = 0d;
        maxInformationLoss = 1d;
    }

    /**
     * Allows anonymous transformations to pass the filter
     */
    public void allowAnonymous() {
        anonymity.add(Anonymity.ANONYMOUS);
    }

    /**
     * Allows transformations with certain generalization level to pass the filter
     * @param dimension
     * @param level
     */
    public void allowGeneralization(final int dimension, final int level) {
        generalizations[dimension].add(level);
    }

    /**
     * Allows transformations with certain information loss to pass the filter
     * @param min
     * @param max
     */
    public void allowInformationLoss(final double min, final double max) {
        if (min<0d || min>1d || max <0d || max>1d) {
            throw new IllegalArgumentException("Threshold must be relative [0,1]");
        }
        minInformationLoss = min;
        maxInformationLoss = max;
    }

    /**
     * Allows non-anonymous transformations to pass the filter
     */
    public void allowNonAnonymous() {
        anonymity.add(Anonymity.NOT_ANONYMOUS);
    }

    /**
     * Allows unknown transformations to pass the filter
     */
    public void allowUnknown() {
        anonymity.add(Anonymity.PROBABLY_NOT_ANONYMOUS);
        anonymity.add(Anonymity.PROBABLY_ANONYMOUS);
        anonymity.add(Anonymity.UNKNOWN);
    }

    /**
     * Allows no transformaions to pass the filter
     */
    public void disallowAll() {
        anonymity.clear();
        minInformationLoss = 0d;
        maxInformationLoss = 1d;
        for (int i = 0; i < generalizations.length; i++) {
            generalizations[i].clear();
        }
    }

    /**
     * Filters out anonymous transformations
     */
    public void disallowAnonymous() {
        anonymity.remove(Anonymity.ANONYMOUS);
    }

    /**
     * Filters out transformations with certain generalization level
     * @param dimension
     * @param level
     */
    public void disallowGeneralization(final int dimension, final int level) {
        generalizations[dimension].remove(level);
    }

    /**
     * Filters out non-anonymous transformations
     */
    public void disallowNonAnonymous() {
        anonymity.remove(Anonymity.NOT_ANONYMOUS);
    }

    /**
     * Filters out unknown transformations
     */
    public void disallowUnknown() {
        anonymity.remove(Anonymity.PROBABLY_ANONYMOUS);
        anonymity.remove(Anonymity.PROBABLY_NOT_ANONYMOUS);
        anonymity.remove(Anonymity.UNKNOWN);
    }

    /**
     * @return the anonymity
     */
    public Set<Anonymity> getAllowedAnonymity() {
        return anonymity;
    }

    /**
     * @return the generalizations
     */
    public Set<Integer> getAllowedGeneralizations(final int dimension) {
        return generalizations[dimension];
    }

    /**
     * @return the maxInformationLoss
     */
    public double getAllowedMaxInformationLoss() {
        return maxInformationLoss;
    }

    /**
     * @return the minInformationLoss
     */
    public double getAllowedMinInformationLoss() {
        return minInformationLoss;
    }

    /**
     * Creates a node filter for the given result
     * 
     * @param result
     */
    public void initialize(final ARXResult result) {
        disallowAll();
        if (result.isResultAvailable()) {

            // Allow specializations and generalizations of optimum
            allowAnonymous();
            final double min = 0d;
            final double max = 1d;
            
            
            allowInformationLoss(min, max);
            final int[] optimum = result.getGlobalOptimum().getTransformation();
            for (int i = 0; i < optimum.length; i++) {
                allowGeneralization(i, optimum[i]);
            }

            // Build sets of visible and hidden nodes
            final Set<ARXNode> visible = new HashSet<ARXNode>();
            final Set<ARXNode> hidden = new HashSet<ARXNode>();
            visible.add(result.getGlobalOptimum());
            for (final ARXNode[] level : result.getLattice().getLevels()) {
                for (final ARXNode node : level) {
                    if (node.getAnonymity() == Anonymity.ANONYMOUS) {
                        if (!node.equals(result.getGlobalOptimum())) {
                            hidden.add(node);
                        }
                    }
                }
            }

            // Determine max generalization
            int maxgen = 0;
            for (int i = 0; i < optimum.length; i++) {
                maxgen = Math.max(result.getLattice()
                                        .getTop()
                                        .getTransformation()[i], maxgen);
            }

            // Show less generalized nodes
            for (int j = 1; j <= maxgen; j++) {
                for (int i = 0; i < optimum.length; i++) {
                    final int gen = optimum[i] - j;
                    if (gen >= 0) {
                        allowGeneralization(i, gen);
                        final int current = count(result.getLattice(), visible, hidden);
                        if (current > maxNumNodesInitial) {
                            disallowGeneralization(i, gen);
                            return;
                        }
                    }
                }
            }

            // Show more generalized nodes
            for (int j = 1; j <= maxgen; j++) {
                for (int i = 0; i < optimum.length; i++) {
                    final int gen = optimum[i] + j;
                    if (gen <= result.getLattice().getTop().getTransformation()[i]) {
                        allowGeneralization(i, gen);
                        final int current = count(result.getLattice(), visible, hidden);
                        if (current > maxNumNodesInitial) {
                            disallowGeneralization(i, gen);
                            return;
                        }
                    }
                }
            }

            // Clean up
            clean(result.getLattice(), visible, optimum);
        } else {

            // Allow generalizations of bottom
            allowNonAnonymous();
            final double max = 1d;
            final double min = 0d;
            allowInformationLoss(min, max);
            final int[] base = result.getLattice()
                                     .getBottom()
                                     .getTransformation();
            for (int i = 0; i < base.length; i++) {
                allowGeneralization(i, base[i]);
            }

            // Build sets of visible and hidden nodes
            final Set<ARXNode> visible = new HashSet<ARXNode>();
            final Set<ARXNode> hidden = new HashSet<ARXNode>();
            visible.add(result.getLattice().getBottom());
            for (final ARXNode[] level : result.getLattice().getLevels()) {
                for (final ARXNode node : level) {
                    if (node.getAnonymity() == Anonymity.NOT_ANONYMOUS) {
                        if (!node.equals(result.getLattice().getBottom())) {
                            hidden.add(node);
                        }
                    }
                }
            }

            // Determine max generalization
            int maxgen = 0;
            for (int i = 0; i < base.length; i++) {
                maxgen = Math.max(result.getLattice()
                                        .getTop()
                                        .getTransformation()[i], maxgen);
            }

            // Show more generalized nodes
            for (int j = 1; j <= maxgen; j++) {
                for (int i = 0; i < base.length; i++) {
                    final int gen = base[i] + j;
                    if (gen <= result.getLattice().getTop().getTransformation()[i]) {
                        allowGeneralization(i, gen);
                        final int current = count(result.getLattice(), visible, hidden);
                        if (current > maxNumNodesInitial) {
                            disallowGeneralization(i, gen);
                            return;
                        }
                    }
                }
            }

            // Clean up
            clean(result.getLattice(), visible, base);
        }
    }

    /**
     * Returns whether the given node is allowed to pass this filter
     * @param lattice
     * @param node
     * @return
     */
    public boolean isAllowed(final ARXLattice lattice, final ARXNode node) {
        
        double max = node.getMaximumInformationLoss().relativeTo(lattice.getMinimumInformationLoss(), 
                                                                 lattice.getMaximumInformationLoss());
        double min = node.getMinimumInformationLoss().relativeTo(lattice.getMinimumInformationLoss(), 
                                                                 lattice.getMaximumInformationLoss());
        
        if (max < minInformationLoss) {
            return false;
        } else if (min > maxInformationLoss) {
            return false;
        } else if (!anonymity.contains(node.getAnonymity())) { return false; }
        final int[] transformation = node.getTransformation();
        for (int i = 0; i < transformation.length; i++) {
            if (!generalizations[i].contains(transformation[i])) { return false; }
        }
        return true;
    }

    /**
     * Returns setting
     * @return
     */
    public boolean isAllowedAnonymous() {
        return anonymity.contains(Anonymity.ANONYMOUS);
    }

    /**
     * Returns whether the given generalization is allowed
     * 
     * @param dimension
     * @param level
     * @return
     */
    public boolean
            isAllowedGeneralization(final int dimension, final int level) {
        return generalizations[dimension].contains(level);
    }

    /**
     * Returns setting
     * @return
     */
    public boolean isAllowedNonAnonymous() {
        return anonymity.contains(Anonymity.NOT_ANONYMOUS);
    }

    /**
     * Returns setting
     * @return
     */
    public boolean isAllowedUnknown() {
        return anonymity.contains(Anonymity.PROBABLY_ANONYMOUS) ||
        	   anonymity.contains(Anonymity.PROBABLY_NOT_ANONYMOUS) ||
               anonymity.contains(Anonymity.UNKNOWN);
    }

    /**
     * Cleans up the settings
     * 
     * @param lattice
     * @return
     */
    private void clean(final ARXLattice lattice, final Set<ARXNode> visible, final int[] optimum) {

        // Remove hidden from visible
        final Iterator<ARXNode> i = visible.iterator();
        while (i.hasNext()) {
            final ARXNode node = i.next();
            if (!isAllowed(lattice, node)) {
                i.remove();
            }
        }

        // Build sets
        @SuppressWarnings("unchecked")
		final Set<Integer>[] required = new HashSet[optimum.length];
        for (int j = 0; j < optimum.length; j++) {
            required[j] = new HashSet<Integer>();
        }
        for (final ARXNode node : visible) {
            for (int j = 0; j < optimum.length; j++) {
                required[j].add(node.getTransformation()[j]);
            }
        }

        // Clean the settings
        for (int j = 0; j < optimum.length; j++) {
            final Iterator<Integer> it = generalizations[j].iterator();
            while (it.hasNext()) {
                final int l = it.next();
                if (!required[j].contains(l)) {
                    it.remove();
                }
            }
        }
    }

    /**
     * Counts the number of visible nodes
     * 
     * @param lattice
     * @return
     */
    private int count(final ARXLattice lattice, final Set<ARXNode> visible, final Set<ARXNode> hidden) {
        final Iterator<ARXNode> i = hidden.iterator();
        while (i.hasNext()) {
            final ARXNode node = i.next();
            if (isAllowed(lattice, node)) {
                i.remove();
                visible.add(node);
            }
        }
        return visible.size();
    }
}
