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

package com.terraforged.mod.worldgen.biome.decorator;

import com.terraforged.mod.worldgen.Generator;
import com.terraforged.mod.worldgen.biome.surface.Surface;
import com.terraforged.mod.worldgen.util.NoiseChunkUtil;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

public class SurfaceDecorator {
    public void decorate(ChunkAccess chunk, WorldGenRegion region, Generator generator) {
        var context = new WorldGenerationContext(generator, region);
        var noiseChunk = NoiseChunkUtil.getNoiseChunk(chunk, generator);

        var biomes = generator.getBiomeSource().getRegistry();
        var biomeManager = region.getBiomeManager();

        var surface = generator.getVanillaGen().getSurfaceSystem();
        var surfaceRules = generator.getVanillaGen().getSettings().value().surfaceRule();
        surface.buildSurface(biomeManager, biomes, false, context, chunk, noiseChunk, surfaceRules);
    }

    public void decoratePost(ChunkAccess chunk, Generator generator) {
        var chunkData = generator.getChunkData(chunk.getPos());
        Surface.apply(chunkData, chunk, generator);
    }
}
