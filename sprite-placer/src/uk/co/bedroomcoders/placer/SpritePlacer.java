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

public class SpritePlacer implements ApplicationListener { 

	private SpriteBatch batch;
	protected OrthographicCamera camera;
	protected TextButton loadButton,saveButton,removeButton,newButton;
    protected TextButton fixtButton,cloneButton;
	protected TextField nameEd,xEd,yEd,sxEd,syEd,angEd,oxEd,oyEd,wEd,hEd;
    protected TextField textureEd,twEd,thEd;
	protected SelectBox<String> xwrapEd,ywrapEd;
	private Window win,butWin,fixtWin;
	private ScrollPane sPane;
	private Table propTable;
	protected Stage stage;
	protected Skin skin;
	protected Pixy selected=null;
    private ShapeRenderer shpBatch; // selection hilight
    private OneBodyRenderer obr;

	private String wraps[] = new String[3];
    private static final Color selCols[] = { Color.RED, Color.GREEN, Color.BLUE,
                                                Color.WHITE, Color.BLACK, Color.YELLOW,
                                                Color.PURPLE }; 
    private int selCol = 0; int physCol = selCols.length/2;
    private int coltick = 0;

    private Events handler;
    protected World world;
    
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
		
		skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		handler = new Events(this);
		stage = new Stage();

		InputMultiplexer multiplexer = new InputMultiplexer();
		multiplexer.addProcessor(stage);
		multiplexer.addProcessor(handler);
		Gdx.input.setInputProcessor(multiplexer);

		butWin = new Window("Functions",skin);
        //butWin.pad(6);
        //butWin.padTop(24);

		newButton = addButton(butWin,false,"New");
        cloneButton = addButton(butWin,false,"Clone");
		removeButton = addButton(butWin,true,"Remove");
		loadButton = addButton(butWin,false,"Load");
		saveButton = addButton(butWin,true,"Save");
        fixtButton = addButton(butWin,false,"+Shape");
        butWin.pack();
				
		win = new Window("Properties",skin);
        win.setWidth(190);
		win.setResizeBorder(8);
        propTable = new Table(skin);
		sPane = new ScrollPane(propTable);
        sPane.setFillParent(true);
        win.add(sPane).fill().expand();
		
		propTable.add(new Label("drag to scroll",skin)).colspan(2);
		propTable.row();
		nameEd = addTextCell(new TextField("",skin),"Name");
		xEd = addTextCell(new TextField("",skin),"Xpos");
		yEd = addTextCell(new TextField("",skin),"Ypos");
		wEd = addTextCell(new TextField("",skin),"width");
		hEd = addTextCell(new TextField("",skin),"height");
		angEd = addTextCell(new TextField("",skin),"angle");
		oxEd = addTextCell(new TextField("",skin),"offsetX");
		oyEd = addTextCell(new TextField("",skin),"offsetY");
		twEd = addTextCell(new TextField("",skin), "tex width");
		thEd = addTextCell(new TextField("",skin), "tex Height");
		sxEd = addTextCell(new TextField("",skin),"scaleX");
		syEd = addTextCell(new TextField("",skin),"scaleY");
		textureEd = addTextCell(new TextField("",skin),"texure");

		xwrapEd = addSelect(new SelectBox<String>(skin), wraps, "Xwrap");
		ywrapEd = addSelect(new SelectBox<String>(skin), wraps, "Ywrap");

        propTable.padTop(24);
        win.setResizable(true);
        		
		stage.addActor(win);
		win.setPosition(8,110);
		stage.addActor(butWin);
		butWin.setPosition(8,8);

        obr = new OneBodyRenderer();     
	}

    /* 
     *      convenience functions to add widgets (for properties)
     */
	private SelectBox<String> addSelect(SelectBox<String> w, String[] list,String label)
	{
		Label nameLabel = new Label(label, skin);
		propTable.add(nameLabel).width(60);
		propTable.add(w).width(120);
        w.addListener(handler);
        w.setItems(list);
		propTable.row();
		return w;
	}

	private TextField addTextCell(TextField w,String label)
	{
		Label nameLabel = new Label(label, skin);
		propTable.add(nameLabel).width(60);
		propTable.add(w).width(120);
        w.addListener(handler);
		propTable.row();
		return w;
	}

    /*
     *	add a text button to a table/window used for the function button window
     */
    private TextButton addButton(Table parent, boolean row, String text) {
        TextButton button = new TextButton(text, skin);
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
	float parseFloatString(TextField tf, float v)
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
	protected void updateProperty( Event event)
	{
		if (selected!=null) { // only if selected
			if (event.getTarget() == nameEd) selected.setName(nameEd.getText());
			if (event.getTarget() == twEd) selected.setTextureWidth((int)parseFloatString(twEd,selected.getTextureWidth()));
			if (event.getTarget() == thEd) selected.setTextureHeight((int)parseFloatString(thEd,selected.getTextureHeight()));
			if (event.getTarget() == xEd) selected.setX(parseFloatString(xEd,selected.getX()));
			if (event.getTarget() == yEd) selected.setY(parseFloatString(yEd,selected.getY()));
			if (event.getTarget() == sxEd) selected.setScaleX(parseFloatString(sxEd,selected.getScaleX()));
			if (event.getTarget() == syEd) selected.setScaleY(parseFloatString(syEd,selected.getScaleY()));
			if (event.getTarget() == angEd) selected.setAngle(parseFloatString(angEd,selected.getAngle()));
			if (event.getTarget() == wEd) {
				selected.setWidth((int)parseFloatString(wEd,selected.getWidth()));
				selected.setOriginX(selected.getWidth() / 2);
			}
			if (event.getTarget() == hEd) {
				selected.setHeight((int)parseFloatString(hEd,selected.getHeight()));
				selected.setOriginY(selected.getHeight() / 2);
			}
			if (event.getTarget() == oxEd) {
				selected.setTextureOffsetX((int)parseFloatString(oxEd,selected.getTextureOffsetX()));
            }
			if (event.getTarget() == oyEd) {
				selected.setTextureOffsetY((int)parseFloatString(oyEd,selected.getTextureOffsetX()));
            }
			if (event.getTarget() == textureEd) {
				selected.setTextureFileName(textureEd.getText());
				try
				{
                    selected.setTexture(new Texture(Gdx.files.internal(selected.getTextureFileName())));
				} 
				catch (Exception e)
				{
					selected.setTexture(Pixy.getBrokenTexture());
					textureEd.setText("missing!");
				}
			}
			if (event.getTarget() == xwrapEd)
			{
				int s = xwrapEd.getSelectedIndex();
				if ( s == Texture.TextureWrap.ClampToEdge.ordinal() )
						selected.setxWrap(Texture.TextureWrap.ClampToEdge.ordinal()); 
				if ( s == Texture.TextureWrap.Repeat.ordinal() )
						selected.setxWrap(Texture.TextureWrap.Repeat.ordinal()); 
				if ( s == Texture.TextureWrap.MirroredRepeat.ordinal() )
						selected.setxWrap(Texture.TextureWrap.MirroredRepeat.ordinal()); 
			}
			
			if (event.getTarget() == ywrapEd)
			{
				int s = ywrapEd.getSelectedIndex();
				if ( s == Texture.TextureWrap.ClampToEdge.ordinal() )
						selected.setyWrap(Texture.TextureWrap.ClampToEdge.ordinal()); 
				if ( s == Texture.TextureWrap.Repeat.ordinal() )
						selected.setyWrap(Texture.TextureWrap.Repeat.ordinal()); 
				if ( s == Texture.TextureWrap.MirroredRepeat.ordinal() )
						selected.setyWrap(Texture.TextureWrap.MirroredRepeat.ordinal()); 
			}
			
			if (event.getTarget() == ywrapEd || event.getTarget() == xwrapEd)
			{
				selected.getTexture().setWrap(
						Texture.TextureWrap.values()[selected.getxWrap()],
						Texture.TextureWrap.values()[selected.getyWrap()]
					);
			}
		}
	}

    // iterate all pixies making them dump themselves to xml
	protected void saveLevel(String fname)
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
	protected void updateGui() {
		nameEd.setText(selected.getName());
		xEd.setText(""+selected.getX());
		yEd.setText(""+selected.getY());
		sxEd.setText(""+selected.getScaleX());
		syEd.setText(""+selected.getScaleY());
		angEd.setText(""+selected.getAngle());
		oxEd.setText(""+selected.getTextureOffsetX());
		oyEd.setText(""+selected.getTextureOffsetY());
		wEd.setText(""+selected.getWidth());
		hEd.setText(""+selected.getHeight());
		twEd.setText(""+selected.getTextureWidth());
		thEd.setText(""+selected.getTextureHeight());
		textureEd.setText(selected.getTextureFileName());
		xwrapEd.setSelectedIndex(selected.getxWrap());
		ywrapEd.setSelectedIndex(selected.getyWrap());		
	}

    public void clearPropsGui() {
        nameEd.setText("");
        xEd.setText("");
        yEd.setText("");
        sxEd.setText("");
        syEd.setText("");
        angEd.setText("");
        oxEd.setText("");
        oyEd.setText("");
        wEd.setText("");
        hEd.setText("");
        twEd.setText("");
        thEd.setText("");
        textureEd.setText("");
        xwrapEd.setSelectedIndex(0);
        ywrapEd.setSelectedIndex(0);
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
        if (coltick>6) {
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

                obr.renderOneBody(selected.body, dm);
            }

        }

        

		stage.act();
		stage.draw();

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
