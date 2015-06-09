package com.boydti.psmg.generator;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.FileUtil;

import com.boydti.psmg.MGMain;
import com.boydti.psmg.event.MainListener;
import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.Configuration;
import com.intellectualcrafters.plot.config.ConfigurationNode;
import com.intellectualcrafters.plot.generator.AugmentedPopulator;
import com.intellectualcrafters.plot.generator.GridPlotWorld;
import com.intellectualcrafters.plot.generator.SquarePlotWorld;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotId;
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
        return new ConfigurationNode[] {new ConfigurationNode("road.width", SquarePlotWorld.ROAD_WIDTH_DEFAULT, "Road width", Configuration.INTEGER, true)};
    }
    
    public int mx;
    public int mz;
    
    public World BASE_WORLD;
    
    public HashMap<ChunkLoc, short[][]> BLOCKS = new HashMap<>();
    public HashMap<ChunkLoc, PlotBlock[][]> DATA = new HashMap<>();
    
    @Override
    public void loadConfiguration(final ConfigurationSection config) {
        this.ROAD_WIDTH = config.getInt("road.width");
        PlotSquared.log("&8[&5PlotSquared Mega Generator&8]");
        PlotSquared.log("&8 - &dGetting files...");
        File folder = new File(MGMain.plugin.getDataFolder() + File.separator + "worlds" + File.separator + worldname);
        File[] files = getRegions(folder);
        int num_files = files.length;
        if (num_files == 0) {
            PlotSquared.log("&8[&4No files found in: &7" + folder.getPath() + "&8]");
            return;
        }
        PlotSquared.log("&8 - &dProcessing files...");
        MCA_WIDTH  = (short) Math.sqrt(num_files);
        PLOT_WIDTH = MCA_WIDTH * 512 - ROAD_WIDTH;
        if (MCA_WIDTH * MCA_WIDTH != num_files) {
            PlotSquared.log("&cERROR LOADING WORLD `" + worldname + "` - INVALID DIMENSIONS: " + num_files);
            return;
        }
        
        mx = Integer.MAX_VALUE;
        mz = Integer.MAX_VALUE;
        for (File file : files) {
            PlotSquared.log(file.getName());
            String[] split = file.getName().split("\\.");
            int x = Integer.parseInt(split[1]);
            int z = Integer.parseInt(split[2]);
            if (x < mx) {
                mx = x;
            }
            if (z < mz) {
                mz = z;
            }
        }
        
        PlotSquared.log("&8 - &dGenerating to radius: " + MGMain.RADIUS);
        for (int x = -MGMain.RADIUS; x <= MGMain.RADIUS; x++) {
            for (int z = -MGMain.RADIUS; z <= MGMain.RADIUS; z++) {
                genMca(worldname, mx, mz, this.MCA_WIDTH, worldname, new PlotId(x, z));
            }
        }
        
        
        final String name = "PSMG_" + worldname;
        PlotSquared.log("&8 - &dCopying files to temp world: " + name);
        genMca(worldname, mx, mz, this.MCA_WIDTH, name, new PlotId(1, 1));
        TaskManager.runTaskLater(new Runnable() {
            @Override
            public void run() {
                PlotSquared.log("&8 - &dInitializing temp world");
                BASE_WORLD = Bukkit.getWorld(name);
                if(BASE_WORLD == null){
                    WorldCreator creator = new WorldCreator(name);
                    creator.environment(World.Environment.NORMAL);
                    creator.type(WorldType.FLAT);
                    creator.generateStructures(false);
                    BASE_WORLD = creator.createWorld();
                }
                BASE_WORLD.setSpawnFlags(false, false);
                BASE_WORLD.setAutoSave(false);
                BASE_WORLD.setAmbientSpawnLimit(0);
                BASE_WORLD.setAnimalSpawnLimit(0);
                BASE_WORLD.setDifficulty(Difficulty.PEACEFUL);
                BASE_WORLD.setKeepSpawnInMemory(false);
                BASE_WORLD.setMonsterSpawnLimit(0);
                BASE_WORLD.setTicksPerAnimalSpawns(Integer.MAX_VALUE);
                BASE_WORLD.setTicksPerMonsterSpawns(Integer.MAX_VALUE);
                BASE_WORLD.setWaterAnimalSpawnLimit(0);
                MainListener.worlds.add(name);
                
                
                PlotSquared.log("&8 - &dInitializing AugmentedPopulator...");
                AugmentedPopulator.initCache();
                PlotSquared.log("&8 - &dProcessing chunks (this may take a while)");
                int size = MCA_WIDTH * MCA_WIDTH * 1024;
                int i = 0;
                for (int cx = mx * 32; cx < (mx + MCA_WIDTH) * 32; cx++) {
                    for (int cz = mz * 32; cz < (mz + MCA_WIDTH) * 32; cz++) {
                        
                        if (i % 256 == 0) {
                            PlotSquared.log(((i * 100) / size) + "%");
                        }
                        
                        ChunkLoc loc = new ChunkLoc(cx, cz);
                        short[][] result = new short[16][];
                        PlotBlock[][] result_data = new PlotBlock[16][];
                        Chunk chunk = BASE_WORLD.getChunkAt(cx, cz);
                        chunk.load(false);
                        while (!chunk.isLoaded()) {
                            System.out.print("waiting on chunk: " + (cx + "," + cz));
                            chunk.load(false);
                        }
                        for (int x = 0; x < 16; x++) {
                            for (int z = 0; z < 16; z++) {
                                for (int y = 0; y < 256; y++) {
                                    Block block = chunk.getBlock(x, y, z);
                                    short id = (short) block.getTypeId();
                                    if (id != 0) {
                                        if (result[y >> 4] == null) {
                                            result[y >> 4] = new short[4096];
                                        }
                                        result[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = id;
                                        byte dmg = block.getData();
                                        if (dmg != 0) {
                                            PlotBlock p = new PlotBlock((short) -1, dmg);
                                            if (result_data[y >> 4] == null) {
                                                result_data[y >> 4] = new PlotBlock[4096];
                                            }
                                            result_data[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = p;
                                        }
                                    }
                                }
                            }
                        }
                        chunk.unload(false, false);
                        DATA.put(loc, result_data);
                        BLOCKS.put(loc, result);
                        i++;
                    }
                }
                PlotSquared.log("&8 - &dUnloading temp world...");
                Bukkit.unloadWorld(BASE_WORLD, false);
                PlotSquared.log("&8 - &dDeleting temp world...");
                File folder = new File(Bukkit.getWorldContainer() + File.separator + name);
                deleteDirectory(folder);
                PlotSquared.log("&aLoaded world: " + worldname + "!");
            }
        }, 1);
    }
    
    public static boolean deleteDirectory(File directory) {
        if(directory.exists()){
            File[] files = directory.listFiles();
            if(null!=files){
                for(int i=0; i<files.length; i++) {
                    if(files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    }
                    else {
                        files[i].delete();
                    }
                }
            }
        }
        return(directory.delete());
    }
    
    public static void genMca(String from, int mx, int mz, int width, String world, PlotId id) {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < width; j++) {
                int x = (id.x - 1) * width + i;
                int z = (id.y - 1) * width + j;
                
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
                File template = getTemplateMca(from, sx, sz);
                copyFile(template, world, x, z);
            }
        }
    }
    
    public static File getTemplateMca(String worldname, int x, int z){
        return new File(MGMain.plugin.getDataFolder() + File.separator + "worlds" + File.separator + worldname + File.separator + "r." + x + "." + z + ".mca");
    }
    
    public static File getNewMca(String world, int x, int z) {
        return new File(Bukkit.getWorldContainer() + File.separator + world + File.separator + "region" + File.separator + "r." + x + "." + z + ".mca");
    }
    
    public static List<File> getFiles(int width, String world, PlotId id) {
        List<File> files = new ArrayList<>();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < width; j++) {
                int x = (id.x - 1) * width + i;
                int z = (id.y - 1) * width + j;
                files.add(getNewMca(world, x, z));
            }
        }
        return files;
    }
    
    public static void copyFile(File file, String world, int x, int z) {
        File other = getNewMca(world, x, z);
        if (other.exists()) {
            return;
        }
        try {
            other.getParentFile().mkdirs();
            other.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        other.mkdirs();
        FileUtil.copy(file, other);
    }
    
    public static File[] getRegions(File folder){
        return folder.listFiles(new FilenameFilter() { 
            public boolean accept(File dir, String filename) { 
                return filename.endsWith(".mca");
            }
        } );
    }

}
