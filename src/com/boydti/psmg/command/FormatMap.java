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
import org.bukkit.World;
import org.bukkit.util.FileUtil;

import com.boydti.psmg.MGMain;
import com.boydti.psmg.generator.MegaPlotWorld;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.commands.CommandCategory;
import com.intellectualcrafters.plot.commands.RequiredType;
import com.intellectualcrafters.plot.commands.SubCommand;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.SetBlockQueue;
import com.plotsquared.general.commands.CommandDeclaration;
@CommandDeclaration(
        command = "formatmap",
        permission = "plots.formatmap",
        category = CommandCategory.ACTIONS,
        requiredType = RequiredType.NONE,
        description = "Format a WP maps",
        usage = "/plot formatmap <map> <plotwidth> <road> <height>"
)
public class FormatMap extends SubCommand {
    public static String downloads, version;

    @Override
    public boolean onCommand(final PlotPlayer plr, final String... args) {
        if (args.length != 4) {
            MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot formatmap <map> <plot> <road> <height>");
            return false;
        }
        final World map = Bukkit.getWorld(args[0]);
        if (map != null) {
            // todo
            return false;
        }

        final int plot = Integer.parseInt(args[1]);
        final int road = Integer.parseInt(args[2]);
        final int height = Integer.parseInt(args[3]);
        final int width = plot + road;

        if ((width % 512) != 0) {
            // todo
            return false;
        }

        int lower;
        if ((road % 2) == 0) {
            lower = (road / 2) - 1;
        } else {
            lower = (road / 2);
        }
        final String world = plr.getLocation().getWorld();
        final int halfwidth = width / 2;
        final int upper = (lower - halfwidth) + plot + 1;

        final PlotBlock roadblock = new PlotBlock((short) 155, (byte) 0);
        final PlotBlock wallblock = new PlotBlock((short) 7, (byte) 0);
        final PlotBlock borderblock = new PlotBlock((short) 44, (byte) 0);
        final PlotBlock zero = new PlotBlock((short) 0, (byte) 0);

        for (int x = -halfwidth; x < halfwidth; x++) {
            for (int z = -halfwidth; z < halfwidth; z++) {
                if ((x < (lower - halfwidth)) || (z < (lower - halfwidth)) || (x > upper) || (z > upper)) {
                    // road
                    for (int y = 0; y < height; y++) {
                        SetBlockQueue.setBlock(world, x, y, z, roadblock);
                    }
                    for (int y = height; y < 256; y++) {
                        SetBlockQueue.setBlock(world, x, y, z, zero);
                    }
                } else if ((x == (lower - halfwidth)) || (z == (lower - halfwidth)) || (x == upper) || (z == upper)) {
                    // wall
                    for (int y = 0; y < height; y++) {
                        SetBlockQueue.setBlock(world, x, y, z, wallblock);
                    }
                    SetBlockQueue.setBlock(world, x, height, z, borderblock);
                    for (int y = height + 1; y < 256; y++) {
                        SetBlockQueue.setBlock(world, x, y, z, zero);
                    }
                }
            }
        }
        PS.get().config.set("worlds." + args[0] + ".road.size", road);
        try {
            PS.get().config.save(PS.get().configFile);
        } catch (final IOException e1) {
            e1.printStackTrace();
        }
        final int MCA_WIDTH = (width / 512);
        final int RADIUS;
        if ((MCA_WIDTH % 2) == 0) {
            RADIUS = MCA_WIDTH / 2;
        } else {
            RADIUS = (MCA_WIDTH / 2) + 1;
        }
        final int MCA_HALFWIDTH = MCA_WIDTH / 2;
        SetBlockQueue.addNotify(new Runnable() {
            @Override
            public void run() {
                final File dest = new File(MGMain.plugin.getDataFolder() + File.separator + "worlds" + File.separator + args[0]);
                if (!dest.exists()) {
                    dest.mkdirs();
                }
                for (int i = -MCA_HALFWIDTH; i < RADIUS; i++) {
                    for (int j = -MCA_HALFWIDTH; j < RADIUS; j++) {
                        // copy file
                        final File region = MegaPlotWorld.getNewMca(world, i, j);
                        final String name = "r." + (i + MCA_HALFWIDTH) + "." + (j + MCA_HALFWIDTH) + ".mca";
                        final File destRegion = new File(dest + File.separator + name);
                        try {
                            destRegion.createNewFile();
                        } catch (final IOException e) {
                            e.printStackTrace();
                        }
                        System.out.print("COPYING: " + region + " | " + destRegion);
                        FileUtil.copy(region, destRegion);
                        MainUtil.sendMessage(plr, "Done!");
                    }
                }
            }
        });
        return true;
    }
}
