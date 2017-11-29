/*
 * LoadingsGibbsOperatorParser.java
 *
 * Copyright (c) 2002-2015 Alexei Drummond, Andrew Rambaut and Marc Suchard
 *
 * This file is part of BEAST.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership and licensing.
 *
 * BEAST is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 *  BEAST is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with BEAST; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
 * Boston, MA  02110-1301  USA
 */

package dr.inferencexml.operators;

import dr.evomodel.treedatalikelihood.TreeDataLikelihood;
import dr.evomodel.treedatalikelihood.continuous.IntegratedFactorAnalysisLikelihood;
import dr.inference.distribution.DistributionLikelihood;
import dr.inference.distribution.MomentDistributionModel;
import dr.inference.model.LatentFactorModel;
import dr.inference.model.MatrixParameterInterface;
import dr.inference.operators.LoadingsGibbsOperator;
import dr.inference.operators.LoadingsGibbsTruncatedOperator;
import dr.inference.operators.factorAnalysis.FactorAnalysisOperatorAdaptor;
import dr.inference.operators.factorAnalysis.NewLoadingsGibbsOperator;
import dr.xml.*;

/**
 * @author Max R. Tolkoff
 * @author Marc A. Suchard
 */
public class LoadingsGibbsOperatorParser extends AbstractXMLObjectParser {

    private final static String LOADINGS_GIBBS_OPERATOR = "loadingsGibbsOperator";
    private final static String WEIGHT = "weight";
    private final static String RANDOM_SCAN = "randomScan";
    private final static String WORKING_PRIOR = "workingPrior";
    private final static String CUTOFF_PRIOR = "cutoffPrior";
    private final static String MULTI_THREADED = "multiThreaded";
    private final static String NUM_THREADS = "numThreads";
    private final static String MODE = "newMode";

    @Override
    public Object parseXMLObject(XMLObject xo) throws XMLParseException {

        // Get XML attributes
        double weight = xo.getDoubleAttribute(WEIGHT);
        boolean randomScan = xo.getAttribute(RANDOM_SCAN, true);
        int numThreads = xo.getAttribute(NUM_THREADS, 4);
        boolean multiThreaded = xo.getAttribute(MULTI_THREADED, false);
        boolean useNewMode = xo.getAttribute(MODE, false);

        // Get main objects
        LatentFactorModel LFM = (LatentFactorModel) xo.getChild(LatentFactorModel.class);

        // TODO The next 3 lines are not necessary, nor in XML rules
        MatrixParameterInterface loadings = null;
        if (xo.getChild(MatrixParameterInterface.class) != null) {
            loadings = (MatrixParameterInterface) xo.getChild(MatrixParameterInterface.class);
        }

        // Get priors
        DistributionLikelihood prior = (DistributionLikelihood) xo.getChild(DistributionLikelihood.class);
        MomentDistributionModel prior2 = (MomentDistributionModel) xo.getChild(MomentDistributionModel.class);

        DistributionLikelihood cutoffPrior = null;
        if (xo.hasChildNamed(CUTOFF_PRIOR)) {
            cutoffPrior = (DistributionLikelihood) xo.getChild(CUTOFF_PRIOR).getChild(DistributionLikelihood.class);
        }

        DistributionLikelihood WorkingPrior = null;
        if (xo.getChild(WORKING_PRIOR) != null) {
            WorkingPrior = (DistributionLikelihood) xo.getChild(WORKING_PRIOR).getChild(DistributionLikelihood.class);
        }

        // Dispatch
        if (prior != null) {
            if (useNewMode) {

                final FactorAnalysisOperatorAdaptor adaptor;
                if (LFM != null) {
                    adaptor = new FactorAnalysisOperatorAdaptor.SampledFactors(LFM);
                } else {
                    IntegratedFactorAnalysisLikelihood integratedLikelihood = (IntegratedFactorAnalysisLikelihood)
                            xo.getChild(IntegratedFactorAnalysisLikelihood.class);
                    TreeDataLikelihood treeLikelihood = (TreeDataLikelihood) xo.getChild(TreeDataLikelihood.class);
                    adaptor = new FactorAnalysisOperatorAdaptor.IntegratedFactors(integratedLikelihood, treeLikelihood);
                }

                return new NewLoadingsGibbsOperator(adaptor, prior, weight, randomScan, WorkingPrior,
                        multiThreaded, numThreads);
            } else {
                return new LoadingsGibbsOperator(LFM, prior, weight, randomScan, WorkingPrior, multiThreaded, numThreads);
            }
        } else {
            return new LoadingsGibbsTruncatedOperator(LFM, prior2, weight, randomScan, loadings, cutoffPrior);
        }
    }

    @Override
    public XMLSyntaxRule[] getSyntaxRules() {
        return rules;
    }

    private XMLSyntaxRule[] rules = new XMLSyntaxRule[]{
            new XORRule(
                    new ElementRule(LatentFactorModel.class),
                    new AndRule(
                            new ElementRule(IntegratedFactorAnalysisLikelihood.class),
                            new ElementRule(TreeDataLikelihood.class)
                    )
            ),
            new XORRule(
                    new ElementRule(DistributionLikelihood.class),
                    new AndRule(
                            new ElementRule(MomentDistributionModel.class),
                            new ElementRule(CUTOFF_PRIOR, new XMLSyntaxRule[] {
                                    new ElementRule(DistributionLikelihood.class)
                            }))
            ),
            AttributeRule.newDoubleRule(WEIGHT),
            AttributeRule.newBooleanRule(MULTI_THREADED, true),
            AttributeRule.newIntegerRule(NUM_THREADS, true),
            AttributeRule.newBooleanRule(MODE, true),
            new ElementRule(WORKING_PRIOR, new XMLSyntaxRule[]{
                    new ElementRule(DistributionLikelihood.class)
            }, true),
    };

    @Override
    public String getParserDescription() {
        return "Gibbs sampler for the loadings matrix of a latent factor model";
    }

    @Override
    public Class getReturnType() {
        return LoadingsGibbsOperator.class;
    }

    @Override
    public String getParserName() {
        return LOADINGS_GIBBS_OPERATOR;
    }
}
