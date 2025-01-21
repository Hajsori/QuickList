package xyz.hajsori.quicklist.common;

import dev.emi.emi.api.recipe.EmiRecipe;
import net.minecraft.util.Identifier;

public class ToDoRecipe {
    private final Identifier identifier;
    private final int amount;
    private final EmiRecipe recipe;

    public ToDoRecipe(EmiRecipe recipe, int amount) {
        this.identifier = recipe.getId();
        this.amount = amount;
        this.recipe = recipe;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public int getAmount() {
        return amount;
    }

    public EmiRecipe getRecipe() {
        return recipe;
    }
}
