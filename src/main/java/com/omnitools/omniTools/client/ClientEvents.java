package com.omnitools.omniTools.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.omnitools.omniTools.core.OmniToolItem;
import com.omnitools.omniTools.core.ModItems;
import com.omnitools.omniTools.core.ToolMode;
import com.omnitools.omniTools.network.SyncToolModePacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = "omnitools", value = Dist.CLIENT)
public class ClientEvents {
    public static KeyMapping CYCLE_MODE_KEYBIND;


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
}
