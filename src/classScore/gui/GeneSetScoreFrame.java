package classScore.gui;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import org.xml.sax.SAXException;

import baseCode.gui.GuiUtil;
import baseCode.gui.StatusJlabel;
import baseCode.util.StatusViewer;
import classScore.AnalysisThread;
import classScore.Settings;
import classScore.GeneSetPvalRun;
import classScore.data.GONames;
import classScore.data.GeneAnnotations;

/**
 * <hr>
 * <p>
 * Copyright (c) 2004 Columbia University
 * 
 * @author Homin Lee
 * @author Paul Pavlidis
 * @version $Id$
 *
 */

public class GeneSetScoreFrame
    extends JFrame {
   
   private JPanel mainPanel = ( JPanel )this.getContentPane();
   private JMenuBar jMenuBar1 = new JMenuBar();
   private JMenu fileMenu = new JMenu();
   private JMenuItem quitMenuItem = new JMenuItem();
   private JMenu classMenu = new JMenu();
   private JMenuItem defineClassMenuItem = new JMenuItem();
   private JMenuItem modClassMenuItem = new JMenuItem();
   private JMenuItem findClassMenuItem = new JMenuItem();
   private JMenu analysisMenu = new JMenu();
   private JMenuItem runAnalysisMenuItem = new JMenuItem();
   private JMenuItem cancelAnalysisMenuItem = new JMenuItem();
   private JMenuItem loadAnalysisMenuItem = new JMenuItem();
   private JMenuItem saveAnalysisMenuItem = new JMenuItem();
   private JMenu helpMenu = new JMenu();
   private JMenuItem helpMenuItem = new JMenuItem();
   private JMenuItem aboutMenuItem = new JMenuItem();

   private JPanel progressPanel;
   private JPanel progInPanel = new JPanel();
   private JProgressBar progressBar = new JProgressBar();
   private OutputPanel oPanel;

   private JLabel jLabelStatus = new JLabel();
   private JPanel jPanelStatus = new JPanel();

   private Settings settings;
   private StatusViewer statusMessenger;
   private GONames goData;
   private GeneAnnotations geneData;
   private LinkedList results = new LinkedList();

   private Map geneDataSets;
   private Map rawDataSets;
   private Map geneScoreSets;

   private JLabel logoLabel;
   
   private AnalysisThread athread=new AnalysisThread();
   
   public GeneSetScoreFrame() {
      try {
         jbInit();
         settings = new Settings();
      }
      catch ( Exception e ) {
         e.printStackTrace();
      }
   }

   /* init */
   private void jbInit() throws Exception {
      this.setDefaultCloseOperation( EXIT_ON_CLOSE );
      this.setJMenuBar( jMenuBar1 );
      this.setSize( new Dimension( 886, 450 ) );
      this.setTitle( "Functional Class Scoring" );
      BorderLayout borderLayout1 = new BorderLayout();
      mainPanel.setLayout(borderLayout1);
      mainPanel.setPreferredSize( new Dimension( 1000, 600 ) );
      mainPanel.setInputVerifier( null );

      //menu stuff
      fileMenu.setText( "File" );
      fileMenu.setMnemonic( 'F' );
      quitMenuItem.setText( "Quit" );
      quitMenuItem.addActionListener( new
                                      GeneSetScoreFrame_quitMenuItem_actionAdapter( this ) );
      quitMenuItem.setMnemonic( 'Q' );
      quitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,InputEvent.CTRL_MASK));
      fileMenu.add( quitMenuItem );
      classMenu.setText( "Classes" );
      classMenu.setMnemonic( 'C' );
      classMenu.setEnabled(false);
      defineClassMenuItem.setText( "Define New Class" );
      defineClassMenuItem.addActionListener( new
                                             GeneSetScoreFrame_defineClassMenuItem_actionAdapter( this ) );
      defineClassMenuItem.setMnemonic( 'D' );
      defineClassMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,InputEvent.CTRL_MASK));
      modClassMenuItem.setText( "Modify Class" );
      modClassMenuItem.addActionListener( new
                                          GeneSetScoreFrame_modClassMenuItem_actionAdapter( this ) );
      modClassMenuItem.setMnemonic( 'M' );
      findClassMenuItem.setText( "Find Class" );
      findClassMenuItem.addActionListener( new
                                          GeneSetScoreFrame_findClassMenuItem_actionAdapter( this ) );
      findClassMenuItem.setMnemonic( 'F' );
      findClassMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,InputEvent.CTRL_MASK));
      classMenu.add( defineClassMenuItem );
      classMenu.add( modClassMenuItem );
      classMenu.add( findClassMenuItem );
      analysisMenu.setText( "Analysis" );
      analysisMenu.setMnemonic( 'A' );
      analysisMenu.setEnabled(false);
      runAnalysisMenuItem.setText( "Run Analysis" );
      runAnalysisMenuItem.addActionListener( new
                                             GeneSetScoreFrame_runAnalysisMenuItem_actionAdapter( this ) );
      runAnalysisMenuItem.setMnemonic( 'R' );
      runAnalysisMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,InputEvent.CTRL_MASK));
      cancelAnalysisMenuItem.setText( "Cancel Analysis" );
      cancelAnalysisMenuItem.addActionListener( new
                                             GeneSetScoreFrame_cancelAnalysisMenuItem_actionAdapter( this ) );
      cancelAnalysisMenuItem.setMnemonic( 'C' );
      cancelAnalysisMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,InputEvent.CTRL_MASK));

      loadAnalysisMenuItem.setText( "Load Analysis" );
      loadAnalysisMenuItem.addActionListener( new
                                              GeneSetScoreFrame_loadAnalysisMenuItem_actionAdapter( this ) );
      loadAnalysisMenuItem.setMnemonic( 'L' );
      loadAnalysisMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,InputEvent.CTRL_MASK));
      saveAnalysisMenuItem.setText( "Save Analysis" );
      saveAnalysisMenuItem.addActionListener( new
                                              GeneSetScoreFrame_saveAnalysisMenuItem_actionAdapter( this ) );
      saveAnalysisMenuItem.setMnemonic( 'S' );
      saveAnalysisMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,InputEvent.CTRL_MASK));
      analysisMenu.add( runAnalysisMenuItem );
      analysisMenu.add( cancelAnalysisMenuItem );
      analysisMenu.add( loadAnalysisMenuItem );
      analysisMenu.add( saveAnalysisMenuItem );
      helpMenu.setText( "Help" );
      helpMenu.setMnemonic( 'H' );
      helpMenuItem.setText( "Help Topics" );
      helpMenuItem.setMnemonic( 'T' );
      helpMenuItem.addActionListener( new
                                      GeneSetScoreFrame_helpMenuItem_actionAdapter( this ) );
      aboutMenuItem.setText( "About ErmineJ" );
      aboutMenuItem.setMnemonic( 'A' );
      aboutMenuItem.addActionListener( new
                                       GeneSetScoreFrame_aboutMenuItem_actionAdapter( this ) );
      helpMenu.add( helpMenuItem );
      helpMenu.add( aboutMenuItem );
      jMenuBar1.add( fileMenu );
      jMenuBar1.add( classMenu );
      jMenuBar1.add( analysisMenu );
      jMenuBar1.add( helpMenu );

      //initialization panel (replaced by main panel when done)
      logoLabel = new JLabel();
      logoLabel.setIcon( new ImageIcon( GeneSetScoreFrame.class
            .getResource( "resources/logo1small.gif" ) ) );
      
      progressPanel = new JPanel();
//      progressPanel.setPreferredSize( new Dimension( 830, 330 ) );
      // todo make the icon and text. centered.
      progressPanel.setLayout(new GridLayout(3, 1));
      
   //   progInPanel.setPreferredSize(new Dimension(350, 100));
      JLabel label= new JLabel("Please wait while the files are loaded in.");
      label.setPreferredSize(new Dimension(195, 30));
      label.setHorizontalTextPosition(JLabel.CENTER);
      label.setLabelFor(progressBar);
      
      progressBar.setPreferredSize(new Dimension(300, 16));
      progressBar.setIndeterminate(false);
      
      progressPanel.add(logoLabel);
      progressPanel.add(label, null);
      progInPanel.add(progressBar, null);
      progressPanel.add(progInPanel);

      //main panel
      oPanel = new OutputPanel( this, results );
      oPanel.setPreferredSize( new Dimension( 830, 330 ) );

      //controls

      //status bar
      jPanelStatus.setBorder( BorderFactory.createEtchedBorder() );
      jPanelStatus.setPreferredSize( new Dimension( 830, 33 ) );
      jLabelStatus.setFont( new java.awt.Font( "Dialog", 0, 11 ) );
      jLabelStatus.setPreferredSize(new Dimension(800, 19) );
      jLabelStatus.setHorizontalAlignment( SwingConstants.LEFT );
      jPanelStatus.add( jLabelStatus, null );
      showStatus( "Please see 'About this software'" );
      statusMessenger = new StatusJlabel( jLabelStatus );
      mainPanel.add( jPanelStatus, BorderLayout.SOUTH );
   }

   private void enableMenusOnStart() {
      classMenu.setEnabled( true );
      analysisMenu.setEnabled( true );
      helpMenu.setEnabled(true);
   }

   public void disableMenusForAnalysis()
   {
      defineClassMenuItem.setEnabled(false);
      modClassMenuItem.setEnabled(false);
      runAnalysisMenuItem.setEnabled(false);
      loadAnalysisMenuItem.setEnabled(false);
      saveAnalysisMenuItem.setEnabled(false);
   }

   public void enableMenusForAnalysis()
   {
      defineClassMenuItem.setEnabled(true);
      modClassMenuItem.setEnabled(true);
      runAnalysisMenuItem.setEnabled(true);
      loadAnalysisMenuItem.setEnabled(true);
      saveAnalysisMenuItem.setEnabled(true);
   }

   public void initialize() {
      try {
         mainPanel.add( progressPanel, BorderLayout.CENTER );

         rawDataSets = new HashMap();
         geneDataSets = new HashMap();
         geneScoreSets = new HashMap();

         progressBar.setValue(10);
         
         statusMessenger.setStatus("Reading GO descriptions " + settings.getClassFile());
         goData = new GONames(settings.getClassFile()); // parse go name file
         progressBar.setValue(70);
         
         statusMessenger.setStatus("Reading gene annotations from " + settings.getAnnotFile());
         geneData = new GeneAnnotations(settings.getAnnotFile(), statusMessenger);
         progressBar.setValue(100);
         
         statusMessenger.setStatus( "Initializing gene class mapping" );
         geneDataSets.put(new Integer("original".hashCode()) , geneData);
         
         statusMessenger.setStatus("Done with setup");
         enableMenusOnStart();

         mainPanel.remove( progressPanel );
         mainPanel.add( oPanel, BorderLayout.CENTER );
         statusMessenger.setStatus("Ready.");
      }
      catch ( IllegalArgumentException e ) {
         GuiUtil.error( 
            "Error during initialization: " + e +
            "\nIf this problem persists, please contact the software developer. " +
            "\nPress OK to quit." );
         System.exit( 1 );
      }
      catch ( IOException e ) {
         GuiUtil.error( 
            "File reading or writing error during initialization: " + e.getMessage()  + 
            "\nIf this problem persists, please contact the software developer. " +
            "\nPress OK to quit.", e );
         System.exit( 1 );
      } catch ( SAXException e ) {
         GuiUtil.error( 
           "Gene Ontology file format is incorrect. " +
           "\nPlease check that it is a valid XML file. " +
           "\nIf this problem persists, please contact the software developer. " +
           "\nPress OK to quit." );
         System.exit( 1 );
      }
      oPanel.addInitialData( geneData, goData );
      statusMessenger.setStatus("Done with initialization.");
   }

   /**
    *
    * @param a
    */
   private void showStatus( String a ) {
      jLabelStatus.setText( a );
   }

   /**
    *
    */
   private void clearStatus() {
      jLabelStatus.setText( "" );
   }

   /**
    *
    */

   void quitMenuItem_actionPerformed( ActionEvent e ) {
      System.exit( 0 );
   }

   void defineClassMenuItem_actionPerformed( ActionEvent e ) {
      GeneSetWizard cwiz = new GeneSetWizard(this, geneData, goData, true);
      cwiz.showWizard();
   }

   void modClassMenuItem_actionPerformed( ActionEvent e ) {
      GeneSetWizard cwiz = new GeneSetWizard(this, geneData, goData, false);
      cwiz.showWizard();
   }

   void findClassMenuItem_actionPerformed( ActionEvent e ) {
      new FindDialog( this, geneData, goData );
   }

   void runAnalysisMenuItem_actionPerformed( ActionEvent e ) {
      AnalysisWizard awiz = new AnalysisWizard(this, geneDataSets, goData);
      awiz.showWizard();
   }

   void cancelAnalysisMenuItem_actionPerformed( ActionEvent e ) {
      athread.cancelAnalysisThread();
      showStatus( "Ready" );
   }

   void loadAnalysisMenuItem_actionPerformed( ActionEvent e ) {
      LoadDialog lgsd = new LoadDialog(this);
      lgsd.showDialog();
   }

   void saveAnalysisMenuItem_actionPerformed( ActionEvent e ) {
      SaveWizard swiz = new SaveWizard( this, results, goData );
      swiz.showWizard();
   }

   void helpMenuItem_actionPerformed( ActionEvent e ) {
      new HelpFrame(this);
   }

   void aboutMenuItem_actionPerformed( ActionEvent e ) {
      new AboutBox( this );
   }

   public Settings getSettings() {
      return settings;
   }

   public void setSettings(Settings settings) {
      this.settings=settings;
   }

   public StatusViewer getStatusMessenger(){
      return statusMessenger;
   }

   public void addResult(GeneSetPvalRun result)
   {
      results.add( result );
      oPanel.addRun();  // this line should come after results.add() or else you'll get errors
   } 

   public void startAnalysis(Settings runSettings)
   {
      disableMenusForAnalysis();
      athread.startAnalysisThread(this,runSettings,statusMessenger,goData,geneDataSets, rawDataSets, geneScoreSets);
   }

   public void loadAnalysis(String loadFile)
   {
      disableMenusForAnalysis();
      Settings loadSettings = new Settings(loadFile);
      athread.loadAnalysisThread(this,loadSettings,statusMessenger,goData,geneDataSets, rawDataSets, geneScoreSets,loadFile);
   }

   public void addedNewGeneSet()
   {
      oPanel.addedNewGeneSet();
   }
   
   /**
    * @return Returns the oPanel.
    */
   public OutputPanel getOPanel() {
      return oPanel;
   }
}

/* end class */

class GeneSetScoreFrame_quitMenuItem_actionAdapter
    implements java.awt.
    event.ActionListener {
   GeneSetScoreFrame adaptee;

   GeneSetScoreFrame_quitMenuItem_actionAdapter( GeneSetScoreFrame adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.quitMenuItem_actionPerformed( e );
   }
}

class GeneSetScoreFrame_defineClassMenuItem_actionAdapter
    implements java.awt.
    event.ActionListener {
   GeneSetScoreFrame adaptee;

   GeneSetScoreFrame_defineClassMenuItem_actionAdapter( GeneSetScoreFrame adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.defineClassMenuItem_actionPerformed( e );
   }
}

class GeneSetScoreFrame_modClassMenuItem_actionAdapter
    implements java.awt.event.
    ActionListener {
   GeneSetScoreFrame adaptee;

   GeneSetScoreFrame_modClassMenuItem_actionAdapter( GeneSetScoreFrame adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.modClassMenuItem_actionPerformed( e );
   }
}

class GeneSetScoreFrame_findClassMenuItem_actionAdapter
    implements java.awt.event.
    ActionListener {
   GeneSetScoreFrame adaptee;

   GeneSetScoreFrame_findClassMenuItem_actionAdapter( GeneSetScoreFrame adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.findClassMenuItem_actionPerformed( e );
   }
}

class GeneSetScoreFrame_runAnalysisMenuItem_actionAdapter
    implements java.awt.
    event.ActionListener {
   GeneSetScoreFrame adaptee;

   GeneSetScoreFrame_runAnalysisMenuItem_actionAdapter( GeneSetScoreFrame adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.runAnalysisMenuItem_actionPerformed( e );
   }
}

class GeneSetScoreFrame_cancelAnalysisMenuItem_actionAdapter
    implements java.awt.
    event.ActionListener {
   GeneSetScoreFrame adaptee;

   GeneSetScoreFrame_cancelAnalysisMenuItem_actionAdapter( GeneSetScoreFrame adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.cancelAnalysisMenuItem_actionPerformed( e );
   }
}

class GeneSetScoreFrame_loadAnalysisMenuItem_actionAdapter
    implements java.awt.
    event.ActionListener {
   GeneSetScoreFrame adaptee;

   GeneSetScoreFrame_loadAnalysisMenuItem_actionAdapter( GeneSetScoreFrame adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.loadAnalysisMenuItem_actionPerformed( e );
   }
}

class GeneSetScoreFrame_saveAnalysisMenuItem_actionAdapter
    implements java.awt.
    event.ActionListener {
   GeneSetScoreFrame adaptee;

   GeneSetScoreFrame_saveAnalysisMenuItem_actionAdapter( GeneSetScoreFrame adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.saveAnalysisMenuItem_actionPerformed( e );
   }
}

class GeneSetScoreFrame_helpMenuItem_actionAdapter
    implements java.awt.
    event.ActionListener {
   GeneSetScoreFrame adaptee;

   GeneSetScoreFrame_helpMenuItem_actionAdapter( GeneSetScoreFrame adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.helpMenuItem_actionPerformed( e );
   }
}

class GeneSetScoreFrame_aboutMenuItem_actionAdapter
    implements java.awt.
    event.ActionListener {
   GeneSetScoreFrame adaptee;

   GeneSetScoreFrame_aboutMenuItem_actionAdapter( GeneSetScoreFrame adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.aboutMenuItem_actionPerformed( e );
   }
}


