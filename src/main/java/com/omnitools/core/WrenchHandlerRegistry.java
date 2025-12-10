package com.omnitools.core;

import com.omnitools.api.IWrenchHandler;
import com.omnitools.api.WrenchContext;
import net.minecraft.world.InteractionResult;

import java.util.ArrayList;
import java.util.List;

public class WrenchHandlerRegistry {
    private static final List<IWrenchHandler> HANDLERS = new ArrayList<>();

    public static void register(IWrenchHandler handler) {
        HANDLERS.add(handler);
    }

    public static InteractionResult handle(WrenchContext context) {
        for (IWrenchHandler handler : HANDLERS) {
            if (!handler.canHandle(context)) {
                continue;
            }
            InteractionResult result = handler.handle(context);
            if (result.consumesAction()) {
                return result;
            }
        }
        return InteractionResult.PASS;
    }
}
