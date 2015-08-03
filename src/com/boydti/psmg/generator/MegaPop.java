package com.boydti.psmg.generator;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Chunk;
import org.bukkit.World;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.PseudoRandom;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.plotsquared.bukkit.generator.BukkitPlotPopulator;

public class MegaPop extends BukkitPlotPopulator {

    @Override
    public void populate(final World world, final RegionWrapper region, final PseudoRandom r, final int X, final int Z) {
        final MegaPlotWorld pw = (MegaPlotWorld) PS.get().getPlotWorld(world.getName());
        if (pw.BASE_WORLD == null) {
            return;
        }
        final int CX = X >> 4;
        final int CZ = Z >> 4;
        int cx = (CX) % (pw.MCA_WIDTH * 32);
        int cz = (CZ) % (pw.MCA_WIDTH * 32);
        if (cx < 0) {
            cx += (pw.MCA_WIDTH * 32);
        }
        if (cz < 0) {
            cz += (pw.MCA_WIDTH * 32);
        }
        cx += pw.mx * 32;
        cz += pw.mx * 32;
        final ChunkLoc loc = new ChunkLoc(cx, cz);
        final HashMap<RelBlockLoc, Byte> result = pw.DATA.get(loc);
        if (result != null) {
            final Chunk chunk = world.getChunkAt(CX, CZ);
            for (final Entry<RelBlockLoc, Byte> entry : result.entrySet()) {
                final RelBlockLoc b = entry.getKey();
                chunk.getBlock(b.x, b.y, b.z).setData(entry.getValue());
            }
        }
    }
}
