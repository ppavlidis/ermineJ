package classScore.analysis;

import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import baseCode.bio.geneset.GONames;
import baseCode.bio.geneset.GeneAnnotations;

import cern.jet.math.Arithmetic;
import classScore.Settings;
import classScore.data.GeneScoreReader;
import classScore.data.GeneSetResult;

/**
 * Generate Overrepresentation p values for gene sets.
 * <p>
 * Copyright (c) 2004 Columbia University
 * </p>
 * 
 * @author pavlidis
 * @version $Id$
 */
public class OraGeneSetPvalSeriesGenerator extends AbstractGeneSetPvalGenerator {

   private Map results;
   private int numOverThreshold;
   private int numUnderThreshold;
   private int inputSize;

   public OraGeneSetPvalSeriesGenerator( Settings settings,
         GeneAnnotations geneData, GeneSetSizeComputer csc, GONames gon,
         int inputSize ) {
      super( settings, geneData, csc, gon );
      this.inputSize = inputSize;
      results = new HashMap();
   }

   public Map getResults() {
      return results;
   }

   /**
    * Generate a complete set of class results. The arguments are not constant under permutations. The second is only
    * needed for the aroc method. This is to be used only for the 'real' data since it modifies 'results',
    * 
    * @param group_pval_map a <code>Map</code> value
    * @param probesToPvals a <code>Map</code> value
    */
   public void classPvalGenerator( Map geneToGeneScoreMap, Map probesToPvals ) {
      Collection entries = geneAnnots.getGeneSetToProbeMap().entrySet(); // go ->

      Iterator it = entries.iterator(); // the classes.

      OraPvalGenerator cpv = new OraPvalGenerator( settings, geneAnnots, csc,
            numOverThreshold, numUnderThreshold, goName, inputSize );

      // For each class.
      while ( it.hasNext() ) {
         Map.Entry e = ( Map.Entry ) it.next();
         String geneSetName = ( String ) e.getKey();
         GeneSetResult res = cpv.classPval( geneSetName, geneToGeneScoreMap,
               probesToPvals );
         if ( res != null ) {
            results.put( geneSetName, res );
         }
      }
   }

   /**
    * Calculate numOverThreshold and numUnderThreshold for hypergeometric distribution. This is a constant under
    * permutations, but depends on weights.
    * 
    * @param inp_entries The pvalues for the probes (no weights) or groups (weights)
    * @todo make this private and called by OraPvalGenerator.
    */
   public int hgSizes( Collection inp_entries ) {

      double geneScoreThreshold = settings.getPValThreshold();

      if ( settings.getDoLog() ) {
         geneScoreThreshold = -Arithmetic.log10(geneScoreThreshold);
      }
      
      Iterator itr = inp_entries.iterator();
      while ( itr.hasNext() ) {
         Map.Entry m = ( Map.Entry ) itr.next();
         double geneScore = ( ( Double ) m.getValue() ).doubleValue();

         if ( scorePassesThreshold( geneScore, geneScoreThreshold ) ) {
            numOverThreshold++;
         } else {
            numUnderThreshold++;
         }

      }
      return numOverThreshold;
   }

}