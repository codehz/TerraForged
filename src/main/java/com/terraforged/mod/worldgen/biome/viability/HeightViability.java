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

package com.terraforged.mod.worldgen.biome.viability;

import com.terraforged.cereal.spec.DataSpec;
import com.terraforged.cereal.value.DataValue;

public record HeightViability(float minOffset, float midOffset, float maxOffset) implements Viability {
    public static final DataSpec<HeightViability> SPEC = DataSpec.builder(
                    "Height",
                    HeightViability.class,
                    (data, spec, context) -> new HeightViability(
                            spec.get("min", data, DataValue::asFloat),
                            spec.get("mid", data, DataValue::asFloat),
                            spec.get("max", data, DataValue::asFloat)
                    )
            )
            .add("min", 0.0F, HeightViability::minOffset)
            .add("mid", 0.5F, HeightViability::midOffset)
            .add("max", 1.0F, HeightViability::maxOffset)
            .build();

    @Override
    public float getFitness(int x, int z, Context context) {
        int height = context.getTerrain().getHeight(x, z);

        var levels = context.getLevels();
        float scale = getScaler(levels);
        float min = levels.seaLevel + minOffset() * scale;
        float mid = levels.seaLevel + midOffset() * scale;
        float max = levels.seaLevel + maxOffset() * scale;

        if (height < min) return 1F;
        if (height > max) return 1F;

        if (height < mid) {
            return (mid - height) / (mid - min);
        }

        return (height - mid) / (max - mid);
    }
}
