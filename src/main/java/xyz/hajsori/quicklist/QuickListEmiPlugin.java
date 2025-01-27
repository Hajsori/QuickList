package xyz.hajsori.quicklist;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import xyz.hajsori.quicklist.decoration.ToDoButtons;

@EmiEntrypoint
public final class QuickListEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry emiRegistry) {
        emiRegistry.addRecipeDecorator(new ToDoButtons());
        System.out.println("Added To Do Button");
    }
}
