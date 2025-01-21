package xyz.hajsori.quicklist.screen;

import xyz.hajsori.quicklist.QuickListClient;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.List;

public class QuickListScreen extends Screen {
    private final QuickListClient client;

    public QuickListScreen(QuickListClient quickListClient) {
        super(Text.stringifiedTranslatable("screen.quicklist.gui"));
        this.client = quickListClient;
    }


    public TextFieldWidget searchField;
    public List<EmiStack> icons = Arrays.asList(new EmiStack[5]);
    public List<ButtonWidget> buttons = Arrays.asList(new ButtonWidget[5]);

    @Override
    protected void init() {
        searchField = new TextFieldWidget(this.textRenderer, width / 2 - 128, height / 2 - 12, 256, 24, Text.empty());

        searchField.setText(client.oldInput);
        client.oldInput = "";
        searchField.setFocused(true);
        addDrawableChild(searchField);
        setFocused(searchField);

        int buttonY = height / 2 - 12;
        for (int i = 0; i < 5; i++) {
            buttonY += 24;
            int buttonX = width / 2 - 128;

            int finalI = i;
            ButtonWidget buttonWidget = ButtonWidget
                    .builder(Text.empty(), (button) -> {
                        EmiStack icon = icons.get(finalI);

                        if (icon != null) {
                            List<EmiRecipe> recipes = EmiApi.getRecipeManager().getRecipesByOutput(icon);
                            if (!recipes.isEmpty()) {
                                EmiApi.displayRecipe(recipes.getFirst());
                            }
                        } else {
                            MinecraftClient.getInstance().keyboard.setClipboard(button.getMessage().getString().replace("=", ""));
                        }
                    })
                    .dimensions(buttonX, buttonY, 256, 24)
                    .build();

            buttonWidget.visible = false;

            buttons.set(i, buttonWidget);
            addDrawableChild(buttonWidget);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        searchField.render(context, mouseX, mouseY, delta);

        int buttonY = height / 2 - 12;
        for (EmiStack icon : icons) {
            if (icon == null) {
                break;
            }

            buttonY += 24;
            icon.render(context, width / 2 - 124, buttonY + 4, delta);
        }
        for (ButtonWidget button : buttons) {
            button.render(context, mouseX, mouseY, delta);
        }
    }
}

