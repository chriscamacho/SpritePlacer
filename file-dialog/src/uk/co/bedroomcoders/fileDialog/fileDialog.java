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
import com.badlogic.gdx.utils.Align;

import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;

import java.util.regex.Pattern;
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
    private TextField textField;
    private Skin skin;
    private String path;
    private String filter;

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
    * @param path	    	The initial file path for the dialog 
    * @param stage		    The stage you intend to add the fileDialog to, only used for positioning
    * @param skin		    The skin to use
    * @param filter         file name match ie "^jpg|^png"
    */
    public fileDialog(String title , String path, Stage stage, Skin skin, String filter) {
        super(title, skin);
        this.skin = skin;
        this.path = path;
        this.filter = filter;

        chosen = "<selecting>";

        setSize(240,200);
        fileTab = new Table();
        sPane = new ScrollPane(fileTab,this.skin);
        sPane.setScrollingDisabled(true,false);
        textField = new TextField("",this.skin);
        cd(this.path);
        getContentTable().add(sPane).expand().fill();
        getContentTable().row();
        getContentTable().add(textField);

        ok = new TextButton("Ok", this.skin);
        button(ok, true);
        
        cancel = new TextButton("Cancel", this.skin);
        button(cancel, false);
        key(Keys.ENTER, false);
        key(Keys.ESCAPE, false);

        setPosition((stage.getWidth()/2)-(getWidth()/2),
                        (stage.getHeight()/2)-(getHeight()/2));
        stage.setKeyboardFocus(textField);
    }

    // used by the dialog to change directory when a user clicks a directory
    private void cd(String path) {
        textField.setText("");
        this.path = path;
        fileTab.clearChildren();
        FileHandle dirHandle = Gdx.files.internal(path);

        Pattern pat = Pattern.compile(filter);

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
                    if (pat.matcher(name).find()) addEntry(name, false);
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
                    textField.setText(((Label)event.getTarget()).getText().toString());
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
        if (!textField.getText().equals("")) {
            chosen = path  + textField.getText();
        }
        return chosen;
    }

}
