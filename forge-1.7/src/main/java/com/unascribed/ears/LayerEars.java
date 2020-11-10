package com.unascribed.ears;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.unascribed.ears.common.EarsCommon;
import com.unascribed.ears.common.EarsRenderDelegate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.util.ResourceLocation;

public class LayerEars implements EarsRenderDelegate {
	
	private final RenderPlayer render;
	
	private int skipRendering;
	private int stackDepth;
	
	public LayerEars(RenderPlayer render) {
		this.render = render;
	}
	
	public void doRenderLayer(AbstractClientPlayer entity, float limbDistance, float partialTicks) {
		ResourceLocation skin = entity.getLocationSkin();
		ITextureObject tex = Minecraft.getMinecraft().getTextureManager().getTexture(skin);
		if (!entity.isInvisible() && Ears.earsSkinFeatures.containsKey(tex)) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(skin);
			this.skipRendering = 0;
			this.stackDepth = 0;
			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			EarsCommon.render(Ears.earsSkinFeatures.get(tex), this, limbDistance);
			GL11.glDisable(GL12.GL_RESCALE_NORMAL);
			GL11.glDisable(GL11.GL_CULL_FACE);
		}
	}

	@Override
	public void push() {
		stackDepth++;
		GL11.glPushMatrix();
		if (skipRendering > 0) skipRendering++;
	}

	@Override
	public void pop() {
		if (stackDepth <= 0) {
			new Exception("STACK UNDERFLOW").printStackTrace();
			return;
		}
		stackDepth--;
		GL11.glPopMatrix();
		if (skipRendering > 0) skipRendering--;
	}

	@Override
	public void anchorTo(BodyPart part) {
		ModelRenderer model;
		switch (part) {
			case HEAD:
				model = render.modelBipedMain.bipedHead;
				break;
			case LEFT_ARM:
				model = render.modelBipedMain.bipedLeftArm;
				break;
			case LEFT_LEG:
				model = render.modelBipedMain.bipedLeftLeg;
				break;
			case RIGHT_ARM:
				model = render.modelBipedMain.bipedRightArm;
				break;
			case RIGHT_LEG:
				model = render.modelBipedMain.bipedRightLeg;
				break;
			case TORSO:
				model = render.modelBipedMain.bipedBody;
				break;
			default: return;
		}
		if (!model.showModel) {
			if (skipRendering == 0) {
				skipRendering = 1;
			}
			return;
		}
		model.postRender(1/16f);
		ModelBox cuboid = (ModelBox)model.cubeList.get(0);
		GL11.glScalef(1/16f, 1/16f, 1/16f);
		GL11.glTranslatef(cuboid.posX1, cuboid.posY2, cuboid.posZ1);
	}

	@Override
	public void translate(float x, float y, float z) {
		if (skipRendering > 0) return;
		GL11.glTranslatef(x, y, z);
	}

	@Override
	public void rotate(float ang, float x, float y, float z) {
		if (skipRendering > 0) return;
		GL11.glRotatef(ang, x, y, z);
	}

	@Override
	public void renderFront(int u, int v, int w, int h, TexRotation rot, TexFlip flip) {
		if (skipRendering > 0) return;
		Tessellator tess = Tessellator.instance;
		
		float[][] uv = EarsCommon.calculateUVs(u, v, w, h, rot, flip);

		tess.startDrawing(GL11.GL_QUADS);
		tess.setNormal(0, 0, -1);
		tess.addVertexWithUV(0, h, 0, uv[0][0], uv[0][1]);
		tess.addVertexWithUV(w, h, 0, uv[1][0], uv[1][1]);
		tess.addVertexWithUV(w, 0, 0, uv[2][0], uv[2][1]);
		tess.addVertexWithUV(0, 0, 0, uv[3][0], uv[3][1]);
		tess.draw();
	}

	@Override
	public void renderBack(int u, int v, int w, int h, TexRotation rot, TexFlip flip) {
		if (skipRendering > 0) return;
		Tessellator tess = Tessellator.instance;
		
		float[][] uv = EarsCommon.calculateUVs(u, v, w, h, rot, flip.flipHorizontally());
		
		tess.startDrawing(GL11.GL_QUADS);
		tess.setNormal(0, 0, 1);
		tess.addVertexWithUV(0, 0, 0, uv[3][0], uv[3][1]);
		tess.addVertexWithUV(w, 0, 0, uv[2][0], uv[2][1]);
		tess.addVertexWithUV(w, h, 0, uv[1][0], uv[1][1]);
		tess.addVertexWithUV(0, h, 0, uv[0][0], uv[0][1]);
		tess.draw();
	}

	@Override
	public void renderDebugDot(float r, float g, float b, float a) {
		if (skipRendering > 0) return;
		
		GL11.glPointSize(8);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		Tessellator tess = Tessellator.instance;
		tess.startDrawing(GL11.GL_POINTS);
		tess.setColorRGBA_F(r, g, b, a);
		tess.addVertex(0, 0, 0);
		tess.draw();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}
}
