package xyz.hajsori.quicklist.event;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;
import xyz.hajsori.quicklist.screen.QuickListScreen;

public class KeyInputHandler {
    public static final String KEY_CATEGORY_QUICKLIST = "key.category.quicklist.quicklist";
    public static final String KEY_OPEN_GUI = "key.quicklist.open_gui";
    public static KeyMapping openGuiKey = new KeyMapping(
            KEY_OPEN_GUI,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_N,
            KEY_CATEGORY_QUICKLIST
    );

    @SubscribeEvent
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(openGuiKey);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (openGuiKey.consumeClick()) {
            Minecraft.getInstance().setScreen(new QuickListScreen());
        }
    }
}
