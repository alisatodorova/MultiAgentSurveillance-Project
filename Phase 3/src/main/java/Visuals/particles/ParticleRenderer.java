package Visuals.particles;

import java.util.List;
import java.util.Map;

import Visuals.engine.graphics.models.RawModel;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjglx.util.vector.Matrix4f;
import org.lwjglx.util.vector.Vector3f;

import Visuals.entities.Camera;
import Visuals.engine.graphics.Loader;
import Visuals.maths.Maths;

public class ParticleRenderer {
	
	private static final float[] VERTICES = {-0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f, -0.5f};
	
	private RawModel quad;
	private ParticleShader shader;
	
	protected ParticleRenderer(Loader loader, Matrix4f projectionMatrix){
		quad = loader.loadToVAO(VERTICES,2);
		shader = new ParticleShader();
		shader.start();
		shader.loadProjectionMatrix(projectionMatrix);
		shader.stop();;
	}
	
	protected void render(Map<ParticleTexture, List<Particle>> particles, Camera camera){
		Matrix4f viewMatrix = Maths.createViewMatrix(camera);
		prepare();
		for(ParticleTexture texture: particles.keySet()){
			GL13.glActiveTexture(GL13.GL_TEXTURE0);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getTextureID());
			for (Particle particle:particles.get(texture)) {
				updateModelViewMatrix(particle.getPosition(), particle.getRotation(), particle.getScale(), viewMatrix);
				shader.loadTextureCoordinateInfo(particle.getTextureOffset1(),particle.getTextureOffset2(), texture.getNumRows(),particle.getBlend());
				GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, quad.getVertexCount());
			}
		}
		finishRendering();
	}

	protected void cleanUp(){
		shader.cleanUp();
	}

	private void updateModelViewMatrix(Vector3f position, float rotation, float scale, Matrix4f viewingMatrix){
		Matrix4f matrix = new Matrix4f();
		Matrix4f.translate(position,matrix,matrix);
		matrix.m00 = viewingMatrix.m00;
		matrix.m01 = viewingMatrix.m10;
		matrix.m02 = viewingMatrix.m20;
		matrix.m10 = viewingMatrix.m01;
		matrix.m11 = viewingMatrix.m11;
		matrix.m12 = viewingMatrix.m21;
		matrix.m20 = viewingMatrix.m02;
		matrix.m21 = viewingMatrix.m12;
		matrix.m22 = viewingMatrix.m22;
		Matrix4f.rotate((float) Math.toRadians(rotation),new Vector3f(0,0,1),matrix,matrix);
		Matrix4f.scale(new Vector3f(scale,scale,scale),matrix,matrix);
		Matrix4f viewMatrix = Matrix4f.mul(viewingMatrix, matrix,null);
		shader.loadModelViewMatrix(viewMatrix);
	}
	
	private void prepare(){
 		shader.start();
		 GL30.glBindVertexArray(quad.getVaoID());
		 GL20.glEnableVertexAttribArray(0);
		 GL11.glEnable(GL11.GL_BLEND);
		 GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);
		 GL11.glDepthMask(false);
	}
	
	private void finishRendering(){
		GL11.glDepthMask(true);
		GL11.glDisable(GL11.GL_BLEND);
		GL20.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
		shader.stop();
	}

}
