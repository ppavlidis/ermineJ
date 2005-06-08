package classScore.gui;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import baseCode.bio.geneset.GONames;
import baseCode.bio.geneset.GeneAnnotations;
import baseCode.gui.GuiUtil;
import baseCode.gui.Wizard;
import classScore.Settings;
import classScore.data.UserDefinedGeneSetManager;

/**
 * <hr>
 * <p>
 * Copyright (c) 2004 Columbia University
 * 
 * @author Homin Lee
 * @version $Id$
 */

public class AnalysisWizard extends Wizard {

    private static Log log = LogFactory.getLog( AnalysisWizard.class.getName() );

    // logic
    int step = 1;
    int analysisType = Settings.ORA;

    Settings settings;
    GeneAnnotations geneData;
    GONames goData;
    AnalysisWizardStep1 step1;
    AnalysisWizardStep2 step2;
    AnalysisWizardStep3 step3;
    AnalysisWizardStep3_1 step31;
    AnalysisWizardStep4 step4;
    AnalysisWizardStep5 step5;
    int maxSteps = 6;

    public AnalysisWizard( GeneSetScoreFrame callingframe, Map geneDataSets, GONames goData ) {
        super( callingframe, 550, 350 );
        this.callingframe = callingframe;
        this.settings = callingframe.getSettings();
        this.geneData = ( GeneAnnotations ) geneDataSets.get( new Integer( "original".hashCode() ) );
        this.goData = goData;

        step1 = new AnalysisWizardStep1( this, settings );
        this.addStep( step1, true );
        step2 = new AnalysisWizardStep2( this, settings );
        this.addStep( step2 );
        step3 = new AnalysisWizardStep3( this, goData, geneData, settings );
        this.addStep( step3 );
        step31 = new AnalysisWizardStep3_1( this, settings );
        this.addStep( step31 );
        step4 = new AnalysisWizardStep4( this, settings );
        this.addStep( step4 );
        step5 = new AnalysisWizardStep5( this, settings );
        this.addStep( step5 );
        this.setTitle( "Create New Analysis - Step 1 of " + maxSteps );

        // determine if the "finish" button should be disabled or not
        if ( ( settings.getRawDataFileName() == null || ( settings.getRawDataFileName() == null || settings
                .getRawDataFileName().length() == 0 )
                && settings.getScoreFile().length() == 0 ) ) {
            setFinishDisabled();
        } else {
            setFinishEnabled();
        }
    }

    protected void nextButton_actionPerformed( ActionEvent e ) {
        clearStatus();
        if ( step == 1 && step1.isReady() ) {
            step = 2;
            step1.saveValues();
            this.analysisType = settings.getClassScoreMethod();
            checkNumSteps();
            checkIfReady();
            this.getContentPane().remove( step1 );
            this.setTitle( "Create New Analysis - Step 2 of " + maxSteps );
            this.getContentPane().add( step2 );
            step2.revalidate();
            backButton.setEnabled( true );
            setFinishEnabled();
            this.repaint();
            nextButton.grabFocus();
            this.nextButton.setEnabled( true );
        } else if ( step == 2 && step2.isReady() ) {
            step = 3;
            this.getContentPane().remove( step2 );
            this.setTitle( "Create New Analysis - Step 3 of " + maxSteps );
            this.getContentPane().add( step3 );
            checkIfReady();
            this.nextButton.setEnabled( true );
            step3.revalidate();
            this.repaint();
        } else if ( step == 3 ) {
            step = 31;
            this.getContentPane().remove( step3 );
            this.setTitle( "Create New Analysis - Step 4 of " + maxSteps );
            this.getContentPane().add( step31 );
            checkIfReady();
            this.nextButton.setEnabled( true );
            step31.revalidate();
            this.repaint();
        } else if ( step == 31 ) {
            step = 4;
            this.getContentPane().remove( step31 );
            this.setTitle( "Create New Analysis - Step 5 of " + maxSteps );
            this.getContentPane().add( step4 );
            checkIfReady();
            step4.revalidate();
            this.repaint();
        } else if ( step == 4 ) {
            step = 5;
            this.getContentPane().remove( step4 );
            step5.addVarPanel( analysisType );
            checkIfReady();
            this.setTitle( "Create New Analysis - Step 6 of " + maxSteps );
            this.getContentPane().add( step5 );
            step5.revalidate();
            nextButton.setEnabled( false );
            this.repaint();
        }
    }

    protected void backButton_actionPerformed( ActionEvent e ) {
        clearStatus();
        if ( step == 2 ) {
            step = 1;
            this.analysisType = settings.getClassScoreMethod();
            this.getContentPane().remove( step2 );
            checkNumSteps();
            this.setTitle( "Create New Analysis - Step 1 of " + maxSteps );
            this.getContentPane().add( step1 );
            step1.revalidate();
            checkIfReady();
            backButton.setEnabled( false );
            nextButton.setEnabled( true );
            this.repaint();
        } else if ( step == 3 ) {
            step = 2;
            this.getContentPane().remove( step3 );
            this.setTitle( "Create New Analysis - Step 2 of " + maxSteps );
            this.getContentPane().add( step2 );
            checkIfReady();
            nextButton.setEnabled( true );
            step2.revalidate();
            this.repaint();
        } else if ( step == 31 ) {
            step = 3;
            this.getContentPane().remove( step31 );
            this.setTitle( "Create New Analysis - Step 3 of " + maxSteps );
            this.getContentPane().add( step3 );
            checkIfReady();
            nextButton.setEnabled( true );
            step2.revalidate();
            this.repaint();
        } else if ( step == 4 ) {
            step = 31;
            this.getContentPane().remove( step4 );
            this.setTitle( "Create New Analysis - Step 4 of " + maxSteps );
            this.getContentPane().add( step31 );
            checkIfReady();
            nextButton.setEnabled( true );
            step31.revalidate();
            this.repaint();
        } else if ( step == 5 ) {
            step = 4;
            step5.removeVarPanel( analysisType );
            this.getContentPane().remove( step5 );
            this.setTitle( "Create New Analysis - Step 5 of " + maxSteps );
            this.getContentPane().add( step4 );
            checkIfReady();
            nextButton.setEnabled( true );
            step4.revalidate();
            this.repaint();
        }
    }

    /**
     * 
     */
    private void checkNumSteps() {
        maxSteps = 6;

        if ( step == 1 ) {
            this.setTitle( "Create New Analysis - Step 1 of " + maxSteps );
            step1.revalidate();
            this.repaint();
        }
    }

    /**
     * 
     */
    private void checkIfReady() {
        if ( step2.isReady() ) {
            finishButton.setEnabled( true );
        } else {
            finishButton.setEnabled( false );
        }
    }

    protected void cancelButton_actionPerformed( ActionEvent e ) {
        dispose();
    }

    protected void finishButton_actionPerformed( ActionEvent e ) {
        if ( step2.isReady() ) {
            try {
                loadAddedClasses();
            } catch ( IOException e1 ) {
                GuiUtil.error( "Could not load the custom classes: " + e + "\n"
                        + "If this problem persists, please contact the software vendor. " );
            }
            saveValues();
            log.info( "Starting analysis" );

            new Thread() {
                public void run() {
                    Settings copyOfSettings = new Settings( settings );
                    ( ( GeneSetScoreFrame ) callingframe ).startAnalysis( copyOfSettings );
                }
            }.start();
            this.dispose();
        }
        this.dispose();
    }

    void saveValues() {
        step1.saveValues();
        step2.saveValues();
        step3.saveValues();
        step31.saveValues();
        step4.saveValues();
        step5.saveValues();
    }

    void loadAddedClasses() throws IOException {
        Iterator it = step3.getAddedClasses().iterator();
        while ( it.hasNext() ) {
            String id = ( String ) ( ( HashMap ) it.next() ).get( "id" );
            if ( !goData.isUserDefined( id ) ) {
                UserDefinedGeneSetManager newGeneSet = new UserDefinedGeneSetManager( geneData, settings, id );
                String filename = newGeneSet.getUserGeneSetFileForName();
                boolean gotSomeProbes = newGeneSet.loadUserGeneSet( filename );
                if ( gotSomeProbes ) newGeneSet.addClass( goData );
            }
        }
    }

    /**
     * @param val
     */
    public void setAnalysisType( int val ) {
        this.analysisType = val;
        this.checkNumSteps();
    }

    /**
     * @return
     */
    public int getAnalysisType() {
        return this.analysisType;
    }

}