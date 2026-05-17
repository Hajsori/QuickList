package xyz.hajsori.quicklist;

import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class CommonClass {
    public static void init() {
        File todo = new File(Minecraft.getInstance().gameDirectory, "quick_todo_list.json");

        if (todo.exists()) {
            try (FileReader reader = new FileReader(todo)) {
                Variables.toDoRecipes = Variables.gson.fromJson(reader, JsonObject.class);
            } catch (IOException ignored) {}
        }
    }
}
