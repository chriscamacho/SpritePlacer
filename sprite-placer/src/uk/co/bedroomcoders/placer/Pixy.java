package uk.co.bedroomcoders.placer;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.Texture.TextureFilter;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.Gdx;

import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Body;

// a Pixy is like a Sprite except that its origin is always in the centre
// and the rotation/scaling and drawing origin is always the same...
public class Pixy
{
	private float x;
	private float y;
	private float angle;
	private float originX = 0;
	private float originY = 0;
	private float scaleX = 1;
	private float scaleY = 1;
	private int textureOffsetX = 0;
	private int textureOffsetY = 0;
	private Texture texture;
	private String textureFileName;
	private int width;
	private int height;
	private String name="";
	private int xWrap;
	private int yWrap;
	private int textureWidth;
	private int textureHeight;

	private static ArrayList<Pixy> pixies = new ArrayList<Pixy>();
	private static Texture brokenTexture = new Texture(Gdx.files.internal("data/missing.png"));

    public Body body=null; // by default a pixy is decorative only

    // TODO this is a monster constructor...
	Pixy(float px, float py, int ox, int oy, int w, int h,
			float sx, float sy, float a, String textureName, String Name,int wx,int wy,int tw,int th)
	{
		setName(Name);
		setTextureOffsetX(ox);setTextureOffsetY(oy);
		setWidth(w);setHeight(h);
		setTextureFileName(textureName);
		setTexture(new Texture(Gdx.files.internal("data/"+textureName)));
		getTexture().setWrap(Texture.TextureWrap.values()[wx], Texture.TextureWrap.values()[wy]);
		setxWrap(wx);
		setyWrap(wy);
		setOriginX(getWidth()/2);
		setOriginY(getHeight()/2);
		setX(px);setY(py);
		setAngle(a);
		setScaleX(sx);setScaleY(sy);
		setTextureWidth(tw);
		setTextureHeight(th);
		getPixies().add(this);
	}

	public void draw(SpriteBatch sb) {
		sb.draw(getTexture(), getX()-getOriginX(), getY()-getOriginY(), getOriginX(), getOriginY(), getWidth(), getHeight(),
					getScaleX(), getScaleY(), getAngle(), getTextureOffsetX(), getTextureOffsetY(), getTextureWidth(), getTextureHeight(), false, false);
					
	}
	
	public static void drawAll(SpriteBatch sb) {
		Iterator<Pixy> itr = getPixies().iterator();
		while(itr.hasNext())
		{
			Pixy p = itr.next();
			p.draw(sb);
		}	
	}

    // dumps a sprite and its extra properties to an xml node
	public String toXml() {
    // example output
	//	<pixy name="small ufo" x="-50" y="50" oy="64" sx="0.5" sy="0.5" texture="libgdx.png" angle="-15" width="128" height="64" />
		String s = "";
		
		s= "    <pixy name=\""+getName()+"\" ";
		s+="x=\""+getX()+"\" ";
		s+="y=\""+getY()+"\" ";
		s+="ox=\""+getTextureOffsetX()+"\" ";
		s+="oy=\""+getTextureOffsetY()+"\" ";
		s+="sx=\""+getScaleX()+"\" ";
		s+="sy=\""+getScaleY()+"\" ";
		s+="texture=\""+getTextureFileName()+"\" ";
		s+="angle=\""+getAngle()+"\" ";
		s+="width=\""+getWidth()+"\" ";
		s+="height=\""+getHeight()+"\" ";
		s+="xwrap=\""+getxWrap()+"\" ";
		s+="ywrap=\""+getyWrap()+"\" ";
		s+="twidth=\""+getTextureWidth()+"\" ";
		s+="theight=\""+getTextureHeight()+"\" ";
		s+=" />\n";
		return s;
	}

    // NB the point must unprojected if using coordinates from the
    // screen - ie from events
    // Can't use box2d as some sprites will be decorative only
	public boolean pointIntersects(Vector2 p) {
		float c = (float)Math.cos(-getAngle()*Const.PI180);
		float s = (float)Math.sin(-getAngle()*Const.PI180);
		float rtx = getX() + c * (p.x - getX()) - s * (p.y - getY());
		float rty = getY() + s * (p.x - getX()) + c * (p.y - getY());
		float wid = (getWidth() / 2) * getScaleX();
		float hgt = (getHeight() / 2) * getScaleY();
		float lx = getX() - wid;
		float rx = getX() + wid;
		float ty = getY() - hgt;
		float by = getY() + hgt;
		
		return lx <= rtx && rtx <= rx && ty <= rty && rty <= by;
	}

	public float getX() { return x; }
	public void setX(float x) { this.x = x; updateBodyTransform(); }
	public float getY() { return y; }
	public void setY(float y) { this.y = y; updateBodyTransform(); }
	public float getAngle() { return angle; }
	public void setAngle(float angle) {	this.angle = angle; updateBodyTransform(); }

	private void updateBodyTransform() {
		if (body!=null) {
			body.setTransform(x*Const.WORLD2BOX,y*Const.WORLD2BOX,angle*Const.PI180);
		}
	}

	public float getOriginX() {
		return originX;
	}

	public void setOriginX(float originX) {
		this.originX = originX;
	}

	public float getOriginY() {
		return originY;
	}

	public void setOriginY(float originY) {
		this.originY = originY;
	}

	public float getScaleX() {
		return scaleX;
	}

	public void setScaleX(float scaleX) {
		this.scaleX = scaleX;
	}

	public float getScaleY() {
		return scaleY;
	}

	public void setScaleY(float scaleY) {
		this.scaleY = scaleY;
	}

	public int getTextureOffsetX() {
		return textureOffsetX;
	}

	public void setTextureOffsetX(int textureOffsetX) {
		this.textureOffsetX = textureOffsetX;
	}

	public int getTextureOffsetY() {
		return textureOffsetY;
	}

	public void setTextureOffsetY(int textureOffsetY) {
		this.textureOffsetY = textureOffsetY;
	}

	public Texture getTexture() {
		return texture;
	}

	public void setTexture(Texture texture) {
		this.texture = texture;
	}

	public String getTextureFileName() {
		return textureFileName;
	}

	public void setTextureFileName(String textureFileName) {
		this.textureFileName = textureFileName;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getxWrap() {
		return xWrap;
	}

	public void setxWrap(int xWrap) {
		this.xWrap = xWrap;
	}

	public int getyWrap() {
		return yWrap;
	}

	public void setyWrap(int yWrap) {
		this.yWrap = yWrap;
	}

	public int getTextureWidth() {
		return textureWidth;
	}

	public void setTextureWidth(int textureWidth) {
		this.textureWidth = textureWidth;
	}

	public int getTextureHeight() {
		return textureHeight;
	}

	public void setTextureHeight(int textureHeight) {
		this.textureHeight = textureHeight;
	}

	public static ArrayList<Pixy> getPixies() {
		return pixies;
	}

	public static void setPixies(ArrayList<Pixy> pixies) {
		Pixy.pixies = pixies;
	}

	public static Texture getBrokenTexture() {
		return brokenTexture;
	}

	private static void setBrokenTexture(Texture brokenTexture) {
		Pixy.brokenTexture = brokenTexture;
	}
}
