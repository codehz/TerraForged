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

package com.terraforged.mod.worldgen.biome;

import com.terraforged.mod.worldgen.Generator;
import com.terraforged.mod.worldgen.biome.decorator.FeatureDecorator;
import com.terraforged.mod.worldgen.biome.decorator.SurfaceDecorator;
import com.terraforged.mod.worldgen.biome.surface.Surface;
import com.terraforged.mod.worldgen.cave.NoiseCaveGenerator;
import com.terraforged.mod.worldgen.util.NoiseChunkUtil;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

public class BiomeGenerator {
    private final SurfaceDecorator surfaceDecorator;
    private final FeatureDecorator featureDecorator;
    private final NoiseCaveGenerator noiseCaveGenerator;

    public BiomeGenerator(long seed, RegistryAccess access) {
        this.surfaceDecorator = new SurfaceDecorator();
        this.featureDecorator = new FeatureDecorator(access);
        this.noiseCaveGenerator = new NoiseCaveGenerator(seed, access);
    }

    public BiomeGenerator(long seed, BiomeGenerator other) {
        this.surfaceDecorator = other.surfaceDecorator;
        this.featureDecorator = other.featureDecorator;
        this.noiseCaveGenerator = new NoiseCaveGenerator(seed, other.noiseCaveGenerator);
    }

    public void surface(ChunkAccess chunk, WorldGenRegion region, Generator generator) {
        surfaceDecorator.decorate(chunk, region, generator);
        surfaceDecorator.decoratePost(chunk, generator);
    }

    public void carve(long seed,
                      ChunkAccess chunk,
                      WorldGenRegion region,
                      BiomeManager biomes,
                      GenerationStep.Carving step,
                      Generator generator) {

        noiseCaveGenerator.carve(chunk, generator);
    }

    public void decorate(ChunkAccess chunk, WorldGenLevel region, StructureFeatureManager structures, Generator generator) {
        var terrain = generator.getChunkDataAsync(chunk.getPos());

        featureDecorator.decorate(chunk, region, structures, terrain, generator);
        noiseCaveGenerator.decorate(chunk, region, generator);

        Surface.applyPost(chunk, terrain.join(), generator);
    }

    protected static void buildVanillaSurface(ChunkAccess chunk, WorldGenRegion region, Generator generator) {
        var context = new WorldGenerationContext(generator, region);
        var noiseChunk = NoiseChunkUtil.getNoiseChunk(chunk, generator);

        var biomes = generator.getBiomeSource().getRegistry();
        var biomeManager = region.getBiomeManager();

        var surface = generator.getVanillaGen().getSurfaceSystem();
        var surfaceRules = generator.getVanillaGen().getSettings().value().surfaceRule();
        surface.buildSurface(biomeManager, biomes, false, context, chunk, noiseChunk, surfaceRules);
    }
}
