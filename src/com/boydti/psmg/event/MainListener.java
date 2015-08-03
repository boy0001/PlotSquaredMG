package com.boydti.psmg.event;

import java.util.HashSet;

import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import com.boydti.psmg.generator.MegaPlotWorld;
import com.intellectualcrafters.plot.PS;

public class MainListener implements Listener {

    public static HashSet<String> worlds = new HashSet<>();

    @EventHandler
    public void onChunkLoad(final ChunkLoadEvent event) {
        if (worlds.contains(event.getWorld().getName())) {
            final Chunk chunk = event.getChunk();
            final String world = event.getWorld().getName();
            final MegaPlotWorld pw = (MegaPlotWorld) PS.get().getPlotWorld(world.substring(5));
            final int size = pw.MCA_WIDTH * 32;
            if ((chunk.getX() >= 0) && (chunk.getZ() >= 0) && ((chunk.getX() / size) == 0) && ((chunk.getZ() / size) == 0)) {
                return;
            }
            event.getChunk().unload(false, false);
        }
    }

    //    @EventHandler
    //    public void onChunkUnload(ChunkUnloadEvent event) {
    //        if (worlds.contains(event.getWorld().getName())) {
    //            Chunk chunk = event.getChunk();
    //            String world = event.getWorld().getName();
    //            MegaPlotWorld pw = (MegaPlotWorld) PlotSquared.getPlotWorld(world.substring(5));
    //            int size = pw.MCA_WIDTH * 32;
    //            if (chunk.getX() >= 0 && chunk.getZ() >= 0 && chunk.getX() / size == 0 && chunk.getZ() / size == 0) {
    //                event.getChunk().load(false);
    //                return;
    //            }
    //        }
    //    }
}
