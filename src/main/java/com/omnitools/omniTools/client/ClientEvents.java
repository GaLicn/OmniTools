package com.omnitools.omniTools.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.omnitools.omniTools.core.OmniToolItem;
import com.omnitools.omniTools.core.ModItems;
import com.omnitools.omniTools.core.ToolMode;
import com.omnitools.omniTools.network.SyncToolModePacket;
import com.supermartijn642.core.render.RenderUtils;
import com.supermartijn642.core.render.RenderWorldEvent;
import com.supermartijn642.entangled.EntangledBinderItem;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = "omnitools", value = Dist.CLIENT)
public class ClientEvents {
    public static KeyMapping CYCLE_MODE_KEYBIND;

    // 在客户端初始化时注册模型属性
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(
                    ModItems.OMNI_WRENCH.get(),
                    ResourceLocation.fromNamespaceAndPath("omnitools", "mode"),
                    (stack, level, entity, seed) -> OmniToolItem.getMode(stack) == ToolMode.LINK ? 1.0F : 0.0F
            );
        });
    }

    //TODO 日后改成轮盘呼出选择
    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        CYCLE_MODE_KEYBIND = new KeyMapping(
                "key.omnitools.cycle_mode",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_U,
                "key.categories.gameplay"
        );
        event.register(CYCLE_MODE_KEYBIND);
    }

    @EventBusSubscriber(modid = "omnitools", value = Dist.CLIENT)
    public static class GameEvents {
        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Pre event) {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;

            if (player != null && mc.screen == null) {
                while (CYCLE_MODE_KEYBIND != null && CYCLE_MODE_KEYBIND.consumeClick()) {
                    ItemStack mainHandStack = player.getMainHandItem();
                    // 检查主手是否拿着 omni 扳手
                    if (mainHandStack.getItem() == ModItems.OMNI_WRENCH.get()) {
                        ToolMode oldMode = OmniToolItem.getMode(mainHandStack);
                        OmniToolItem.cycleMode(mainHandStack);
                        ToolMode newMode = OmniToolItem.getMode(mainHandStack);
                        
                        // 发送网络包到服务器同步模式
                        PacketDistributor.sendToServer(new SyncToolModePacket(newMode.getId()));
                        
                        // 通知玩家模式已切换
                        player.displayClientMessage(
                            Component.literal("§6[OmniTool] §rMode switched to: §e" + newMode.getDisplayName()),
                            true
                        );
                    }
                }
            }
        }
    }







    //纠缠方块模组的选择方块渲染
    @EventBusSubscriber(modid = "omnitools", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
    public static class HighlightEvents {
        @SubscribeEvent
        public static void onRenderWorld(RenderWorldEvent event) {
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

            EntangledBinderItem.BinderTarget target = stack.get(EntangledBinderItem.BINDER_TARGET);
            if (target == null || !target.dimension().equals(world.dimension().location())) {
                return;
            }

            var pos = target.pos();

            event.getPoseStack().pushPose();
            Vec3 camera = RenderUtils.getCameraPosition();
            event.getPoseStack().translate(-camera.x, -camera.y, -camera.z);
            event.getPoseStack().translate(pos.getX(), pos.getY(), pos.getZ());

            var shape = world.getBlockState(pos).getOcclusionShape(world, pos);
            RenderUtils.renderShape(event.getPoseStack(), shape, 235 / 255f, 210 / 255f, 52 / 255f, false);
            RenderUtils.renderShapeSides(event.getPoseStack(), shape, 235 / 255f, 210 / 255f, 52 / 255f, 30 / 255f, false);

            event.getPoseStack().popPose();
        }
    }
}
