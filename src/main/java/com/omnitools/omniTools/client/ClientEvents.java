package com.omnitools.omniTools.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.omnitools.omniTools.compat.entangled.EntangledHighlightHandler;
import com.omnitools.omniTools.core.OmniToolItem;
import com.omnitools.omniTools.core.ModItems;
import com.omnitools.omniTools.core.ToolMode;
import com.omnitools.omniTools.network.SyncToolModePacket;
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
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
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
                    (stack, level, entity, seed) -> {
                        ToolMode mode = OmniToolItem.getMode(stack);
                        if (mode == ToolMode.LINK) {
                            return 1.0F;
                        }
                        if (mode == ToolMode.RENAME) {
                            return 2.0F;
                        }
                        return 0.0F;
                    }
            );

            if (ModList.get().isLoaded("entangled")) {
                NeoForge.EVENT_BUS.addListener(EntangledHighlightHandler::onRenderWorld);
            }
        });
    }

    //TODO 日后改成轮盘呼出选择
    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        CYCLE_MODE_KEYBIND = new KeyMapping(
                "key.omnitools.cycle_mode",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_ALT,
                "key.categories.omnitools"
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

                        // 通知玩家模式已切换（整句使用翻译键，模式名作为参数）
                        player.displayClientMessage(
                            Component.translatable(
                                    "message.omnitools.mode_switched",
                                    Component.translatable(newMode.getTranslationKey())
                            ),
                            true
                        );
                    }
                }
            }
        }
    }
}
