package xyz.hajsori.quicklist.event;

import xyz.hajsori.quicklist.QuickListClient;
import xyz.hajsori.quicklist.screen.QuickListScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeyInputHandler {
    public static final String KEY_CATEGORY_QUICKLIST = "key.category.quicklist.quicklist";
    public static final String KEY_OPEN_GUI = "key.quicklist.open_gui";

    public static KeyBinding openGuiKey;

    public static void registerKeyInputs(QuickListClient quickListClient) {
        ClientTickEvents.END_CLIENT_TICK.register((client) -> {
            if (openGuiKey.wasPressed()) {
                MinecraftClient.getInstance().setScreen(new QuickListScreen(quickListClient));
            }
        });
    }

    public static void register(QuickListClient quickListClient) {
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                KEY_OPEN_GUI,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                KEY_CATEGORY_QUICKLIST
        ));

        registerKeyInputs(quickListClient);
    }
}
