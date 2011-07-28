/*
 * The ermineJ project
 * 
 * Copyright (c) 2011 University of British Columbia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ubic.erminej;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.configuration.PropertiesConfiguration;

import ubic.erminej.data.GeneAnnotationParser.Format;

/**
 * Holds settings for retrieval but doesn't allow changing or saving them. This is basically a value object to use
 * during analyses.
 * 
 * @author paul
 * @version $Id$
 */
public class SettingsHolder {

    /**
     * For the gene set resampling method, how are scores computed for the group?
     */
    public enum GeneScoreMethod {
        MEAN, MEAN_ABOVE_QUANTILE, QUANTILE
    }

    /**
     * Which gene set scoring method to use.
     */
    public enum Method {
        ORA, ROC, GSR, CORR
    }

    /**
     * What to do when there are multiple values for a gene.
     */
    public enum MultiProbeHandling {
        BEST, MEAN
    }

    /**
     * How to correct for multiple tests.
     */
    public enum MultiTestCorrMethod {
        BONFERONNI, WESTFALLYOUNG, BENJAMINIHOCHBERG
    }

    // not this is also listed in erminejdefault.properties.
    private static final String DEFAULT_GENE_URL_BASE = "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?db=gene&cmd=search&term=@@";

    /*
     * Strings used in the config file
     */
    protected static final String ALWAYS_USE_EMPIRICAL = "alwaysUseEmpirical";
    protected static final String ANNOT_FILE = "annotFile";
    protected static final String ANNOT_FORMAT = "annotFormat";
    protected static final String BIG_IS_BETTER = "bigIsBetter";
    protected static final String CLASS_FILE = "classFile";
    protected static final String CLASS_SCORE_METHOD = "classScoreMethod";
    protected static final String DATA_DIRECTORY = "dataDirectory";
    protected static final String DO_LOG = "doLog";
    protected static final String GENE_REP_TREATMENT = "geneRepTreatment";
    protected static final String GOLD_STANDARD_FILE = "goldStandardFile";
    protected static final String IS_TESTER = "isTester";
    protected static final String ITERATIONS = "iterations";
    protected static final String MAX_CLASS_SIZE = "maxClassSize";
    protected static final String MIN_CLASS_SIZE = "minClassSize";
    protected static final String MTC_CONFIG_NAME = "mtc";
    protected static final String USE_MULTIFUNCTIONALITY_CORRECTION = "multifuncCorr";
    protected static final String OUTPUT_FILE = "outputFile";
    protected static final String GENE_SCORE_THRESHOLD = "pValThreshold"; // TODO deprecate this string, use
    // 'scoreThreshold' instead.
    protected static final String PREFERENCES_FILE_NAME = "preferencesFileName";
    protected static final String QUANTILE_CONFIG_NAME = "quantile";
    protected static final String RAW_FILE_CONFIG_NAME = "rawFile";
    protected static final String GENE_SET_RESAMPLING_SCORE_METHOD = "rawScoreMethod";
    protected static final String SCORE_COL = "scoreCol";
    protected static final String SCORE_FILE = "scoreFile";
    protected static final String SELECTED_CUSTOM_GENESETS = "selectedCustomGeneSets";
    protected static final String FILTER_NONSPECIFIC = "filterNonSpecific";
    protected static final String USE_BIOL_PROC = "useGOBiologicalProcess";
    protected static final String USE_MOL_FUNC = "useGOMolecularFunction";
    protected static final String USE_CELL_COMP = "useGOCellularComponent";
    protected static final String USE_USER_DEFINED_GROUPS = "useUserDefinedGroups";
    protected static final String CUSTOM_GENE_SET_DIRECTORY_PROPERTY = "classFolder";

    public static final String GENE_URL_BASE = "gene.url.base";
    protected static final String DEFAULT_CUSTOM_GENE_SET_DIR_NAME = "genesets";
    protected static final String DEFAULT_USER_DATA_DIR_NAME = "ermineJ.data";

    protected PropertiesConfiguration config = null;

    /**
     * @param oldConfig
     */
    @SuppressWarnings("unchecked")
    public SettingsHolder( PropertiesConfiguration oldConfig ) {
        this.config = new PropertiesConfiguration();
        for ( Iterator<String> iter = oldConfig.getKeys(); iter.hasNext(); ) {
            String key = iter.next();
            Object value = oldConfig.getProperty( key );
            this.config.setProperty( key, value );
        }
    }

    protected SettingsHolder() {

    }

    /**
     * @return
     */
    public boolean getAlwaysUseEmpirical() {
        return config.getBoolean( ALWAYS_USE_EMPIRICAL, false );

    }

    public String getAnnotFile() {
        return config.getString( ANNOT_FILE );
    }

    /**
     * @return
     */
    public Format getAnnotFormat() {
        String storedValue = config.getString( ANNOT_FORMAT, Format.DEFAULT.toString() );

        return Format.valueOf( storedValue );
    }

    /**
     * @return
     */
    public boolean getBigIsBetter() {
        return config.getBoolean( BIG_IS_BETTER, false );
    }

    /**
     * Returns setting values.
     */
    public String getClassFile() {
        assert config != null;
        return config.getString( CLASS_FILE );
    }

    /**
     * @return
     */
    public Settings.Method getClassScoreMethod() {
        String storedValue = config.getString( CLASS_SCORE_METHOD, Settings.Method.ORA.toString() );

        return Settings.Method.valueOf( storedValue );
    }

    public String getCustomGeneSetDirectory() {
        return config.getString( CUSTOM_GENE_SET_DIRECTORY_PROPERTY, getDefaultUserClassesDirPath() );
    }

    public String getDataDirectory() {
        return config.getString( DATA_DIRECTORY, getDefaultUserDataDirPath() );

    }

    public String getDefaultGeneUrl() {
        return DEFAULT_GENE_URL_BASE;
    }

    /**
     * @return
     */
    public boolean getDoLog() {
        return config.getBoolean( DO_LOG, true );
    }

    /**
     * @return
     */
    public boolean getFilterNonSpecific() {
        return config.getBoolean( FILTER_NONSPECIFIC );
    }

    /**
     * @return
     */
    public MultiProbeHandling getGeneRepTreatment() {
        String storedValue = config.getString( GENE_REP_TREATMENT, MultiProbeHandling.BEST.toString() );

        return Settings.MultiProbeHandling.valueOf( storedValue );
    }

    /**
     * Value used for ORA
     * 
     * @return
     */
    public double getGeneScoreThreshold() {
        return config.getDouble( GENE_SCORE_THRESHOLD, 0.001 );
    }

    /**
     * @return the method to be used to combine scores. This is only relevant for the gene set resampling method.
     */
    public GeneScoreMethod getGeneSetResamplingScoreMethod() {
        String storedValue = config.getString( GENE_SET_RESAMPLING_SCORE_METHOD, GeneScoreMethod.MEAN.toString() );

        return Settings.GeneScoreMethod.valueOf( storedValue );
    }

    public String getGeneUrlBase() {
        return config.getString( SettingsHolder.GENE_URL_BASE, DEFAULT_GENE_URL_BASE );
    }

    /**
     * Mostly used for testing
     * 
     * @return
     */
    public String getGoldStandardFile() {
        return config.getString( GOLD_STANDARD_FILE );
    }

    /**
     * @return
     */
    public int getIterations() {
        return config.getInteger( ITERATIONS, 10000 );
    }

    public int getMaxClassSize() {
        return config.getInteger( MAX_CLASS_SIZE, 100 );
    }

    public int getMinClassSize() {
        return config.getInteger( MIN_CLASS_SIZE, 5 );
    }

    /**
     * @return Returns the mtc.
     */
    public MultiTestCorrMethod getMtc() {
        return MultiTestCorrMethod.valueOf( config.getString( MTC_CONFIG_NAME, MultiTestCorrMethod.BENJAMINIHOCHBERG
                .toString() ) );
    }

    public int getQuantile() {
        return config.getInteger( QUANTILE_CONFIG_NAME, 50 );
    }

    /**
     * @return
     */
    public String getRawDataFileDirectory() {
        String rdf = this.getRawDataFileName();
        if ( rdf == null ) return getDataDirectory();

        File rdfFile = new File( rdf );
        return rdfFile.getParent() == null ? getDataDirectory() : rdfFile.getParent();
    }

    public String getRawDataFileName() {
        return config.getString( RAW_FILE_CONFIG_NAME );
    }

    public int getScoreCol() {
        return config.getInteger( SCORE_COL, 2 );
    }

    public String getScoreFile() {
        return config.getString( SCORE_FILE );
    }

    @SuppressWarnings("unchecked")
    public Collection<String> getSelectedCustomGeneSets() {
        return config.getList( SELECTED_CUSTOM_GENESETS, new ArrayList<String>() );
    }

    /**
     * @return Returns the useBiologicalProcess.
     */
    public boolean getUseBiologicalProcess() {
        return config.getBoolean( USE_BIOL_PROC, true );

    }

    /**
     * @return Returns the useCellularComponent.
     */
    public boolean getUseCellularComponent() {
        return config.getBoolean( USE_CELL_COMP, true );

    }

    /**
     * @return
     */
    public boolean getUseLog() {
        return config.getBoolean( DO_LOG, true );
    }

    /**
     * @return Returns the useMolecularFunction.
     */
    public boolean getUseMolecularFunction() {
        return config.getBoolean( USE_MOL_FUNC, true );

    }

    /**
     * Get the path to the directory where custom gene sets are stored (default should be like
     * ${HOME}/ermineJ.data/genesets). This path is not guaranteed to exist.
     * 
     * @return
     */
    public String getUserGeneSetDirectory() {
        String dir = config.getString( CUSTOM_GENE_SET_DIRECTORY_PROPERTY );
        return dir;
    }

    public boolean getUseUserDefined() {
        return config.getBoolean( USE_USER_DEFINED_GROUPS, true );
    }

    /**
     * @return true if multiple values for a gene should be combined, or whether each probe should be treated
     *         independently regardless
     * @see getGeneRepTreatment for setting of how the combination occurs.
     */
    public boolean getUseWeights() {
        if ( this.getGeneRepTreatment().equals( MultiProbeHandling.MEAN )
                || this.getGeneRepTreatment().equals( MultiProbeHandling.BEST ) ) return true;
        return false;
    }

    public boolean isTester() {
        return config.getBoolean( IS_TESTER, false );
    }

    /**
     * @return true if multifunctionality corrections should be applied, if possible.
     */
    public boolean isUseMultifunctionalityCorrection() {
        return config.getBoolean( USE_MULTIFUNCTIONALITY_CORRECTION, false );
    }

    public void setUseUserDefined( boolean b ) {
        this.config.setProperty( USE_USER_DEFINED_GROUPS, b );
    }

    /**
     * Determine whether we should be using the upper tails of our histograms. The "big is better" setting reflects the
     * original gene scores, not the log-transformed scores. Therefore if we are taking the log, and the user indicates
     * smaller values are better, then we do want to use the upper tail. If we're not taking the log, then we just
     * directly interpret what the user selected for 'bigIsBetter'.
     * 
     * @return true if we are using the "upper tail" of our distributions.
     */
    public boolean upperTail() {
        return this.getDoLog() && !this.getBigIsBetter() || !this.getDoLog() && this.getBigIsBetter();
    }

    protected PropertiesConfiguration getConfig() {
        return config;
    }

    protected String getDefaultUserClassesDirPath() {
        return getDefaultUserDataDirPath() + System.getProperty( "file.separator" ) + DEFAULT_CUSTOM_GENE_SET_DIR_NAME;
    }

    /**
     * @return
     */
    protected String getDefaultUserDataDirPath() {
        String dataDirName = System.getProperty( "user.home" ) + System.getProperty( "file.separator" )
                + DEFAULT_USER_DATA_DIR_NAME;
        return dataDirName;
    }

    protected Object getProperty( String propertyName ) {
        return this.config.getProperty( propertyName );
    }

}
