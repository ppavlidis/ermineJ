package classScore;

import java.util.*;
import javax.swing.event.*;
import javax.swing.table.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class SortFilterModel
    extends AbstractTableModel {
    private TableModel model;
    private int sortColumn;
    private Row[] rows;

    public SortFilterModel(TableModel m) {
	model = m;
	rows = new Row[model.getRowCount()];
      
	// Initially, underlying row order is the same as displayed row order
	for (int i = 0; i < rows.length; i++) {
	    rows[i] = new Row();
	    rows[i].index = i;
	}
    }

    public void sort(int c) {
	sortColumn = c;
	Arrays.sort(rows);
	//     fireTableDataChanged();
    }

    /** 
     * Translate from the row order that is displayed
     * to the underlying row order.
     */
    public Object getValueAt(int r, int c) {
	return model.getValueAt(rows[r].index, c);
    }

    public int getRowCount() {
	return model.getRowCount();
    }

    public int getColumnCount() {
	return model.getColumnCount();
    }

    public String getColumnName(int c) {
	return model.getColumnName(c);
    }

   private class Row
       implements Comparable {
       public int index;
       public int compareTo(Object other) {
	   Row otherRow = (Row) other;
	   Object a = model.getValueAt(index, sortColumn);
	   Object b = model.getValueAt(otherRow.index, sortColumn);
	   if (a instanceof Comparable) {
	       return ( (Comparable) a).compareTo(b);
	   } else {
	       return a.toString().compareTo(b.toString());
	   }
       }
   }
}
