package uk.co.bedroomcoders.fileDialog;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.Input.Keys;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.utils.Align;

import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;

/**
 *  fileDialog class an extension of Dialog to provide
 *  a rudimentary file selector
 *
 * @author codifies
 */
public class fileDialog extends Dialog {

    private String chosen;
    private Table fileTab;
    private ScrollPane sPane;
    private TextField tf;
    private Skin skin;
    private String path;
    /** The ok button is left public so that external listeners can
     *  conveniently check which button was pressed */
    public TextButton ok;
    /** The cancel button is left public so that external listeners can
     *  conveniently check which button was pressed */
    public TextButton cancel;

    /** constructs a fileDialog object, once created you need to
    *   add it to your scene and add your own listener
    * 
    * @param title			A title for the dialog title bar
    * @param p		    	The initial file path for the dialog 
    * @param st			    The stage you intend to add the fileDialog to, only used for positioning
    * @param s			    The skin to use
    */
    public fileDialog(String title , String p, Stage st, Skin s) {
        super(title, s);
        skin = s;
        path = p;

        chosen = "<selecting>";

        setSize(240,200);
        fileTab = new Table();
        sPane = new ScrollPane(fileTab,skin);
        sPane.setScrollingDisabled(true,false);
        tf = new TextField("",skin);
        cd(path);
        getContentTable().add(sPane).expand().fill();
        getContentTable().row();
        getContentTable().add(tf);

        ok = new TextButton("Ok", skin);
        button(ok, true);
        
        cancel = new TextButton("Cancel", skin);
        button(cancel, false);
        key(Keys.ENTER, true);
        key(Keys.ESCAPE, false);

        setPosition((st.getWidth()/2)-(getWidth()/2),(st.getHeight()/2)-(getHeight()/2));
        
    }

    // used by the dialog to change directory when a user click a directory
    private void cd(String p) {
        tf.setText("");
        path = p;
        fileTab.clearChildren();
        FileHandle dirHandle = Gdx.files.internal(path);

        addEntry("..",true);
        // do the loop twice grabbing all folders first
        for (FileHandle entry: dirHandle.list()) {
            if (entry.isDirectory()) {
                String name = entry.name();
                if (!name.startsWith(".")) {
                    addEntry(name,true);
                }
            }    
        }
        
        for (FileHandle entry: dirHandle.list()) {
            if (!entry.isDirectory()) {
                String name = entry.name();
                if (!name.startsWith(".")) {
                    addEntry(name, false);
                }   
            }
        }

    }

    // used by the dialog to add a file/directory into the list
    private void addEntry(String name, boolean isDir) {
        if (isDir) { name = name+"/"; }
        Label l=new Label(name,skin);
        fileTab.add(l).expandX().fillX();
        fileTab.row();

        if (!isDir) {
            l.addListener(new ClickListener() {
                public void clicked (InputEvent event, float x, float y) {
                    tf.setText(((Label)event.getTarget()).getText().toString());
                }
            });
        } else {
            l.addListener(new ClickListener() {
                public void clicked (InputEvent event, float x, float y) {
                    String c = ((Label)event.getTarget()).getText().toString();
                    cd(path+c); // TODO platform sep.
                }
            });
        }
    }

    /** getChosen allows you to get the chosen path/file
    *   @return     a string of the relative path (from the initial path) and file selected
    *               NB depending on navigation the path could traverse up and down the hiarchy
    */ 
    public String getChosen() {
        chosen="";
        if (!tf.getText().equals("")) {
            chosen = path  + tf.getText();
        }
        return chosen;
    }

}
