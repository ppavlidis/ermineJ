package classScore.gui.geneSet;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import baseCode.dataStructure.DenseDoubleMatrix2DNamed;
import baseCode.dataStructure.reader.DoubleMatrixReader;
import baseCode.graphics.text.Util;
import baseCode.gui.ColorMap;
import baseCode.gui.JGradientBar;
import baseCode.gui.JMatrixDisplay;
import baseCode.gui.table.JHorizontalTableHeaderRenderer;
import baseCode.gui.table.JMatrixTableCellRenderer;
import baseCode.gui.table.JVerticalTableHeaderRenderer;
import classScore.GeneAnnotations;
import classScore.Settings;
import classScore.SortFilterModel;
import javax.swing.*;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Will Braynen
 * @version $Id$
 */
public class JDetailsFrame
    extends JFrame {

   final int PREFERRED_WIDTH_MATRIXDISPLAY_COLUMN = 6;
   final int MIN_WIDTH_MATRIXDISPLAY_COLUMN = 1;
   final int MAX_WIDTH_MATRIXDISPLAY_COLUMN = 19;
   final int PREFERRED_WIDTH_COLUMN_0 = 75;
   final int PREFERRED_WIDTH_COLUMN_1 = 75;
   final int PREFERRED_WIDTH_COLUMN_2 = 75;
   final int PREFERRED_WIDTH_COLUMN_3 = 300;
   final int COLOR_RANGE_SLIDER_RESOLUTION = 12;
   final int NORMALIZED_COLOR_RANGE_MAX = 12;

   public JMatrixDisplay m_matrixDisplay = null;
   protected JScrollPane m_tableScrollPane = new JScrollPane();
   protected JTable m_table = new JTable();
   protected BorderLayout borderLayout1 = new BorderLayout();
   protected JToolBar m_toolbar = new JToolBar();
   JSlider m_matrixDisplayCellWidthSlider = new JSlider();
   JMenuBar m_menuBar = new JMenuBar();
   JMenu m_fileMenu = new JMenu();
   JRadioButtonMenuItem m_greenredColormapMenuItem = new JRadioButtonMenuItem();
   JMenu m_viewMenu = new JMenu();
   JRadioButtonMenuItem m_blackbodyColormapMenuItem = new JRadioButtonMenuItem();
   JMenuItem m_saveImageMenuItem = new JMenuItem();
   JCheckBoxMenuItem m_normalizeMenuItem = new JCheckBoxMenuItem();
   JLabel jLabel1 = new JLabel();
   JLabel jLabel2 = new JLabel();
   JLabel jLabel3 = new JLabel();
   JSlider m_colorRangeSlider = new JSlider();
   JGradientBar m_gradientBar = new JGradientBar();
   JMenuItem m_saveDataMenuItem = new JMenuItem();

   /** 
    * The name of the raw data file, as an absolute path, where we look up the 
    * microarray data for each gene in the current gene set.
    */ 
   String m_filename;
   DecimalFormat m_nf = new DecimalFormat( "0.##E0" );
   String[] m_probeIDs;
   Double[] m_pvalues;
   String[] m_geneNames;
   String[] m_probeDescriptions;
   
   /**
    * @param  args[0]  the name of the raw data file, as an absolute path, 
    *                  where we look up the microarray data for each gene in 
    *                  the current gene set.
    */
   public static void main( String[] args ) {
      
      // Make sure the filename was passed in
      if (args.length < 1) {
         System.err.println( "Please specify the name of the data file as a program argument" );
         return;
      }
      String filename = args[0];
      String[] geneSet = { "32254_at", "32533_s_at", "32534_f_at" }; // probe IDs
      JDetailsFrame frame = new JDetailsFrame( geneSet, null, null, null, filename );
      frame.show();
   }
   
   public JDetailsFrame( 
         String[] probeIDs,
         Double[] pvalues,
         String[] geneNames, 
         String[] probeDescriptions, 
         String filename ) {
      try {
         m_probeIDs = probeIDs;
         m_pvalues = pvalues;
         m_geneNames = geneNames;
         m_probeDescriptions = probeDescriptions;
         m_filename = filename;
         jbInit();
      }
      catch ( Exception e ) {
         e.printStackTrace();
      }
   }

   public JDetailsFrame( 
         Map pvals, 
         Map classToProbe, 
         String classID,
         GeneAnnotations geneData, 
         Settings settings ) {
      try {
         translateVars( pvals, classToProbe, classID, geneData, settings );         
         jbInit();
      }
      catch ( Exception e ) {
         e.printStackTrace();
      }
   }

   /**
    * This method of course add an unnecessary loop to this class.  We could
    * instead look up data -- do this translation -- on the fly in the
    * table model (DetailsTableModel).  However, I find that this translation 
    * here greatly improves code readability in this class and in the table 
    * model class.  It also encapsulates the class and makes it less dependent
    * on implementation details of the calling class (classPvalRun).  This
    * opens the possibility of using this class elsewhere and also makes
    * the main() debug function possible.
    *
    * If very big gene sets (classes) are ever used and the frame starts
    * taking noticeably longer to open, then revert the following
    *  this class to   :  classScore.gui.JDetails to revision 1.16
    *  the table model :  classScore.gui.DetailsTableModel to revision 1.6
    *                     (see getValueAt() method there)
    */
   private void translateVars( 
         Map pvals, 
         Map classToProbe, 
         String classID, 
         GeneAnnotations geneData, 
         Settings settings ) {

      m_filename = settings.getRawFile();
      
      int probeCount = (( ArrayList ) classToProbe.get( classID )).size();
      m_probeIDs  = new String[ probeCount ];
      m_pvalues   = new Double[ probeCount ];
      m_geneNames = new String[ probeCount ];
      m_probeDescriptions = new String[ probeCount ];

      for ( int i = 0; i < probeCount; i++ ) {

         // probe ID
         m_probeIDs[i] = 
               ( String ) ( ( ArrayList ) classToProbe.get( classID ) ).get( i );

         // p value
         m_pvalues[i] = ( Double ) pvals.get( 
               ( String ) ( ( ArrayList ) classToProbe.get( classID ) ).get( i )
            );

         // probe's gene name
         m_geneNames[i] = geneData.getProbeGeneName( 
               ( String ) ( ( ArrayList ) classToProbe.get( classID ) ).get( i )
            );

         // probe description
         m_probeDescriptions[i] = geneData.getProbeDescription( 
               ( String ) ( ( ArrayList ) classToProbe.get( classID ) ).get( i ) 
            );
      }
   } // end init
   
   private void jbInit() throws Exception {

      createDetailsTable( m_probeIDs, m_pvalues, m_geneNames, m_probeDescriptions, m_filename );
      
      setSize( 800, m_table.getHeight() );
      setResizable( false );
      setLocation( 200, 100 );
      getContentPane().setLayout( borderLayout1 );
      setDefaultCloseOperation( DISPOSE_ON_CLOSE );

      // Enable the horizontal scroll bar
      m_table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

      // Prevent user from moving tables around
      m_table.getTableHeader().setReorderingAllowed( false );

      // Make sure the matrix display doesn't have a grid separating color cells.
      m_table.setIntercellSpacing( new Dimension( 0, 0 ) );

      // The rest of the table (text and value) should have a light gray grid
      m_table.setGridColor( Color.lightGray );

      // add a viewport with a table inside it
      m_toolbar.setFloatable( false );
      this.setJMenuBar( m_menuBar );
      m_fileMenu.setText( "File" );
      m_greenredColormapMenuItem.setSelected( false );
      m_greenredColormapMenuItem.setText( "Green-Red" );
      m_greenredColormapMenuItem.addActionListener( new
          JDetailsFrame_m_greenredColormapMenuItem_actionAdapter( this ) );
      m_greenredColormapMenuItem.addActionListener( new
          JDetailsFrame_m_greenredColormapMenuItem_actionAdapter( this ) );
      m_viewMenu.setText( "View" );
      m_blackbodyColormapMenuItem.setSelected( true );
      m_blackbodyColormapMenuItem.setText( "Blackbody" );
      m_blackbodyColormapMenuItem.addActionListener( new
          JDetailsFrame_m_blackbodyColormapMenuItem_actionAdapter( this ) );
      m_blackbodyColormapMenuItem.addActionListener( new
          JDetailsFrame_m_blackbodyColormapMenuItem_actionAdapter( this ) );
      m_saveImageMenuItem.setActionCommand("SaveImage");
      m_saveImageMenuItem.setText("Save Image..." );
      m_saveImageMenuItem.addActionListener( new JDetailsFrame_m_saveImageMenuItem_actionAdapter( this ) );
      m_normalizeMenuItem.setText( "Normalize" );
      m_normalizeMenuItem.addActionListener( new JDetailsFrame_m_normalizeMenuItem_actionAdapter( this ) );
      m_matrixDisplayCellWidthSlider.setInverted( false );
      m_matrixDisplayCellWidthSlider.setMajorTickSpacing( 0 );
      m_matrixDisplayCellWidthSlider.setMaximum( MAX_WIDTH_MATRIXDISPLAY_COLUMN );
      m_matrixDisplayCellWidthSlider.setMinimum( MIN_WIDTH_MATRIXDISPLAY_COLUMN );
      m_matrixDisplayCellWidthSlider.setValue( PREFERRED_WIDTH_MATRIXDISPLAY_COLUMN );
      m_matrixDisplayCellWidthSlider.setMinorTickSpacing( 3 );
      m_matrixDisplayCellWidthSlider.setPaintLabels( false );
      m_matrixDisplayCellWidthSlider.setPaintTicks( true );
      m_matrixDisplayCellWidthSlider.setMaximumSize( new Dimension( 90, 24 ) );
      m_matrixDisplayCellWidthSlider.setPreferredSize( new Dimension( 90, 24 ) );
      m_matrixDisplayCellWidthSlider.addChangeListener( new
          JDetailsFrame_m_matrixDisplayCellWidthSlider_changeAdapter( this ) );
      this.setResizable( true );
      jLabel1.setText( "Cell Width:" );
      jLabel2.setText( "    " );
      jLabel3.setText( "Color Range:" );
      
      m_gradientBar.setMaximumSize(new Dimension(200, 30));
      m_gradientBar.setPreferredSize(new Dimension(120, 30));
      m_gradientBar.setColorMap( m_matrixDisplay.getColorMap() );

      initColorRangeWidget();

      m_colorRangeSlider.setMaximumSize( new Dimension( 50, 24 ) );
      m_colorRangeSlider.setPreferredSize( new Dimension( 50, 24 ) );
      m_colorRangeSlider.addChangeListener( new JDetailsFrame_m_colorRangeSlider_changeAdapter( this ) );
      m_saveDataMenuItem.setActionCommand("SaveData");
      m_saveDataMenuItem.setText("Save Data...");
      m_saveDataMenuItem.addActionListener(new JDetailsFrame_m_saveDataMenuItem_actionAdapter(this));
      m_tableScrollPane.getViewport().add( m_table, null );

      // Reposition the table inside the scrollpane
      int x = m_table.getSize().width; // should probably subtract the size of the viewport, but it gets trimmed anyway, so it's okay to be lazy here
      m_tableScrollPane.getViewport().setViewPosition( new Point( x, 0 ) );

      this.getContentPane().add( m_tableScrollPane, BorderLayout.CENTER );
      this.getContentPane().add( m_toolbar, BorderLayout.NORTH );
      m_toolbar.add( jLabel1, null );
      m_toolbar.add( m_matrixDisplayCellWidthSlider, null );
      m_toolbar.add( jLabel2, null );
      m_toolbar.add( jLabel3, null );
      m_toolbar.add( m_colorRangeSlider, null );
      m_toolbar.add( m_gradientBar, null );

      m_menuBar.add( m_fileMenu );
      m_menuBar.add( m_viewMenu );

     // Color map menu items (radio button group -- only one can be selected at one time)
      ButtonGroup group = new ButtonGroup();
      group.add( m_greenredColormapMenuItem );
      group.add( m_blackbodyColormapMenuItem );

      m_viewMenu.add( m_normalizeMenuItem );
      m_viewMenu.addSeparator();
      m_viewMenu.add( m_greenredColormapMenuItem );
      m_viewMenu.add( m_blackbodyColormapMenuItem );
      m_fileMenu.add( m_saveImageMenuItem );
      m_fileMenu.add(m_saveDataMenuItem);
      m_matrixDisplayCellWidthSlider.setPaintTrack( true );
      m_matrixDisplayCellWidthSlider.setPaintTicks( false );
      
      m_nf.setMaximumFractionDigits( 3 );
      boolean isNormalized = m_matrixDisplay.getStandardizedEnabled();
      m_normalizeMenuItem.setSelected( isNormalized );      
   }
      
   private void createDetailsTable(
       String[] probesIDs,
       Double[] pvalues,
       String[] geneNames,
       String[] probeDescriptions,
       String filename ) {

      //
      // Create a matrix display
      //

      // compile the matrix data
      DoubleMatrixReader matrixReader = new DoubleMatrixReader();
      DenseDoubleMatrix2DNamed matrix = null;
      try {
         matrix = ( DenseDoubleMatrix2DNamed ) matrixReader.read( filename, probesIDs );
      }
      catch ( IOException e ) {
         System.err.println( "IOException: wrong filename for MatrixReader" );
      }

      // create the matrix display
      m_matrixDisplay = new JMatrixDisplay( matrix );
      m_matrixDisplay.setStandardizedEnabled( true );

      //
      // Create the rest of the table
      //

      DetailsTableModel m = new DetailsTableModel(
          m_matrixDisplay, pvalues, probesIDs, geneNames, probeDescriptions, m_nf
          );
      SortFilterModel sorter = new SortFilterModel( m, m_matrixDisplay );
      m_table.setModel( sorter );

      m_table.getTableHeader().addMouseListener( new MouseAdapter() {
         public void mouseClicked( MouseEvent event ) {
            int tableColumn = m_table.columnAtPoint( event.getPoint() );
            int modelColumn = m_table.convertColumnIndexToModel( tableColumn );
            ( ( SortFilterModel ) m_table.getModel() ).sort( modelColumn );
         }
      } );

      //
      // Set up the matrix display part of the table
      //

      // Make the columns in the matrix display not too wide (cell-size)
      // and set a custom cell renderer
      JMatrixTableCellRenderer cellRenderer = new JMatrixTableCellRenderer(
          m_matrixDisplay
          ); // create one instance that will be used to draw each cell

      JVerticalTableHeaderRenderer verticalHeaderRenderer =
          new JVerticalTableHeaderRenderer(); // create only one instance
      int matrixColumnCount = m_matrixDisplay.getColumnCount();

      // Set each column
      for ( int i = 0; i < matrixColumnCount; i++ ) {
         TableColumn col = m_table.getColumnModel().getColumn( i );
         col.setResizable( false );
         col.setPreferredWidth( PREFERRED_WIDTH_MATRIXDISPLAY_COLUMN );
         col.setMinWidth( MIN_WIDTH_MATRIXDISPLAY_COLUMN ); // no narrower than this
         col.setMaxWidth( MAX_WIDTH_MATRIXDISPLAY_COLUMN ); // no wider than this
         col.setCellRenderer( cellRenderer );
         col.setHeaderRenderer( verticalHeaderRenderer );
      }

      //
      // Set up the rest of the table
      //
      JHorizontalTableHeaderRenderer horizontalHeaderRenderer =
          new JHorizontalTableHeaderRenderer(); // create only one instance
      TableColumn col;

      // The columns containing text or values (not matrix display) should be a bit wider
      col = m_table.getColumnModel().getColumn( matrixColumnCount + 0 );
      col.setPreferredWidth( PREFERRED_WIDTH_COLUMN_0 );
      col.setHeaderRenderer( horizontalHeaderRenderer );

      col = m_table.getColumnModel().getColumn( matrixColumnCount + 1 );
      col.setPreferredWidth( PREFERRED_WIDTH_COLUMN_1 );
      col.setHeaderRenderer( horizontalHeaderRenderer );

      col = m_table.getColumnModel().getColumn( matrixColumnCount + 2 );
      col.setPreferredWidth( PREFERRED_WIDTH_COLUMN_2 );
      col.setHeaderRenderer( horizontalHeaderRenderer );

      col = m_table.getColumnModel().getColumn( matrixColumnCount + 3 );
      col.setPreferredWidth( PREFERRED_WIDTH_COLUMN_3 );
      col.setHeaderRenderer( horizontalHeaderRenderer );

      //
      // Sort initially by the pvalue column
      //
      int modelColumn = m_table.convertColumnIndexToModel( matrixColumnCount + 1 );
      ( ( SortFilterModel ) m_table.getModel() ).sort( modelColumn );

      //
      // Save the dimensions of the table just in case
      //
      int width =
          matrixColumnCount * PREFERRED_WIDTH_MATRIXDISPLAY_COLUMN +
          PREFERRED_WIDTH_COLUMN_0 +
          PREFERRED_WIDTH_COLUMN_1 +
          PREFERRED_WIDTH_COLUMN_2 +
          PREFERRED_WIDTH_COLUMN_3;
      int height = m_table.getPreferredScrollableViewportSize().height;

      Dimension d = new Dimension( width, height );
      m_table.setSize( d );

   } // end createDetailsTable

   protected String[] getProbes( Map classToProbe, String id, int count ) {

      // Compile a list of gene probe ID's in this probe class
      String[] probes = new String[count];
      for ( int i = 0; i < count; i++ ) {
         probes[i] = ( String ) ( ( ArrayList ) classToProbe.get( id ) ).get( i );
      }
      return probes;

   }

   void m_greenredColormapMenuItem_actionPerformed( ActionEvent e ) {

      try {
         Color[] colorMap = ColorMap.GREENRED_COLORMAP;
         m_matrixDisplay.setColorMap( colorMap );
         m_gradientBar.setColorMap( colorMap );
      }
      catch ( Exception ex ) {
      }

   }

   void m_blackbodyColormapMenuItem_actionPerformed( ActionEvent e ) {

      try {
         Color[] colorMap = ColorMap.BLACKBODY_COLORMAP;
         m_matrixDisplay.setColorMap( colorMap );
         m_gradientBar.setColorMap( colorMap );
      }
      catch ( Exception ex ) {
      }

   }

   void m_saveImageMenuItem_actionPerformed( ActionEvent e ) {

      // Create a file chooser
      final JImageFileChooser fc = new JImageFileChooser( true, m_matrixDisplay.getStandardizedEnabled() );
      int returnVal = fc.showSaveDialog( this );
      if ( returnVal == JFileChooser.APPROVE_OPTION ) {

         File file = fc.getSelectedFile();
         boolean includeLabels = fc.includeLabels();
         boolean normalize = fc.normalized();

         // Make sure the filename has an image extension
         String filename = file.getPath();
         if ( !Util.hasImageExtension( filename ) ) {
            filename = Util.addImageExtension( filename );
         }
         // Save the color matrix image
         try {
            saveImage( filename, includeLabels, normalize );
         }
         catch ( IOException ex ) {
            System.err.println( "IOException error saving png to " + filename );
         }
      }
      // else canceled by user
   }

   void m_saveDataMenuItem_actionPerformed(ActionEvent e) {

      // Create a file chooser
      final JDataFileChooser fc = new JDataFileChooser( true, m_matrixDisplay.getStandardizedEnabled() );
      int returnVal = fc.showSaveDialog( this );
      if ( returnVal == JFileChooser.APPROVE_OPTION ) {

         File file = fc.getSelectedFile();
         boolean includeEverything = fc.includeEverything();
         boolean normalize = fc.normalized();

         // Make sure the filename has a data extension
         String filename = file.getPath();
         if ( !Util.hasDataExtension( filename ) ) {
            filename = Util.addDataExtension( filename );
         }
         // Save the values
         try {
            saveData( filename, true, includeEverything, normalize );
         }
         catch ( IOException ex ) {
            System.err.println( "IOException error saving data to " + filename );
         }
      }
      // else canceled by user
   }

   
   protected void saveImage( String filename, boolean includeLabels, boolean normalized ) throws IOException {

      boolean isStandardized = m_matrixDisplay.getStandardizedEnabled();
      m_matrixDisplay.setStandardizedEnabled( normalized );
      m_matrixDisplay.setRowKeys( getCurrentMatrixDisplayRowOrder() );
      try {
         m_matrixDisplay.saveImage( filename, includeLabels, normalized );
      }
      catch (IOException e) {
         // clean up
         m_matrixDisplay.setStandardizedEnabled( isStandardized ); // return to previous state
         m_matrixDisplay.resetRowKeys();
         throw e;
      }
      
      // clean up
      m_matrixDisplay.setStandardizedEnabled( isStandardized ); // return to previous state
      m_matrixDisplay.resetRowKeys();
      
   } // end saveImage
   
   
   protected void saveData( String filename, boolean includeMatrixValues, boolean includeNonMatrix, boolean normalized ) throws IOException {

       // Should this be a newline (UNIX) or a carriage return & newline (Windows/DOS)?
      final String NEWLINE = "\r\n";

      BufferedWriter out = new BufferedWriter( new FileWriter( filename ) );
      
      boolean isStandardized = m_matrixDisplay.getStandardizedEnabled();
      m_matrixDisplay.setStandardizedEnabled( normalized );
      {
         int totalRowCount = m_table.getRowCount();
         int totalColumnCount = m_table.getColumnCount();
         int matrixColumnCount = m_matrixDisplay.getColumnCount();

         // write out column names
         if ( includeMatrixValues ) {
            
            for ( int c = 0; c < matrixColumnCount; c++ ) {
               String columnName = m_matrixDisplay.getColumnName( c );
               out.write( columnName + "\t" );
            }
            out.write( NEWLINE );
         }
         
         // write out the table, one row at a time
         for ( int r = 0; r < totalRowCount; r++ ) {

            if ( includeMatrixValues ) {

               // for this row: write out matrix values
               String probeID = getProbeID( r );
               double[] row = m_matrixDisplay.getRowByName( probeID );
               for ( int c = 0; c < row.length; c++ ) {
                  out.write( row[c] + "\t" );
               }
               //out.write( probeID + "\t" ); // DEBUG - REMOVE THIS!!!
               m_matrixDisplay.setStandardizedEnabled( isStandardized ); // return to previous state
            }

            if ( includeNonMatrix ) {
               // for this row: write out the rest of the table
               for ( int c = matrixColumnCount; c < totalColumnCount; c++) {
                  out.write( m_table.getValueAt( r, c ) + "\t" );
               }
            }
            out.write( NEWLINE );
         }
      }
      m_matrixDisplay.setStandardizedEnabled( isStandardized ); // return to previous state
      
      // close the file
      out.close();

   } // end saveData
   
   
   /**
    * Creates new row keys for the JMatrixDisplay object (m_matrixDisplay).
    *
    * You would probably want to call this method to print out the matrix in
    * the order in which it is displayed in the table.  In this case, you will
    * want to do something like this:<br><br>
    *
    *  <code>m_matrixDisplay.setRowKeys( getCurrentMatrixDisplayRowOrder() );</code>
    * 
    * However, do not forget to call <code>m_matrixDisplay.resetRowKeys()</code>
    * when you are done because the table sorter filter does its own mapping, 
    * so the matrix rows have to remain in their original order (or it might
    * not be displayed correctly inside the table).
    */
   protected int[] getCurrentMatrixDisplayRowOrder() {
   
      int matrixRowCount = m_matrixDisplay.getRowCount();
      int[] rowKeys = new int[matrixRowCount];

      // write out the table, one row at a time
      for ( int r = 0; r < matrixRowCount; r++ ) {
         // for this row: write out matrix values
         String probeID = getProbeID( r );
         rowKeys[r] = m_matrixDisplay.getRowIndexByName( probeID );
      }

      return rowKeys;
      
   } // end createRowKeys

   
   private String getProbeID( int row ) {
      int offset = m_matrixDisplay.getColumnCount(); // matrix display ends
      return (String) m_table.getValueAt( row, offset + 0 );
   }
   
   void m_normalizeMenuItem_actionPerformed( ActionEvent e ) {

      boolean normalize = m_normalizeMenuItem.isSelected();
      m_matrixDisplay.setStandardizedEnabled( normalize );

      initColorRangeWidget();
      m_table.repaint();
   }

   void m_matrixDisplayCellWidthSlider_stateChanged( ChangeEvent e ) {

      JSlider source = ( JSlider ) e.getSource();

      //if ( ! source.getValueIsAdjusting() ) {

      // Adjust the width of every matrix display column
      int width = ( int ) source.getValue();
      if ( width >= MIN_WIDTH_MATRIXDISPLAY_COLUMN && width <= MAX_WIDTH_MATRIXDISPLAY_COLUMN ) {

         m_table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

         int matrixColumnCount = m_matrixDisplay.getColumnCount();
         for ( int i = 0; i < matrixColumnCount; i++ ) {
            TableColumn col = m_table.getColumnModel().getColumn( i );
            col.setResizable( false );
            col.setPreferredWidth( width );
         }
      }
   }

   void m_colorRangeSlider_stateChanged( ChangeEvent e ) {

      JSlider source = ( JSlider ) e.getSource();
      double value = source.getValue();
         
      double displayMin, displayMax;
      boolean normalized = m_matrixDisplay.getStandardizedEnabled();
      if ( normalized ) {
         double rangeMax = NORMALIZED_COLOR_RANGE_MAX;
         double zoomFactor = COLOR_RANGE_SLIDER_RESOLUTION / rangeMax;
         double range = value / zoomFactor;
         displayMin = - ( range / 2 );
         displayMax = + ( range / 2 );
      }
      else {
         double rangeMax = m_matrixDisplay.getMax() - m_matrixDisplay.getMin();
         double zoomFactor = COLOR_RANGE_SLIDER_RESOLUTION / rangeMax;
         double range = value / zoomFactor;
         double midpoint = m_matrixDisplay.getMax() - ( rangeMax / 2 );
         displayMin = midpoint - ( range / 2 );
         displayMax = midpoint + ( range / 2 );
      }
      
      m_gradientBar.setLabels( displayMin, displayMax );
      m_matrixDisplay.setDisplayRange( displayMin, displayMax );
      m_table.repaint();
   }

   private void initColorRangeWidget() {
      
      // init the slider
      m_colorRangeSlider.setMinimum( 0 );
      m_colorRangeSlider.setMaximum( COLOR_RANGE_SLIDER_RESOLUTION );

      double rangeMax;
      boolean normalized =  m_matrixDisplay.getStandardizedEnabled();
      if ( normalized ) {
         rangeMax = NORMALIZED_COLOR_RANGE_MAX;
      }
      else {
         rangeMax = m_matrixDisplay.getMax() - m_matrixDisplay.getMin();
      }
      double zoomFactor = COLOR_RANGE_SLIDER_RESOLUTION / rangeMax;
      m_colorRangeSlider.setValue( ( int ) ( m_matrixDisplay.getDisplayRange() *
                                             zoomFactor ) );
      
      // init gradient bar
      double min = m_matrixDisplay.getDisplayMin();
      double max = m_matrixDisplay.getDisplayMax();
      m_gradientBar.setLabels( min, max );
   }   
   
} // end class JDetailsFrame

class JDetailsFrame_m_greenredColormapMenuItem_actionAdapter
    implements java.awt.event.ActionListener {
   JDetailsFrame adaptee;

   JDetailsFrame_m_greenredColormapMenuItem_actionAdapter( JDetailsFrame adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.m_greenredColormapMenuItem_actionPerformed( e );
   }
}

class JDetailsFrame_m_blackbodyColormapMenuItem_actionAdapter
    implements java.awt.event.ActionListener {
   JDetailsFrame adaptee;

   JDetailsFrame_m_blackbodyColormapMenuItem_actionAdapter( JDetailsFrame adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.m_blackbodyColormapMenuItem_actionPerformed( e );
   }
}

class JDetailsFrame_m_saveImageMenuItem_actionAdapter
    implements java.awt.event.ActionListener {
   JDetailsFrame adaptee;

   JDetailsFrame_m_saveImageMenuItem_actionAdapter( JDetailsFrame adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.m_saveImageMenuItem_actionPerformed( e );
   }
}

class JDetailsFrame_m_normalizeMenuItem_actionAdapter
    implements java.awt.event.ActionListener {
   JDetailsFrame adaptee;

   JDetailsFrame_m_normalizeMenuItem_actionAdapter( JDetailsFrame adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.m_normalizeMenuItem_actionPerformed( e );
   }
}

class JDetailsFrame_m_matrixDisplayCellWidthSlider_changeAdapter
    implements javax.swing.event.ChangeListener {
   JDetailsFrame adaptee;

   JDetailsFrame_m_matrixDisplayCellWidthSlider_changeAdapter( JDetailsFrame adaptee ) {
      this.adaptee = adaptee;
   }

   public void stateChanged( ChangeEvent e ) {
      adaptee.m_matrixDisplayCellWidthSlider_stateChanged( e );
   }
}

class JDetailsFrame_m_colorRangeSlider_changeAdapter
    implements javax.swing.event.ChangeListener {
   JDetailsFrame adaptee;

   JDetailsFrame_m_colorRangeSlider_changeAdapter( JDetailsFrame adaptee ) {
      this.adaptee = adaptee;
   }

   public void stateChanged( ChangeEvent e ) {
      adaptee.m_colorRangeSlider_stateChanged( e );
   }
}

class JDetailsFrame_m_saveDataMenuItem_actionAdapter implements java.awt.event.ActionListener {
   JDetailsFrame adaptee;

   JDetailsFrame_m_saveDataMenuItem_actionAdapter(JDetailsFrame adaptee) {
      this.adaptee = adaptee;
   }
   public void actionPerformed(ActionEvent e) {
      adaptee.m_saveDataMenuItem_actionPerformed(e);
   }
}
