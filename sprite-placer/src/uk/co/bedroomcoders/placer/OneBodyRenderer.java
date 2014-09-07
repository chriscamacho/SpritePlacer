
package uk.co.bedroomcoders.placer;

import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class OneBodyRenderer extends Box2DDebugRenderer {

    OneBodyRenderer() {
        super();
    }

    public void renderOneBody(Body bod, Matrix4 proj) {
        renderer.begin(ShapeType.Line);
        renderer.setProjectionMatrix(proj);
        renderBody(bod);
        renderer.end();
    }
}
