package dev.dercoderjo.quicklist.common;

import dev.emi.emi.api.stack.EmiStack;

import java.util.List;

public class ToDoIngredient {
    private final List<EmiStack> stacks;
    private long amount;
    private final int craftAmount;

    public ToDoIngredient(List<EmiStack> stacks, int craftAmount) {
        this.stacks = stacks;
        this.amount = stacks.getFirst().getAmount();
        this.craftAmount = craftAmount;
    }

    public List<EmiStack> getStacks() {
        return stacks;
    }

    public long getAmount() {
        return amount;
    }
    public long addAmount(long amount) {
        return this.amount += amount;
    }

    public int getCraftAmount() {
        return craftAmount;
    }
}
