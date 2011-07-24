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
package ubic.erminej.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.apache.commons.lang.StringUtils;

import ubic.basecode.util.FileTools;
import ubic.erminej.Settings;

/**
 * Set the inputs for the analysis - score file, raw data file.
 * 
 * @author Kiran Keshav
 * @author Homin Lee
 * @author Paul Pavlidis
 * @version $Id$
 */
public class AnalysisWizardStep2 extends WizardStep {

    private static final long serialVersionUID = -1L;
    private AnalysisWizard wiz;
    private Settings settings;
    private JFileChooser chooser = new JFileChooser();
    private JTextField rawFile;
    private JTextField scoreFile;

    private JTextField jTextFieldScoreCol;

    public AnalysisWizardStep2( AnalysisWizard wiz, Settings settings ) {
        super( wiz );
        this.jbInit();
        this.wiz = wiz;
        this.settings = settings;
        chooser.setCurrentDirectory( new File( settings.getDataDirectory() ) );
        setValues();
        wiz.clearStatus();
    }

    @Override
    protected void jbInit() {
        // initialization
        JPanel step2Panel = new JPanel();
        step2Panel.setLayout( new GridLayout( 5, 1 ) );
        // panel 1
        JPanel jPanel1 = new JPanel();
        JLabel jLabel1 = new JLabel();
        jLabel1.setText( "Gene score file (optional for correlation score):" );
        jPanel1.add( jLabel1, null );

        // panel 2
        JPanel scoreFileBrowsePanel = new JPanel();
        JButton jButton1 = new JButton();
        jButton1.setEnabled( true );
        jButton1.setText( "Browse" );
        jButton1.addActionListener( new AnalysisWizardStep2_scoreBrowseButton_actionAdapter( this ) );
        scoreFile = new JTextField();
        scoreFile.setPreferredSize( new Dimension( 325, 19 ) );
        scoreFileBrowsePanel.add( scoreFile, null );
        scoreFileBrowsePanel.add( jButton1, null );

        // panel 3
        JPanel scoreColumnPanel = new JPanel();
        JLabel jLabel3 = new JLabel();
        jLabel3.setText( "Column:" );
        jTextFieldScoreCol = new JTextField();
        jTextFieldScoreCol.setPreferredSize( new Dimension( 30, 19 ) );
        jTextFieldScoreCol.setHorizontalAlignment( SwingConstants.RIGHT ); // moves textbox text to the right
        jTextFieldScoreCol.setText( "2" );
        jTextFieldScoreCol
                .setToolTipText( "Column of the gene score file containing the scores. This must be a value of 2 or higher." );
        jTextFieldScoreCol.setEditable( true );
        scoreColumnPanel.add( jLabel3, null );
        scoreColumnPanel.add( jTextFieldScoreCol, null );

        JButton previewButton = new JButton( "Preview" );
        previewButton.setToolTipText( "Preview the scores to be imported" );
        previewButton.addActionListener( new PreviewButtonAdapter( this ) );

        scoreColumnPanel.add( previewButton, null );

        // panel 4
        JPanel rawDataPanel = new JPanel();
        JLabel jLabel4 = new JLabel();
        jLabel4.setText( "Raw data file (optional for ORA or resampling):" );
        rawDataPanel.add( jLabel4, null );

        // panel 5
        JPanel rawDataBrowsePanel = new JPanel();
        JButton jButton4 = new JButton();
        jButton4.setEnabled( true );
        jButton4.setText( "Browse" );
        jButton4.addActionListener( new AnalysisWizardStep2_rawBrowseButton_actionAdapter( this ) );
        rawFile = new JTextField();
        rawFile.setPreferredSize( new Dimension( 325, 19 ) );
        rawDataBrowsePanel.add( rawFile, null );
        rawDataBrowsePanel.add( jButton4, null );

        // Now create border layouts for each of the rows to get them aligned.
        JPanel jPanelA = new JPanel();
        jPanelA.setLayout( new BorderLayout() );
        jPanelA.add( jPanel1, BorderLayout.WEST );

        JPanel jPanelB = new JPanel();
        jPanelB.setLayout( new BorderLayout() );
        jPanelB.add( scoreFileBrowsePanel, BorderLayout.WEST );
        jPanelB.add( scoreColumnPanel, BorderLayout.EAST );

        JPanel jPanelC = new JPanel();
        jPanelC.setLayout( new BorderLayout() );
        jPanelC.add( rawDataPanel, BorderLayout.WEST );

        JPanel jPanelD = new JPanel();
        jPanelD.setLayout( new BorderLayout() );
        jPanelD.add( rawDataBrowsePanel, BorderLayout.WEST );

        // MAIN PANEL = GridLayout(BorderLayouts); GridLayout is (1,6)
        step2Panel.add( jPanelA, null );
        step2Panel.add( jPanelB, null );
        step2Panel.add( jPanelC, null );
        step2Panel.add( jPanelD, null );

        this.addHelp( "<html><b>Choose the data files to use</b><br>"
                + "&quot;Gene scores&quot; refer to a score or p value "
                + " associated with each gene in your data set. This "
                + "file can have as few as two columns. &quot;Raw data&quot;"
                + " refers to the expression profile data, usually a large matrix. "
                + "Files must be tab-delimited text. For details, see the user manual." );
        this.addMain( step2Panel );

    }

    @Override
    public boolean isReady() {

        if ( wiz.getAnalysisType().equals( Settings.Method.CORR ) && rawFile.getText().compareTo( "" ) == 0 ) {
            wiz.showError( "Correlation analyses require a raw data file." );
            return false;
        } else if ( ( wiz.getAnalysisType().equals( Settings.Method.GSR ) || wiz.getAnalysisType().equals(
                Settings.Method.ORA ) )
                && scoreFile.getText().compareTo( "" ) == 0 ) {
            wiz.showError( "ORA and resampling analyses require a gene score file." );
            return false;
        }

        if ( ( !wiz.getAnalysisType().equals( Settings.Method.CORR ) )
                && Integer.valueOf( jTextFieldScoreCol.getText() ).intValue() < 2 ) {
            wiz.showError( "The score column must be 2 or higher" );
            return false;
        }

        // make sure we got at least one file that's readable.
        if ( rawFile.getText().length() != 0 && !FileTools.testFile( rawFile.getText() ) ) {
            wiz.showError( "The raw data file is not valid." );
            return false;
        }

        if ( scoreFile.getText().length() != 0 && !FileTools.testFile( scoreFile.getText() ) ) {
            wiz.showError( "The gene score file is not valid." );
            return false;
        }

        wiz.clearStatus();
        return true;
    }

    void rawBrowseButton_actionPerformed() {
        chooser.setCurrentDirectory( new File( settings.getRawDataFileDirectory() ) );
        chooser.setDialogTitle( "Choose Raw Data File" );
        wiz.clearStatus();
        int result = chooser.showOpenDialog( this );
        if ( result == JFileChooser.APPROVE_OPTION ) {
            rawFile.setText( chooser.getSelectedFile().toString() );
            settings.setDataDirectory( chooser.getCurrentDirectory().toString() );
        }
    }

    void scoreBrowseButton_actionPerformed() {
        chooser.setCurrentDirectory( new File( settings.getGeneScoreFileDirectory() ) );
        chooser.setDialogTitle( "Choose Gene Score File" );
        wiz.clearStatus();
        int result = chooser.showOpenDialog( this );
        if ( result == JFileChooser.APPROVE_OPTION ) {
            scoreFile.setText( chooser.getSelectedFile().toString() );
            settings.setDataDirectory( chooser.getCurrentDirectory().toString() );
        }
    }

    void setValues() {
        jTextFieldScoreCol.setText( String.valueOf( settings.getScoreCol() ) );
        scoreFile.setText( settings.getScoreFile() );
        rawFile.setText( settings.getRawDataFileName() );
    }

    public void saveValues() {
        settings.setScoreCol( Integer.valueOf( jTextFieldScoreCol.getText() ).intValue() );

        settings.setScoreFile( scoreFile.getText() );
        settings.setRawFile( rawFile.getText() );
        if ( rawFile.getText() != null || rawFile.getText().length() > 0 ) {
            settings.userSetRawFile( true );
        }
        settings.setDataDirectory( chooser.getCurrentDirectory().toString() );
    }

    /**
     * Show a preview of the dat
     */
    public void previewScoresActionPerformed() {

        if ( scoreFile.getText().length() != 0 && !FileTools.testFile( scoreFile.getText() ) ) {
            wiz.showError( "You must choose a valid file to preview" );
            return;
        }

        if ( StringUtils.isBlank( jTextFieldScoreCol.getText() ) ) {
            wiz.showError( "You must valid column" );
            return;
        }

        int column = Integer.valueOf( jTextFieldScoreCol.getText() ).intValue();

        try {
            FileInputStream fis = new FileInputStream( scoreFile.getText() );
            BufferedInputStream bis = new BufferedInputStream( fis );
            BufferedReader dis = new BufferedReader( new InputStreamReader( bis ) );

            String line = null;

            int lineNum = 0;

            List<Object[]> table = new ArrayList<Object[]>();

            while ( ( line = dis.readLine() ) != null ) {
                if ( line.length() == 0 ) continue;

                String[] toks = StringUtils.splitPreserveAllTokens( line, '\t' );

                if ( toks.length < column ) {
                    wiz.showError( "The file does not have enough fields at line " + lineNum );
                    break;
                }

                String[] toUseToks = new String[2];
                toUseToks[0] = toks[0];
                toUseToks[1] = toks[column - 1];

                table.add( toUseToks );

                lineNum++;

                if ( lineNum > 100 ) break;
            }

            dis.close();

            Object[][] tab = new Object[table.size()][];
            for ( int i = 0; i < tab.length; i++ ) {
                tab[i] = table.get( i );
            }

            TableModel tableModel = new DefaultTableModel( tab, new Object[] { "Identifier", "Score" } );

            final JDialog previewPanel = new JDialog( wiz );

            Container content = previewPanel.getContentPane();

            previewPanel.setTitle( "Preview of scores" );
            previewPanel.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
            previewPanel.setFocusable( true );

            JTable tableView = new JTable( tableModel );
            JScrollPane scrollPane = new JScrollPane( tableView );
            content.add( scrollPane, BorderLayout.CENTER );

            JButton closeButton = new JButton( "Close" );

            closeButton.addActionListener( new ActionListener() {

                @Override
                public void actionPerformed( ActionEvent e ) {
                    previewPanel.dispose();

                }
            } );

            JPanel p2 = new JPanel();

            p2.add( closeButton );
            content.add( p2, BorderLayout.SOUTH );

            previewPanel.pack();
            previewPanel.setSize( 400, 300 );

            Toolkit tk = Toolkit.getDefaultToolkit();
            Dimension screenSize = tk.getScreenSize();
            int screenHeight = screenSize.height;
            int screenWidth = screenSize.width;

            previewPanel.setLocation( screenWidth / 2, screenHeight / 2 );

            previewPanel.setVisible( true );
            previewPanel.requestFocus();

        } catch ( IOException e ) {
            wiz.showError( "The file could not be previewed: " + e.getMessage() );
            return;
        }

    }

}

class AnalysisWizardStep2_rawBrowseButton_actionAdapter implements java.awt.event.ActionListener {
    AnalysisWizardStep2 adaptee;

    AnalysisWizardStep2_rawBrowseButton_actionAdapter( AnalysisWizardStep2 adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.rawBrowseButton_actionPerformed();
    }
}

class AnalysisWizardStep2_scoreBrowseButton_actionAdapter implements java.awt.event.ActionListener {
    AnalysisWizardStep2 adaptee;

    AnalysisWizardStep2_scoreBrowseButton_actionAdapter( AnalysisWizardStep2 adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.scoreBrowseButton_actionPerformed();
    }
}

class PreviewButtonAdapter implements ActionListener {
    AnalysisWizardStep2 adaptee;

    PreviewButtonAdapter( AnalysisWizardStep2 adaptee ) {
        this.adaptee = adaptee;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed( ActionEvent e ) {
        adaptee.previewScoresActionPerformed();
    }

}
