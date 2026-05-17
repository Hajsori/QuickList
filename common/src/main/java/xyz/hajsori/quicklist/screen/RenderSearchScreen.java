package xyz.hajsori.quicklist.screen;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import xyz.hajsori.quicklist.Variables;

import org.jetbrains.annotations.Nullable;
import java.util.*;

public class RenderSearchScreen {
    public RenderSearchScreen() {
        Screen currentScreen = Minecraft.getInstance().screen;
        LocalPlayer player = Minecraft.getInstance().player;

        if (!(currentScreen instanceof SearchScreen searchScreen) || player == null) {
            return;
        }

        String input = searchScreen.searchFieldWidget.getValue();

        if (!Objects.equals(Variables.oldInput, input)) {
            Variables.oldInput = input;

            for (int i = 0; i < 5; i++) {
                searchScreen.icons.set(i, null);
                searchScreen.buttons.get(i).visible = false;
            }

            if (input.contains("+") || input.contains("-") || input.contains("*") || input.contains("/") || input.contains("%")) {
                @Nullable Number result = calculate(input, player);
                Button button = searchScreen.buttons.getFirst();

                button.visible = true;
                button.setMessage(Component.literal("=" + (result == null ? "?" : result)));
            } else if (!input.isEmpty()) {
                List<EmiStack> items = getItems(input);

                for (int i = 0; i < items.size(); i++) {
                    EmiStack item = items.get(i);

                    List<Component> tooltipList = item.getTooltipText();
                    MutableComponent tooltip = Component.empty();

                    for (Component line : tooltipList) {
                        tooltip.append(line);

                        if (line != tooltipList.getLast()) {
                            tooltip.append("\n");
                        }
                    }

                    searchScreen.icons.set(i, item);
                    searchScreen.buttons.get(i).setMessage(item.getName());
                    searchScreen.buttons.get(i).setTooltip(Tooltip.create(tooltip));
                    searchScreen.buttons.get(i).visible = true;
                }

                EmiApi.setSearchText(input);
            }
        }
    }

    private @Nullable Number calculate(String input, Player player) {
        String math = input
                .replace("x", String.valueOf(player.getX()))
                .replace("y", String.valueOf(player.getY()))
                .replace("z", String.valueOf(player.getZ()));

        try {
            Expression expr = new ExpressionBuilder(math).build();
            double result = expr.evaluate();

            if (result % 1 == 0) {
                return (int) result;
            } else {
                return result;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private List<EmiStack> getItems(String input) {
        List<EmiStack> items = new ArrayList<>(List.of());
        var emiItems = EmiApi.getIndexStacks().stream()
                .filter((stack) ->
                        stack.getName().getString().toLowerCase().contains(input.toLowerCase()) ||
                                stack.getId().toString().toLowerCase().contains(input.toLowerCase())
                ).toList();

        items.addAll(
                emiItems.subList(0, Math.min(5, emiItems.size()))
        );

        return items;
    }
}
