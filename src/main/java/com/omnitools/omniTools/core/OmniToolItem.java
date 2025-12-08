package com.omnitools.omniTools.core;

import com.omnitools.omniTools.api.WrenchContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

public class OmniToolItem extends Item {
    public OmniToolItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var level = context.getLevel();
        var player = context.getPlayer();
        var pos = context.getClickedPos();
        var face = context.getClickedFace();

        if (player != null) {
            WrenchContext wrenchContext = new WrenchContext(
                    level,
                    pos,
                    face,
                    player,
                    context.getItemInHand()
            );

            InteractionResult result = WrenchHandlerRegistry.handle(wrenchContext);
            if (result.consumesAction()) {
                return result;
            }
        }

        return super.useOn(context);
    }
}
