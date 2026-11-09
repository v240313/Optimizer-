package com.example.optimizer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.ParticlesMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OptimizerClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("optimizer");

    // Configuración
    private static final int MIN_VIEW = 6;
    private static final int MAX_VIEW = 12;
    private static final int LOW_FPS = 40;
    private static final int HIGH_FPS = 90;
    private static final int CHECK_INTERVAL_TICKS = 100; // 5 s

    private int ticks = 0;

    @Override
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register(this::applyBaseSettings);
        ClientTickEvents.END_CLIENT_TICK.register(this::dynamicViewDistance);
    }

    private void applyBaseSettings(MinecraftClient client) {
        GameOptions o = client.options;
        o.getParticles().setValue(ParticlesMode.MINIMAL);
        o.getCloudRenderMode().setValue(CloudRenderMode.OFF);
        o.getMipmapLevels().setValue(2);
        o.getBiomeBlendRadius().setValue(1);
        o.getEntityDistanceScaling().setValue(0.75);
        o.getSimulationDistance().setValue(Math.min(o.getSimulationDistance().getValue(), 8));
        o.getViewDistance().setValue(Math.min(o.getViewDistance().getValue(), MAX_VIEW));
        o.write();
        LOGGER.info("Optimizer: ajustes base aplicados");
    }

    private void dynamicViewDistance(MinecraftClient client) {
        if (client.world == null || ++ticks < CHECK_INTERVAL_TICKS) return;
        ticks = 0;

        int fps = client.getCurrentFps();
        var view = client.options.getViewDistance();
        int current = view.getValue();

        if (fps < LOW_FPS && current > MIN_VIEW) {
            view.setValue(current - 1);
        } else if (fps > HIGH_FPS && current < MAX_VIEW) {
            view.setValue(current + 1);
        }
    }
  }
