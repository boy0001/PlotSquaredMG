package com.boydti.psmg.generator;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.mutable.MutableInt;
import org.bukkit.Bukkit;
import org.bukkit.World;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.generator.GridPlotManager;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.TaskManager;

public class MegaPlotManager extends GridPlotManager {

    @Override
    public boolean clearPlot(final PlotWorld plotworld, final Plot plot, final boolean isDelete, final Runnable whenDone) {
        final Location bot = MainUtil.getPlotBottomLoc(plot.world, plot.id);
        final Location top = MainUtil.getPlotTopLoc(plot.world, plot.id);
        final World world = Bukkit.getWorld(plot.world);
        final List<ChunkLoc> chunks = new ArrayList<>();
        for (int x = bot.getX() >> 4; x <= (top.getX() >> 4); x++) {
            for (int z = bot.getZ() >> 4; z <= (top.getZ() >> 4); z++) {
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
                final long start = System.currentTimeMillis();
                while (((System.currentTimeMillis() - start) < 25) && (chunks.size() > 0)) {
                    final ChunkLoc loc = chunks.remove(0);
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
        plotIds.get(0);
        return true;
    }

    @Override
    public boolean finishPlotUnlink(final PlotWorld plotworld, final ArrayList<PlotId> plotIds) {
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
        final MegaPlotWorld mpw = (MegaPlotWorld) pw;
        final int px = id.x;
        final int pz = id.y;
        final int x = (px * (mpw.ROAD_WIDTH + mpw.PLOT_WIDTH)) - mpw.PLOT_WIDTH - ((int) Math.floor(mpw.ROAD_WIDTH / 2)) - 1;
        final int z = (pz * (mpw.ROAD_WIDTH + mpw.PLOT_WIDTH)) - mpw.PLOT_WIDTH - ((int) Math.floor(mpw.ROAD_WIDTH / 2)) - 1;
        return new Location(pw.worldname, x, 1, z);
    }

    @Override
    public PlotId getPlotId(final PlotWorld pw, final int x, final int y, final int z) {
        final MegaPlotWorld mpw = (MegaPlotWorld) pw;
        int lower;
        if ((mpw.ROAD_WIDTH % 2) == 0) {
            lower = (mpw.ROAD_WIDTH / 2) - 1;
        } else {
            lower = (mpw.ROAD_WIDTH / 2);
        }
        final int size = (mpw.MCA_WIDTH * 512);
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
        final boolean northSouth = (zz <= lower) || (zz > (mpw.PLOT_WIDTH + lower));
        final boolean eastWest = (xx <= lower) || (xx > (mpw.PLOT_WIDTH + lower));

        if (northSouth && eastWest) {
            // This means you are in the intersection
            final Location loc = new Location(pw.worldname, x + 5, 0, z + 5);
            final PlotId id = MainUtil.getPlotAbs(loc);
            final Plot plot = PS.get().getPlots(pw.worldname).get(id);
            if (plot == null) {
                return null;
            }
            if ((plot.getMerged(0) && plot.getMerged(3))) {
                return MainUtil.getBottomPlot(plot).id;
            }
            return null;
        }
        if (northSouth) {
            // You are on a road running West to East (yeah, I named the var poorly)
            final Location loc = new Location(pw.worldname, x, 0, z + 5);
            final PlotId id = MainUtil.getPlotAbs(loc);
            final Plot plot = PS.get().getPlots(pw.worldname).get(id);
            if (plot == null) {
                return null;
            }
            if (plot.getMerged(0)) {
                return MainUtil.getBottomPlot(plot).id;
            }
            return null;
        }
        if (eastWest) {
            // This is the road separating an Eastern and Western plot
            final Location loc = new Location(pw.worldname, x + 5, 0, z);
            final PlotId id = MainUtil.getPlotAbs(loc);
            final Plot plot = PS.get().getPlots(pw.worldname).get(id);
            if (plot == null) {
                return null;
            }
            if (plot.getMerged(3)) {
                return MainUtil.getBottomPlot(plot).id;
            }
            return null;
        }
        final PlotId id = new PlotId(X, Z);
        final Plot plot = PS.get().getPlots(pw.worldname).get(id);
        if (plot == null) {
            return id;
        }
        return MainUtil.getBottomPlot(plot).id;
    }

    @Override
    public PlotId getPlotIdAbs(final PlotWorld pw, final int x, final int y, final int z) {
        final MegaPlotWorld mpw = (MegaPlotWorld) pw;
        int lower;
        if ((mpw.ROAD_WIDTH % 2) == 0) {
            lower = (mpw.ROAD_WIDTH / 2) - 1;
        } else {
            lower = (mpw.ROAD_WIDTH / 2);
        }
        final int size = (mpw.MCA_WIDTH * 512);
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
        if ((xx > (mpw.PLOT_WIDTH + lower)) || (zz > (mpw.PLOT_WIDTH + lower)) || (xx <= lower) || (zz <= lower)) {
            return null;
        }
        return new PlotId(X, Z);
    }

    @Override
    public Location getPlotTopLocAbs(final PlotWorld pw, final PlotId id) {
        final MegaPlotWorld mpw = (MegaPlotWorld) pw;
        final int px = id.x;
        final int pz = id.y;
        final int x = (px * (mpw.ROAD_WIDTH + mpw.PLOT_WIDTH)) - ((int) Math.floor(mpw.ROAD_WIDTH / 2)) - 1;
        final int z = (pz * (mpw.ROAD_WIDTH + mpw.PLOT_WIDTH)) - ((int) Math.floor(mpw.ROAD_WIDTH / 2)) - 1;
        return new Location(pw.worldname, x, 256, z);
    }
}
