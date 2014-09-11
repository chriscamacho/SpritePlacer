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

// STATIC class for holding UI items and intitalising them

public class UI {

    protected static Stage stage;
    protected static Skin skin;
    private static String wraps[] = new String[3];

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

    protected static void initialise() {
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
		props.name = addTextCell(new TextField("",skin),"Name");
		props.x = addTextCell(new TextField("",skin),"Xpos");
		props.y = addTextCell(new TextField("",skin),"Ypos");
		props.width = addTextCell(new TextField("",skin),"width");
		props.height = addTextCell(new TextField("",skin),"height");
		props.ang = addTextCell(new TextField("",skin),"angle");
		props.offx = addTextCell(new TextField("",skin),"offsetX");
		props.offy = addTextCell(new TextField("",skin),"offsetY");
		props.twidth = addTextCell(new TextField("",skin), "tex width");
		props.theight = addTextCell(new TextField("",skin), "tex Height");
		props.sclx = addTextCell(new TextField("",skin),"scaleX");
		props.scly = addTextCell(new TextField("",skin),"scaleY");
		props.texture = addTextCell(new TextField("",skin),"texure");

		props.xwrap = addSelect(new SelectBox<String>(skin), wraps, "Xwrap");
		props.ywrap = addSelect(new SelectBox<String>(skin), wraps, "Ywrap");

        props.table.padTop(24);
        props.win.setResizable(true);

        body.win = new Window("Body",skin);
        body.win.setWidth(190);
        
        		
		stage.addActor(props.win);
		props.win.setPosition(8,110);
		stage.addActor(func.win);
		func.win.setPosition(8,8);        
    }


    /* 
     *      convenience functions to add widgets (for properties)
     *      TODO should use parent param like addButton
     */
	private static SelectBox<String> addSelect(SelectBox<String> w, String[] list,String label)
	{
		Label nameLabel = new Label(label, UI.skin);
		UI.props.table.add(nameLabel).width(60);
		UI.props.table.add(w).width(120);
        w.addListener(Events.handler);
        w.setItems(list);
		UI.props.table.row();
		return w;
	}

	private static TextField addTextCell(TextField w,String label)
	{
		Label nameLabel = new Label(label, UI.skin);
		UI.props.table.add(nameLabel).width(60);
		UI.props.table.add(w).width(120);
        w.addListener(Events.handler);
		UI.props.table.row();
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
