package com.omnitools.compat.entangled;

import com.mojang.blaze3d.vertex.PoseStack;
import com.omnitools.core.ModItems;
import com.omnitools.core.OmniToolItem;
import com.omnitools.core.ToolMode;
import com.supermartijn642.core.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;

/**
 * 纠缠方块模组的选择方块渲染
 */
public class EntangledHighlightHandler {

    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        Level world = mc.level;
        if (player == null || world == null) {
            return;
        }

        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() != ModItems.OMNI_WRENCH.get()) {
            return;
        }

        if (OmniToolItem.getMode(stack) != ToolMode.LINK) {
            return;
        }

        var nbt = stack.getTag();
        if (nbt == null || !nbt.getBoolean("bound")) {
            return;
        }

        String targetDimension = nbt.getString("dimension");
        if (!targetDimension.equals(world.dimension().location().toString())) {
            return;
        }

        int boundx = nbt.getInt("boundx");
        int boundy = nbt.getInt("boundy");
        int boundz = nbt.getInt("boundz");

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();

        Vec3 camera = RenderUtils.getCameraPosition();
        poseStack.translate(-camera.x, -camera.y, -camera.z);
        poseStack.translate(boundx, boundy, boundz);

        var pos = new net.minecraft.core.BlockPos(boundx, boundy, boundz);
        var shape = world.getBlockState(pos).getOcclusionShape(world, pos);

        RenderUtils.renderShape(poseStack, shape, 235 / 255f, 210 / 255f, 52 / 255f, false);
        RenderUtils.renderShapeSides(poseStack, shape, 235 / 255f, 210 / 255f, 52 / 255f, 30 / 255f, false);

        poseStack.popPose();
    }
}
