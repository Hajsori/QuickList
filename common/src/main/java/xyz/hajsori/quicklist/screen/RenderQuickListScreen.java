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
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import xyz.hajsori.quicklist.Variables;

import java.util.List;
import java.util.Objects;

public class RenderQuickListScreen {
    public RenderQuickListScreen() {
        Screen currentScreen = Minecraft.getInstance().screen;
        LocalPlayer player = Minecraft.getInstance().player;

        if (currentScreen instanceof QuickListScreen && player != null) {
            String input = ((QuickListScreen) currentScreen).searchField.getMessage().getString();
            String math = input
                    .replace("x", String.valueOf(player.getX()))
                    .replace("y", String.valueOf(player.getY()))
                    .replace("z", String.valueOf(player.getZ()))
                    .replace("pi", String.valueOf(Math.PI))
                    .replace("e", String.valueOf(Math.E));

            if (input.contains("+") || input.contains("-") || input.contains("*") || input.contains("/") || input.contains("%")) {
                try {
                    Expression expression = new ExpressionBuilder(math).build();
                    double result = expression.evaluate();
                    result = result % 1 == 0 ? (int) result : result;

                    ((QuickListScreen) currentScreen).buttons.getFirst().setMessage(Component.literal(("=" + result + "\\").replace(".0\\", "").replace("\\", "")));
                } catch (Exception ignored) {
                    ((QuickListScreen) currentScreen).buttons.getFirst().setMessage(Component.literal("=?"));
                }

                for (Button button : ((QuickListScreen) currentScreen).buttons) {
                    if (button == ((QuickListScreen) currentScreen).buttons.getFirst()) {
                        button.visible = true;
                        button.setTooltip(Tooltip.create(Component.translatable("screen.quicklist.gui.copy_result")));
                    } else {
                        button.visible = false;
                    }
                }
            } else if (!Objects.equals(Variables.oldInput, input)) {
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

                            if (line != tooltipList.getLast()) {
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

            if (!Objects.equals(Variables.oldInput, input)) {
                Variables.oldInput = input;
            }
        }
    }
}
