package classScore;

import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 * @todo make columns start out better sizes
 * @todo integers don't sort correctly
 */

public class OutputPanel extends JScrollPane {
   JTable table;
   JPanel labelPanel;
   OutputTableModel model;
   Vector results;

   public OutputPanel(Vector results) {
      this.results=results;
      model = new OutputTableModel(results);
      labelPanel= new JPanel();
      JLabel label= new JLabel("Please wait while the files are loaded in.");
      label.setPreferredSize(new Dimension(250,250));
      labelPanel.add(label,BorderLayout.CENTER);
      this.getViewport().add(labelPanel);
      table = new JTable();
      table.addMouseListener( new OutputPanel_mouseAdapter( this ) );
   }

   // public void addClassDetailsListener(ClassDetailsEventListener listener) {
   //   listenerList.add(ClassDetailsEventListener.class, listener);
   // }

   public void addInitialData(GeneAnnotations geneData, GONames goData) {
      this.getViewport().remove(labelPanel);
      model.addInitialData(geneData, goData);
      TableSorter sorter = new TableSorter(model);
      table.setModel(sorter);
      sorter.setTableHeader(table.getTableHeader());
      this.getViewport().add(table, null);
      table.getColumnModel().getColumn(0).setPreferredWidth(70);
      table.getColumnModel().getColumn(2).setPreferredWidth(50);
      table.getColumnModel().getColumn(3).setPreferredWidth(50);
      table.revalidate();
   }

   public void addRunData(Map data) {
      model.addRunData(data);
      table.addColumn(new TableColumn(model.getColumnCount() - 3));
      table.addColumn(new TableColumn(model.getColumnCount() - 2));
      table.addColumn(new TableColumn(model.getColumnCount() - 1));
      table.getColumnModel().getColumn(model.getColumnCount() - 3).setPreferredWidth(30);
      table.getColumnModel().getColumn(model.getColumnCount() - 2).setPreferredWidth(30);
      table.getColumnModel().getColumn(model.getColumnCount() - 1).setPreferredWidth(30);
   }

   public void addRun() {
      model.addRun();
      table.addColumn(new TableColumn(model.getColumnCount() - 3));
      table.addColumn(new TableColumn(model.getColumnCount() - 2));
      table.addColumn(new TableColumn(model.getColumnCount() - 1));
      table.getColumnModel().getColumn(model.getColumnCount() - 3).setPreferredWidth(30);
      table.getColumnModel().getColumn(model.getColumnCount() - 2).setPreferredWidth(30);
      table.getColumnModel().getColumn(model.getColumnCount() - 1).setPreferredWidth(30);
      table.setDefaultRenderer(Object.class,new OutputPanelTableCellRenderer());
      table.revalidate();
   }

   void table_mouseReleased( MouseEvent e ) {
      int i = table.getSelectedRow();
      int j = table.getSelectedColumn();
      if(!table.getValueAt(i,j).equals("") && j>=OutputTableModel.init_cols)
      {
         int runnum=(int)Math.floor((j - OutputTableModel.init_cols) / OutputTableModel.cols_per_run);
         String id = (String)table.getValueAt(i,0);
         ((classPvalRun)results.get(runnum)).showDetails(id);
      }
   }
}

class OutputPanel_mouseAdapter
    extends java.awt.event.MouseAdapter {
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

class OutputTableModel extends AbstractTableModel {
   GeneAnnotations geneData;
   GONames goData;
   Vector results;
   Vector columnNames = new Vector();
   private NumberFormat nf = NumberFormat.getInstance();
   int state = -1;
   public static final int init_cols = 4;
   public static final int cols_per_run = 3;

   public OutputTableModel(Vector results) {
      this.results=results;
      nf.setMaximumFractionDigits(8);
      columnNames.add("Name");
      columnNames.add("Description");
      columnNames.add("# of Probes");
      columnNames.add("# of Genes");
   }

   public void addInitialData(GeneAnnotations geneData, GONames goData) {
      state = 0;
      this.geneData = geneData;
      this.goData = goData;
   }

   public void addRunData(Map result) {
      state++;
      columnNames.add("Run " + state + " Rank");
      columnNames.add("Run " + state + " Score");
      columnNames.add("Run " + state + " Pval");
      results.add(result);
   }

   public void addRun() {
      state++;
      columnNames.add("Run " + state + " Rank");
      columnNames.add("Run " + state + " Score");
      columnNames.add("Run " + state + " Pval");
   }

   public String getColumnName(int i) {return (String) columnNames.get(i);
   }

   public int getColumnCount() {return columnNames.size();
   }

   public int getRowCount() {
      if (state == -1) {
         return 20;
      } else {
         return geneData.numClasses();
      }
   }

   public Object getValueAt(int i, int j) {
      if (state >= 0 && j < init_cols) {
         String classid;
            classid = geneData.getClass(i);
         switch (j) {
         case 0:
            return classid;
         case 1:
               return goData.getNameForId(classid);
         case 2:
               return new Integer(geneData.numProbes(classid));
         case 3:
               return new Integer(geneData.numGenes(classid));
         }
      } else if (state > 0) {
         String classid;
            classid = geneData.getClass(i);
         double runnum = Math.floor((j - init_cols) / cols_per_run);
         Map data = ((classPvalRun)results.get((int) runnum)).getResults();
         if (data.containsKey(classid)) {
            classresult res = (classresult) data.get(classid);
            if ((j - init_cols) % cols_per_run == 0) {
               return new Integer(res.getRank());
            } else if ((j - init_cols) % cols_per_run == 1) {
               return new Double(nf.format(res.getScore()));
            } else {
               return new Double(nf.format(res.getPvalue()));
            }
         } else {
            return "";
         }
      }
      return "";
   }
}


class OutputPanelTableCellRenderer extends DefaultTableCellRenderer
{
   static Color spread1 = new Color(220,220,160);
   static Color spread2 = new Color(205,222,180);
   static Color spread3 = new Color(190,224,200);
   static Color spread4 = new Color(175,226,220);
   static Color spread5 = new Color(160,228,240);

   public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                  boolean hasFocus, int row, int column)
   {
      super.getTableCellRendererComponent(table, value, isSelected,
                                          hasFocus, row, column);
      int runcol = column - OutputTableModel.init_cols;
      setOpaque(true);
      if(runcol % OutputTableModel.cols_per_run == 2 && value.getClass().equals(Double.class))
      {
         if(((Double)value).doubleValue() > 0.8)
            setBackground(spread1);
         else if(((Double)value).doubleValue() > 0.6)
            setBackground(spread2);
         else if(((Double)value).doubleValue() > 0.4)
            setBackground(spread3);
         else if(((Double)value).doubleValue() > 0.2)
            setBackground(spread4);
         else
            setBackground(spread5);
      }
      else
      {
          setOpaque(false);
      }
//      {
 //        if(((Double)value).doubleValue() > 0.5)
 //           panel.setBackground(new Color(220,220,160));
 //        else
 //           panel.setBackground(new Color(160,230,240));
//      }
      return this;
   }

}

