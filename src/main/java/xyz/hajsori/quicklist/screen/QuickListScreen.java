package xyz.hajsori.quicklist.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import xyz.hajsori.quicklist.QuickList;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QuickListScreen extends Screen {
    private final QuickList.QuickListClient client;

    public QuickListScreen(QuickList.QuickListClient client) {
        super(Component.translatable("screen.quicklist.gui"));
        this.client = client;
    }


    public EditBox searchField;
    public List<EmiStack> icons = Arrays.asList(new EmiStack[5]);
    public List<Button> buttons = Arrays.asList(new Button[5]);

    @Override
    protected void init() {
        searchField = new EditBox(this.font, width / 2 - 128, height / 2 - 12, 256, 24, Component.empty());

        searchField.setValue(client.oldInput);
        client.oldInput = "";
        searchField.setFocused(true);
        addRenderableWidget(searchField);
        setFocused(searchField);

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
                                EmiApi.displayRecipe(recipes.get(0));
                            }
                        } else {
                            assert this.minecraft != null;
                            this.minecraft.keyboardHandler.setClipboard(button.getMessage().getString().replace("=", ""));
                        }
                    })
                    .bounds(buttonX, buttonY, 256, 24)
                    .build();

            buttonWidget.visible = false;

            buttons.set(i, buttonWidget);
            addRenderableWidget(buttonWidget);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        searchField.render(graphics, mouseX, mouseY, delta);

        int buttonY = height / 2 - 12;
        for (EmiStack icon : icons) {
            if (icon == null) {
                break;
            }

            buttonY += 24;
            icon.render(graphics, width / 2 - 124, buttonY + 4, delta);
        }
        for (Button button : buttons) {
            button.render(graphics, mouseX, mouseY, delta);
        }
    }
}
