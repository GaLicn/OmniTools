package com.omnitools.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.omnitools.OmniTools;
import com.omnitools.core.ModItems;
import com.omnitools.core.OmniToolItem;
import com.omnitools.core.ToolMode;
import com.omnitools.network.NetworkHandler;
import com.omnitools.network.SyncToolModePacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = OmniTools.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientEvents {
    public static KeyMapping CYCLE_MODE_KEYBIND;

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(
                    ModItems.OMNI_WRENCH.get(),
                    new ResourceLocation(OmniTools.MODID, "mode"),
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
        });
    }

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

    @Mod.EventBusSubscriber(modid = OmniTools.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class GameEvents {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.START) {
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            if (player != null && mc.screen == null) {
                while (CYCLE_MODE_KEYBIND != null && CYCLE_MODE_KEYBIND.consumeClick()) {
                    ItemStack mainHandStack = player.getMainHandItem();
                    if (mainHandStack.getItem() == ModItems.OMNI_WRENCH.get()) {
                        ToolMode oldMode = OmniToolItem.getMode(mainHandStack);
                        OmniToolItem.cycleMode(mainHandStack);
                        ToolMode newMode = OmniToolItem.getMode(mainHandStack);

                        NetworkHandler.CHANNEL.sendToServer(new SyncToolModePacket(newMode.getId()));

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
