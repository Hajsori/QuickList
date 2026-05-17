package xyz.hajsori.quicklist.event;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import xyz.hajsori.quicklist.screen.SearchScreen;

public class KeyInputHandler {
    public static final String KEY_CATEGORY_QUICKLIST = "key.category.quicklist.quicklist";
    public static final String KEY_OPEN_GUI = "key.quicklist.open_gui";

    public static KeyMapping openGuiKey;

    public static void registerKeyInputs() {
        ClientTickEvents.END_CLIENT_TICK.register((client) -> {
            if (openGuiKey.consumeClick()) {
                Minecraft.getInstance().setScreen(new SearchScreen());
            }
        });
    }

    public static void register() {
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                KEY_OPEN_GUI,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                KEY_CATEGORY_QUICKLIST
        ));

        registerKeyInputs();
    }
}
