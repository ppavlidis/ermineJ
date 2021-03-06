/*
 * The baseCode project
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
package ubic.erminej.gui.util;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * Abstract WizardStep class.
 * </p>
 *
 * @author Homin Lee
 * @author pavlidis
 * @version $Id$
 */
public abstract class WizardStep extends JPanel {

    private static final long serialVersionUID = 1L;

    /** Constant <code>log</code> */
    protected static Log log = LogFactory.getLog( WizardStep.class.getName() );
    private Wizard owner;

    /**
     * <p>
     * Constructor for WizardStep.
     * </p>
     *
     * @param wiz a {@link ubic.erminej.gui.util.Wizard} object.
     */
    public WizardStep( Wizard wiz ) {
        super();
        owner = wiz;
        try {
            this.setLayout( new BorderLayout() ); // needed...
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     * <p>
     * Getter for the field <code>owner</code>.
     * </p>
     *
     * @return a {@link ubic.erminej.gui.util.Wizard} object.
     */
    public Wizard getOwner() {
        return owner;
    }

    /**
     * <p>
     * isReady.
     * </p>
     *
     * @return a boolean.
     */
    abstract public boolean isReady();

    /**
     * Print an error message to the status bar.
     *
     * @param a error message to show.
     */
    public void showError( String a ) {
        owner.showError( a );
    }

    /**
     * Print a message to the status bar.
     *
     * @param a message to show.
     */
    public void showStatus( String a ) {
        owner.showStatus( a );
    }

    /**
     * <p>
     * addHelp.
     * </p>
     *
     * @param text a {@link java.lang.String} object.
     */
    protected void addHelp( String text ) {
        JLabel label = new JLabel( text );
        label.setOpaque( true );
        label.setBackground( Color.WHITE );
        label.setBorder( BorderFactory.createEmptyBorder( 15, 34, 23, 15 ) );

        this.add( label, BorderLayout.NORTH );
    }

    /**
     * <p>
     * addMain.
     * </p>
     *
     * @param panel a {@link javax.swing.JPanel} object.
     */
    protected void addMain( JPanel panel ) {
        this.add( panel, BorderLayout.CENTER );
    }

    // Component initialization
    /**
     * <p>
     * jbInit.
     * </p>
     *
     * @throws java.lang.Exception if any.
     */
    protected abstract void jbInit() throws Exception;

}
