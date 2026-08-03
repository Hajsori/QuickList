package xyz.hajsori.quicklist.common;

import net.minecraft.resources.ResourceLocation;

public class ToDoEntry {
    private final ResourceLocation identifier;
    private final int amount;

    public ToDoEntry(ResourceLocation identifier, int amount) {
        this.identifier = identifier;
        this.amount = amount;
    }

    public ResourceLocation getIdentifier() {
        return identifier;
    }

    public int getAmount() {
        return amount;
    }
}
