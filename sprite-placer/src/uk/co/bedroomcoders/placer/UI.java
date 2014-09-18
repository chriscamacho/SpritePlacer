package uk.co.bedroomcoders.placer;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.Gdx;

import com.badlogic.gdx.physics.box2d.BodyDef;
import java.util.Arrays;

// STATIC class for holding UI items and intitalising them

public class UI {

    protected static Stage stage;
    protected static Skin skin;
    private static String wraps[] = new String[3];
    private static String bodyTypes[] = new String[3];

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

        protected static SelectBox<String> bodyType;
        protected static SelectBox<String> shapeIndex;
        protected static TextField shapeType;
        protected static TextField offsetX;
        protected static TextField offsetY;
        
    }

    protected static void initialise() {


        bodyTypes[0] = new String("Static");
        bodyTypes[1] = new String("Kinematic");
        bodyTypes[2] = new String("Dynamic");
         
        wraps[Texture.TextureWrap.MirroredRepeat.ordinal()]="Mirror";
        wraps[Texture.TextureWrap.Repeat.ordinal()]="Repeat";
        wraps[Texture.TextureWrap.ClampToEdge.ordinal()]="Clamp";

		
		skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		stage = new Stage();
		Events.handler = new Events();

        func.win = new Window("Functions",skin);

		func.add = addButton(func.win,false,"New");
        func.clone = addButton(func.win,false,"Clone");
		func.remove = addButton(func.win,true,"Remove");
		func.load = addButton(func.win,false,"Load");
		func.save = addButton(func.win,true,"Save");
        func.fixture = addButton(func.win,false,"+Shape");
        func.win.pack();
        func.win.setResizable(false);
        		
		props.win = new Window("Properties",skin);
        props.win.setWidth(190);
		props.win.setResizeBorder(8);
        props.table = new Table(skin);
		props.pane = new ScrollPane(props.table);
        props.pane.setFillParent(true);
        props.win.add(props.pane).fill().expand();
		
		props.table.add(new Label("drag to scroll",skin)).colspan(2);
		props.table.row();
		props.name = addTextCell(props.table, new TextField("",skin),"Name");
		props.x = addTextCell(props.table, new TextField("",skin),"Xpos");
		props.y = addTextCell(props.table, new TextField("",skin),"Ypos");
		props.width = addTextCell(props.table, new TextField("",skin),"width");
		props.height = addTextCell(props.table, new TextField("",skin),"height");
		props.ang = addTextCell(props.table, new TextField("",skin),"angle");
		props.offx = addTextCell(props.table, new TextField("",skin),"offsetX");
		props.offy = addTextCell(props.table, new TextField("",skin),"offsetY");
		props.twidth = addTextCell(props.table, new TextField("",skin), "tex width");
		props.theight = addTextCell(props.table, new TextField("",skin), "tex Height");
		props.sclx = addTextCell(props.table, new TextField("",skin),"scaleX");
		props.scly = addTextCell(props.table, new TextField("",skin),"scaleY");
		props.texture = addTextCell(props.table, new TextField("",skin),"texure");

		props.xwrap = addSelect(props.table, new SelectBox<String>(skin), wraps, "Xwrap");
		props.ywrap = addSelect(props.table, new SelectBox<String>(skin), wraps, "Ywrap");

        props.table.padTop(24);
        props.win.setResizable(true);

        body.win = new Window("Body",skin);
        body.win.setWidth(190);
        body.win.setResizeBorder(8);
        body.table = new Table(skin);
        body.pane = new ScrollPane(body.table);
        body.pane.setFillParent(true);
        body.win.add(body.pane).fill().expand();

        
        body.bodyType = addSelect(body.table, new SelectBox<String>(skin), bodyTypes, "type");
        body.shapeIndex = addSelect(body.table, new SelectBox<String>(skin), null, "EDIT: ");
        body.shapeType = addTextCell(body.table, new TextField("",skin),"type");
        body.shapeType.setDisabled(true);
        body.offsetX = addTextCell(body.table, new TextField("",skin),"Offset X");
        body.offsetY = addTextCell(body.table, new TextField("",skin),"Offset Y");
        
        		
		stage.addActor(props.win);
		props.win.setPosition(8,110);
		stage.addActor(func.win);
		func.win.setPosition(8,8);
        stage.addActor(body.win);
        body.win.setPosition(8,280);
    }


	private static SelectBox<String> addSelect(Table parent, SelectBox<String> w, String[] list,String label)
	{
		Label nameLabel = new Label(label, UI.skin);
		parent.add(nameLabel).width(60);
		parent.add(w).width(120);
        w.addListener(Events.handler);
        if (list!=null) w.setItems(list);
		parent.row();
		return w;
	}

	private static TextField addTextCell(Table parent, TextField w,String label)
	{
		Label nameLabel = new Label(label, UI.skin);
		parent.add(nameLabel).width(60);
		parent.add(w).width(120);
        w.addListener(Events.handler);
		parent.row();
        w.setUserObject(nameLabel);
		return w;
	}

    /*
     *	add a text button to a table/window used for the function button window
     */
    private static TextButton addButton(Table parent, boolean row, String text) {
        TextButton button = new TextButton(text, UI.skin);
        parent.add(button).width(60);
        if (row) parent.row();
        button.addListener(Events.handler);
        return button;
    }



        
}
