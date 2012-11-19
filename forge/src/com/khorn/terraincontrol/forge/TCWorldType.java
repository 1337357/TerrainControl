package com.khorn.terraincontrol.forge;

import java.io.File;

import net.minecraft.src.IChunkProvider;
import net.minecraft.src.World;
import net.minecraft.src.WorldChunkManager;
import net.minecraft.src.WorldClient;
import net.minecraft.src.WorldType;

import com.khorn.terraincontrol.configuration.WorldConfig;

public class TCWorldType extends WorldType
{
    public SingleWorld worldTC;
    private TCPlugin plugin;

    public TCWorldType(TCPlugin plugin, int paramInt, String paramString)
    {
        super(paramInt, paramString);
        this.plugin = plugin;
    }

    @Override
    public WorldChunkManager getChunkManager(World world)
    {
        if (world instanceof WorldClient)
            return super.getChunkManager(world);
        
        // Restore old biomes
        SingleWorld.restoreBiomes();

        // Load everything
        File worldDirectory = new File(plugin.terrainControlDirectory, "worlds" + File.separator + world.getSaveHandler().getSaveDirectoryName());

        if (!worldDirectory.exists())
        {
            System.out.println("TerrainControl: settings does not exist, creating defaults");

            if (!worldDirectory.mkdirs())
                System.out.println("TerrainControl: cant create folder " + worldDirectory.getAbsolutePath());
        }

        this.worldTC = new SingleWorld(world.getSaveHandler().getSaveDirectoryName());
        WorldConfig config = new WorldConfig(worldDirectory, worldTC, false);
        this.worldTC.Init(world, config);

        WorldChunkManager ChunkManager = null;

        switch (this.worldTC.getSettings().ModeBiome)
        {
        case FromImage:
        case Normal:
            ChunkManager = new BiomeManager(this.worldTC);
            this.worldTC.setBiomeManager((BiomeManager) ChunkManager);
            break;
        case OldGenerator:
            ChunkManager = new BiomeManagerOld(this.worldTC);
            this.worldTC.setOldBiomeManager((BiomeManagerOld) ChunkManager);
            break;
        case Default:
            ChunkManager = super.getChunkManager(world);
            break;
        }

        return ChunkManager;
    }

    @Override
    public IChunkProvider getChunkGenerator(World world, String generatorOptions)
    {
        if (this.worldTC.getSettings().ModeTerrain != WorldConfig.TerrainMode.Default)
        {
            return this.worldTC.getChunkGenerator();
        } else
            return super.getChunkGenerator(world, generatorOptions);
    }

}
