package classScore.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.Map;
import java.awt.Cursor;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import baseCode.gui.table.TableSorter;

import classScore.Settings;
import classScore.classPvalRun;
import classScore.data.GONames;
import classScore.data.GeneAnnotations;
import classScore.data.GeneSetResult;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 * @todo make columns start out better sizes
 * @todo integers don't sort correctly
 * @todo deletion of geneDataSets when remove is used.
 */

public class OutputPanel extends JScrollPane {
   /**
    * Copyright (c) 2004 Columbia University
    * @author Owner
    * @version $Id$
    */
   
   JTable table;
   OutputTableModel model;
   TableSorter sorter;
   GeneSetScoreFrame callingframe;
   LinkedList results;
   LinkedList resultToolTips = new LinkedList();
   GeneAnnotations geneData;
   GONames goData;

   public OutputPanel( GeneSetScoreFrame callingframe, LinkedList results ) {
      this.callingframe = callingframe;
      this.results = results;
      model = new OutputTableModel( results );
      table = new JTable() {
         //Implement table header tool tips.
         protected JTableHeader createDefaultTableHeader() {
            return new JTableHeader( columnModel ) {
               public String getToolTipText( MouseEvent e ) {
                  String tip = null;
                  java.awt.Point p = e.getPoint();
                  int index = columnModel.getColumnIndexAtX( p.x );
                  int realIndex = columnModel.getColumn( index )
                        .getModelIndex();
                  return getHeaderToolTip( realIndex );
               }
            };
         }
      };
      table.addMouseListener( new OutputPanel_mouseAdapter( this ) );
      table.getTableHeader().setReorderingAllowed( false );
      table.getTableHeader().addMouseListener( new TableHeader_mouseAdapterCursorChanger( this ));
      
      OutputPanelPopupMenu popup = new OutputPanelPopupMenu();
      JMenuItem menuItem = new JMenuItem( "Modify this class (Step 1 of 3)" );
      menuItem
            .addActionListener( new OutputPanel_PopupMenu_actionAdapter( this ) );
      popup.add( menuItem );
      MouseListener popupListener = new OutputPanel_PopupListener( popup );
      table.addMouseListener( popupListener );

      RemoveRunPopupMenu removeRunPopup = new RemoveRunPopupMenu();
      JMenuItem removeRunMenuItem = new JMenuItem( "Remove this run..." );
      removeRunMenuItem
            .addActionListener( new OutputPanel_removeRunPopupMenu_actionAdapter(
                  this ) );
      removeRunPopup.add( removeRunMenuItem );
      MouseListener removeRunPopupListener = new OutputPanel_removeRunPopupListener(
            removeRunPopup );
      table.getTableHeader().addMouseListener( removeRunPopupListener );
   }

   void table_mouseReleased( MouseEvent e ) {
      int i = table.getSelectedRow();
      int j = table.getSelectedColumn();
      if ( table.getValueAt( i, j ) != null && j >= OutputTableModel.init_cols ) {
         int runnum = model.getRunNum( j );
         String id = ( String ) table.getValueAt( i, 0 );
         ( ( classPvalRun ) results.get( runnum ) ).showDetails( id );
      } else {
         for ( int k = model.getColumnCount() - 1; k >= 0; k-- ) {
            if ( table.getValueAt( i, k ) != null
                  && k >= OutputTableModel.init_cols ) {
               String message;
               String id = ( String ) table.getValueAt( i, 0 );
               String shownRunName = model.getColumnName( k );
               shownRunName = shownRunName.substring( 0,
                     shownRunName.length() - 5 );
               if ( j >= OutputTableModel.init_cols ) {
                  message = model.getColumnName( j );
                  message = message.substring( 0, message.length() - 5 );
                  message = message + " doesn't include the class " + id
                        + ". Showing " + shownRunName + " instead.";
               } else {
                  message = "Showing details for " + shownRunName + ".";
               }
               JOptionPane.showMessageDialog( this, message, "FYI",
                     JOptionPane.PLAIN_MESSAGE );
               int runnum = model.getRunNum( k );
               ( ( classPvalRun ) results.get( runnum ) ).showDetails( id );
            }
         }

      }
   }

   void popupMenu_actionPerformed( ActionEvent e ) {
      OutputPanelPopupMenu sourcePopup = ( OutputPanelPopupMenu ) ( ( Container ) e
            .getSource() ).getParent();
      int r = table.rowAtPoint( sourcePopup.getPoint() );
      String id = ( String ) table.getValueAt( r, 0 );
      GeneSetWizard cwiz = new GeneSetWizard( callingframe, geneData, goData,
            id );
      cwiz.showWizard();
   }

   void removeRunPopupMenu_actionPerformed( ActionEvent e ) {
      RemoveRunPopupMenu sourcePopup = ( RemoveRunPopupMenu ) ( ( Container ) e
            .getSource() ).getParent();
      int c = table.getTableHeader().columnAtPoint( sourcePopup.getPoint() );
      String colname = model.getColumnName( c );
      TableColumn col = table.getColumn( colname );
      int yesno = JOptionPane.showConfirmDialog( null,
            "Are you sure you want to remove "
                  + colname.substring( 0, colname.length() - 5 ) + "?",
            "Remove Run", JOptionPane.YES_NO_OPTION );
      if ( yesno == JOptionPane.YES_OPTION ) {
         System.err.println( "remove popup for col: " + c );
         table.removeColumn( col );
         model.removeRunData( c );
         model.fireTableStructureChanged();
         int runnum = model.getRunNum( c );
         results.remove( runnum );
         resultToolTips.remove( runnum );
         table.revalidate();
      }
   }

   String getHeaderToolTip( int index ) {
      if ( index >= OutputTableModel.init_cols ) {
         //int runnum=(int)Math.floor((index - OutputTableModel.init_cols) / OutputTableModel.cols_per_run);
         int runnum = model.getRunNum( index );
         return ( String ) resultToolTips.get( runnum );
      }

      return null;
   }

   void generateToolTip( int runnum ) {
      Settings runSettings = ( ( classPvalRun ) results.get( runnum ) )
            .getSettings();
      String tooltip = new String( "<html>" );
      String coda = new String();
      if ( runSettings.getAnalysisMethod() == Settings.ORA ) {
         tooltip += "ORA Analysis<br>";
         coda += "P value threshold: " + runSettings.getPValThreshold();
      } else if ( runSettings.getAnalysisMethod() == Settings.RESAMP ) {
         tooltip += "Resampling Analysis<br>";
         coda += runSettings.getIterations() + " iterations<br>";
         coda += "Using score column: " + runSettings.getScorecol();
      } else if ( runSettings.getAnalysisMethod() == Settings.CORR ) {
         tooltip += "Correlation Analysis<br>";
         coda += runSettings.getIterations() + " iterations";
      }
      tooltip += new String( "Max set size: " + runSettings.getMaxClassSize()
            + "<br>" + "Min set size: " + runSettings.getMinClassSize()
            + "<br>" );
      if ( runSettings.getDoLog() ) tooltip += "Log normalized<br>";
      if ( runSettings.getGeneRepTreatment() == Settings.MEAN_PVAL )
         tooltip += "Gene Rep Treatment: Mean <br>";
      else if ( runSettings.getGeneRepTreatment() == Settings.BEST_PVAL )
            tooltip += "Gene Rep Treatment: Best <br>";
      if ( runSettings.getRawScoreMethod() == Settings.MEAN_METHOD )
         tooltip += "Class Raw Score Method: Mean <br>";
      else if ( runSettings.getRawScoreMethod() == Settings.QUANTILE_METHOD )
            tooltip += "Class Raw Score Method: Median <br>";
      tooltip += coda;
      resultToolTips.add( runnum, tooltip );
   }

   public void addInitialData( GeneAnnotations geneData, GONames goData ) {
      this.geneData = geneData;
      this.goData = goData;
      model.addInitialData( geneData, goData );
      sorter = new TableSorter( model );
      table.setModel( sorter );
      sorter.setTableHeader( table.getTableHeader() );
      this.getViewport().add( table, null );
      table.getColumnModel().getColumn( 0 ).setPreferredWidth( 80 );
      table.getColumnModel().getColumn( 1 ).setPreferredWidth( 300 );
      table.getColumnModel().getColumn( 2 ).setPreferredWidth( 80 );
      table.getColumnModel().getColumn( 3 ).setPreferredWidth( 80 );
      table.setDefaultRenderer( Object.class, new OutputPanelTableCellRenderer(
            goData, results ) );
      table.revalidate();
   }

   public void addRun() {
      model.addRun();
      int c = model.getColumnCount() - 1;
      TableColumn col = new TableColumn( c );
      col.setIdentifier( model.getColumnName( c ) );
      table.addColumn( col );
      table.getColumnModel().getColumn( c ).setPreferredWidth( 80 );
      generateToolTip( model.getColumnCount() - OutputTableModel.init_cols - 1 );
      sorter.cancelSorting();
      sorter.setSortingStatus( c, TableSorter.ASCENDING );
      if ( results.size() > 4 )
            table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
      table.revalidate();
   }
}

class OutputPanel_mouseAdapter extends java.awt.event.MouseAdapter {
   OutputPanel adaptee;

   OutputPanel_mouseAdapter( OutputPanel adaptee ) {
      this.adaptee = adaptee;
   }

   public void mouseReleased( MouseEvent e ) {
      if ( e.getClickCount() < 2 ) {
         return;
      }
      adaptee.table_mouseReleased( e );
   }
}


class TableHeader_mouseAdapterCursorChanger extends java.awt.event.MouseAdapter  {
   OutputPanel adaptee;

   TableHeader_mouseAdapterCursorChanger( OutputPanel adaptee ) {
      this.adaptee = adaptee;
   }

   public void mouseEntered( MouseEvent e ) {
      Container c = adaptee.getParent();  
      c.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
   }

   public void mouseExited( MouseEvent e ) {
      Container c = adaptee.getParent();  
      c.setCursor(Cursor.getDefaultCursor());
   }

}



class OutputPanel_PopupMenu_actionAdapter implements
      java.awt.event.ActionListener {
   OutputPanel adaptee;

   OutputPanel_PopupMenu_actionAdapter( OutputPanel adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.popupMenu_actionPerformed( e );
   }
}

class OutputPanelPopupMenu extends JPopupMenu {
   Point popupPoint;

   public Point getPoint() {
      return popupPoint;
   }

   public void setPoint( Point point ) {
      popupPoint = point;
   }
}

class OutputPanel_PopupListener extends MouseAdapter {
   OutputPanelPopupMenu popup;

   OutputPanel_PopupListener( OutputPanelPopupMenu popupMenu ) {
      popup = popupMenu;
   }

   public void mousePressed( MouseEvent e ) {
      maybeShowPopup( e );
   }

   public void mouseReleased( MouseEvent e ) {
      maybeShowPopup( e );
   }

   private void maybeShowPopup( MouseEvent e ) {
      if ( e.isPopupTrigger() ) {
         JTable source = ( JTable ) e.getSource();
         int r = source.rowAtPoint( e.getPoint() );
         String id = ( String ) source.getValueAt( r, 0 );
         if ( id != null ) {
            popup.show( e.getComponent(), e.getX(), e.getY() );
            popup.setPoint( e.getPoint() );
         }
      }
   }
}

class OutputPanel_removeRunPopupMenu_actionAdapter implements
      java.awt.event.ActionListener {
   OutputPanel adaptee;

   OutputPanel_removeRunPopupMenu_actionAdapter( OutputPanel adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.removeRunPopupMenu_actionPerformed( e );
   }
}

class RemoveRunPopupMenu extends JPopupMenu {
   Point popupPoint;

   public Point getPoint() {
      return popupPoint;
   }

   public void setPoint( Point point ) {
      popupPoint = point;
   }
}

class OutputPanel_removeRunPopupListener extends MouseAdapter {
   RemoveRunPopupMenu popup;

   OutputPanel_removeRunPopupListener( RemoveRunPopupMenu popupMenu ) {
      popup = popupMenu;
   }

   public void mousePressed( MouseEvent e ) {
      maybeShowPopup( e );
   }

   public void mouseReleased( MouseEvent e ) {
      maybeShowPopup( e );
   }

   private void maybeShowPopup( MouseEvent e ) {
      if ( e.isPopupTrigger() ) {
         JTableHeader source = ( JTableHeader ) e.getSource();
         int c = source.columnAtPoint( e.getPoint() );
         if ( c >= OutputTableModel.init_cols ) {
            popup.show( e.getComponent(), e.getX(), e.getY() );
            popup.setPoint( e.getPoint() );
         }
      }
   }
}

class OutputTableModel extends AbstractTableModel {
   GeneAnnotations geneData;
   GONames goData;
   LinkedList results;
   LinkedList columnNames = new LinkedList();
   private NumberFormat nf = NumberFormat.getInstance();
   int state = -1;
   public static final int init_cols = 4;

   //public static final int cols_per_run = 3;

   public OutputTableModel( LinkedList results ) {
      this.results = results;
      nf.setMaximumFractionDigits( 2 );
      columnNames.add( "Name" );
      columnNames.add( "Description" );
      columnNames.add( "# of Probes" );
      columnNames.add( "# of Genes" );
   }

   public void addInitialData( GeneAnnotations geneData, GONames goData ) {
      state = 0;
      this.geneData = geneData;
      this.goData = goData;
   }

   public void addRunData( Map result ) {
      state++;
      columnNames.add( "Run " + state + " Pval" );
      results.add( result );
   }

   public void removeRunData( int c ) {
      columnNames.remove( c );
      System.err.println( "number of cols: " + columnNames.size() );
   }

   public void addRun() {
      state++;
      columnNames.add( "Run " + state + " Pval" );
   }

   public String getColumnName( int i ) {
      return ( String ) columnNames.get( i );
   }

   public int getColumnCount() {
      return columnNames.size();
   }

   public int getRowCount() {
      if ( state == -1 ) {
         return 20;
      }
      return geneData.numClasses();
   }

   public int getRunNum( int c ) {
      return c - init_cols;
   }

   public Object getValueAt( int i, int j ) {
      String classid = geneData.getClass( i );
      if ( state >= 0 && j < init_cols ) {
         switch ( j ) {
            case 0:
               return classid;
            case 1:
               return goData.getNameForId( classid );
            case 2:
               return new Integer( geneData.numProbes( classid ) );
            case 3:
               return new Integer( geneData.numGenes( classid ) );
         }
      } else if ( state > 0 ) {
         int runnum = getRunNum( j );
         Map data = ( ( classPvalRun ) results.get( runnum ) ).getResults();
         if ( data.containsKey( classid ) ) {
            GeneSetResult res = ( GeneSetResult ) data.get( classid );
            return new Double( nf.format( res.getPvalue() ) );
         }
         return null;
      }
      return null;
   }
}

class OutputPanelTableCellRenderer extends DefaultTableCellRenderer {
   GONames goData;
   LinkedList results;
   
   static Color goParent = Color.LIGHT_GRAY;
   static Color goChild = Color.YELLOW;
   static Color spread1 = new Color( 220, 220, 160 );
   static Color spread2 = new Color( 205, 222, 180 );
   static Color spread3 = new Color( 190, 224, 200 );
   static Color spread4 = new Color( 175, 226, 220 );
   static Color spread5 = new Color( 160, 228, 240 );
   static Color modified = new Color( 220, 160, 220 );
   private NumberFormat nf = NumberFormat.getInstance();

   public OutputPanelTableCellRenderer( GONames goData, LinkedList results ) {
      super();
      this.goData = goData;
      this.results = results;
      nf.setMaximumFractionDigits( 5 );
   }

   public Component getTableCellRendererComponent( JTable table, Object value,
         boolean isSelected, boolean hasFocus, int row, int column ) {
      super.getTableCellRendererComponent( table, value, isSelected, hasFocus,
            row, column );
      int runcol = column - OutputTableModel.init_cols;
      setOpaque( true );
      if ( isSelected || hasFocus )
         setOpaque( true );
      else if ( value == null )
         setOpaque( false );
      else if ( column == 0 && goData.newSet( ( String ) value ) ) {
         setBackground( modified );
      } else if ( value.getClass().equals( Double.class ) )
      //else if(runcol % OutputTableModel.cols_per_run == 2 && value.getClass().equals(Double.class))
      {
         String classid = ( String ) table.getValueAt( row, 0 );
         classPvalRun result = ( classPvalRun ) results.get( runcol );
         Map data = result.getResults();
         if ( data.containsKey( classid ) ) {
            GeneSetResult res = ( GeneSetResult ) data.get( classid );
            setToolTipText( "<html>Rank: " + res.getRank() + "<br>Score: " +

            nf.format( res.getScore() ) );

            if ( res.getPvalue_corr() == 1 )
               setBackground( spread5 );
            else if ( res.getPvalue_corr() == 0 ) setBackground( spread1 );
         }
      } else
         setOpaque( false );
      return this;
   }

}

