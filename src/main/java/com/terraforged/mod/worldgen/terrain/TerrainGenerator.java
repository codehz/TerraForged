package com.terraforged.mod.worldgen.terrain;

import com.terraforged.mod.util.ObjectPool;
import com.terraforged.mod.worldgen.noise.NoiseGenerator;
import net.minecraft.world.level.ChunkPos;

public class TerrainGenerator {
    protected final TerrainLevels levels;
    protected final NoiseGenerator noiseGenerator;
    protected final ObjectPool<TerrainData> terrainDataPool;

    public TerrainGenerator(NoiseGenerator generator) {
        this(TerrainLevels.DEFAULT, generator);
    }

    public TerrainGenerator(TerrainLevels levels, NoiseGenerator noiseGenerator) {
        this.levels = levels;
        this.noiseGenerator = noiseGenerator;
        this.terrainDataPool = new ObjectPool<>(() -> new TerrainData(this.levels));
    }

    public TerrainGenerator withNoise(NoiseGenerator generator) {
        return new TerrainGenerator(levels, generator);
    }

    public NoiseGenerator getNoiseGenerator() {
        return noiseGenerator;
    }

    public void restore(TerrainData terrainData) {
        terrainDataPool.restore(terrainData);
    }

    public TerrainData generate(ChunkPos chunkPos) {
        var noiseData = noiseGenerator.generate(chunkPos);
        var terrainData = terrainDataPool.take();
        terrainData.assign(noiseData);
        return terrainData;
    }

    public int getHeight(int x, int z) {
        float heightNoise = noiseGenerator.getHeightNoise(x, z);
        float scaledHeight = levels.getScaledHeight(heightNoise);
        return levels.getHeight(scaledHeight);
    }
}
