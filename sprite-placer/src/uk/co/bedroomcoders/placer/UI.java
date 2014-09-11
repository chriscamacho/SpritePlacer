package uk.co.bedroomcoders.placer;

import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.Stage;

// a dumb class used for holding fields only no functionality
// only static fields!

// kinda welds the main class and event handler together
// as they are really the same class just split for
// managability

public class UI {

    protected static Stage stage;
    protected static Skin skin;

    protected static class func {
        protected static Window win;
        protected static TextButton load,save,remove,
                            add,fixture,clone;
    }
    
    protected static class props {
        protected static Window win;
        protected static Table table;
        protected static ScrollPane pane;
        
        protected static TextField name,x,y,sclx,scly,ang,
                                    offx,offy,width,height;
        protected static TextField texture,twidth,theight;
        protected static SelectBox<String> xwrap,ywrap;

    }

    protected static class body {
        protected static Window win;
        protected static Table table;
        protected static ScrollPane pane;
    }
    
}
