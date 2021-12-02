package com.terraforged.mod;

import com.google.common.base.Suppliers;
import com.terraforged.mod.platform.Platform;
import com.terraforged.mod.registry.GenRegistry;
import com.terraforged.mod.registry.ModRegistry;
import com.terraforged.mod.worldgen.Generator;
import com.terraforged.mod.worldgen.asset.TerrainConfig;
import com.terraforged.mod.worldgen.asset.ViabilityConfig;
import com.terraforged.mod.worldgen.biome.Source;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.function.Supplier;

public abstract class TerraForged implements Platform {
	public static final String MODID = "terraforged";
	public static final String TITLE = "TerraForged";
	public static final Logger LOG = LogManager.getLogger(TITLE);

	private final Supplier<Path> container;

	protected TerraForged(Supplier<Path> containerGetter) {
		this.container = Suppliers.memoize(containerGetter::get);
		Platform.ACTIVE_PLATFORM.set(this);
	}

	@Override
	public final Path getContainer() {
		return container.get();
	}

	protected void init() {
		LOG.info("Registering world-gen core codecs");
		Registry.register(Registry.BIOME_SOURCE, TerraForged.location("climate"), Source.CODEC);
		Registry.register(Registry.CHUNK_GENERATOR, TerraForged.location("generator"), Generator.CODEC);

		LOG.info("Registering world-gen component codecs");
		GenRegistry.get().register(ModRegistry.TERRAIN, TerrainConfig.CODEC);
		GenRegistry.get().register(ModRegistry.VIABILITY, ViabilityConfig.CODEC);
	}

	public static Platform getPlatform() {
		return Platform.ACTIVE_PLATFORM.get();
	}

	public static ResourceLocation location(String name) {
		return new ResourceLocation(MODID, name);
	}

	public static <T> ResourceKey<Registry<T>> registry(String name) {
		return ResourceKey.createRegistryKey(location(name));
	}
}
