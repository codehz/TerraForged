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

import com.terraforged.engine.world.terrain.Terrain;
import com.terraforged.mod.util.MathUtil;
import com.terraforged.mod.util.map.Object2FloatCache;
import com.terraforged.mod.util.map.WeightMap;
import com.terraforged.mod.util.seed.Seedable;
import com.terraforged.mod.worldgen.asset.TerrainConfig;
import com.terraforged.noise.Module;
import com.terraforged.noise.domain.Domain;
import com.terraforged.noise.util.NoiseUtil;

public class TerrainBlender implements Module, Seedable<TerrainBlender> {
    private static final int REGION_SEED_OFFSET = 21491124;
    private static final int WARP_SEED_OFFSET = 12678;

    private final int regionSeed;
    private final int scale;
    private final float frequency;
    private final float jitter;
    private final float blending;

    private final Domain warp;
    private final WeightMap<TerrainConfig> terrains;
    private final ThreadLocal<Blender> localBlender = ThreadLocal.withInitial(Blender::new);

    public TerrainBlender(long seed, int scale, float jitter, float blending, TerrainConfig[] terrains) {
        this.regionSeed = (int) seed + REGION_SEED_OFFSET;
        this.scale = scale;
        this.frequency = 1F / scale;
        this.jitter = jitter;
        this.blending = blending;
        this.terrains = WeightMap.of(terrains);
        this.warp = Domain.warp((int) seed + WARP_SEED_OFFSET, scale / 2, 3,scale / 2F);
    }

    @Override
    public TerrainBlender withSeed(long seed) {
        var input = terrains.getValues();
        var output = new TerrainConfig[input.length];
        for (int i = 0; i < output.length; i++) {
            output[i] = input[i].withSeed(seed);
        }
        return new TerrainBlender(seed, scale, jitter, blending, output);
    }

    @Override
    public float getValue(float x, float z) {
        var blender = localBlender.get();
        return getValue(x, z, blender);
    }

    public float getValue(float x, float z, Blender blender) {
        float rx = warp.getX(x, z) * frequency;
        float rz = warp.getY(x, z) * frequency;
        getCell(regionSeed, x * frequency, z * frequency, jitter, blender);
        return blender.getValue(x, z, blending, terrains);
    }

    public Blender getBlenderResource() {
        return localBlender.get();
    }

    public Terrain getTerrain(Blender blender) {
        float index = blender.getCentreNoiseIndex();
        return terrains.getValue(index).terrain();
    }

    private static void getCell(int seed, float x, float z, float jitter, Blender blender) {
        int xr = NoiseUtil.floor(x);
        int zr = NoiseUtil.floor(z);

        blender.closestIndex = 0;
        blender.closestIndex2 = 0;

        int nearestIndex = -1;
        int nearestIndex2 = -1;

        float nearestDistance = Float.MAX_VALUE;
        float nearestDistance2 = Float.MAX_VALUE;

        for (int cz = zr - 1, i = 0; cz <= zr + 1; cz++) {
            for (int cx = xr - 1; cx <= xr + 1; cx++, i++) {
                int hash = NoiseUtil.hash2D(seed, cx, cz);

                float dx = MathUtil.rand(hash, NoiseUtil.X_PRIME);
                float dz = MathUtil.rand(hash, NoiseUtil.Y_PRIME);

                float px = cx + dx * jitter;
                float pz = cz + dz * jitter;
                float dist2 = NoiseUtil.dist2(x, z, px, pz);

                blender.hashes[i] = hash;
                blender.distances[i] = dist2;

                if (dist2 < nearestDistance) {
                    nearestDistance2 = nearestDistance;
                    nearestDistance = dist2;
                    nearestIndex2 = nearestIndex;
                    nearestIndex = i;
                } else if (dist2 < nearestDistance2) {
                    nearestDistance2 = dist2;
                    nearestIndex2 =  i;
                }
            }
        }

        blender.closestIndex = nearestIndex;
        blender.closestIndex2 = nearestIndex2;
    }

    public static class Blender {
        protected int closestIndex;
        protected int closestIndex2;

        protected final int[] hashes = new int[9];
        protected final float[] distances = new float[9];
        protected final Object2FloatCache<TerrainConfig> cache = new Object2FloatCache<>(9);

        public float getCentreNoiseIndex() {
            return getNoiseIndex(closestIndex);
        }

        public float getDistance(int index) {
            return NoiseUtil.sqrt(distances[index]);
        }

        public float getCentreValue(float x, float z, WeightMap<TerrainConfig> terrains) {
            float noise = getCentreNoiseIndex();
            return terrains.getValue(noise).noise().getValue(x, z);
        }

        public float getValue(float x, float z, float edge, WeightMap<TerrainConfig> terrains) {
            float dist0 = getDistance(closestIndex);
            float dist1 = getDistance(closestIndex2);
            float edgeNoise = 1 - dist0 / dist1;

            if (edgeNoise >= edge) {
                return getCentreValue(x, z, terrains);
            } else {
                float blendRange = getBlendRange(dist0, dist1, edge, edgeNoise);
                return getBlendedValue(x, z, dist0, dist1, blendRange, terrains);
            }
        }

        public float getBlendedValue(float x, float z, float nearest, float nearest2, float blendRange, WeightMap<TerrainConfig> terrains) {
            cache.clear();

            float sumNoise = getCacheValue(closestIndex, x, z, terrains);
            float sumWeight = getWeight(nearest, nearest, blendRange);

            float nearestWeight2 = getWeight(nearest2, nearest, blendRange);
            if (nearestWeight2 > 0) {
                sumNoise += getCacheValue(closestIndex2, x, z, terrains) * nearestWeight2;
                sumWeight += nearestWeight2;
            }

            for (int i = 0; i < 9; i++) {
                if (i == closestIndex || i == closestIndex2) continue;

                float weight = getWeight(getDistance(i), nearest, blendRange);
                if (weight > 0) {
                    sumNoise += getCacheValue(i, x, z, terrains) * weight;
                    sumWeight += weight;
                }
            }

            return NoiseUtil.clamp(sumNoise / sumWeight, 0, 1);
        }

        private float getBlendRange(float dist0, float dist1, float edge, float edgeNoise) {
            float alpha = edgeNoise / edge;
            if (alpha == 0) return dist0 * edge;

            float mid = (dist0 + dist1) * 0.5F;
            return ((mid - dist0) / alpha);
        }

        private float getCacheValue(int index, float x, float z, WeightMap<TerrainConfig> terrains) {
            float noiseIndex = getNoiseIndex(index);
            var terrain = terrains.getValue(noiseIndex);

            float value = cache.get(terrain);
            if (Float.isNaN(value)) {
                value = terrain.noise().getValue(x, z);
                cache.put(terrain, value);
            }

            return value;
        }

        private float getNoiseIndex(int index) {
            return MathUtil.rand(hashes[index]);
        }

        private static float getWeight(float dist, float origin, float blendRange) {
            float delta = dist - origin;
            float weight = delta >= blendRange ? 0 : 1 - delta / blendRange;
            return weight * weight;
        }
    }
}
