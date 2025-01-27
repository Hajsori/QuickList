package xyz.hajsori.quicklist.event;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import xyz.hajsori.quicklist.QuickList;
import net.minecraftforge.client.settings.KeyConflictContext;

public class Keybindings {
    public static final Keybindings INSTANCE = new Keybindings();

    private Keybindings() {}

    private static final String CATEGORY = "key.category." + QuickList.MODID + ".quicklist";

    public final KeyMapping openGuiKey = new KeyMapping(
            "key." + QuickList.MODID + ".open_gui",
            KeyConflictContext.IN_GAME,
            InputConstants.getKey(InputConstants.KEY_N, -1),
            CATEGORY
    );
}
