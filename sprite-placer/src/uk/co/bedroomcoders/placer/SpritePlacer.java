package uk.co.bedroomcoders.placer;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.Stage;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.InputMultiplexer;

import java.util.ArrayList;
import java.util.Iterator;

import java.io.OutputStream;

import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.math.Matrix4;

import java.lang.Throwable;
import java.lang.Exception;

public class SpritePlacer implements ApplicationListener { 

	private SpriteBatch batch;
	protected static OrthographicCamera camera;

	protected static Pixy selected=null;
    private ShapeRenderer shpBatch; // selection hilight
    private OneBodyRenderer obr;

	private String wraps[] = new String[3];
    private static final Color selCols[] = { Color.RED, Color.GREEN, Color.BLUE,
                                                Color.WHITE, Color.BLACK, Color.YELLOW,
                                                Color.PURPLE }; 
    private int selCol = 0; int physCol = selCols.length/2;
    private int coltick = 0;

    private Events handler;
    protected static World world;
    
	@Override
	public void create() {


        world = new World(Const.GRAVITY, false);
        // provide a textual version of wrap types
        wraps[Texture.TextureWrap.MirroredRepeat.ordinal()]="Mirror";
        wraps[Texture.TextureWrap.Repeat.ordinal()]="Repeat";
        wraps[Texture.TextureWrap.ClampToEdge.ordinal()]="Clamp";
        
		// sets up UI controls
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();

		camera = new OrthographicCamera(w,h);
		
		batch = new SpriteBatch();
        shpBatch = new ShapeRenderer();
		
		UI.skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		UI.stage = new Stage();
		handler = new Events();

		InputMultiplexer multiplexer = new InputMultiplexer();
		multiplexer.addProcessor(UI.stage);
		multiplexer.addProcessor(handler);
		Gdx.input.setInputProcessor(multiplexer);

		UI.func.win = new Window("Functions",UI.skin);

		UI.func.add = addButton(UI.func.win,false,"New");
        UI.func.clone = addButton(UI.func.win,false,"Clone");
		UI.func.remove = addButton(UI.func.win,true,"Remove");
		UI.func.load = addButton(UI.func.win,false,"Load");
		UI.func.save = addButton(UI.func.win,true,"Save");
        UI.func.fixture = addButton(UI.func.win,false,"+Shape");
        UI.func.win.pack();
        UI.func.win.setResizable(false);
        		
		UI.props.win = new Window("Properties",UI.skin);
        UI.props.win.setWidth(190);
		UI.props.win.setResizeBorder(8);
        UI.props.table = new Table(UI.skin);
		UI.props.pane = new ScrollPane(UI.props.table);
        UI.props.pane.setFillParent(true);
        UI.props.win.add(UI.props.pane).fill().expand();
		
		UI.props.table.add(new Label("drag to scroll",UI.skin)).colspan(2);
		UI.props.table.row();
		UI.props.name = addTextCell(new TextField("",UI.skin),"Name");
		UI.props.x = addTextCell(new TextField("",UI.skin),"Xpos");
		UI.props.y = addTextCell(new TextField("",UI.skin),"Ypos");
		UI.props.width = addTextCell(new TextField("",UI.skin),"width");
		UI.props.height = addTextCell(new TextField("",UI.skin),"height");
		UI.props.ang = addTextCell(new TextField("",UI.skin),"angle");
		UI.props.offx = addTextCell(new TextField("",UI.skin),"offsetX");
		UI.props.offy = addTextCell(new TextField("",UI.skin),"offsetY");
		UI.props.twidth = addTextCell(new TextField("",UI.skin), "tex width");
		UI.props.theight = addTextCell(new TextField("",UI.skin), "tex Height");
		UI.props.sclx = addTextCell(new TextField("",UI.skin),"scaleX");
		UI.props.scly = addTextCell(new TextField("",UI.skin),"scaleY");
		UI.props.texture = addTextCell(new TextField("",UI.skin),"texure");

		UI.props.xwrap = addSelect(new SelectBox<String>(UI.skin), wraps, "Xwrap");
		UI.props.ywrap = addSelect(new SelectBox<String>(UI.skin), wraps, "Ywrap");

        UI.props.table.padTop(24);
        UI.props.win.setResizable(true);

        UI.body.win = new Window("Body",UI.skin);
        UI.body.win.setWidth(190);
        
        		
		UI.stage.addActor(UI.props.win);
		UI.props.win.setPosition(8,110);
		UI.stage.addActor(UI.func.win);
		UI.func.win.setPosition(8,8);

        obr = new OneBodyRenderer();     
	}

    /* 
     *      convenience functions to add widgets (for properties)
     *      TODO should use parent param like addButton
     */
	private SelectBox<String> addSelect(SelectBox<String> w, String[] list,String label)
	{
		Label nameLabel = new Label(label, UI.skin);
		UI.props.table.add(nameLabel).width(60);
		UI.props.table.add(w).width(120);
        w.addListener(handler);
        w.setItems(list);
		UI.props.table.row();
		return w;
	}

	private TextField addTextCell(TextField w,String label)
	{
		Label nameLabel = new Label(label, UI.skin);
		UI.props.table.add(nameLabel).width(60);
		UI.props.table.add(w).width(120);
        w.addListener(handler);
		UI.props.table.row();
        w.setUserObject(nameLabel);
		return w;
	}

    /*
     *	add a text button to a table/window used for the function button window
     */
    private TextButton addButton(Table parent, boolean row, String text) {
        TextButton button = new TextButton(text, UI.skin);
        parent.add(button).width(60);
        if (row) parent.row();
        button.addListener(handler);
        return button;
    }

	@Override
	public void dispose() {
		batch.dispose();
	}
	
	// converts an input string to a float, if the conversion fails the string
	// is replaced with a string of the existing property value.
	protected static float parseFloatString(TextField tf, float v)
	{
		float f;
		try {
			f = Float.parseFloat(tf.getText());
		} catch (Exception e) {
			f = v;
			tf.setText(Float.toString(v));
		}
		return f;
	}

	// updates pixy properties depending on current UI widget
	protected static void updateProperty( Event event)
	{
		if (selected!=null) { // only if selected
			if (event.getTarget() == UI.props.name)
                    selected.setName(UI.props.name.getText());
			if (event.getTarget() == UI.props.twidth)
                    selected.setTextureWidth((int)parseFloatString(UI.props.twidth,selected.getTextureWidth()));
			if (event.getTarget() == UI.props.theight)
                    selected.setTextureHeight((int)parseFloatString(UI.props.theight,selected.getTextureHeight()));
			if (event.getTarget() == UI.props.x)
                    selected.setX(parseFloatString(UI.props.x,selected.getX()));
			if (event.getTarget() == UI.props.y)
                    selected.setY(parseFloatString(UI.props.y,selected.getY()));
			if (event.getTarget() == UI.props.sclx)
                    selected.setScaleX(parseFloatString(UI.props.sclx,selected.getScaleX()));
			if (event.getTarget() == UI.props.scly)
                    selected.setScaleY(parseFloatString(UI.props.scly,selected.getScaleY()));
			if (event.getTarget() == UI.props.ang)
                    selected.setAngle(parseFloatString(UI.props.ang,selected.getAngle()));
			if (event.getTarget() == UI.props.width) {
				selected.setWidth((int)parseFloatString(UI.props.width,selected.getWidth()));
				selected.setOriginX(selected.getWidth() / 2);
			}
			if (event.getTarget() == UI.props.height) {
				selected.setHeight((int)parseFloatString(UI.props.height,selected.getHeight()));
				selected.setOriginY(selected.getHeight() / 2);
			}
			if (event.getTarget() == UI.props.offx) {
				selected.setTextureOffsetX((int)parseFloatString(UI.props.offx,selected.getTextureOffsetX()));
            }
			if (event.getTarget() == UI.props.offy) {
				selected.setTextureOffsetY((int)parseFloatString(UI.props.offy,selected.getTextureOffsetX()));
            }
			if (event.getTarget() == UI.props.texture) {
				selected.setTextureFileName(UI.props.texture.getText());
				try
				{
                    selected.setTexture(new Texture(Gdx.files.internal(selected.getTextureFileName())));
				} 
				catch (Exception e)
				{
					selected.setTexture(Pixy.getBrokenTexture());
					UI.props.texture.setText("missing!");
				}
			}
			if (event.getTarget() == UI.props.xwrap)
			{
				int s = UI.props.xwrap.getSelectedIndex();
				if ( s == Texture.TextureWrap.ClampToEdge.ordinal() )
						selected.setxWrap(Texture.TextureWrap.ClampToEdge.ordinal()); 
				if ( s == Texture.TextureWrap.Repeat.ordinal() )
						selected.setxWrap(Texture.TextureWrap.Repeat.ordinal()); 
				if ( s == Texture.TextureWrap.MirroredRepeat.ordinal() )
						selected.setxWrap(Texture.TextureWrap.MirroredRepeat.ordinal()); 
			}
			
			if (event.getTarget() == UI.props.ywrap)
			{
				int s = UI.props.ywrap.getSelectedIndex();
				if ( s == Texture.TextureWrap.ClampToEdge.ordinal() )
						selected.setyWrap(Texture.TextureWrap.ClampToEdge.ordinal()); 
				if ( s == Texture.TextureWrap.Repeat.ordinal() )
						selected.setyWrap(Texture.TextureWrap.Repeat.ordinal()); 
				if ( s == Texture.TextureWrap.MirroredRepeat.ordinal() )
						selected.setyWrap(Texture.TextureWrap.MirroredRepeat.ordinal()); 
			}
			
			if (event.getTarget() == UI.props.xwrap ||
                event.getTarget() == UI.props.ywrap)
			{
				selected.getTexture().setWrap(
						Texture.TextureWrap.values()[selected.getxWrap()],
						Texture.TextureWrap.values()[selected.getyWrap()]
					);
			}
		}
	}

    // iterate all pixies making them dump themselves to xml
	protected static void saveLevel(String fname)
	{
		OutputStream os = Gdx.files.local(fname).write(false);
		try 
		{
			os.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<level>\n".getBytes());
			Iterator<Pixy> itr = Pixy.getPixies().iterator();
			while(itr.hasNext())
			{
				Pixy p = itr.next();
				os.write(p.toXml().getBytes());
			}
			os.write("</level>\n".getBytes());
			os.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

    // updates the gui controls from a pixy
	protected static void updatePropGui() {
		UI.props.name.setText(selected.getName());
		UI.props.x.setText(""+selected.getX());
		UI.props.y.setText(""+selected.getY());
		UI.props.sclx.setText(""+selected.getScaleX());
		UI.props.scly.setText(""+selected.getScaleY());
		UI.props.ang.setText(""+selected.getAngle());
		UI.props.offx.setText(""+selected.getTextureOffsetX());
		UI.props.offy.setText(""+selected.getTextureOffsetY());
		UI.props.width.setText(""+selected.getWidth());
		UI.props.height.setText(""+selected.getHeight());
		UI.props.twidth.setText(""+selected.getTextureWidth());
		UI.props.theight.setText(""+selected.getTextureHeight());
		UI.props.texture.setText(selected.getTextureFileName());
		UI.props.xwrap.setSelectedIndex(selected.getxWrap());
		UI.props.ywrap.setSelectedIndex(selected.getyWrap());		
	}

    protected static void clearPropsGui() {
        UI.props.name.setText("");
        UI.props.x.setText("");
        UI.props.y.setText("");
        UI.props.sclx.setText("");
        UI.props.scly.setText("");
        UI.props.ang.setText("");
        UI.props.offx.setText("");
        UI.props.offy.setText("");
        UI.props.width.setText("");
        UI.props.height.setText("");
        UI.props.twidth.setText("");
        UI.props.theight.setText("");
        UI.props.texture.setText("");
        UI.props.xwrap.setSelectedIndex(0);
        UI.props.ywrap.setSelectedIndex(0);
    }

	@Override
	public void render() {
		Gdx.gl.glClearColor(1, .5f, .25f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		
		batch.begin();
		Pixy.drawAll(batch);
		batch.end();

        coltick++;
        if (coltick>4) {
            coltick=0;
            selCol++;physCol++;
            if (selCol==selCols.length) selCol=0;
            if (physCol==selCols.length) physCol=0;
        }
        if (selected!=null) {
            shpBatch.setProjectionMatrix(camera.combined);
            shpBatch.begin(ShapeType.Line);
            shpBatch.setColor(selCols[selCol]);
            // see pixy draw - offset / x,y is done this way to decouple
            // offset from position so it only works with texture...
            shpBatch.rect(selected.getX()-selected.getOriginX(), selected.getY()-selected.getOriginY(),
                        selected.getOriginX(), selected.getOriginY(),
                        selected.getWidth(), selected.getHeight(),
                        selected.getScaleX(), selected.getScaleY(),
                        selected.getAngle());

            shpBatch.end();
            
            // Overlay the selected objects physics shapes

            if (selected.body!=null) {
                Matrix4 dm=new Matrix4();
                dm.set(camera.combined);
                dm.scale(Const.BOX2WORLD,Const.BOX2WORLD,1f);

                obr.renderOneBody(selected.body, dm, selCols[physCol]);
            }

        }

		UI.stage.act();
		UI.stage.draw();

	}

	@Override
	public void resize(int width, int height) {
		// TODO update cam matrix and viewport here
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}
}
