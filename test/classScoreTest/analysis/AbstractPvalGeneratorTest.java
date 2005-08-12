package classScoreTest.analysis;

import java.io.InputStream;

import junit.framework.TestCase;
import baseCode.bio.geneset.GONames;
import baseCode.bio.geneset.GeneAnnotations;
import classScore.Settings;
import classScore.analysis.GeneSetSizeComputer;
import classScore.analysis.OraPvalGenerator;
import classScore.data.GeneScores;

/**
 * <hr>
 * <p>
 * Copyright (c) 2004 Columbia University
 * 
 * @author pavlidis
 * @version $Id$
 */
abstract class AbstractPvalGeneratorTest extends TestCase {
    protected OraPvalGenerator test = null;
    protected GeneAnnotations g = null;
    protected GeneScores gsr = null;
    protected InputStream is = null;
    protected InputStream ism = null;
    protected InputStream isi = null;
    protected Settings s = null;
    protected GONames gon = null;
    protected GeneSetSizeComputer csc = null;

    protected void setUp() throws Exception {
        ism = AbstractPvalGeneratorTest.class.getResourceAsStream( "/data/test.an.txt" );
        is = AbstractPvalGeneratorTest.class.getResourceAsStream( "/data/test.scores.txt" );

        isi = AbstractPvalGeneratorTest.class.getResourceAsStream( "/data/go_test_termdb.xml" );

        s = new Settings();
        s.setPValThreshold( 0.015 );
        s.setMinClassSize( 2 );
        s.setDoLog( true );

        g = new GeneAnnotations( ism, null, null, null );
       

        gsr = new GeneScores( is, s, null, g );
        gon = new GONames( isi );
        csc = new GeneSetSizeComputer( gsr.getProbeToScoreMap().keySet(), g, gsr, true );

        super.setUp();
    }

    protected void tearDown() throws Exception {
        isi.close();
        ism.close();
        is.close();
        super.tearDown();
    }

}