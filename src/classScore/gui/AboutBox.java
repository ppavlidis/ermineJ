package classScore.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;

/**
 * Displays 'about' information for the software.
 * <p>
 * Copyright: Copyright (c) 2003-2004 Columbia University
 * </p>
 *
 * @author Paul Pavlidis
 * @version $Id$
 * @todo fix the layout.
 */

public class AboutBox extends JDialog implements ActionListener {

   private final static String VERSION = "2.0b2";
   private final static String COPYRIGHT = "Copyright (c) 2003-2004 Columbia University";
   private static final String SOFTWARENAME = "ermineJ";

   JPanel panel1 = new JPanel();
   JPanel panel2 = new JPanel();
   JPanel insetsPanel1 = new JPanel();
   JPanel insetsPanel3 = new JPanel();
   JButton button1 = new JButton();
   JLabel labelAuthors = new JLabel();
   JLabel imageLabel = new JLabel();
   JLabel label1 = new JLabel();
   JLabel label2 = new JLabel();
   JLabel label3 = new JLabel();
   ImageIcon image1;

   JTextPane jTextPane1 = new JTextPane();
   GridLayout gridLayout1 = new GridLayout();


   public AboutBox( Frame parent ) {
      super( parent );
      enableEvents( AWTEvent.WINDOW_EVENT_MASK );
      try {
         jbInit();
      } catch ( Exception e ) {
         e.printStackTrace();
      }
      setModal( true );
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension dlgSize = getPreferredSize();
      setLocation( ( screenSize.width - dlgSize.width ) / 2,
            ( screenSize.height - dlgSize.height ) / 2 );
      pack();
      show();
   }

   //Component initialization
   private void jbInit() throws Exception {


      this.getContentPane().setBackground(Color.white);
      this.setResizable(false);
      this.setTitle( "About " + SOFTWARENAME);

      panel1.setLayout( new BorderLayout() );
      panel2.setLayout( new BorderLayout() );
      panel1.setBackground(Color.white);
      panel1.setPreferredSize(new Dimension(350, 500) );
      panel1.setRequestFocusEnabled(true);
      panel1.setVerifyInputWhenFocusTarget(true);
      panel2.setBackground(Color.white);
      panel2.setAlignmentY((float) 0.5);
      panel2.setMinimumSize(new Dimension(491, 500));
      panel2.setPreferredSize(new Dimension(350, 400));

      imageLabel.setDebugGraphicsOptions(0);
      imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
      imageLabel.setHorizontalTextPosition(SwingConstants.CENTER);
      imageLabel.setIcon( new ImageIcon( GeneSetScoreFrame.class
            .getResource( "resources/logo1small.gif" ) ) );
      imageLabel.setIconTextGap(0);
    //  label1.setText( SOFTWARENAME );
      label2.setBackground(Color.white);
      label2.setFont(new java.awt.Font("Dialog", 1, 11));
      label2.setHorizontalAlignment(SwingConstants.CENTER);
      label2.setHorizontalTextPosition(SwingConstants.LEFT);
      label2.setText( "Version " + VERSION );
      label3.setHorizontalAlignment(SwingConstants.CENTER);
      label3.setText( COPYRIGHT );
      labelAuthors.setBackground(Color.red);
      labelAuthors.setOpaque(false);
      labelAuthors.setHorizontalAlignment(SwingConstants.CENTER);
      labelAuthors.setHorizontalTextPosition(SwingConstants.CENTER);
      labelAuthors.setText("Authors: Paul Pavlidis, Homin Lee and Will Braynen." );

      insetsPanel3.setLayout( gridLayout1 );
      gridLayout1.setRows(4 );
      gridLayout1.setVgap(0);
      insetsPanel3.setBackground(Color.white);
      insetsPanel3.setOpaque(true);
      insetsPanel3.setPreferredSize(new Dimension(350, 400));
      insetsPanel3.setRequestFocusEnabled(true);

      jTextPane1.setBackground(Color.white);
      jTextPane1.setAlignmentX((float) 0.5);
      jTextPane1.setMinimumSize(new Dimension(20, 100));
      jTextPane1.setPreferredSize(new Dimension(350, 200) );
      jTextPane1.setDisabledTextColor( Color.black );
      jTextPane1.setEditable( false );
      jTextPane1.setMargin(new Insets(10, 10, 10, 10));
      jTextPane1.setText("For license information , see LICENSE.txt. Direct questions to Paul " +
    " Pavlidis: pavlidis@dbmi.columbia.edu. If you use this software for your work, please cite Pavlidis, P., " +
    " Lewis, D.P., and Noble, W.S. (2002) Exploring gene expression data " +
    " with class scores.Proceedings of the Pacific Symposium on Biocomputing " +
    " 7. pp 474-485" );

      insetsPanel1.setBorder(BorderFactory.createEtchedBorder());
      insetsPanel3.add( label2, null );
      insetsPanel3.add(label3, null);
      insetsPanel3.add(labelAuthors, null);

      insetsPanel3.add(jTextPane1, null);

      button1.setText( "Ok" );
      button1.addActionListener( this );
      insetsPanel1.add( button1, null );
      panel1.add(imageLabel, BorderLayout.NORTH);

      panel2.add( insetsPanel3, BorderLayout.NORTH );

      panel1.add( panel2, BorderLayout.CENTER );
      panel1.add( insetsPanel1, BorderLayout.SOUTH );

      this.getContentPane().add( panel1, BorderLayout.CENTER );


   }

   //Overridden so we can exit when window is closed
   protected void processWindowEvent( WindowEvent e ) {
      if ( e.getID() == WindowEvent.WINDOW_CLOSING ) {
         cancel();
      }
      super.processWindowEvent( e );
   }

   //Close the dialog
   void cancel() {
      dispose();
   }

   //Close the dialog on a button event
   public void actionPerformed( ActionEvent e ) {
      if ( e.getSource() == button1 ) {
         cancel();
      }
   }

}
