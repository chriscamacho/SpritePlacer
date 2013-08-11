package uk.co.bedroomcoders.placer;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.Texture.TextureFilter;

import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.Gdx;

// a Pixy is like a Sprite except that its origin is always in the centre
// and the rotation/scaling and drawing origin is always the same...
public class Pixy
{
	public float x,y,angle;
	public float originX = 0;
	public float originY = 0;
	public float scaleX = 1,scaleY = 1;
	public int textureOffsetX = 0;
	public int textureOffsetY = 0;
	public Texture texture;
	public String textureFileName;
	public int width,height;
	public String name="";

	public static ArrayList<Pixy> pixies = new ArrayList<Pixy>();
	
	public static Texture brokenTexture = new Texture(Gdx.files.internal("data/missing.png"));
	
	Pixy(float px, float py, int ox, int oy, int w, int h,
			float sx, float sy, float a, String textureName, String Name)
	{
		name=Name;
		textureOffsetX=ox;textureOffsetY=oy;
		width=w;height=h;
		textureFileName=textureName;
		texture = new Texture(Gdx.files.internal("data/"+textureName));
		
		originX = width/2;
		originY = height/2;
		x=px;y=py;
		angle=a;
		scaleX=sx;scaleY=sy;
		pixies.add(this);
	}

	public void draw(SpriteBatch sb)
	{
		sb.draw(texture, x-originX, y-originY, originX, originY, width, height,
					scaleX, scaleY, angle, textureOffsetX, textureOffsetY, width, height, false, false);
	}
	
	public static void drawAll(SpriteBatch sb)
	{
		Iterator<Pixy> itr = pixies.iterator();
		while(itr.hasNext())
		{
			Pixy p = itr.next();
			p.draw(sb);
		}	
	}
	
	public String toXml()
	{
	//	<pixy name="small ufo" x="-50" y="50" oy="64" sx="0.5" sy="0.5" texture="libgdx.png" angle="-15" width="128" height="64" />
		String s = "";
		
		s= "    <pixy name=\""+name+"\" ";
		s+="x=\""+x+"\" ";
		s+="y=\""+y+"\" ";
		s+="ox=\""+textureOffsetX+"\" ";
		s+="oy=\""+textureOffsetY+"\" ";
		s+="sx=\""+scaleX+"\" ";
		s+="sy=\""+scaleY+"\" ";
		s+="texture=\""+textureFileName+"\" ";
		s+="angle=\""+angle+"\" ";
		s+="width=\""+width+"\" ";
		s+="height=\""+height+"\" />\n";
		
		return s;
	}

}
