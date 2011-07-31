/*
 * The ermineJ project
 * 
 * Copyright (c) 2006 University of British Columbia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package ubic.erminej.data;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.math.Rank;
import ubic.basecode.util.CancellationException;
import ubic.basecode.util.FileTools;
import ubic.basecode.util.StatusStderr;
import ubic.basecode.util.StatusViewer;

import cern.jet.math.Arithmetic;
import ubic.erminej.Settings;
import ubic.erminej.SettingsHolder;

/**
 * Parse and store probe->score associations. The values are stored in a Map probeToPvalMap. This is used to see what
 * probes are int the data set, as well as the score for each probe.
 * 
 * @author Shahmil Merchant
 * @author Paul Pavlidis
 * @version $Id$
 */
public class GeneScores {

    private static final double SMALL = 10e-16;
    protected static final Log log = LogFactory.getLog( GeneScores.class );

    /**
     * @return true if these scores were transformed via -log_10(x) when they were read in (according to the settings)
     */
    public boolean isNegativeLog10Transformed() {
        return logTransform;
    }

    private Map<Gene, Double> geneToScoreMap;
    private Map<Probe, Double> probeToScoreMap;
    final private GeneAnnotations geneAnnots;
    private StatusViewer messenger = new StatusStderr();

    /**
     * Refers to the _original_ scores.
     */
    private boolean biggerIsBetter = false;

    /**
     * Refers to what was done to the original scores. The scores stored here are negative-logged if this is true.
     */
    private boolean logTransform = true;

    private Settings.MultiProbeHandling gpMethod = SettingsHolder.MultiProbeHandling.BEST;

    /**
     * Create a copy of source that contains only the probes given.
     * 
     * @param source
     * @param probes
     */
    public GeneScores( GeneScores source, Collection<Probe> probes ) {
        this.geneAnnots = source.geneAnnots;
        this.messenger = source.messenger;

        this.biggerIsBetter = source.biggerIsBetter;
        this.logTransform = source.logTransform;
        this.gpMethod = source.gpMethod;

        this.init();

        for ( Probe p : probes ) {
            Double s = source.getProbeToScoreMap().get( p );
            if ( s == null ) {
                throw new IllegalArgumentException( "Probe given that wasn't in the source: " + p );
            }
            this.probeToScoreMap.put( p, s );
        }

        setUpGeneToScoreMap();
    }

    /**
     * @param is - input stream
     * @param settings
     * @param messenger
     * @param geneAnnotations
     * @throws IOException
     */
    public GeneScores( InputStream is, SettingsHolder settings, StatusViewer m, GeneAnnotations geneAnnotations )
            throws IOException {

        // used only in tests.
        if ( geneAnnotations == null ) {
            throw new IllegalArgumentException( "Annotations cannot be null" );
        }
        this.geneAnnots = geneAnnotations;
        this.init( settings );
        if ( m != null ) this.messenger = m;
        read( is, settings.getScoreCol(), -1 );

    }

    /**
     * @return The annotation set that was used to set this up.
     */
    public GeneAnnotations getGeneAnnots() {
        return geneAnnots;
    }

    /**
     * Constructor designed for use when input is not a file.
     * 
     * @param probes List of Strings.
     * @param scores List of java.lang.Doubles containing the scores for each probe.
     * @param geneAnnots
     * @param settings
     */
    public GeneScores( List<String> probes, List<Double> scores, GeneAnnotations geneAnnots, SettingsHolder settings ) {

        this.geneAnnots = geneAnnots;

        if ( probes.size() != scores.size() ) {
            throw new IllegalArgumentException( "Probe and scores must be equal in number" );
        }
        if ( probes.size() == 0 ) {
            throw new IllegalArgumentException( "No probes" );
        }

        this.init( settings );
        boolean invalidLog = false;
        boolean invalidNumber = false;
        String badNumberString = "";
        int numProbesKept = 0;
        int numRepeatedProbes = 0;
        Collection<String> unknownProbes = new HashSet<String>();
        Collection<String> unannotatedProbes = new HashSet<String>();

        for ( int i = 0; i < probes.size(); i++ ) {
            String ps = probes.get( i );
            Double value = scores.get( i );

            // only keep probes that are in our array platform.
            Probe probe = geneAnnots.findProbe( ps );
            if ( probe == null ) {
                unknownProbes.add( ps );
                continue;
            }

            if ( probe.getGeneSets().isEmpty() ) {
                /*
                 * Important. We're ignoring probes that don't have any terms.
                 */
                unannotatedProbes.add( ps );
                continue;
            }

            if ( probe.getName().matches( "AFFX.*" ) ) { // FIXME: put this rule somewhere else // todo use a filter.
                continue;
            }

            double pValue = value.doubleValue();

            // Fudge when pvalues are zero.
            if ( settings.getDoLog() && pValue <= 0.0 ) {
                invalidLog = true;
                pValue = SMALL;
            }

            if ( settings.getDoLog() ) {
                pValue = -Arithmetic.log10( pValue );
            }

            /* we're done... */
            numProbesKept++;
            if ( probeToScoreMap.containsKey( probe ) ) {
                log.warn( "Repeated identifier: " + probe + ", keeping original value." );
                numRepeatedProbes++;
            } else {
                probeToScoreMap.put( probe, new Double( pValue ) );
            }
        }
        reportProblems( invalidLog, unknownProbes, unannotatedProbes, invalidNumber, badNumberString, numProbesKept,
                numRepeatedProbes );
        setUpGeneToScoreMap();
    }

    public int numGenesAboveThreshold( double geneScoreThreshold ) {
        int count = 0;

        double t = geneScoreThreshold;
        if ( isNegativeLog10Transformed() ) {
            t = -Arithmetic.log10( geneScoreThreshold );
        }

        for ( Gene g : this.geneToScoreMap.keySet() ) {

            if ( rankLargeScoresBest() ) {
                if ( this.geneToScoreMap.get( g ) > t ) {
                    count++;
                }
            } else {
                if ( this.geneToScoreMap.get( g ) < t ) {
                    count++;
                }
            }
        }

        return count;
    }

    public GeneScores( String filename, SettingsHolder settings, StatusViewer messenger, GeneAnnotations geneAnnots,
            int limit ) throws IOException {
        if ( StringUtils.isBlank( filename ) ) {
            throw new IllegalArgumentException( "Filename for gene scores can't be blank" );
        }
        this.geneAnnots = geneAnnots;
        this.messenger = messenger;
        this.init( settings );
        FileTools.checkPathIsReadableFile( filename );
        InputStream is = FileTools.getInputStreamFromPlainOrCompressedFile( filename );
        read( is, settings.getScoreCol(), limit );
        is.close();
    }

    /**
     * @param filename
     * @param settings
     * @param messenger
     * @param geneAnnots
     * @throws IOException
     */
    public GeneScores( String filename, SettingsHolder settings, StatusViewer messenger, GeneAnnotations geneAnnots )
            throws IOException {
        this( filename, settings, messenger, geneAnnots, -1 );
    }

    /**
     * Note that these will already be log-transformed, if that was requested by the user.
     * 
     * @return
     */
    public Double[] getGeneScores() {
        return this.geneToScoreMap.values().toArray( new Double[] {} );
    }

    /**
     * @param shuffle Whether the map should be scrambled first. If so, then groups are randomly associated with scores,
     *        but the actual values are the same. This is used for resampling multiple test correction.
     * @return Map of groups of genes to scores (which will have been -log-transformed already, if requested)
     */
    public Map<Gene, Double> getGeneToScoreMap( boolean shuffle ) {
        if ( shuffle ) {
            Map<Gene, Double> scrambled_map = new LinkedHashMap<Gene, Double>();
            Set<Gene> keys = geneToScoreMap.keySet();
            Iterator<Gene> it = keys.iterator();

            Collection<Double> values = geneToScoreMap.values();
            List<Double> valvec = new Vector<Double>( values );
            Collections.shuffle( valvec );

            // randomly associate keys and values
            int i = 0;
            while ( it.hasNext() ) {
                scrambled_map.put( it.next(), valvec.get( i ) );
                i++;
            }
            return scrambled_map;

        }
        return geneToScoreMap;

    }

    /**
     * Note that these values will already be log tranformed if that was requested.
     * 
     * @return
     */
    public Map<Gene, Double> getGeneToScoreMap() {
        return geneToScoreMap;
    }

    public int getNumProbesUsed() {
        return probeToScoreMap.size();
    }

    public int getNumGenesUsed() {
        return geneToScoreMap.size();
    }

    /**
     * Note that these values will already be log-transformed if that was requested.
     * 
     * @return
     */
    public Double[] getProbeScores() {
        return this.probeToScoreMap.values().toArray( new Double[] {} );
    }

    /**
     * Note that these values will already be log-transformed if that was requested
     */
    public Map<Probe, Double> getProbeToScoreMap() {
        return probeToScoreMap;
    }

    /**
     * @return list of genes in order of their scores, where the <em>first</em> gene is the 'best'. If 'big is better',
     *         genes with large scores will be given first. If smaller is better (pvalues) and the data are -log
     *         transformed (usual), then the gene that had the smallest pvalue will be first.
     */
    public List<Gene> getRankedGenes() {

        Map<Gene, Integer> ranked = Rank.rankTransform( getGeneToScoreMap(), this.rankLargeScoresBest() );

        List<Gene> rankedGenes = new ArrayList<Gene>( ranked.keySet() );

        for ( Gene g : ranked.keySet() ) {
            Integer r = ranked.get( g );
            rankedGenes.set( r, g );
        }

        return rankedGenes;
    }

    /**
     * Note that these values will already be log-transformed if that was requested.
     */
    public Double[] getScores() {
        return this.probeToScoreMap.values().toArray( new Double[] {} );
    }

    /**
     * @param probe_id
     * @return
     */
    public double getValueMap( String probe_id ) {
        double value = 0.0;

        if ( probeToScoreMap.get( probe_id ) != null ) {
            value = Double.parseDouble( ( probeToScoreMap.get( probe_id ) ).toString() );
        }

        return value;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        for ( Probe probe : probeToScoreMap.keySet() ) {
            double score = probeToScoreMap.get( probe );
            buf.append( probe.getName() + "\t" + score + "\n" );
        }
        return buf.toString();
    }

    private void init() {
        this.geneToScoreMap = new LinkedHashMap<Gene, Double>( 1000 );
        this.probeToScoreMap = new LinkedHashMap<Probe, Double>( 1000 );
    }

    private void init( SettingsHolder settings ) {
        init();

        this.biggerIsBetter = settings.getBigIsBetter();
        this.logTransform = settings.getDoLog();
        this.gpMethod = settings.getGeneRepTreatment();

    }

    /**
     * @param is
     * @param limit
     * @throws IOException
     * @throws IllegalStateException
     */
    private void read( InputStream is, int scoreCol, int limit ) throws IOException, IllegalStateException {
        assert geneAnnots != null;
        if ( scoreCol < 2 ) {
            throw new IllegalArgumentException( "Illegal column number " + scoreCol + ", must be greater than 1" );
        }

        if ( messenger != null ) {
            messenger.showStatus( "Reading gene scores from column " + scoreCol );
        }

        BufferedReader dis = new BufferedReader( new InputStreamReader( new BufferedInputStream( is ) ) );
        String row;
        boolean invalidLog = false;
        boolean invalidNumber = false;
        String badNumberString = "";
        int scoreColumnIndex = scoreCol - 1;
        int numProbesKept = 0;
        int numUnknownProbes = 0;
        int numRepeatedProbes = 0;
        Collection<String> unknownProbes = new HashSet<String>();
        dis.readLine(); // skip header.
        Collection<String> unannotatedProbes = new HashSet<String>();

        boolean warned = false;
        while ( ( row = dis.readLine() ) != null ) {
            String[] fields = row.split( "\t" );

            // ignore rows that have insufficient columns.
            if ( fields.length < scoreCol ) {
                continue;
            }

            String probeId = StringUtils.strip( fields[0] );

            if ( probeId.matches( "AFFX.*" ) ) { // FIXME: put this rule somewhere else
                if ( messenger != null && !warned ) {
                    messenger.showStatus( "Skipping probe in pval file: " + probeId + " (further warnings suppressed)" );
                    warned = true;
                }
                continue;
            }

            // only keep probes that are in our array platform.

            Probe p = geneAnnots.findProbe( probeId );

            if ( p == null ) {
                if ( log.isDebugEnabled() ) log.debug( "\"" + probeId + "\" not in the annotations, ignoring" );
                unknownProbes.add( probeId );
                numUnknownProbes++;
                continue;
            }

            if ( p.getGeneSets().isEmpty() ) {
                unannotatedProbes.add( probeId );
                continue;
            }

            double score = 0.0;
            try {
                score = Double.parseDouble( fields[scoreColumnIndex] );
            } catch ( NumberFormatException e ) {
                /* the first line can be a header; we ignore it if it looks bad */
                if ( probeToScoreMap.size() > 0 ) {
                    invalidNumber = true;
                    badNumberString = fields[scoreColumnIndex];
                }
            }

            // Fudge when pvalues are zero.
            if ( logTransform && score <= 0.0 ) {
                invalidLog = true;
                score = SMALL;

            }

            if ( logTransform ) {
                score = -Arithmetic.log10( score );
            }

            /* we're done... */
            numProbesKept++;

            if ( probeToScoreMap.containsKey( p ) ) {
                if ( !warned ) {
                    messenger.showStatus( "Repeated identifier: " + probeId + ", keeping original value." );
                    warned = true;
                }
                numRepeatedProbes++;
            } else {
                probeToScoreMap.put( p, score );
            }

            if ( numProbesKept % 100 == 0 && Thread.currentThread().isInterrupted() ) {
                dis.close();
                throw new CancellationException();
            }

            if ( limit > 0 && numProbesKept == limit ) break;

        }
        dis.close();

        reportProblems( invalidLog, unknownProbes, unannotatedProbes, invalidNumber, badNumberString, numProbesKept,
                numRepeatedProbes );

        setUpGeneToScoreMap();

    }

    /**
     * @param invalidLog
     * @param unannotatedProbes
     * @param unknownProbe
     * @param invalidNumber
     * @param badNumberString
     * @param numProbesKept
     */
    private void reportProblems( boolean invalidLog, Collection<String> unknownProbes,
            Collection<String> unannotatedProbes, boolean invalidNumber, String badNumberString, int numProbesKept,
            int numRepeatedProbes ) {
        if ( invalidNumber && messenger != null ) {

            messenger.showError( "Non-numeric gene scores(s) " + " ('" + badNumberString + "') "
                    + " found for input file. These are set to an initial value of zero." );
        }
        if ( invalidLog && messenger != null ) {
            messenger
                    .showError( "Warning: There were attempts to take the log of non-positive values. These are set to "
                            + SMALL );
        }
        if ( messenger != null && unknownProbes.size() > 0 ) {
            messenger.showError( "Warning: " + unknownProbes.size()
                    + " probes in your gene score file don't match the ones in the annotation file." );

            int count = 0;
            StringBuffer buf = new StringBuffer();
            for ( Iterator<String> iter = unknownProbes.iterator(); iter.hasNext(); ) {
                if ( count >= 10 ) break;
                String probe = iter.next();
                buf.append( probe + "," );
                count++;
            }
            messenger.showError( "Unmatched probes are (up to 10 shown): " + buf );
        }

        if ( messenger != null && !unannotatedProbes.isEmpty() ) {
            messenger.showError( unannotatedProbes.size()
                    + " probes in your gene score file had no gene annotations and were ignored." );
        }

        if ( messenger != null && numRepeatedProbes > 0 ) {
            messenger
                    .showError( "Warning: "
                            + numRepeatedProbes
                            + " identifiers in your gene score file were repeats. Only the first occurrence encountered was kept in each case." );
        }

        if ( numProbesKept == 0 && messenger != null ) {
            messenger.showError( "None of the probes in the gene score file correspond to probes in the "
                    + "annotation file you selected. None of your data will be displayed." );
        }

        if ( probeToScoreMap.isEmpty() && messenger != null ) {
            messenger.showError( "No probe scores found! Please check the file has"
                    + " the correct plain text format and"
                    + " corresponds to the gene annotation (\".an\") file you selected." );
        } else if ( messenger != null ) {
            messenger.showStatus( "Found " + probeToScoreMap.size() + " scores in the file" );
        }
    }

    /**
     * Each pvalue is adjusted to the mean (or best) of all the values in the 'replicate group' to yield a "group to
     * pvalue map".
     * 
     * @param settings
     * @param collection - this should be generated from the annotation file.
     * @param messenger
     */
    private void setUpGeneToScoreMap() {

        Collection<Gene> genes = geneAnnots.getGenes();

        assert genes.size() > 0;
        double[] geneScoreTemp = new double[genes.size()];
        int counter = 0;

        for ( Gene geneSymbol : genes ) {

            if ( Thread.currentThread().isInterrupted() ) {
                return;
            }

            /*
             * probes in this group according to the array platform.
             */
            Collection<Probe> probes = geneSymbol.getProbes();

            // Analyze all probes in this 'group' (pointing to the same gene)
            int in_size = 0;
            for ( Probe probe : probes ) {

                if ( !probeToScoreMap.containsKey( probe ) ) {
                    continue;
                }

                // these values are already log transformed if the user selected that option.
                double score = probeToScoreMap.get( probe );

                switch ( gpMethod ) {
                    case MEAN: {
                        geneScoreTemp[counter] += score;
                        break;
                    }
                    case BEST: {
                        if ( in_size == 0 ) {
                            geneScoreTemp[counter] = score;
                        } else {

                            if ( rankLargeScoresBest() ) {
                                geneScoreTemp[counter] = Math.max( score, geneScoreTemp[counter] );
                            } else {
                                geneScoreTemp[counter] = Math.min( score, geneScoreTemp[counter] );
                            }
                        }
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException( "Illegal selection for groups score method." );
                    }
                }
                in_size++;
            }

            if ( in_size > 0 ) {
                if ( gpMethod.equals( SettingsHolder.MultiProbeHandling.MEAN ) ) {
                    geneScoreTemp[counter] /= in_size; // take the mean
                }
                Double dbb = new Double( geneScoreTemp[counter] );
                geneToScoreMap.put( geneSymbol, dbb );
                counter++;
            }
        } // end of while

        if ( counter == 0 ) {
            // this is okay, if we're trying to show the class despite there being no results.
            log.warn( "No valid gene to score mappings were found." );
            return;
        }

        if ( messenger != null ) messenger.showStatus( counter + " distinct genes found in the gene scores." );

    }

    /**
     * @see also Settings.upperTail(), which does the same thing.
     * @return true if the values returned by methods such as getGeneToScoreMap are returning values which should be
     *         treated as "big better". This will be true in the following (common) cases based on the settings the user
     *         made:
     *         <ul>
     *         <li>The scores were -log transformed, and small values are better (e.g., input probabilities)
     *         <li>The scores were not -log transformed, and big values were better in the original input.
     *         </ul>
     */
    public boolean rankLargeScoresBest() {
        // The first case is the common one, if input is pvalues.
        return ( logTransform && !biggerIsBetter ) || ( !logTransform && biggerIsBetter );
    }

} // end of class
