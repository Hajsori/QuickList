package xyz.hajsori.quicklist;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import xyz.hajsori.quicklist.event.KeyInputHandler;
import xyz.hajsori.quicklist.screen.RenderSearchScreen;
import xyz.hajsori.quicklist.screen.RenderToDoList;

public class QuickListClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KeyInputHandler.register();


        ClientTickEvents.END_CLIENT_TICK.register((client) -> {
            new RenderSearchScreen();
        });

        HudRenderCallback.EVENT.register(RenderToDoList::new);
    }
}
