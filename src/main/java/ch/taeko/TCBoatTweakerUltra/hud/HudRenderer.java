package ch.taeko.TCBoatTweakerUltra.hud;

import ch.taeko.TCBoatTweakerUltra.client.TCBoatTweakerClient;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class HudRenderer
	   extends DrawableHelper {

    private static final Identifier WIDGETS_TEXTURE = new Identifier("tcboattweaker","textures/widgets.png");
    private final MinecraftClient client;
    private int scaledWidth;
    private int scaledHeight;

    // The index to be used in these scales is the bar type (stored internally as an integer, defined in Config)
    private static final double MIN_V = 0d;
    private static final double MAX_V = 70.83d; // 255 kmh
    private static final double SCALE_V = 4.5d; // Pixels for 1 unit of speed (px*s/m) (BarWidth / (VMax - VMin))
    // V coordinates for each bar type in the texture file
    //                                    Pk Mix Blu
    private static final int BAR_OFF = 10;
    private static final int BAR_ON =  15;

    // Used for lerping
    private double displayedSpeed = 0.0d;

    public HudRenderer(MinecraftClient client) {
	   this.client = client;
    }

    public void render(MatrixStack stack, float tickDelta) {
	   this.scaledWidth = this.client.getWindow().getScaledWidth();
	   this.scaledHeight = this.client.getWindow().getScaledHeight();
	   int i = this.scaledWidth / 2;

	   // Render boilerplate
	   RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
	   RenderSystem.setShaderTexture(0, WIDGETS_TEXTURE);
	   RenderSystem.enableBlend();
	   RenderSystem.defaultBlendFunc();

	   // Lerping the displayed speed with the actual speed against how far we are into the tick not only is mostly accurate,
	   // but gives the impression that it's being updated faster than 20 hz (which it isn't)
	   this.displayedSpeed = MathHelper.lerp(tickDelta, this.displayedSpeed, TCBoatTweakerClient.hudData.speed);

	   this.drawTexture(stack, i - 91, this.scaledHeight - 83, 0, 50, 182, 20);
	   this.renderBar(stack, i - 91, this.scaledHeight - 83);
		  // Speed and drift angle
	   this.typeCentered(stack, String.format("%03.0f km/h", this.displayedSpeed * 3.6d), i - 58, this.scaledHeight - 76, 0xFFFFFF);
	   this.typeCentered(stack, String.valueOf(TCBoatTweakerClient.hudData.rpm) + " RPM", i, this.scaledHeight - 76, 0xFFFFFF);
	   this.typeCentered(stack, String.valueOf((int) TCBoatTweakerClient.hudData.torque) + " NM", i + 58, this.scaledHeight - 76, 0xFFFFFF);

	   RenderSystem.disableBlend();
    }

    /** Renders the speed bar atop the HUD, uses displayedSpeed to, well, display the speed. */
    private void renderBar(MatrixStack stack, int x, int y) {
	   this.drawTexture(stack, x, y, 0, BAR_OFF, 182, 5);
	   if(TCBoatTweakerClient.hudData.speed < MIN_V) return;
	   if(TCBoatTweakerClient.hudData.speed > MAX_V) {
		  if(this.client.world.getTime() % 2 == 0) return;
		  this.drawTexture(stack, x, y, 0, BAR_ON, 182, 5);
		  return;
	   }
	   this.drawTexture(stack, x, y, 0, BAR_ON, (int)((this.displayedSpeed - MIN_V) * SCALE_V), 5);
    }

    /** Implementation is cloned from the notchian ping display in the tab player list.	 */

    /** Renders a piece of text centered horizontally on an X coordinate. */
    private void typeCentered(MatrixStack stack, String text, int centerX, int y, int color) {
	   this.client.textRenderer.drawWithShadow(stack, text, centerX - this.client.textRenderer.getWidth(text) / 2, y, color);
    }
}