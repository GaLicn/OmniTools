package com.omnitools.core;

import com.omnitools.api.IUseHandler;
import com.omnitools.api.UseContext;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class UseHandlerRegistry {
    private static final List<IUseHandler> HANDLERS = new ArrayList<>();

    public static void register(IUseHandler handler) {
        HANDLERS.add(handler);
    }

    public static InteractionResultHolder<ItemStack> handle(UseContext context) {
        for (IUseHandler handler : HANDLERS) {
            if (!handler.canHandle(context)) {
                continue;
            }
            InteractionResultHolder<ItemStack> result = handler.handle(context);
            if (result != null && result.getResult().consumesAction()) {
                return result;
            }
        }
        return InteractionResultHolder.pass(context.getStack());
    }
}
