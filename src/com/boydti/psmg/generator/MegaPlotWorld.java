package com.boydti.psmg.generator;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.block.Block;
import org.bukkit.util.FileUtil;

import com.boydti.psmg.MGMain;
import com.boydti.psmg.event.MainListener;
import com.intellectualcrafters.configuration.ConfigurationSection;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.Configuration;
import com.intellectualcrafters.plot.config.ConfigurationNode;
import com.intellectualcrafters.plot.generator.GridPlotWorld;
import com.intellectualcrafters.plot.generator.SquarePlotWorld;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.TaskManager;

public class MegaPlotWorld extends GridPlotWorld {

    public int ROAD_WIDTH;
    public int PLOT_WIDTH;
    public int MCA_WIDTH;

    public MegaPlotWorld(final String worldname) {
        super(worldname);
    }

    @Override
    public ConfigurationNode[] getSettingNodes() {
        return new ConfigurationNode[] { new ConfigurationNode("road.width", SquarePlotWorld.ROAD_WIDTH_DEFAULT, "Road width", Configuration.INTEGER, true) };
    }

    public int mx;
    public int mz;

    public World BASE_WORLD;

    public HashMap<ChunkLoc, short[][]> BLOCKS = new HashMap<>();
    //    public HashMap<ChunkLoc, byte[][]> DATA = new HashMap<>();
    public HashMap<ChunkLoc, HashMap<RelBlockLoc, Byte>> DATA = new HashMap<>();

    @Override
    public void loadConfiguration(final ConfigurationSection config) {
        this.ROAD_WIDTH = config.getInt("road.width");
        PS.get();
        PS.log("&8[&5PS.get() Mega Generator&8]");
        PS.get();
        PS.log("&8 - &dGetting files...");
        final File folder = new File(MGMain.plugin.getDataFolder() + File.separator + "worlds" + File.separator + this.worldname);
        final File[] files = getRegions(folder);
        final int num_files = files.length;
        if (num_files == 0) {
            PS.get();
            PS.log("&8[&4No files found in: &7" + folder.getPath() + "&8]");
            return;
        }
        PS.get();
        PS.log("&8 - &dProcessing files...");
        this.MCA_WIDTH = (short) Math.sqrt(num_files);
        this.PLOT_WIDTH = (this.MCA_WIDTH * 512) - this.ROAD_WIDTH;
        if ((this.MCA_WIDTH * this.MCA_WIDTH) != num_files) {
            PS.get();
            PS.log("&cERROR LOADING WORLD `" + this.worldname + "` - INVALID DIMENSIONS: " + num_files);
            return;
        }

        this.mx = Integer.MAX_VALUE;
        this.mz = Integer.MAX_VALUE;
        for (final File file : files) {
            PS.get();
            PS.log(file.getName());
            final String[] split = file.getName().split("\\.");
            final int x = Integer.parseInt(split[1]);
            final int z = Integer.parseInt(split[2]);
            if (x < this.mx) {
                this.mx = x;
            }
            if (z < this.mz) {
                this.mz = z;
            }
        }

        PS.get();
        PS.log("&8 - &dGenerating to radius: " + MGMain.RADIUS);
        for (int x = -MGMain.RADIUS; x <= MGMain.RADIUS; x++) {
            for (int z = -MGMain.RADIUS; z <= MGMain.RADIUS; z++) {
                genMca(this.worldname, this.mx, this.mz, this.MCA_WIDTH, this.worldname, new PlotId(x, z));
            }
        }

        final String name = "PSMG_" + this.worldname;
        PS.get();
        PS.log("&8 - &dCopying files to temp world: " + name);
        genMca(this.worldname, this.mx, this.mz, this.MCA_WIDTH, name, new PlotId(1, 1));
        TaskManager.runTaskLater(new Runnable() {
            @Override
            public void run() {
                PS.get();
                PS.log("&8 - &dInitializing temp world");
                MegaPlotWorld.this.BASE_WORLD = Bukkit.getWorld(name);
                if (MegaPlotWorld.this.BASE_WORLD == null) {
                    final WorldCreator creator = new WorldCreator(name);
                    creator.environment(World.Environment.NORMAL);
                    creator.type(WorldType.FLAT);
                    creator.generateStructures(false);
                    MegaPlotWorld.this.BASE_WORLD = creator.createWorld();
                }
                MegaPlotWorld.this.BASE_WORLD.setSpawnFlags(false, false);
                MegaPlotWorld.this.BASE_WORLD.setAutoSave(false);
                MegaPlotWorld.this.BASE_WORLD.setAmbientSpawnLimit(0);
                MegaPlotWorld.this.BASE_WORLD.setAnimalSpawnLimit(0);
                MegaPlotWorld.this.BASE_WORLD.setDifficulty(Difficulty.PEACEFUL);
                MegaPlotWorld.this.BASE_WORLD.setKeepSpawnInMemory(false);
                MegaPlotWorld.this.BASE_WORLD.setMonsterSpawnLimit(0);
                MegaPlotWorld.this.BASE_WORLD.setTicksPerAnimalSpawns(Integer.MAX_VALUE);
                MegaPlotWorld.this.BASE_WORLD.setTicksPerMonsterSpawns(Integer.MAX_VALUE);
                MegaPlotWorld.this.BASE_WORLD.setWaterAnimalSpawnLimit(0);
                MainListener.worlds.add(name);

                PS.get();
                PS.log("&8 - &dInitializing AugmentedPopulator...");
                MainUtil.initCache();
                PS.get();
                PS.log("&8 - &dProcessing chunks (this may take a while)");
                final int size = MegaPlotWorld.this.MCA_WIDTH * MegaPlotWorld.this.MCA_WIDTH * 1024;
                int i = 0;
                for (int cx = MegaPlotWorld.this.mx * 32; cx < ((MegaPlotWorld.this.mx + MegaPlotWorld.this.MCA_WIDTH) * 32); cx++) {
                    for (int cz = MegaPlotWorld.this.mz * 32; cz < ((MegaPlotWorld.this.mz + MegaPlotWorld.this.MCA_WIDTH) * 32); cz++) {

                        if ((i % 256) == 0) {
                            PS.get();
                            PS.log(((i * 100) / size) + "%");
                        }

                        final ChunkLoc loc = new ChunkLoc(cx, cz);
                        final short[][] result = new short[16][];
                        //                        byte[][] result_data = new byte[16][];
                        final HashMap<RelBlockLoc, Byte> result_data = new HashMap<>();
                        final Chunk chunk = MegaPlotWorld.this.BASE_WORLD.getChunkAt(cx, cz);
                        chunk.load(false);
                        while (!chunk.isLoaded()) {
                            System.out.print("waiting on chunk: " + (cx + "," + cz));
                            chunk.load(false);
                        }
                        for (int x = 0; x < 16; x++) {
                            for (int z = 0; z < 16; z++) {
                                for (int y = 0; y < 256; y++) {
                                    final Block block = chunk.getBlock(x, y, z);
                                    final short id = (short) block.getTypeId();
                                    if (id != 0) {
                                        if (result[y >> 4] == null) {
                                            result[y >> 4] = new short[4096];
                                        }
                                        result[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = id;
                                        final byte dmg = block.getData();
                                        if (dmg != 0) {
                                            final RelBlockLoc l = new RelBlockLoc(x, y, z);
                                            result_data.put(l, dmg);
                                            //                                            if (result_data[y >> 4] == null) {
                                            //                                                result_data[y >> 4] = new byte[4096];
                                            //                                            }
                                            //                                            result_data[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = dmg;
                                        }
                                    }
                                }
                            }
                        }
                        chunk.unload(false, false);
                        if (result_data.size() != 0) {
                            MegaPlotWorld.this.DATA.put(loc, result_data);
                        }
                        //                        DATA.put(loc, result_data);
                        MegaPlotWorld.this.BLOCKS.put(loc, result);
                        i++;
                    }
                }
                PS.get();
                PS.log("&8 - &dUnloading temp world...");
                Bukkit.unloadWorld(MegaPlotWorld.this.BASE_WORLD, false);
                PS.get();
                PS.log("&8 - &dDeleting temp world...");
                final File folder = new File(Bukkit.getWorldContainer() + File.separator + name);
                deleteDirectory(folder);
                PS.get();
                PS.log("&aLoaded world: " + MegaPlotWorld.this.worldname + "!");
            }
        }, 1);
    }

    public static boolean deleteDirectory(final File directory) {
        if (directory.exists()) {
            final File[] files = directory.listFiles();
            if (null != files) {
                for (final File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
        }
        return (directory.delete());
    }

    public static void genMca(final String from, final int mx, final int mz, final int width, final String world, final PlotId id) {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < width; j++) {
                final int x = ((id.x - 1) * width) + i;
                final int z = ((id.y - 1) * width) + j;

                int sx = x % width;
                int sz = z % width;

                if (sx < 0) {
                    sx += width;
                }

                if (sz < 0) {
                    sz += width;
                }

                sx += mx;
                sz += mz;
                final File template = getTemplateMca(from, sx, sz);
                copyFile(template, world, x, z);
            }
        }
    }

    public static File getTemplateMca(final String worldname, final int x, final int z) {
        return new File(MGMain.plugin.getDataFolder() + File.separator + "worlds" + File.separator + worldname + File.separator + "r." + x + "." + z + ".mca");
    }

    public static File getNewMca(final String world, final int x, final int z) {
        return new File(Bukkit.getWorldContainer() + File.separator + world + File.separator + "region" + File.separator + "r." + x + "." + z + ".mca");
    }

    public static List<File> getFiles(final int width, final String world, final PlotId id) {
        final List<File> files = new ArrayList<>();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < width; j++) {
                final int x = ((id.x - 1) * width) + i;
                final int z = ((id.y - 1) * width) + j;
                files.add(getNewMca(world, x, z));
            }
        }
        return files;
    }

    //    public static void copyFileNew(File file, String world, int x, int z) {
    //        try {
    //            FileInputStream fis = new FileInputStream(file);
    //            ZInputStream zis = new ZInputStream(fis, 2);
    //            NBTInputStream is = new NBTInputStream(zis);
    //            final CompoundTag tag = (CompoundTag) is.readTag();
    //            fis.close();
    //            zis.close();
    ////            bis.close();
    ////            dis.close();
    //            is.close();
    //            for (Entry<String, Tag> entry : tag.getValue().entrySet()) {
    //                System.out.print(entry.getKey() + " : " + entry.getValue().getClass().getCanonicalName() + " | " + entry.getValue());
    //            }
    //        }
    //        catch (Exception e) {
    //            e.printStackTrace();
    //        }
    //    }

    public static void copyFile(final File file, final String world, final int x, final int z) {
        final File other = getNewMca(world, x, z);
        if (other.exists()) {
            return;
        }
        try {
            other.getParentFile().mkdirs();
            other.createNewFile();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        other.mkdirs();
        FileUtil.copy(file, other);
    }

    public static File[] getRegions(final File folder) {
        return folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String filename) {
                return filename.endsWith(".mca");
            }
        });
    }

}
