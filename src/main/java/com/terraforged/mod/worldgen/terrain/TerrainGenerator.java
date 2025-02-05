/*
 * MIT License
 *
 * Copyright (c) 2021 TerraForged
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.terraforged.mod.worldgen.terrain;

import com.terraforged.mod.util.ObjectPool;
import com.terraforged.mod.worldgen.noise.INoiseGenerator;

public class TerrainGenerator {
    protected final TerrainLevels levels;
    protected final INoiseGenerator noiseGenerator;
    protected final ObjectPool<TerrainData> terrainDataPool;

    public TerrainGenerator(TerrainLevels levels, INoiseGenerator noiseGenerator) {
        this.levels = levels;
        this.noiseGenerator = noiseGenerator;
        this.terrainDataPool = new ObjectPool<>(() -> new TerrainData(this.levels));
    }

    public INoiseGenerator getNoiseGenerator() {
        return noiseGenerator;
    }

    public void restore(TerrainData terrainData) {
        terrainDataPool.restore(terrainData);
    }

    public TerrainData generate(int chunkX, int chunkZ) {
        var terrainData = terrainDataPool.take();
        noiseGenerator.generate(chunkX, chunkZ, terrainData);
        return terrainData;
    }

    public int getHeight(int x, int z) {
        float heightNoise = noiseGenerator.getHeightNoise(x, z);
        float scaledHeight = levels.getScaledHeight(heightNoise);
        return levels.getHeight(scaledHeight);
    }
}
