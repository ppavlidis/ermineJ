package classScore;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

/**
  Description:Parses the file of the form
  <pre>probe_id[tab]pval</pre>
  <p>The values are stored in a HashTable probe_pval_map. This is used to
  see what probes are int the data set, as well as the score for each
  probe.   Created :09/02/02</p>
  @author Shahmil Merchant, Paul Pavlidis
  @version $Id$
 */
public class GeneScoreReader {

   private String[] probeID = null;
   private double[] pval = null;
   private int num_pvals;
   private static Map probe_pval_map;
   private double log10 = Math.log(10);

   /**
     Create the probe -> pval mapping
     @param filename: a tab-delmited file with columns probe_id pval
    */
   public GeneScoreReader(String filename) throws IOException {
      this(filename, 1, true);
   }

   /**
     Create the probe -> pval mapping
    @param filename: a tab-delmited file with columns probe_id pval pval pval...
     @param column: which column the pvalues are in.
     @param dolog: take the log (base 10) of the value.
    */
   public GeneScoreReader(String filename, int column, boolean dolog) throws
           IOException {
      //read in file

      File infile = new File(filename);
      if (!infile.exists() || !infile.canRead()) {
         System.err.println("Could not read " + filename);
      }

      if (column < 1) {
         System.err.println("Illegal column number " + column +
                            ", must be greater or equal to 1");
      } else {
         System.err.println("Reading gene scores from column " + column);
      }

      FileInputStream fis = new FileInputStream(filename);
      BufferedInputStream bis = new BufferedInputStream(fis);
      BufferedReader dis = new BufferedReader(new InputStreamReader(bis));
      Double[] doubleArray = null;
      String row;
      String col;
      Vector rows = new Vector();
      Vector cols = null;
      probe_pval_map = new LinkedHashMap();

      while ((row = dis.readLine()) != null) {
         StringTokenizer st = new StringTokenizer(row, "\t");
         cols = new Vector();
         while (st.hasMoreTokens()) {
            cols.add(st.nextToken());
         }
         rows.add(cols);
      }

      dis.close();
      probeID = new String[rows.size() - 1];
      pval = new double[rows.size() - 1];
      doubleArray = new Double[rows.size() - 1];

      double small = 10e-16;

      for (int i = 1; i < rows.size(); i++) {

         if (((Vector) (rows.elementAt(i))).size() < column) {
            throw new IOException("Insufficient columns in row " + i +
                                  ", expecting file to have at least " + column +
                                  " columns.");
         }

         String name = (String) (((Vector) (rows.elementAt(i))).elementAt(0));

         if (name.matches("AFFX.*")) { // todo: put this rule somewhere else
            System.err.println("Skipping probe in pval file: " + name);
            continue;
         }
         probeID[i - 1] = name;

         pval[i - 1] =
                 Double.parseDouble((String) (((Vector) (rows.elementAt(i))).
                                              elementAt(column - 1)));

         // Fudge when pvalues are zero.
         if (dolog && pval[i - 1] <= 0) {
            System.err.println(
                    "Warning: Cannot take log of non-positive value for " +
                    name +
                    " (" + pval[i - 1] + ") from gene score file: Setting to " +
                    small);
            pval[i - 1] = small;
         }

         if (dolog) {
            pval[i - 1] = -(Math.log(pval[i - 1]) / log10); // Make -log base 10.
         }

         doubleArray[i - 1] = new Double(pval[i - 1]);
         probe_pval_map.put(probeID[i - 1], doubleArray[i - 1]); // put key, value.
      }

      num_pvals = Array.getLength(pval);

      if (num_pvals <= 0) {
         System.err.println("No pvalues found in the file!");
         System.exit(1);
      } else {
         System.err.println("Found " + num_pvals + " pvals in the file");
      }

   } //

   /**
    */
   public String[] get_probe_ids() {
      return probeID;
   }

   /**
    */
   public double[] get_pval() {
      return pval;
   }

   /**
    */
   public int get_numpvals() {
      return num_pvals;
   }

   /**
    */
   public Map get_map() {
      return probe_pval_map;
   }

   /**
    */
   public double get_value_map(String probe_id) {
      double value = 0.0;

      if (probe_pval_map.get(probe_id) != null) {
         value = Double.parseDouble((probe_pval_map.get(probe_id)).toString());
      }

      return value;
   }

   /**
     Main
    */
   public static void main(String[] args) {
      try {
         GeneScoreReader t = new GeneScoreReader(args[0]);
      } catch (IOException e) {
         e.printStackTrace();
      }

   }

} // end of class
