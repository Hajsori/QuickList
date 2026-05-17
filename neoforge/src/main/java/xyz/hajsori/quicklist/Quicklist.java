package xyz.hajsori.quicklist;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.common.NeoForge;
import xyz.hajsori.quicklist.event.KeyInputHandler;
import xyz.hajsori.quicklist.renderer.ToDoListRenderer;

@Mod(Constants.MOD_ID)
public class Quicklist {
    public Quicklist(IEventBus modEventBus) {
        modEventBus.addListener(KeyInputHandler::registerBindings);
        NeoForge.EVENT_BUS.addListener(KeyInputHandler::onClientTick);

        CommonClass.init();

        modEventBus.addListener(this::registerGuiLayersEvent);
    }

    public void registerGuiLayersEvent(RegisterGuiLayersEvent event) {
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath("quicklist", "todo_list"), new ToDoListRenderer());
    }
}
