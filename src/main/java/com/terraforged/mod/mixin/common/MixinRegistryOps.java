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

package com.terraforged.mod.mixin.common;

import com.mojang.serialization.DynamicOps;
import com.terraforged.mod.registry.hooks.BuiltinHook;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.RegistryResourceAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RegistryOps.class)
public class MixinRegistryOps {
    @Inject(
            method = "createAndLoad(Lcom/mojang/serialization/DynamicOps;Lnet/minecraft/core/RegistryAccess$Writable;Lnet/minecraft/resources/RegistryResourceAccess;)Lnet/minecraft/resources/RegistryOps;",
            at = @At("RETURN")
    )
    private static <T> void onCreateAndLoad(DynamicOps<T> ops,
                                     RegistryAccess.Writable writable,
                                     RegistryResourceAccess resource,
                                     CallbackInfoReturnable<RegistryOps<T>> cir) {
        BuiltinHook.load(writable, cir.getReturnValue());
    }
}
