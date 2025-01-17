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

package com.terraforged.mod.worldgen.noise;

import com.terraforged.mod.worldgen.terrain.TerrainLevels;

public class NoiseLevels {
    public final boolean auto;
    public final float scale;

    public final float depthMin;
    public final float depthRange;

    public final float heightMin;
    public final float baseRange;
    public final float heightRange;

    public final float frequency;

    public NoiseLevels(boolean autoScale, float scale, int seaLevel, int seaFloor, int genDepth) {
        this.auto = autoScale;
        this.scale = scale;
        this.depthMin = seaFloor / (float) genDepth;
        this.heightMin = seaLevel / (float) genDepth;
        this.baseRange = (genDepth * 0.15F) / genDepth;
        this.heightRange = 1F - (heightMin + baseRange);
        this.depthRange = heightMin - depthMin;
        this.frequency = calcFrequency(genDepth - seaLevel, auto, scale);
    }

    public float toDepthNoise(float noise) {
        return depthMin + noise * depthRange;
    }

    public float toHeightNoise(float baseNoise, float heightNoise) {
        return heightMin + baseRange * baseNoise + heightRange * heightNoise;
    }

    public static NoiseLevels getDefault() {
        return TerrainLevels.DEFAULT.get().noiseLevels;
    }

    public static float calcFrequency(int verticalRange, boolean auto, float scale) {
        scale = scale <= 0F ? 1 : scale;

        if (!auto) return scale;

        float frequency = (TerrainLevels.Defaults.LEGACY_GEN_DEPTH - TerrainLevels.Defaults.SEA_LEVEL) / (float) verticalRange;

        return frequency * scale;
    }
}
