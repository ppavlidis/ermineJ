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
package ubic.erminej.gui.analysis;

import java.awt.event.ActionEvent;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.erminej.Settings;
import ubic.erminej.SettingsHolder;
import ubic.erminej.SettingsHolder.GeneScoreMethod;
import ubic.erminej.SettingsHolder.Method;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.gui.MainFrame;
import ubic.erminej.gui.util.Wizard;

/**
 * <p>
 * AnalysisWizard class.
 * </p>
 *
 * @author Homin Lee
 * @version $Id$
 */
public class AnalysisWizard extends Wizard {

    private static final long serialVersionUID = 1L;

    private static final int WIZARD_PREFERRED_HEIGHT = 380;

    private static final int WIZARD_PREFERRED_WIDTH = 570;

    private static Log log = LogFactory.getLog( AnalysisWizard.class.getName() );

    // logic
    int step = 1;
    Method analysisType = Method.ORA;

    // really only needed for precision-recall method.
    GeneScoreMethod geneScoreMethod = GeneScoreMethod.MEAN;

    Settings settings;

    GeneAnnotations geneAnnots;

    AnalysisWizardStep1 step1;

    AnalysisWizardStep2 step2;

    AnalysisWizardStep3 step3;
    AnalysisWizardStep4 step4;
    AnalysisWizardStep5 step5;
    int maxSteps = 5;

    /**
     * <p>
     * Constructor for AnalysisWizard.
     * </p>
     *
     * @param callingframe a {@link ubic.erminej.gui.MainFrame} object.
     * @param geneAnnots a {@link ubic.erminej.data.GeneAnnotations} object.
     */
    public AnalysisWizard( MainFrame callingframe, GeneAnnotations geneAnnots ) {
        super( callingframe, WIZARD_PREFERRED_WIDTH, WIZARD_PREFERRED_HEIGHT );
        this.callingframe = callingframe;
        this.settings = callingframe.getSettings();

        this.geneAnnots = geneAnnots;

        step1 = new AnalysisWizardStep1( this, settings );
        this.addStep( step1, true );
        step2 = new AnalysisWizardStep2( this, settings );
        this.addStep( step2 );
        step3 = new AnalysisWizardStep3( this, settings );
        this.addStep( step3 );
        step4 = new AnalysisWizardStep4( this, settings );
        this.addStep( step4 );
        step5 = new AnalysisWizardStep5( this, settings );
        this.addStep( step5 );
        this.setTitle( "Create New Analysis - Step 1 of " + maxSteps );

        // determine if the "finish" button should be disabled or not
        if ( StringUtils.isBlank( settings.getRawDataFileName() ) && StringUtils.isBlank( settings.getScoreFile() ) ) {
            setFinishDisabled();
        } else {
            setFinishEnabled();
        }
    }

    /**
     * <p>
     * Getter for the field <code>analysisType</code>.
     * </p>
     *
     * @return a ubic.erminej.Settings$Method object.
     */
    public Settings.Method getAnalysisType() {
        return this.analysisType;
    }

    /**
     * <p>
     * Getter for the field <code>geneAnnots</code>.
     * </p>
     *
     * @return a {@link ubic.erminej.data.GeneAnnotations} object.
     */
    public GeneAnnotations getGeneAnnots() {
        return geneAnnots;
    }

    /**
     * <p>
     * Getter for the field <code>geneScoreMethod</code>.
     * </p>
     *
     * @return a {@link ubic.erminej.SettingsHolder.GeneScoreMethod} object.
     */
    public GeneScoreMethod getGeneScoreMethod() {
        return geneScoreMethod;
    }

    /**
     * <p>
     * Getter for the field <code>settings</code>.
     * </p>
     *
     * @return a {@link ubic.erminej.SettingsHolder} object.
     */
    public SettingsHolder getSettings() {
        return settings;
    }

    /**
     * <p>
     * Setter for the field <code>analysisType</code>.
     * </p>
     *
     * @param val a {@link ubic.erminej.SettingsHolder.Method} object.
     */
    public void setAnalysisType( Method val ) {
        this.analysisType = val;
        this.checkNumSteps();
    }

    /**
     * <p>
     * Setter for the field <code>geneScoreMethod</code>.
     * </p>
     *
     * @param geneScoreMethod a {@link ubic.erminej.SettingsHolder.GeneScoreMethod} object.
     */
    public void setGeneScoreMethod( GeneScoreMethod geneScoreMethod ) {
        this.geneScoreMethod = geneScoreMethod;
    }

    void saveValues() {
        step1.saveValues();
        step2.saveValues();
        step3.saveValues();
        step4.saveValues();
        step5.saveValues();
    }

    /** {@inheritDoc} */
    @Override
    protected void backButton_actionPerformed( ActionEvent e ) {
        clearStatus();
        if ( step == 2 ) {
            this.clearStatus();
            step = 1;
            this.analysisType = settings.getClassScoreMethod();
            this.getContentPane().remove( step2 );
            checkNumSteps();
            this.setTitle( "Create New Analysis - Step 1 of " + maxSteps );
            this.getContentPane().add( step1 );
            step1.revalidate();
            backButton.setEnabled( false );
            nextButton.setEnabled( true );
            step2.saveValues();
            this.repaint();
        } else if ( step == 3 ) {
            this.clearStatus();
            step = 2;
            this.getContentPane().remove( step3 );
            this.setTitle( "Create New Analysis - Step 2 of " + maxSteps );
            this.getContentPane().add( step2 );
            nextButton.setEnabled( true );
            step2.revalidate();
            step3.saveValues();
            this.repaint();
        } else if ( step == 4 ) {
            this.clearStatus();
            step = 3;
            this.getContentPane().remove( step4 );
            this.setTitle( "Create New Analysis - Step 4 of " + maxSteps );
            this.getContentPane().add( step3 );
            step4.saveValues();
            nextButton.setEnabled( true );
            step3.updateNumGeneSetsActive();
            step3.revalidate();
            this.repaint();
        } else if ( step == 5 ) {
            this.clearStatus();
            step = 4;
            step5.removeVarPanel( analysisType );
            this.getContentPane().remove( step5 );
            this.setTitle( "Create New Analysis - Step 5 of " + maxSteps );
            this.getContentPane().add( step4 );
            step5.saveValues();
            checkIfReady();
            nextButton.setEnabled( true );
            step4.updateNumGeneSetsActive();
            step4.revalidate();
            this.repaint();
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void cancelButton_actionPerformed( ActionEvent e ) {
        dispose();
    }

    /** {@inheritDoc} */
    @Override
    protected void finishEditing( ActionEvent e ) {
        if ( step2.isReady() && step3.isReady() && step4.isReady() ) {

            saveValues();
            log.info( "Starting analysis" );

            new Thread() {
                @Override
                public void run() {
                    SettingsHolder copyOfSettings = new Settings( settings );
                    ( ( MainFrame ) callingframe ).startAnalysis( copyOfSettings );
                }
            }.start();
            this.dispose();
        }
        this.dispose();
    }

    /** {@inheritDoc} */
    @Override
    protected void nextButton_actionPerformed( ActionEvent e ) {
        clearStatus();
        if ( step == 1 && step1.isReady() ) {
            this.clearStatus();
            step = 2;
            step1.saveValues();
            this.analysisType = settings.getClassScoreMethod();
            this.geneScoreMethod = settings.getGeneSetResamplingScoreMethod();
            checkNumSteps();
            checkIfReady();
            this.getContentPane().remove( step1 );
            this.setTitle( "Create New Analysis - Step 2 of " + maxSteps );
            this.getContentPane().add( step2 );
            step2.updateView();
            step2.revalidate();
            backButton.setEnabled( true );
            setFinishEnabled();
            this.repaint();
            nextButton.requestFocusInWindow();
            this.nextButton.setEnabled( true );
        } else if ( step == 2 && step2.isReady() ) {
            this.clearStatus();
            step = 3;
            step2.saveValues();
            this.getContentPane().remove( step2 );
            this.setTitle( "Create New Analysis - Step 3 of " + maxSteps );
            this.getContentPane().add( step3 );
            checkIfReady();
            this.nextButton.setEnabled( true );
            step3.updateNumGeneSetsActive();
            step3.revalidate();
            this.repaint();
        } else if ( step == 3 && step3.isReady() ) {
            this.clearStatus();
            step = 4;
            step3.saveValues();
            this.getContentPane().remove( step3 );
            this.setTitle( "Create New Analysis - Step 4 of " + maxSteps );
            this.getContentPane().add( step4 );
            checkIfReady();
            step4.updateNumGeneSetsActive();
            step4.revalidate();
            this.repaint();
        } else if ( step == 4 && step4.isReady() ) {
            this.clearStatus();
            step = 5;
            step4.saveValues();
            this.getContentPane().remove( step4 );
            step5.addVarPanel( analysisType, geneScoreMethod );
            checkIfReady();
            this.setTitle( "Create New Analysis - Step 5 of " + maxSteps );
            this.getContentPane().add( step5 );
            step5.update();
            step5.revalidate();
            nextButton.setEnabled( false );
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

    private void checkNumSteps() {
        if ( step == 1 ) {
            this.setTitle( "Create New Analysis - Step 1 of " + maxSteps );
            step1.revalidate();
            this.repaint();
        }
    }

}
