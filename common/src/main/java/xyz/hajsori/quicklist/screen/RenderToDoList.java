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
    public RenderToDoList(GuiGraphics context, DeltaTracker tickDeltaManager) {
        try {
            List<EmiRecipe> allRecipes = EmiApi.getRecipeManager().getRecipes();
            List<ToDoRecipe> recipes = new java.util.ArrayList<>(List.of());

            File todo = new File(Minecraft.getInstance().gameDirectory, "config/quicklist/todo.json");
            File configDir = todo.getParentFile();
            if (configDir.exists() && todo.exists()) {
                try (FileReader reader = new FileReader(todo)) {
                    JsonObject jsonObject = Variables.gson.fromJson(reader, JsonObject.class);
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
                            String itemId = itemObject.get("id").getAsString();
                            int amount = itemObject.get("amount").getAsInt();

                            allRecipes.stream().filter((emiRecipe) -> emiRecipe.getId().toString().equals(itemId)).findFirst().ifPresent(recipe -> recipes.add(new ToDoRecipe(recipe, amount)));
                        }

                        try (FileWriter writer = new FileWriter(todo)) {
                            Variables.gson.toJson(jsonObject, writer);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (!recipes.isEmpty()) {
                int y = Minecraft.getInstance().getWindow().getGuiScaledHeight();
                List<ToDoIngredient> ingredients = new java.util.ArrayList<>(List.of());
                for (ToDoRecipe recipe : recipes) {
                    for (EmiIngredient ingredient : recipe.getRecipe().getInputs()) {
                        List<EmiStack> stacks = ingredient.getEmiStacks().stream().filter((stack) -> !Objects.equals(stack.getId(), ResourceLocation.fromNamespaceAndPath("emi", "empty"))).toList();
                        if (stacks.isEmpty()) {
                            continue;
                        }

                        boolean found = false;
                        for (ToDoIngredient toDoIngredient : ingredients) {
                            if (toDoIngredient.getStacks().equals(stacks)) {
                                found = true;

                                toDoIngredient.addAmount(stacks.getFirst().getAmount());
                            }
                        }

                        if (!found) {
                            ingredients.add(new ToDoIngredient(stacks, recipe.getAmount()));
                            y -= (int) (10 * 0.75 + 26);
                        }
                    }
                }

                y += 4;
                y /= 2;

                int x = Minecraft.getInstance().getWindow().getGuiScaledWidth() - 192;
                PoseStack pose = context.pose();
                for (ToDoRecipe recipe : recipes) {
                    pose.pushPose();
                    pose.scale(0.75f, 0.75f, 0.75f);
                    context.drawString(Minecraft.getInstance().font, recipe.getIdentifier().toString(), (int) (x / 0.75), (int) (y / 0.75), 0xFF707070, false);
                    pose.popPose();
                    y += (int) (10 * 0.75);
                    context.drawString(Minecraft.getInstance().font, ((MutableComponent) recipe.getRecipe().getOutputs().getFirst().getName()).setStyle(Style.EMPTY.withBold(true)), x, y, 0xFFFFFFFF, false);
                    String itemCount = recipe.getRecipe().getOutputs().getFirst().getAmount() * recipe.getAmount() + "x";
                    context.drawString(Minecraft.getInstance().font, itemCount, x + 192 - Minecraft.getInstance().font.width(itemCount), y, 0xFFFFFFFF, false);
                    y += 10;
                }
                context.fill(x, y, x + 192, y + 1, 0xFFFFFFFF);
                y += 4;

                for (ToDoIngredient ingredient : ingredients) {
                    List<EmiStack> stacks = ingredient.getStacks();
                    EmiStack item = stacks.get((int) Math.floor((double) System.currentTimeMillis() / 1000 % stacks.size()));

                    context.drawString(Minecraft.getInstance().font, item.getName(), x, y, 0xFFFFFFFF, false);

                    assert Minecraft.getInstance().player != null;
                    int playerItemCount = 0;
                    Inventory playerInventory = Minecraft.getInstance().player.getInventory();
                    for (EmiStack stack : stacks) {
                        playerItemCount += playerInventory.countItem(stack.getItemStack().getItem());
                    }
                    String itemCount = playerItemCount + "/" + ingredient.getAmount() * ingredient.getCraftAmount();
                    int lineWidth = (int) Math.min(173, 173 * ((double) playerItemCount / (ingredient.getAmount() * ingredient.getCraftAmount())));

                    context.drawString(Minecraft.getInstance().font, itemCount, x + 173 - Minecraft.getInstance().font.width(itemCount), y, 0xFFFFFFFF, false);
                    item.render(context, x + 176, y - 2, 1);

                    context.fill(x, y + 10, x + 173, y + 12, 0x64000000);
                    if (lineWidth > 0) {
                        context.fill(x, y + 10, x + lineWidth, y + 12, -1);
                    }

                    y += 16;
                }
            }
        } catch (Exception ignored) {}
    }
}
