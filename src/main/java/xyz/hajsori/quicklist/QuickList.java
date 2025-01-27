package xyz.hajsori.quicklist;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.telemetry.events.WorldLoadEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.slf4j.Logger;
import xyz.hajsori.quicklist.common.ToDoIngredient;
import xyz.hajsori.quicklist.common.ToDoRecipe;
import xyz.hajsori.quicklist.event.Keybindings;
import xyz.hajsori.quicklist.screen.QuickListScreen;

import javax.swing.plaf.ColorUIResource;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Mod(QuickList.MODID)
public class QuickList {
    public static final String MODID = "quicklist";
    private static final Logger LOGGER = LogUtils.getLogger();
    static List<ToDoRecipe> recipes = null;
    static Gson gson = new Gson();

    public QuickList(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new QuickListClient());
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.error("QuickList is not made for Servers. Disabling automatically");
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class QuickListClient {
        public String oldInput = "";
        public QuickListClient client = this;

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("HELLO FROM CLIENT");
        }

        @SubscribeEvent
        public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
            event.register(Keybindings.INSTANCE.openGuiKey);
        }

        @SubscribeEvent
        public void onClientTick(TickEvent.ClientTickEvent event) {
            if (Keybindings.INSTANCE.openGuiKey.isDown()) {
                Minecraft.getInstance().setScreen(new QuickListScreen(this.client));
            }



            Screen currentScreen = Minecraft.getInstance().screen;
            LocalPlayer player = Minecraft.getInstance().player;

            if (currentScreen instanceof QuickListScreen && player != null) {
                String input = ((QuickListScreen) currentScreen).searchField.getValue();
                String math = input
                        .replace("x", String.valueOf(player.getX()))
                        .replace("y", String.valueOf(player.getY()))
                        .replace("z", String.valueOf(player.getZ()))
                        .replace("pi", String.valueOf(Math.PI))
                        .replace("e", String.valueOf(Math.E))
                        .replace("yaw", String.valueOf(Math.sin(Math.toRadians(player.getYRot()))));

                if (input.contains("+") || input.contains("-") || input.contains("*") || input.contains("/") || input.contains("%")) {
                    try {
                        Expression expression = new ExpressionBuilder(math).build();
                        double result = expression.evaluate();
                        result = result % 1 == 0 ? (int) result : result;

                        ((QuickListScreen) currentScreen).buttons.get(0).setMessage(Component.literal(("=" + result + "\\").replace(".0\\", "").replace("\\", "")));
                    } catch (Exception ignored) {
                        ((QuickListScreen) currentScreen).buttons.get(0).setMessage(Component.literal("=?"));
                    }

                    for (Button button : ((QuickListScreen) currentScreen).buttons) {
                        if (button == ((QuickListScreen) currentScreen).buttons.get(0)) {
                            button.visible = true;
                            button.setTooltip(Tooltip.create(Component.translatable("screen.quicklist.gui.copy_result")));
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

                            List<Component> tooltipList = item.getTooltipText();
                            MutableComponent tooltip = Component.empty();
                            for (Component line : tooltipList) {
                                tooltip.append(line);

                                if (line != tooltipList.get(tooltipList.size() - 1)) {
                                    tooltip.append("\n");
                                }
                            }

                            ((QuickListScreen) currentScreen).icons.set(i, item);
                            ((QuickListScreen) currentScreen).buttons.get(i).setMessage(item.getName());
                            ((QuickListScreen) currentScreen).buttons.get(i).setTooltip(Tooltip.create(tooltip));
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
        }

        @SubscribeEvent
        public void onCustomizeGuiOverlay(RenderGuiOverlayEvent event) {
            Font font = Minecraft.getInstance().font;
            GuiGraphics graphics = event.getGuiGraphics();

            if (recipes == null) {
                updateToDoList();
            }

            if (recipes != null && !recipes.isEmpty()) {
                int y = event.getWindow().getGuiScaledHeight();
                List<ToDoIngredient> ingredients = new ArrayList<>();
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

                                toDoIngredient.addAmount(stacks.get(0).getAmount());
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

                int x = event.getWindow().getGuiScaledWidth() - 192;
                PoseStack pose = graphics.pose();
                for (ToDoRecipe recipe : recipes) {
                    pose.pushPose();
                    pose.scale(0.75f, 0.75f, 0.75f);
                    graphics.drawString(font, recipe.getIdentifier().toString(), (int) (x / 0.75), (int) (y / 0.75), ColorUIResource.DARK_GRAY.hashCode(), false);
                    pose.popPose();
                    y += (int) (10 * 0.75);
                    graphics.drawString(font, MutableComponent.create(recipe.getRecipe().getOutputs().get(0).getName().getContents()).setStyle(Style.EMPTY.withBold(true)), x, y, ColorUIResource.WHITE.hashCode(), false);
                    String itemCount = recipe.getRecipe().getOutputs().get(0).getAmount() * recipe.getAmount() + "x";
                    graphics.drawString(font, itemCount, x + 192 - font.width(itemCount), y, ColorUIResource.WHITE.hashCode(), false);
                    y += 10;
                }
                graphics.hLine(x, x + 192, y, ColorUIResource.WHITE.hashCode());
                y += 4;

                for (ToDoIngredient ingredient : ingredients) {
                    List<EmiStack> stacks = ingredient.getStacks();
                    EmiStack item = stacks.get((int) Math.floor((double) System.currentTimeMillis() / 1000 % stacks.size()));

                    graphics.drawString(font, item.getName(), x, y, ColorUIResource.WHITE.hashCode(), false);

                    assert Minecraft.getInstance().player != null;
                    int playerItemCount = 0;
                    Inventory playerInventory = Minecraft.getInstance().player.getInventory();
                    for (EmiStack stack : stacks) {
                        playerItemCount += playerInventory.countItem(stack.getItemStack().getItem());
                    }
                    String itemCount = playerItemCount + "/" + ingredient.getAmount() * ingredient.getCraftAmount();
                    int lineWidth = (int) Math.min(173, 173 * ((double) playerItemCount / (ingredient.getAmount() * ingredient.getCraftAmount())));
                    graphics.drawString(font, itemCount, x + 173 - font.width(itemCount), y, ColorUIResource.WHITE.hashCode(), false);
                    item.render(graphics, x + 176, y - 2, 1);

                    RenderSystem.disableDepthTest();
                    RenderSystem.enableBlend();
                    RenderSystem.setShaderColor(1, 1, 1, 0.4F);
                    graphics.hLine(x, x + 173, y + 10, ColorUIResource.BLACK.hashCode());
                    graphics.hLine(x, x + 173, y + 11, ColorUIResource.BLACK.hashCode());
                    RenderSystem.setShaderColor(1, 1, 1, 1F);
                    if (lineWidth > 0) {
                        graphics.hLine(x, x + lineWidth, y + 10, -1);
                        graphics.hLine(x, x + lineWidth, y + 11, -1);
                    }

                    y += 16;
                }
            }
        }
    }


    public static void updateToDoList() {
        File todo = new File(FMLPaths.CONFIGDIR.get().toFile(), "quicklist/todo.json");
        List<EmiRecipe> allRecipes = EmiApi.getRecipeManager().getRecipes();

        if (todo.exists() && !allRecipes.isEmpty()) {
            try (FileReader reader = new FileReader(todo)) {
                JsonObject toDoList = gson.fromJson(reader, JsonObject.class);
                if (toDoList == null || toDoList.size() == 0) {
                    return;
                }
                recipes = new ArrayList<>();

                String worldPath = null;
                if (Minecraft.getInstance().getCurrentServer() != null) {
                    worldPath = Minecraft.getInstance().getCurrentServer().ip;
                } else if (Minecraft.getInstance().getSingleplayerServer() != null) {
                    worldPath = Minecraft.getInstance().getSingleplayerServer().getWorldData().getLevelName();
                }
                if (worldPath != null) {
                    String worldName = worldPath.substring(worldPath.lastIndexOf("\\") + 1);
                    if (toDoList.has(worldName)) {
                        JsonArray worldArray = toDoList.get(worldName).getAsJsonArray();
                        for (int i = 0; i < worldArray.size(); i++) {
                            JsonObject itemObject = worldArray.get(i).getAsJsonObject();
                            String itemId = itemObject.get("id").getAsString();
                            int amount = itemObject.get("amount").getAsInt();

                            allRecipes.stream().filter((emiRecipe) -> emiRecipe.getId().toString().equals(itemId)).findFirst().ifPresent((recipe) -> recipes.add(new ToDoRecipe(recipe, amount)));
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
