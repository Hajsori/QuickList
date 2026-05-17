package xyz.hajsori.quicklist.renderer;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import xyz.hajsori.quicklist.screen.RenderToDoList;

public class ToDoListRenderer implements LayeredDraw.Layer {
    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        new RenderToDoList(guiGraphics, deltaTracker);
    }
}
