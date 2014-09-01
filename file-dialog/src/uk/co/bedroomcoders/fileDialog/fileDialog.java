package uk.co.bedroomcoders.fileDialog;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.Input.Keys;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.utils.Align;

import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;

public class fileDialog extends Dialog {

    private String chosen;
    private Table fileTab;
    private ScrollPane sPane;
    private TextField tf;
    private Skin skin;
    private String path;
    public TextButton ok,cancel;

    public String getChosen() {
        chosen="";
        if (!tf.getText().equals("")) {
            chosen = path  + tf.getText();
        }
        return chosen;
    }
    
    public fileDialog(String title , String p, Skin s) {
        super(title, s);
        skin = s;
        path = p;

        chosen = "<selecting>";
        fileTab = new Table();
        sPane = new ScrollPane(fileTab,skin);
        tf = new TextField("",skin);
        cd(path);
        
        getContentTable().add(sPane).row();
        getContentTable().add(tf);

        ok = new TextButton("Ok", skin);
        button(ok, true);
        
        cancel = new TextButton("Cancel", skin);
        button(cancel, false);
        key(Keys.ENTER, true);
        key(Keys.ESCAPE, false);

    }

    protected void cd(String p) {
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


}
