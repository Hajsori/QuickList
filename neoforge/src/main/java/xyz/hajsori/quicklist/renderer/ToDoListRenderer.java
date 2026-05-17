package xyz.hajsori.quicklist.renderer;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import org.jetbrains.annotations.NotNull;
import xyz.hajsori.quicklist.screen.RenderToDoList;

import javax.annotation.Nullable;

public class ToDoListRenderer implements LayeredDraw.Layer {
    @Override
    public void render(@NotNull GuiGraphics guiGraphics, @Nullable DeltaTracker deltaTracker) {
        new RenderToDoList(guiGraphics, deltaTracker);
    }
}
