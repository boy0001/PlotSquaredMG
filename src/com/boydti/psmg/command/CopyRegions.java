////////////////////////////////////////////////////////////////////////////////////////////////////
// PlotSquared - A plot manager and world generator for the Bukkit API                             /
// Copyright (c) 2014 IntellectualSites/IntellectualCrafters                                       /
//                                                                                                 /
// This program is free software; you can redistribute it and/or modify                            /
// it under the terms of the GNU General Public License as published by                            /
// the Free Software Foundation; either version 3 of the License, or                               /
// (at your option) any later version.                                                             /
//                                                                                                 /
// This program is distributed in the hope that it will be useful,                                 /
// but WITHOUT ANY WARRANTY; without even the implied warranty of                                  /
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                   /
// GNU General Public License for more details.                                                    /
//                                                                                                 /
// You should have received a copy of the GNU General Public License                               /
// along with this program; if not, write to the Free Software Foundation,                         /
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA                               /
//                                                                                                 /
// You can contact us via: support@intellectualsites.com                                           /
////////////////////////////////////////////////////////////////////////////////////////////////////
package com.boydti.psmg.command;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.util.FileUtil;

import com.boydti.psmg.MGMain;
import com.boydti.psmg.generator.MegaPlotWorld;
import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.commands.SubCommand;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Configuration;
import com.intellectualcrafters.plot.config.ConfigurationNode;
import com.intellectualcrafters.plot.generator.SquarePlotWorld;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.object.SetupObject;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.SetupUtils;

public class CopyRegions extends SubCommand {
    public static String downloads, version;

    public CopyRegions() {
        super("copyregion", "plots.copyregion", "Copy plot regions", "copyregion <template>", "copyregions", CommandCategory.DEBUG, true);
    }


    @Override
    public boolean execute(final PlotPlayer plr, final String... args) {
        if (args.length < 1) {
            MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot copyregion <world>");
            return false;
        }
        boolean generate = true;
        if (args.length == 2) {
            if (args[2].equalsIgnoreCase("-g")) {
                generate = false;
            }
            else {
                MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot copyregion <world> [-g]");
                MainUtil.sendMessage(plr, "The -g argument is optional, and will only copy the .mca files, but not generate the world");
                return false;
                
            }
        }
        File dest = new File(MGMain.plugin.getDataFolder() + File.separator + "worlds" + File.separator + args[0]);
        Plot plot = MainUtil.getPlot(plr.getLocation());
        if (plot ==  null) {
            MainUtil.sendMessage(plr, "You need to be standing in a plot to use that command");
            return false;
        }
        PlotWorld pw = PlotSquared.getPlotWorld(plot.world);
        if (!(pw instanceof SquarePlotWorld)) {
            MainUtil.sendMessage(plr, "INVALID PLOT WORLD!");
            return false;
        }
        File newWorldFolder = new File(Bukkit.getWorldContainer() + File.separator + args[0]);
        if (newWorldFolder.exists()) {
            MainUtil.sendMessage(plr, "World is already taken, please delete it or choose a new name.");
            return false;
        }
        
        SquarePlotWorld spw = (SquarePlotWorld) pw;
        if ((spw.PLOT_WIDTH + spw.ROAD_WIDTH) % 512 != 0) {
            MainUtil.sendMessage(plr, "Plot width + Road width is not multiple of 512: ");
            MainUtil.sendMessage(plr, spw.PLOT_WIDTH + " + " + spw.ROAD_WIDTH + " = " + (spw.PLOT_WIDTH + spw.ROAD_WIDTH));
            return false;
        }
        int width = (spw.PLOT_WIDTH + spw.ROAD_WIDTH + 511) / 512;
        if (dest.exists()) {
            MegaPlotWorld.deleteDirectory(dest);
        }
        dest.mkdirs();
        
        Bukkit.getWorld(plot.world).save();
        
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < width; j++) {
                int x = (plot.id.x - 1) * width + i;
                int z = (plot.id.y - 1) * width + j;
                File region = MegaPlotWorld.getNewMca(plot.world, z, x);
                File destRegion = new File(dest + File.separator + region.getName());
                try {
                    destRegion.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.print("COPYING: " + region + " | " + destRegion);
                FileUtil.copy(region, destRegion);
            }
        }
        if (generate) {
            MainUtil.sendMessage(plr, "Starting world creation...");
            SetupObject setup = new SetupObject();
            ConfigurationNode node = new ConfigurationNode("road.width", SquarePlotWorld.ROAD_WIDTH_DEFAULT, "Road width", Configuration.INTEGER, true);
            node.setValue(spw.ROAD_WIDTH + "");
            setup.step = new ConfigurationNode[] { node };
            setup.world = args[0];
            setup.setupGenerator = "PlotSquaredMG";
            String result = SetupUtils.manager.setupWorld(setup);
            plr.teleport(new Location(result, 0, 64, 0));
            MainUtil.sendMessage(plr, "&aCreated world!");
        }
        else {
            MainUtil.sendMessage(plr, "Done!");
        }
        return true;
    }
}
