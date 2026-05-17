package xyz.hajsori.quicklist.screen;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import xyz.hajsori.quicklist.Variables;

import java.util.Arrays;
import java.util.List;

public class QuickListScreen extends Screen {
    public QuickListScreen() {
        super(Component.translatable("screen.quicklist.gui"));
    }


    public StringWidget searchField;
    public List<EmiStack> icons = Arrays.asList(new EmiStack[5]);
    public List<Button> buttons = Arrays.asList(new Button[5]);

    @Override
    protected void init() {
        searchField = new StringWidget(width / 2 - 128, height / 2 - 12, 256, 24, Component.empty(), this.font);

        searchField.setMessage(Component.literal(Variables.oldInput));
        Variables.oldInput = "";
        searchField.setFocused(true);
        this.addRenderableWidget(searchField);
        this.setFocused(searchField);

        int buttonY = height / 2 - 12;
        for (int i = 0; i < 5; i++) {
            buttonY += 24;
            int buttonX = width / 2 - 128;

            int finalI = i;
            Button buttonWidget = Button
                    .builder(Component.empty(), (button) -> {
                        EmiStack icon = icons.get(finalI);

                        if (icon != null) {
                            List<EmiRecipe> recipes = EmiApi.getRecipeManager().getRecipesByOutput(icon);
                            if (!recipes.isEmpty()) {
                                EmiApi.displayRecipe(recipes.getFirst());
                            }
                        } else {
                            Minecraft.getInstance().keyboardHandler.setClipboard(button.getMessage().getString().replace("=", ""));
                        }
                    })
                    .pos(buttonX, buttonY)
                    .size(256, 24)
                    .build();

            buttonWidget.visible = false;

            buttons.set(i, buttonWidget);
            this.addRenderableWidget(buttonWidget);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        searchField.render(context, mouseX, mouseY, delta);

        int buttonY = height / 2 - 12;
        for (EmiStack icon : icons) {
            if (icon == null) {
                break;
            }

            buttonY += 24;
            icon.render(context, width / 2 - 124, buttonY + 4, delta);
        }
        for (Button button : buttons) {
            button.render(context, mouseX, mouseY, delta);
        }
    }
}

