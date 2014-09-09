
package uk.co.bedroomcoders.placer;

import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Transform;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Shape.Type;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.PolygonShape;

public class OneBodyRenderer extends Box2DDebugRenderer {

    OneBodyRenderer() {
        super();
        for (int i = 0; i < vertices.length; i++)
			vertices[i] = new Vector2();
    }

    private Color col;

    // only needed this method... till I wanted to change the colour
    public void renderOneBody(Body bod, Matrix4 proj, Color c) {
        col=c;
        renderer.begin(ShapeType.Line);
        renderer.setProjectionMatrix(proj);
        renderBody(bod);
        renderer.end();
    }


	protected void renderBody (Body body) {
		Transform transform = body.getTransform();
		for (Fixture fixture : body.getFixtureList()) {
            drawShape(fixture, transform, col);
		}
	}

    private static Vector2 t = new Vector2();
	private static Vector2 axis = new Vector2();

	private void drawShape (Fixture fixture, Transform transform, Color color) {
		if (fixture.getType() == Type.Circle) {
			CircleShape circle = (CircleShape)fixture.getShape();
			t.set(circle.getPosition());
			transform.mul(t);
			drawSolidCircle(t, circle.getRadius(), axis.set(transform.vals[Transform.COS], transform.vals[Transform.SIN]), color);
			return;
		}
/*
		if (fixture.getType() == Type.Edge) {
			EdgeShape edge = (EdgeShape)fixture.getShape();
			edge.getVertex1(vertices[0]);
			edge.getVertex2(vertices[1]);
			transform.mul(vertices[0]);
			transform.mul(vertices[1]);
			drawSolidPolygon(vertices, 2, color, true);
			return;
		}
*/
		if (fixture.getType() == Type.Polygon) {
			PolygonShape chain = (PolygonShape)fixture.getShape();
			int vertexCount = chain.getVertexCount();
			for (int i = 0; i < vertexCount; i++) {
				chain.getVertex(i, vertices[i]);
				transform.mul(vertices[i]);
			}
			drawSolidPolygon(vertices, vertexCount, color, true);
			return;
		}
/*
		if (fixture.getType() == Type.Chain) {
			ChainShape chain = (ChainShape)fixture.getShape();
			int vertexCount = chain.getVertexCount();
			for (int i = 0; i < vertexCount; i++) {
				chain.getVertex(i, vertices[i]);
				transform.mul(vertices[i]);
			}
			drawSolidPolygon(vertices, vertexCount, color, false);
		}
*/
	}

	private final static Vector2[] vertices = new Vector2[4];

	private final Vector2 f = new Vector2();
	private final Vector2 v = new Vector2();
	private final Vector2 lv = new Vector2();

	private void drawSolidCircle (Vector2 center, float radius, Vector2 axis, Color color) {
		float angle = 0;
		float angleInc = 2 * (float)Math.PI / 20;
		renderer.setColor(color.r, color.g, color.b, color.a);
		for (int i = 0; i < 20; i++, angle += angleInc) {
			v.set((float)Math.cos(angle) * radius + center.x, (float)Math.sin(angle) * radius + center.y);
			if (i == 0) {
				lv.set(v);
				f.set(v);
				continue;
			}
			renderer.line(lv.x, lv.y, v.x, v.y);
			lv.set(v);
		}
		renderer.line(f.x, f.y, lv.x, lv.y);
		renderer.line(center.x, center.y, 0, center.x + axis.x * radius, center.y + axis.y * radius, 0);
	}

	private void drawSolidPolygon (Vector2[] vertices, int vertexCount, Color color, boolean closed) {
		renderer.setColor(color.r, color.g, color.b, color.a);
		lv.set(vertices[0]);
		f.set(vertices[0]);
		for (int i = 1; i < vertexCount; i++) {
			Vector2 v = vertices[i];
			renderer.line(lv.x, lv.y, v.x, v.y);
			lv.set(v);
		}
		if (closed) renderer.line(f.x, f.y, lv.x, lv.y);
	}

}
