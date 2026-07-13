package yifei.pua.api;

import yifei.pua.api.impl.PunctuationAPIImpl;

public class PunctuationAPIAccess {
    private static PunctuationAPI instance;

    private PunctuationAPIAccess() {
    }

    public static PunctuationAPI getInstance() {
        if (instance == null) {
            instance = new PunctuationAPIImpl();
        }
        return instance;
    }

    public static RenderAPI getRenderAPI() {
        return getInstance().getRenderAPI();
    }

    public static ParticleAPI getParticleAPI() {
        return getInstance().getParticleAPI();
    }

    public static RaycastAPI getRaycastAPI() {
        return getInstance().getRaycastAPI();
    }
}