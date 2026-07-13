package yifei.pua.api;

import net.minecraft.util.math.Vec3d;

public interface ParticleAPI {

    void spawnMarkerParticles(Vec3d pos);

    void spawnBeamParticles(Vec3d startPos, Vec3d endPos);

    void spawnContinuousBlockParticles(Vec3d pos);

    void spawnContinuousEntityParticles(Vec3d pos);
}