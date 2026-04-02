package work.crash.skyraah.sanityjs.event;

import com.mojang.blaze3d.vertex.PoseStack;
import croissantnova.sanitydim.config.ConfigProxy;
import croissantnova.sanitydim.config.SanityIndicatorLocation;
import dev.latvian.mods.kubejs.client.ClientEventJS;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import work.crash.skyraah.sanityjs.util.IPlayerSanity;

public class SanityIndicatorRenderEventJS extends ClientEventJS {
    private final ForgeGui gui;
    private final PoseStack poseStack;
    private final float partialTick;
    private final int scw;
    private final int sch;

    public SanityIndicatorRenderEventJS(ForgeGui gui, PoseStack poseStack, float partialTick, int scw, int sch) {
        this.gui = gui;
        this.poseStack = poseStack;
        this.partialTick = partialTick;
        this.scw = scw;
        this.sch = sch;
    }

    @Info("get the partial tick")
    public float getPartialTick() {
        return partialTick;
    }

    @Info("get the Gui")
    public ForgeGui getGui() {
        return gui;
    }

    @Info("get the GuiGraphics, used to add your custom gui.")
    public PoseStack getGuiGraphics() {
        return poseStack;
    }

    @Info("get the screen width")
    public int getScw() {
        return scw;
    }

    @Info("get the screen height")
    public int getSch() {
        return sch;
    }

    @Info("Get the local player")
    public LocalPlayer getPlayer() {
        return Minecraft.getInstance().player;
    }

    @Info("Get the current sanity value on the same 0-100 scale as the server events")
    public float getSanity() {
        LocalPlayer player = getPlayer();
        return player == null ? 0.0F : ((IPlayerSanity) player).getSanity();
    }

    @HideFromJS
    @Info("Get the current raw sanity value on the base mod's 0-1 scale")
    public float getRawSanity() {
        return 1.0F - (getSanity() / 100.0F);
    }

    @Info("Get the player's current dimension id")
    public ResourceLocation getDimensionId() {
        LocalPlayer player = getPlayer();
        return player == null ? null : player.level().dimension().location();
    }

    @Info("Get the configured indicator scale for the current dimension")
    public float getIndicatorScale() {
        ResourceLocation dimensionId = getDimensionId();
        return dimensionId == null ? 0.0F : ConfigProxy.getIndicatorScale(dimensionId);
    }

    @Info("Get the configured indicator location for the current dimension")
    public SanityIndicatorLocation getIndicatorLocation() {
        ResourceLocation dimensionId = getDimensionId();
        return dimensionId == null ? null : ConfigProxy.getIndicatorLocation(dimensionId);
    }

    @Info("Check whether the base Sanity mod would render its vanilla indicator in this dimension")
    public boolean shouldRenderVanillaIndicator() {
        ResourceLocation dimensionId = getDimensionId();
        return dimensionId != null && ConfigProxy.getRenderIndicator(dimensionId);
    }
}
