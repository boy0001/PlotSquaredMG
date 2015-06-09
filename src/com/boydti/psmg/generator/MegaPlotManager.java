package com.boydti.psmg.generator;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.mutable.MutableInt;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.generator.GridPlotManager;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.object.PseudoRandom;
import com.intellectualcrafters.plot.util.BlockManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.SetBlockQueue;
import com.intellectualcrafters.plot.util.TaskManager;

public class MegaPlotManager extends GridPlotManager {

    
    
    @Override
    public boolean clearPlot(final PlotWorld plotworld, final Plot plot, final boolean isDelete, final Runnable whenDone) {
        Location bot = MainUtil.getPlotBottomLoc(plot.world, plot.id);
        Location top = MainUtil.getPlotTopLoc(plot.world, plot.id);
        final World world = Bukkit.getWorld(plot.world);
        final List<ChunkLoc> chunks = new ArrayList<>();
        for (int x = bot.getX() >> 4; x <= top.getX() >> 4; x++) {
            for (int z = bot.getZ() >> 4; z <= top.getZ() >> 4; z++) {
                chunks.add(new ChunkLoc(x, z));
            }
        }
        final MutableInt id = new MutableInt(0);
        id.setValue(TaskManager.runTaskRepeat(new Runnable() {
            @Override
            public void run() {
                if (chunks.size() == 0) {
                    Bukkit.getScheduler().cancelTask(id.intValue());
                    MainUtil.update(plot);
                    whenDone.run();
                    return;
                }
                long start = System.currentTimeMillis();
                while (System.currentTimeMillis() - start < 50 && chunks.size() > 0) {
                    ChunkLoc loc = chunks.remove(0);
                    if (world.loadChunk(loc.x, loc.z, false)) {
                        world.regenerateChunk(loc.x, loc.z);
                    }
                }
            }
        }, 1));
        return true;
    }

    @Override
    public boolean setComponent(final PlotWorld plotworld, final PlotId plotid, final String component, final PlotBlock[] blocks) {
        if (blocks.length == 0) {
            return false;
        }
        switch (component) {
            case "floor": {
                setFloor(plotworld, plotid, blocks[0]);
                return true;
            }
            case "wall": {
                setWallFilling(plotworld, plotid, blocks[0]);
                return true;
            }
            case "border": {
                setWall(plotworld, plotid, blocks[0]);
                return true;
            }
        }
        return false;
    }

    public boolean setFloor(final PlotWorld plotworld, final PlotId plotid, final PlotBlock block) {
        final Location pos1 = MainUtil.getPlotBottomLoc(plotworld.worldname, plotid).add(1, 0, 1);
        final Location pos2 = MainUtil.getPlotTopLoc(plotworld.worldname, plotid).add(1, 0, 1);
        pos1.setY(MegaGen.HEIGHT);
        pos2.setY(MegaGen.HEIGHT + 1);
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, pos1, pos2, block);
        return true;
    }

    public boolean setWallFilling(final PlotWorld plotworld, final PlotId plotid, final PlotBlock block) {
        final Location bottom = MainUtil.getPlotBottomLoc(plotworld.worldname, plotid);
        final Location top = MainUtil.getPlotTopLoc(plotworld.worldname, plotid).add(1, 0, 1);
        int x, z;
        z = bottom.getZ();
        final int length1 = top.getX() - bottom.getX();
        final int length2 = top.getZ() - bottom.getZ();
        final int size = ((length1 * 2) + (length2 * 2)) * (MegaGen.HEIGHT);
        final int[] xl = new int[size];
        final int[] yl = new int[size];
        final int[] zl = new int[size];
        final PlotBlock[] bl = new PlotBlock[size];
        int i = 0;
        new PseudoRandom();
        for (x = bottom.getX(); x <= (top.getX() - 1); x++) {
            for (int y = 1; y <= MegaGen.HEIGHT; y++) {
                xl[i] = x;
                zl[i] = z;
                yl[i] = y;
                bl[i] = block;
                i++;
            }
        }
        x = top.getX();
        for (z = bottom.getZ(); z <= (top.getZ() - 1); z++) {
            for (int y = 1; y <= MegaGen.HEIGHT; y++) {
                xl[i] = x;
                zl[i] = z;
                yl[i] = y;
                bl[i] = block;
                i++;
            }
        }
        z = top.getZ();
        for (x = top.getX(); x >= (bottom.getX() + 1); x--) {
            for (int y = 1; y <= MegaGen.HEIGHT; y++) {
                xl[i] = x;
                zl[i] = z;
                yl[i] = y;
                bl[i] = block;
                i++;
            }
        }
        x = bottom.getX();
        for (z = top.getZ(); z >= (bottom.getZ() + 1); z--) {
            for (int y = 1; y <= MegaGen.HEIGHT; y++) {
                xl[i] = x;
                zl[i] = z;
                yl[i] = y;
                bl[i] = block;
                i++;
            }
        }
        BlockManager.setBlocks(plotworld.worldname, xl, yl, zl, bl);
        return true;
    }

    public boolean setWall(final PlotWorld plotworld, final PlotId plotid, final PlotBlock block) {
        final Location bottom = MainUtil.getPlotBottomLoc(plotworld.worldname, plotid);
        final Location top = MainUtil.getPlotTopLoc(plotworld.worldname, plotid).add(1, 0, 1);
        int x, z;
        z = bottom.getZ();
        new PseudoRandom();
        final int y = MegaGen.HEIGHT + 1;
        for (x = bottom.getX(); x <= (top.getX() - 1); x++) {
            SetBlockQueue.setBlock(plotworld.worldname, x, y, z, block);
        }
        x = top.getX();
        for (z = bottom.getZ(); z <= (top.getZ() - 1); z++) {
            SetBlockQueue.setBlock(plotworld.worldname, x, y, z, block);
        }
        z = top.getZ();
        for (x = top.getX(); x >= (bottom.getX() + 1); x--) {
            SetBlockQueue.setBlock(plotworld.worldname, x, y, z, block);
        }
        x = bottom.getX();
        for (z = top.getZ(); z >= (bottom.getZ() + 1); z--) {
            SetBlockQueue.setBlock(plotworld.worldname, x, y, z, block);
        }
        return true;
    }

    /**
     * PLOT MERGING
     */
    @Override
    public boolean createRoadEast(final PlotWorld plotworld, final Plot plot) {
//        final MegaPlotWorld dpw = (MegaPlotWorld) plotworld;
//        final Location pos1 = getPlotBottomLocAbs(plotworld, plot.id);
//        final Location pos2 = getPlotTopLocAbs(plotworld, plot.id);
//        final int sx = pos2.getX() + 1;
//        final int ex = (sx + dpw.ROAD_WIDTH) - 1;
//        final int sz = pos1.getZ() - 1;
//        final int ez = pos2.getZ() + 2;
//        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx, Math.min(dpw.WALL_HEIGHT, dpw.ROAD_HEIGHT) + 1, sz + 1), new Location(plotworld.worldname, ex + 1, 257, ez), new PlotBlock((short) 0, (byte) 0));
//        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx, 1, sz + 1), new Location(plotworld.worldname, ex + 1, dpw.PLOT_HEIGHT, ez), new PlotBlock((short) 7, (byte) 0));
//        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx, 1, sz + 1), new Location(plotworld.worldname, sx + 1, dpw.WALL_HEIGHT + 1, ez), dpw.WALL_FILLING);
//        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx, dpw.WALL_HEIGHT + 1, sz + 1), new Location(plotworld.worldname, sx + 1, dpw.WALL_HEIGHT + 2, ez), dpw.WALL_BLOCK);
//        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, ex, 1, sz + 1), new Location(plotworld.worldname, ex + 1, dpw.WALL_HEIGHT + 1, ez), dpw.WALL_FILLING);
//        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, ex, dpw.WALL_HEIGHT + 1, sz + 1), new Location(plotworld.worldname, ex + 1, dpw.WALL_HEIGHT + 2, ez), dpw.WALL_BLOCK);
//        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, 1, sz + 1), new Location(plotworld.worldname, ex, dpw.ROAD_HEIGHT + 1, ez), dpw.ROAD_BLOCK);
        return true;
    }

    @Override
    public boolean createRoadSouth(final PlotWorld plotworld, final Plot plot) {
//        final MegaPlotWorld dpw = (MegaPlotWorld) plotworld;
//        final Location pos1 = getPlotBottomLocAbs(plotworld, plot.id);
//        final Location pos2 = getPlotTopLocAbs(plotworld, plot.id);
//        final int sz = pos2.getZ() + 1;
//        final int ez = (sz + dpw.ROAD_WIDTH) - 1;
//        final int sx = pos1.getX() - 1;
//        final int ex = pos2.getX() + 2;
//        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, Math.min(dpw.WALL_HEIGHT, dpw.ROAD_HEIGHT) + 1, sz), new Location(plotworld.worldname, ex, 257, ez + 1), new PlotBlock((short) 0, (byte) 0));
//        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, 0, sz), new Location(plotworld.worldname, ex, 1, ez + 1), new PlotBlock((short) 7, (byte) 0));
//        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, 1, sz), new Location(plotworld.worldname, ex, dpw.WALL_HEIGHT + 1, sz + 1), dpw.WALL_FILLING);
//        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, dpw.WALL_HEIGHT + 1, sz), new Location(plotworld.worldname, ex, dpw.WALL_HEIGHT + 2, sz + 1), dpw.WALL_BLOCK);
//        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, 1, ez), new Location(plotworld.worldname, ex, dpw.WALL_HEIGHT + 1, ez + 1), dpw.WALL_FILLING);
//        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, dpw.WALL_HEIGHT + 1, ez), new Location(plotworld.worldname, ex, dpw.WALL_HEIGHT + 2, ez + 1), dpw.WALL_BLOCK);
//        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, 1, sz + 1), new Location(plotworld.worldname, ex, dpw.ROAD_HEIGHT + 1, ez), dpw.ROAD_BLOCK);
        return true;
    }

    @Override
    public boolean createRoadSouthEast(final PlotWorld plotworld, final Plot plot) {
//        final MegaPlotWorld dpw = (MegaPlotWorld) plotworld;
//        final Location pos2 = getPlotTopLocAbs(plotworld, plot.id);
//        final int sx = pos2.getX() + 1;
//        final int ex = (sx + dpw.ROAD_WIDTH) - 1;
//        final int sz = pos2.getZ() + 1;
//        final int ez = (sz + dpw.ROAD_WIDTH) - 1;
//        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, dpw.ROAD_HEIGHT + 1, sz + 1), new Location(plotworld.worldname, ex, 257, ez), new PlotBlock((short) 0, (byte) 0));
//        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, 0, sz + 1), new Location(plotworld.worldname, ex, 1, ez), new PlotBlock((short) 7, (byte) 0));
//        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, 1, sz + 1), new Location(plotworld.worldname, ex, dpw.ROAD_HEIGHT + 1, ez), dpw.ROAD_BLOCK);
        return true;
    }

    @Override
    public boolean removeRoadEast(final PlotWorld plotworld, final Plot plot) {
//        final MegaPlotWorld dpw = (MegaPlotWorld) plotworld;
//        final Location pos1 = getPlotBottomLocAbs(plotworld, plot.id);
//        final Location pos2 = getPlotTopLocAbs(plotworld, plot.id);
//        final int sx = pos2.getX() + 1;
//        final int ex = (sx + dpw.ROAD_WIDTH) - 1;
//        final int sz = pos1.getZ();
//        final int ez = pos2.getZ() + 1;
//        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx, Math.min(dpw.PLOT_HEIGHT, dpw.ROAD_HEIGHT) + 1, sz), new Location(plotworld.worldname, ex + 1, 257, ez + 1), new PlotBlock((short) 0, (byte) 0));
//        MainUtil.setCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx, 1, sz + 1), new Location(plotworld.worldname, ex + 1, dpw.PLOT_HEIGHT, ez), dpw.MAIN_BLOCK);
//        MainUtil.setCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx, dpw.PLOT_HEIGHT, sz + 1), new Location(plotworld.worldname, ex + 1, dpw.PLOT_HEIGHT + 1, ez), dpw.TOP_BLOCK);
        return true;
    }

    @Override
    public boolean removeRoadSouth(final PlotWorld plotworld, final Plot plot) {
//        final MegaPlotWorld dpw = (MegaPlotWorld) plotworld;
//        final Location pos1 = getPlotBottomLocAbs(plotworld, plot.id);
//        final Location pos2 = getPlotTopLocAbs(plotworld, plot.id);
//        final int sz = pos2.getZ() + 1;
//        final int ez = (sz + dpw.ROAD_WIDTH) - 1;
//        final int sx = pos1.getX();
//        final int ex = pos2.getX() + 1;
//        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx, Math.min(dpw.PLOT_HEIGHT, dpw.ROAD_HEIGHT) + 1, sz), new Location(plotworld.worldname, ex + 1, 257, ez + 1), new PlotBlock((short) 0, (byte) 0));
//        MainUtil.setCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, 1, sz), new Location(plotworld.worldname, ex, dpw.PLOT_HEIGHT, ez + 1), dpw.MAIN_BLOCK);
//        MainUtil.setCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, dpw.PLOT_HEIGHT, sz), new Location(plotworld.worldname, ex, dpw.PLOT_HEIGHT + 1, ez + 1), dpw.TOP_BLOCK);
        return true;
    }

    @Override
    public boolean removeRoadSouthEast(final PlotWorld plotworld, final Plot plot) {
//        final MegaPlotWorld dpw = (MegaPlotWorld) plotworld;
//        final Location loc = getPlotTopLocAbs(dpw, plot.id);
//        final int sx = loc.getX() + 1;
//        final int ex = (sx + dpw.ROAD_WIDTH) - 1;
//        final int sz = loc.getZ() + 1;
//        final int ez = (sz + dpw.ROAD_WIDTH) - 1;
//        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx, dpw.ROAD_HEIGHT + 1, sz), new Location(plotworld.worldname, ex + 1, 257, ez + 1), new PlotBlock((short) 0, (byte) 0));
//        MainUtil.setCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx, 1, sz), new Location(plotworld.worldname, ex + 1, dpw.ROAD_HEIGHT, ez + 1), dpw.MAIN_BLOCK);
//        MainUtil.setCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx, dpw.ROAD_HEIGHT, sz), new Location(plotworld.worldname, ex + 1, dpw.ROAD_HEIGHT + 1, ez + 1), dpw.TOP_BLOCK);
        return true;
    }

    /**
     * Finishing off plot merging by adding in the walls surrounding the plot (OPTIONAL)(UNFINISHED)
     */
    @Override
    public boolean finishPlotMerge(final PlotWorld plotworld, final ArrayList<PlotId> plotIds) {
        final PlotId id = plotIds.get(0);
        final PlotBlock block = new PlotBlock(MegaGen.BORDER_CLAIMED_BLOCK, (byte) 0);
        final PlotBlock unclaim = new PlotBlock(MegaGen.BORDER_BLOCK, (byte) 0);
        if (!block.equals(unclaim)) {
            setWall(plotworld, id, block);
        }
        return true;
    }

    @Override
    public boolean finishPlotUnlink(final PlotWorld plotworld, final ArrayList<PlotId> plotIds) {
        final PlotBlock block = new PlotBlock(MegaGen.BORDER_CLAIMED_BLOCK, (byte) 0);
        final PlotBlock unclaim = new PlotBlock(MegaGen.BORDER_BLOCK, (byte) 0);
        for (final PlotId id : plotIds) {
            if ((block.id != 0) || !block.equals(unclaim)) {
                setWall(plotworld, id, block);
            }
        }
        return true;
    }

    @Override
    public boolean startPlotMerge(final PlotWorld plotworld, final ArrayList<PlotId> plotIds) {
        return true;
    }

    @Override
    public boolean startPlotUnlink(final PlotWorld plotworld, final ArrayList<PlotId> plotIds) {
        return true;
    }

    @Override
    public boolean claimPlot(final PlotWorld plotworld, final Plot plot) {
        final PlotBlock claim = new PlotBlock(MegaGen.BORDER_CLAIMED_BLOCK, (byte) 0);
        final PlotBlock unclaim = new PlotBlock(MegaGen.BORDER_BLOCK, (byte) 0);
        if ((claim.id != 0) || !claim.equals(unclaim)) {
            setWall(plotworld, plot.id, claim);
        }
        return true;
    }

    @Override
    public boolean unclaimPlot(final PlotWorld plotworld, final Plot plot) {
        final PlotBlock claim = new PlotBlock(MegaGen.BORDER_CLAIMED_BLOCK, (byte) 0);
        final PlotBlock unclaim = new PlotBlock(MegaGen.BORDER_BLOCK, (byte) 0);
        if ((unclaim.id != 0) || !claim.equals(unclaim)) {
            setWall(plotworld, plot.id, unclaim);
        }
        MainUtil.removeSign(plot);
        return true;
    }

    @Override
    public String[] getPlotComponents(final PlotWorld plotworld, final PlotId plotid) {
        return new String[] { "floor", "wall", "border" };
    }

    /**
     * Remove sign for a plot
     */
    @Override
    public Location getSignLoc(final PlotWorld plotworld, final Plot plot) {
        final Location bot = MainUtil.getPlotBottomLoc(plotworld.worldname, plot.id);
        return new com.intellectualcrafters.plot.object.Location(plotworld.worldname, bot.getX(), MegaGen.HEIGHT + 1, bot.getZ() - 1);
    }

    @Override
    public Location getPlotBottomLocAbs(final PlotWorld pw, final PlotId id) {
        MegaPlotWorld mpw = (MegaPlotWorld) pw;
        int lower;
        if (mpw.ROAD_WIDTH % 2 == 0) {
            lower = (mpw.ROAD_WIDTH / 2) - 1;
        }
        else {
            lower = (mpw.ROAD_WIDTH / 2);
        }
        return new Location(pw.worldname, (((id.x - 1) << 9) * mpw.MCA_WIDTH) + lower, 0, (((id.y - 1) << 9) * mpw.MCA_WIDTH) + lower);
    }

    @Override
    public PlotId getPlotId(final PlotWorld pw, final int x, final int y, final int z) {
        MegaPlotWorld mpw = (MegaPlotWorld) pw;
        int lower;
        if (mpw.ROAD_WIDTH % 2 == 0) {
            lower = (mpw.ROAD_WIDTH / 2) - 1;
        }
        else {
            lower = (mpw.ROAD_WIDTH / 2);
        }
        int size = (mpw.MCA_WIDTH * 512);
        int X = (x / size);
        int Z = (z / size);
        if (x >= 0) {
            X++;
        }
        if (z >= 0) {
            Z++;
        }
        int xx = x % (size);
        int zz = z % (size);
        if (xx < 0) {
            xx += (size);
        }
        if (zz < 0) {
            zz += (size);
        }
        final boolean northSouth = (zz <= lower) || (zz > mpw.PLOT_WIDTH + lower + 1);
        final boolean eastWest = (xx <= lower) || (xx > mpw.PLOT_WIDTH + lower + 1);

        if (northSouth && eastWest) {
            // This means you are in the intersection
            final Location loc = new Location(pw.worldname, x + 5, 0, z + 5);
            final PlotId id = MainUtil.getPlotAbs(loc);
            final Plot plot = PlotSquared.getPlots(pw.worldname).get(id);
            if (plot == null) {
                return null;
            }
            if ((plot.settings.getMerged(0) && plot.settings.getMerged(3))) {
                return MainUtil.getBottomPlot(plot).id;
            }
            return null;
        }
        if (northSouth) {
            // You are on a road running West to East (yeah, I named the var poorly)
            final Location loc = new Location(pw.worldname, x, 0, z + 5);
            final PlotId id = MainUtil.getPlotAbs(loc);
            final Plot plot = PlotSquared.getPlots(pw.worldname).get(id);
            if (plot == null) {
                return null;
            }
            if (plot.settings.getMerged(0)) {
                return MainUtil.getBottomPlot(plot).id;
            }
            return null;
        }
        if (eastWest) {
            // This is the road separating an Eastern and Western plot
            final Location loc = new Location(pw.worldname, x + 5, 0, z);
            final PlotId id = MainUtil.getPlotAbs(loc);
            final Plot plot = PlotSquared.getPlots(pw.worldname).get(id);
            if (plot == null) {
                return null;
            }
            if (plot.settings.getMerged(3)) {
                return MainUtil.getBottomPlot(plot).id;
            }
            return null;
        }
        final PlotId id = new PlotId(X, Z);
        final Plot plot = PlotSquared.getPlots(pw.worldname).get(id);
        if (plot == null) {
            return id;
        }
        return MainUtil.getBottomPlot(plot).id;
    }

    @Override
    public PlotId getPlotIdAbs(final PlotWorld pw, final int x, final int y, final int z) {
        MegaPlotWorld mpw = (MegaPlotWorld) pw;
        int lower;
        if (mpw.ROAD_WIDTH % 2 == 0) {
            lower = (mpw.ROAD_WIDTH / 2) - 1;
        }
        else {
            lower = (mpw.ROAD_WIDTH / 2);
        }
        int size = (mpw.MCA_WIDTH * 512);
        int X = (x / size);
        int Z = (z / size);
        if (x >= 0) {
            X++;
        }
        if (z >= 0) {
            Z++;
        }
        int xx = x % (mpw.ROAD_WIDTH + mpw.PLOT_WIDTH);
        int zz = z % (mpw.ROAD_WIDTH + mpw.PLOT_WIDTH);
        if (xx < 0) {
            xx += (mpw.ROAD_WIDTH + mpw.PLOT_WIDTH);
        }
        if (zz < 0) {
            zz += (mpw.ROAD_WIDTH + mpw.PLOT_WIDTH);
        }
        if ((xx > mpw.PLOT_WIDTH + lower + 1) || (zz > mpw.PLOT_WIDTH + lower + 1) || (xx <= lower) || (zz <= lower)) {
            return null;
        }
        return new PlotId(X, Z);
    }

    @Override
    public Location getPlotTopLocAbs(final PlotWorld pw, final PlotId id) {
        MegaPlotWorld mpw = (MegaPlotWorld) pw;
        int lower;
        if (mpw.ROAD_WIDTH % 2 == 0) {
            lower = (mpw.ROAD_WIDTH / 2) - 1;
        }
        else {
            lower = (mpw.ROAD_WIDTH / 2);
        }
        return new Location(pw.worldname, (((id.x - 1) << 9) * mpw.MCA_WIDTH) + mpw.PLOT_WIDTH + lower, 0, (((id.y - 1) << 9) * mpw.MCA_WIDTH) + mpw.PLOT_WIDTH + lower);
    }

    @Override
    public boolean setBiome(final Plot plot, final int biome) {
        return false;
    }
}
