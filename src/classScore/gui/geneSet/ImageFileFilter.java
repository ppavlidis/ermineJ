package classScore.gui.geneSet;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import baseCode.graphics.text.Util;

/**
 * <hr>
 * <p>
 * Copyright (c) 2004 Columbia University
 * 
 * @author  Will Braynen
 * @version $Id$
 */
public class ImageFileFilter
    extends FileFilter {

   public boolean accept( File f ) {

      if ( f.isDirectory() ) {
         return true;
      }

      return Util.hasImageExtension( f.getName() );

   } // end accept

   public String getDescription() {

      return "PNG or GIF images";
   }
}
