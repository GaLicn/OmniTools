package com.omnitools.datagen;

import com.omnitools.OmniTools;
import com.omnitools.core.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class OmniToolsRecipeProvider extends RecipeProvider {

    public OmniToolsRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> consumer) {
        addCraftingRecipes(consumer);
    }

    private void addCraftingRecipes(Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.OMNI_WRENCH.get())
                .pattern(" bi")
                .pattern(" ab")
                .pattern("a  ")
                .define('i', Items.NETHERITE_INGOT)
                .define('a', Items.OBSIDIAN)
                .define('b', Items.DIAMOND)
                .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .unlockedBy(getHasName(Items.STICK), has(Items.STICK))
                .save(consumer, new ResourceLocation(OmniTools.MODID, "crafting/omni_wrench"));
    }
}
