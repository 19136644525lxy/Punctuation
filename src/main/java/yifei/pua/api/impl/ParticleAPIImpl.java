package yifei.pua.api.impl;

import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import yifei.pua.api.ParticleAPI;

public class ParticleAPIImpl implements ParticleAPI {
    private final Random random = Random.create();

    @Override
    public void spawnMarkerParticles(Vec3d pos) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        for (int i = 0; i < 8; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 0.5;
            double offsetY = (random.nextDouble() - 0.5) * 0.5;
            double offsetZ = (random.nextDouble() - 0.5) * 0.5;

            client.world.addParticle(ParticleTypes.END_ROD,
                    pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ,
                    0, 0.1, 0);
        }
    }

    @Override
    public void spawnBeamParticles(Vec3d startPos, Vec3d endPos) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        Vec3d direction = endPos.subtract(startPos);
        double distance = direction.length();
        Vec3d normalized = direction.normalize();

        int particleCount = (int) (distance * 2);
        for (int i = 0; i < particleCount; i++) {
            double t = (double) i / particleCount;
            Vec3d pos = startPos.add(normalized.multiply(t * distance));

            client.world.addParticle(ParticleTypes.ELECTRIC_SPARK,
                    pos.x, pos.y, pos.z,
                    (random.nextDouble() - 0.5) * 0.1,
                    (random.nextDouble() - 0.5) * 0.1,
                    (random.nextDouble() - 0.5) * 0.1);
        }
    }

    @Override
    public void spawnContinuousBlockParticles(Vec3d pos) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        for (int i = 0; i < 2; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 0.3;
            double offsetY = (random.nextDouble() - 0.5) * 0.3;
            double offsetZ = (random.nextDouble() - 0.5) * 0.3;

            client.world.addParticle(ParticleTypes.WAX_ON,
                    pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ,
                    0, 0.05, 0);
        }
    }

    @Override
    public void spawnContinuousEntityParticles(Vec3d pos) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        for (int i = 0; i < 3; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 0.2;
            double offsetY = (random.nextDouble() - 0.5) * 0.2;
            double offsetZ = (random.nextDouble() - 0.5) * 0.2;

            client.world.addParticle(ParticleTypes.END_ROD,
                    pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ,
                    (random.nextDouble() - 0.5) * 0.05,
                    0.05,
                    (random.nextDouble() - 0.5) * 0.05);
        }
    }
}