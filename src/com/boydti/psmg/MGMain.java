package com.boydti.psmg;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import com.boydti.psmg.command.CopyRegions;
import com.boydti.psmg.command.FormatMap;
import com.boydti.psmg.event.MainListener;
import com.boydti.psmg.generator.MegaGen;
import com.intellectualcrafters.plot.commands.MainCommand;

public class MGMain extends JavaPlugin {

    public static MGMain plugin;

    public static FileConfiguration config;

    public static int RADIUS;

    @Override
    public void onEnable() {
        MGMain.plugin = this;
        setupConfig();
        Bukkit.getPluginManager().registerEvents(new MainListener(), this);
        MainCommand.getInstance().addCommand(new CopyRegions());
        MainCommand.getInstance().addCommand(new FormatMap());
    }

    public void setupConfig() {
        if (MGMain.config == null) {
            plugin.saveDefaultConfig();
        }
        MGMain.config = plugin.getConfig();
        config.set("version", plugin.getDescription().getVersion());

        final Map<String, Object> options = new HashMap<>();

        options.put("generate-radius", 2);

        for (final Entry<String, Object> node : options.entrySet()) {
            if (!config.contains(node.getKey())) {
                config.set(node.getKey(), node.getValue());
            }
        }

        MGMain.RADIUS = config.getInt("generate-radius");

        plugin.saveConfig();
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(final String worldName, final String id) {
        return new MegaGen(worldName);
    }
}
