package xyz.hajsori.quicklist.screen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import xyz.hajsori.quicklist.Variables;
import xyz.hajsori.quicklist.common.ToDoIngredient;
import xyz.hajsori.quicklist.common.ToDoRecipe;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class RenderToDoList {
    public RenderToDoList(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        List<ToDoRecipe> toDoRecipes = readToDoRecipes();
        var player = Minecraft.getInstance().player;

        if (toDoRecipes.isEmpty() || player == null) {
            return;
        }

        int y = ((Minecraft.getInstance().getWindow().getGuiScaledHeight() + 4) / 2);
        int x = Minecraft.getInstance().getWindow().getGuiScaledWidth() - 192;
        List<ToDoIngredient> ingredients = new java.util.ArrayList<>(List.of());
        PoseStack pose = guiGraphics.pose();

        for (ToDoRecipe recipe : toDoRecipes) {
            for (EmiIngredient ingredient : recipe.getRecipe().getInputs()) {
                List<EmiStack> stacks = ingredient.getEmiStacks().stream().filter((stack) -> !Objects.equals(stack.getId(), ResourceLocation.fromNamespaceAndPath("emi", "empty"))).toList();
                if (stacks.isEmpty()) {
                    continue;
                }

                var ing = ingredients.stream().filter((toDoIngredient) -> toDoIngredient.getStacks().equals(stacks)).findFirst().orElse(null);

                if (ing == null) {
                    ing = new ToDoIngredient(stacks, recipe.getAmount());
                    y -= (int) (10 * 0.75 + 26);
                } else {
                    ing.addAmount(stacks.getFirst().getAmount());
                }

                ingredients.removeIf((toDoIngredient) -> toDoIngredient.getStacks().equals(stacks));
                ingredients.add(ing);
            }
        }

        for (ToDoRecipe recipe : toDoRecipes) {
            var emiRecipe = recipe.getRecipe();

            pose.pushPose();
            pose.scale(0.75f, 0.75f, 0.75f);

            guiGraphics.drawString(Minecraft.getInstance().font, emiRecipe.getCategory().getName(), (int) (x / 0.75), (int) (y / 0.75), 0xFF707070, false);

            pose.popPose();
            y += (int) (10 * 0.75);

            guiGraphics.drawString(Minecraft.getInstance().font, ((MutableComponent) emiRecipe.getOutputs().getFirst().getName()).setStyle(Style.EMPTY.withBold(true)), x, y, 0xFFFFFFFF, false);

            String itemCount = emiRecipe.getOutputs().getFirst().getAmount() * recipe.getAmount() + "x";
            guiGraphics.drawString(Minecraft.getInstance().font, itemCount, x + 192 - Minecraft.getInstance().font.width(itemCount), y, 0xFFFFFFFF, false);
            y += 10;
        }

        guiGraphics.fill(x, y, x + 192, y + 1, 0xFFFFFFFF);
        y += 4;

        for (ToDoIngredient ingredient : ingredients) {
            List<EmiStack> stacks = ingredient.getStacks();
            EmiStack item = stacks.get((int) Math.floor((double) System.currentTimeMillis() / 1000 % stacks.size()));

            guiGraphics.drawString(Minecraft.getInstance().font, item.getName(), x, y, 0xFFFFFFFF, false);

            int playerItemCount = 0;
            Inventory playerInventory = player.getInventory();
            for (EmiStack stack : stacks) {
                playerItemCount += playerInventory.countItem(stack.getItemStack().getItem());
            }

            String itemCount = playerItemCount + "/" + ingredient.getAmount() * ingredient.getCraftAmount();
            int lineWidth = (int) Math.min(173, 173 * ((double) playerItemCount / (ingredient.getAmount() * ingredient.getCraftAmount())));

            guiGraphics.drawString(Minecraft.getInstance().font, itemCount, x + 173 - Minecraft.getInstance().font.width(itemCount), y, 0xFFFFFFFF, false);
            item.render(guiGraphics, x + 176, y - 2, 1);

            guiGraphics.fill(x, y + 10, x + 173, y + 12, 0x64000000);
            if (lineWidth > 0) {
                guiGraphics.fill(x, y + 10, x + lineWidth, y + 12, -1);
            }

            y += 16;
        }
    }

    private List<ToDoRecipe> readToDoRecipes() {
        List<ToDoRecipe> toDoRecipes = new java.util.ArrayList<>(List.of());
        JsonObject jsonObject = Variables.toDoRecipes;

        if (jsonObject != null) {
            String worldPath;

            try {
                worldPath = Objects.requireNonNull(Minecraft.getInstance().getLevelSource()).getName();
            } catch (NullPointerException e) {
                worldPath = Objects.requireNonNull(Minecraft.getInstance().getCurrentServer()).ip;
            }

            String worldName = worldPath.substring(worldPath.lastIndexOf("\\") + 1);

            if (jsonObject.has(worldName)) {
                JsonArray worldArray = jsonObject.get(worldName).getAsJsonArray();
                for (int i = 0; i < worldArray.size(); i++) {
                    JsonObject itemObject = worldArray.get(i).getAsJsonObject();
                    var recipeId = ResourceLocation.parse(itemObject.get("id").getAsString());
                    int amount = itemObject.get("amount").getAsInt();

                    var recipe = EmiApi.getRecipeManager().getRecipe(recipeId);
                    if (recipe != null) {
                        toDoRecipes.add(new ToDoRecipe(recipe, amount));
                    }
                }
            }
        }

        return toDoRecipes;
    }
}
