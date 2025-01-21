package xyz.hajsori.quicklist;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import xyz.hajsori.quicklist.common.ToDoIngredient;
import xyz.hajsori.quicklist.common.ToDoRecipe;
import xyz.hajsori.quicklist.event.KeyInputHandler;
import xyz.hajsori.quicklist.screen.QuickListScreen;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.ColorHelper;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class QuickListClient implements ClientModInitializer {
    Gson gson = new Gson();
    public String oldInput = "";

    @Override
    public void onInitializeClient() {
        KeyInputHandler.register(this);


        ClientTickEvents.END_CLIENT_TICK.register((client) -> {
            Screen currentScreen = MinecraftClient.getInstance().currentScreen;
            ClientPlayerEntity player = MinecraftClient.getInstance().player;

            if (currentScreen instanceof QuickListScreen && player != null) {
                String input = ((QuickListScreen) currentScreen).searchField.getText();
                String math = input
                        .replace("x", String.valueOf(player.getX()))
                        .replace("y", String.valueOf(player.getY()))
                        .replace("z", String.valueOf(player.getZ()))
                        .replace("pi", String.valueOf(Math.PI))
                        .replace("e", String.valueOf(Math.E))
                        .replace("yaw", String.valueOf(Math.sin(Math.toRadians(player.getYaw()))));

                if (input.contains("+") || input.contains("-") || input.contains("*") || input.contains("/") || input.contains("%")) {
                    try {
                        Expression expression = new ExpressionBuilder(math).build();
                        double result = expression.evaluate();
                        result = result % 1 == 0 ? (int) result : result;

                        ((QuickListScreen) currentScreen).buttons.getFirst().setMessage(Text.of(("=" + result + "\\").replace(".0\\", "").replace("\\", "")));
                    } catch (Exception ignored) {
                        ((QuickListScreen) currentScreen).buttons.getFirst().setMessage(Text.of("=?"));
                    }

                    for (ButtonWidget button : ((QuickListScreen) currentScreen).buttons) {
                        if (button == ((QuickListScreen) currentScreen).buttons.getFirst()) {
                            button.visible = true;
                            button.setTooltip(Tooltip.of(Text.translatable("screen.quicklist.gui.copy_result")));
                        } else {
                            button.visible = false;
                        }
                    }
                } else if (!Objects.equals(oldInput, input)) {
                    List<EmiStack> items = new java.util.ArrayList<>(List.of());
                    for (EmiStack item : EmiApi.getIndexStacks()) {
                        if (item.getName().getString().toLowerCase().contains(input.toLowerCase())) {
                            items.add(item);
                        }
                    }

                    for (int i = 0; i < 5; i++) {
                        if (items.size() > i && !input.isEmpty()) {
                            EmiStack item = items.get(i);

                            List<Text> tooltipList = item.getTooltipText();
                            MutableText tooltip = Text.empty();
                            for (Text line : tooltipList) {
                                tooltip.append(line);

                                if (line != tooltipList.getLast()) {
                                    tooltip.append("\n");
                                }
                            }

                            ((QuickListScreen) currentScreen).icons.set(i, item);
                            ((QuickListScreen) currentScreen).buttons.get(i).setMessage(item.getName());
                            ((QuickListScreen) currentScreen).buttons.get(i).setTooltip(Tooltip.of(tooltip));
                            ((QuickListScreen) currentScreen).buttons.get(i).visible = true;
                        } else {
                            ((QuickListScreen) currentScreen).icons.set(i, null);
                            ((QuickListScreen) currentScreen).buttons.get(i).visible = false;
                        }
                    }

                    EmiApi.setSearchText(input);
                }

                if (!Objects.equals(oldInput, input)) {
                    oldInput = input;
                }
            }
        });

        HudRenderCallback.EVENT.register((context, tickDeltaManager) -> {
            try {
                List<EmiRecipe> allRecipes = EmiApi.getRecipeManager().getRecipes();
                List<ToDoRecipe> recipes = new java.util.ArrayList<>(List.of());

                File todo = new File(FabricLoader.getInstance().getConfigDir().toFile(), "quicklist/todo.json");
                File configDir = todo.getParentFile();
                if (configDir.exists() && todo.exists()) {
                    try (FileReader reader = new FileReader(todo)) {
                        JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
                        String worldPath;
                        try {
                            worldPath = Objects.requireNonNull(MinecraftClient.getInstance().getServer()).getSavePath(WorldSavePath.ROOT).toFile().getParent();
                        } catch (NullPointerException e) {
                            worldPath = Objects.requireNonNull(MinecraftClient.getInstance().getCurrentServerEntry()).address;
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
                                gson.toJson(jsonObject, writer);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (!recipes.isEmpty()) {
                    int y = MinecraftClient.getInstance().getWindow().getScaledHeight();
                    List<ToDoIngredient> ingredients = new java.util.ArrayList<>(List.of());
                    for (ToDoRecipe recipe : recipes) {
                        for (EmiIngredient ingredient : recipe.getRecipe().getInputs()) {
                            List<EmiStack> stacks = ingredient.getEmiStacks().stream().filter((stack) -> !Objects.equals(stack.getId(), Identifier.of("emi", "empty"))).toList();
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

                    int x = MinecraftClient.getInstance().getWindow().getScaledWidth() - 192;
                    MatrixStack matrices = context.getMatrices();
                    for (ToDoRecipe recipe : recipes) {
                        matrices.push();
                        matrices.scale(0.75f, 0.75f, 0.75f);
                        context.drawText(MinecraftClient.getInstance().textRenderer, recipe.getIdentifier().toString(), (int) (x / 0.75), (int) (y / 0.75), ColorHelper.Argb.getArgb(112, 112, 112), false);
                        matrices.pop();
                        y += (int) (10 * 0.75);
                        context.drawText(MinecraftClient.getInstance().textRenderer, ((MutableText) recipe.getRecipe().getOutputs().getFirst().getName()).setStyle(Style.EMPTY.withBold(true)), x, y, ColorHelper.Argb.getArgb(255, 255, 255), false);
                        String itemCount = recipe.getRecipe().getOutputs().getFirst().getAmount() * recipe.getAmount() + "x";
                        context.drawText(MinecraftClient.getInstance().textRenderer, itemCount, x + 192 - MinecraftClient.getInstance().textRenderer.getWidth(itemCount), y, ColorHelper.Argb.getArgb(255, 255, 255), false);
                        y += 10;
                    }
                    context.drawHorizontalLine(x, x + 192, y, ColorHelper.Argb.getArgb(255, 255, 255));
                    y += 4;

                    for (ToDoIngredient ingredient : ingredients) {
                        List<EmiStack> stacks = ingredient.getStacks();
                        EmiStack item = stacks.get((int) Math.floor((double) System.currentTimeMillis() / 1000 % stacks.size()));

                        context.drawText(MinecraftClient.getInstance().textRenderer, item.getName(), x, y, ColorHelper.Argb.getArgb(255, 255, 255), false);

                        assert MinecraftClient.getInstance().player != null;
                        int playerItemCount = 0;
                        PlayerInventory playerInventory = MinecraftClient.getInstance().player.getInventory();
                        for (EmiStack stack : stacks) {
                            playerItemCount += playerInventory.count(stack.getItemStack().getItem());
                        }
                        String itemCount = playerItemCount + "/" + ingredient.getAmount() * ingredient.getCraftAmount();
                        int lineWidth = (int) Math.min(173, 173 * ((double) playerItemCount / (ingredient.getAmount() * ingredient.getCraftAmount())));

                        context.drawText(MinecraftClient.getInstance().textRenderer, itemCount, x + 173 - MinecraftClient.getInstance().textRenderer.getWidth(itemCount), y, ColorHelper.Argb.getArgb(255, 255, 255), false);
                        item.render(context, x + 176, y - 2, 1);

                        context.drawHorizontalLine(x, x + 173, y + 10, ColorHelper.Argb.withAlpha(100, 0));
                        context.drawHorizontalLine(x, x + 173, y + 11, ColorHelper.Argb.withAlpha(100, 0));
                        if (lineWidth > 0) {
                            context.drawHorizontalLine(x, x + lineWidth, y + 10, -1);
                            context.drawHorizontalLine(x, x + lineWidth, y + 11, -1);
                        }

                        y += 16;
                    }
                }
            } catch (Exception ignored) {}
        });
    }
}
