package classScore.gui;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import baseCode.gui.Wizard;
import classScore.Settings;
import classScore.data.GONames;
import classScore.data.GeneAnnotations;
import classScore.data.NewGeneSet;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class AnalysisWizard extends Wizard
{
   //logic
   int step = 1;
   int analysisType = 1;

   Settings settings;
   GeneAnnotations geneData;
   GONames goData;
   AnalysisWizardStep1 step1;
   AnalysisWizardStep2 step2;
   AnalysisWizardStep3 step3;
   AnalysisWizardStep4 step4;
   AnalysisWizardStep5 step5;
   
   public AnalysisWizard(GeneSetScoreFrame callingframe, Map geneDataSets, GONames goData) {
      super(callingframe,550,350);
      //enableEvents(AWTEvent.WINDOW_EVENT_MASK);
      this.callingframe = callingframe;
      this.settings = new Settings(callingframe.getSettings()); //own copy of settings
      this.geneData = (GeneAnnotations)geneDataSets.get(new Integer("original".hashCode()));
      this.goData = goData;

      step1 = new AnalysisWizardStep1(this,settings);
      this.addStep(1,step1);
      step2 = new AnalysisWizardStep2(this,settings);
      this.addStep(2,step2);
      step3 = new AnalysisWizardStep3(this,settings,goData);
      this.addStep(3,step3);
      step4 = new AnalysisWizardStep4(this,settings);
      this.addStep(4,step4);
      step5 = new AnalysisWizardStep5(this,settings);
      this.addStep(5,step5);
      this.setTitle("Create New Analysis - Step 1 of 5");
   }

   protected void nextButton_actionPerformed(ActionEvent e) {
      clearStatus();
      if (step == 1) {
         step = 2;
         this.getContentPane().remove(step1);
         this.setTitle("Create New Analysis - Step 2 of 5");
         this.getContentPane().add(step2);
         step2.revalidate();
         backButton.setEnabled(true);
         this.repaint();
         nextButton.grabFocus();
      } else if (step == 2 && step2.isReady())
         {
            step = 3;
            this.getContentPane().remove(step2);
            this.setTitle("Create New Analysis - Step 3 of 5");
            this.getContentPane().add(step3);
            step3.revalidate();
            this.repaint();
         }
      else if (step == 3) {
         step = 4;
         this.getContentPane().remove(step3);
         this.setTitle("Create New Analysis - Step 4 of 5");
         this.getContentPane().add(step4);
         step4.revalidate();
         this.repaint();
      }
      else if (step == 4) {
         step = 5;
         this.getContentPane().remove(step4);
         step5.addVarPanel(analysisType);
         this.setTitle("Create New Analysis - Step 5 of 5");
         this.getContentPane().add(step5);
         step5.revalidate();
         nextButton.setEnabled(false);
         this.repaint();
      }
   }

   protected void backButton_actionPerformed(ActionEvent e) {
      clearStatus();
      if (step == 2) {
         step = 1;
         this.getContentPane().remove(step2);
         this.setTitle("Create New Analysis - Step 1 of 5");
         this.getContentPane().add(step1);
         step1.revalidate();
         backButton.setEnabled(false);
         this.repaint();
      } else if (step == 3) {
         step = 2;
         this.getContentPane().remove(step3);
         this.setTitle("Create New Analysis - Step 2 of 5");
         this.getContentPane().add(step2);
         step2.revalidate();
         this.repaint();
      } else if (step == 4) {
         step = 3;
         this.getContentPane().remove(step4);
         this.setTitle("Create New Analysis - Step 3 of 5");
         this.getContentPane().add(step3);
         step3.revalidate();
         this.repaint();
      } else if (step == 5) {
         step = 4;
         step5.removeVarPanel( analysisType );
         this.getContentPane().remove( step5 );
         this.setTitle( "Create New Analysis - Step 4 of 5" );
         this.getContentPane().add( step4 );
         step4.revalidate();
         nextButton.setEnabled( true );
         this.repaint();
      }
   }

   protected void cancelButton_actionPerformed(ActionEvent e) {
      dispose();
   }

   protected void finishButton_actionPerformed(ActionEvent e) {
      saveValues();
      loadAddedClasses();
      ((GeneSetScoreFrame)callingframe).startAnalysis(settings);
      dispose();
   }

   void saveValues(){
      step1.saveValues();
      step2.saveValues();
      step4.saveValues();
      step5.saveValues();
      try{
         settings.writePrefs();
      } catch (IOException ex) {
         System.err.println("Could not write prefs:" + ex);
         ex.printStackTrace();
      }
   }

   void loadAddedClasses()
   {
      Iterator it=step3.getAddedClasses().iterator();
      while(it.hasNext())
      {
         String id=(String)((HashMap)it.next()).get("id");
         if(!goData.newSet(id))
         {
            NewGeneSet newGeneSet=new NewGeneSet(geneData);
            String filename = NewGeneSet.getFileName(settings.getClassFolder(),id);
            newGeneSet.loadClassFile(filename);
            newGeneSet.addToMaps(goData);
         }
      }
   }

   public void setAnalysisType(int val) { this.analysisType = val; }
   public int getAnalysisType() { return this.analysisType; }


}
