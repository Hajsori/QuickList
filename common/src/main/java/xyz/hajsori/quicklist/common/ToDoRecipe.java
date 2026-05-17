package xyz.hajsori.quicklist.common;

import dev.emi.emi.api.recipe.EmiRecipe;
import net.minecraft.resources.ResourceLocation;

public class ToDoRecipe {
    private final ResourceLocation identifier;
    private final int amount;
    private final EmiRecipe recipe;

    public ToDoRecipe(EmiRecipe recipe, int amount) {
        this.identifier = recipe.getId();
        this.amount = amount;
        this.recipe = recipe;
    }

    public ResourceLocation getIdentifier() {
        return identifier;
    }

    public int getAmount() {
        return amount;
    }

    public EmiRecipe getRecipe() {
        return recipe;
    }
}
