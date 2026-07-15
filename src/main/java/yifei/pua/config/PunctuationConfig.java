package yifei.pua.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Properties;

public class PunctuationConfig {
    public static boolean showBorder = true;
    public static ColorScheme colorScheme = ColorScheme.NATURE;
    public static boolean useGradient = true;
    public static int markerCacheTime = 300;
    public static final int MAX_CACHE_TIME = 600;

    private static File configFile;

    public enum ColorScheme {
        NATURE(0x00FF80, 0x00FF00),
        FIRE(0xFF4500, 0xFF0000),
        ICE(0x87CEEB, 0x00BFFF),
        PURPLE(0x9400D3, 0x4B0082),
        GOLD(0xFFD700, 0xFFA500),
        RED(0xFF0000, 0x8B0000),
        BLUE(0x0000FF, 0x00008B),
        GREEN(0x00FF00, 0x008000),
        PINK(0xFF69B4, 0xFF1493),
        ORANGE(0xFFA500, 0xFF8C00),
        CYAN(0x00FFFF, 0x00CED1),
        MAGENTA(0xFF00FF, 0x8B008B),
        WHITE(0xFFFFFF, 0xC0C0C0),
        BLACK(0x808080, 0x000000);

        private final int startColor;
        private final int endColor;

        ColorScheme(int startColor, int endColor) {
            this.startColor = startColor;
            this.endColor = endColor;
        }

        public int getStartColor() {
            return startColor;
        }

        public int getEndColor() {
            return endColor;
        }

        public Text getDisplayName() {
            return Text.translatable("config.pua.color_scheme." + this.name().toLowerCase());
        }

        @Override
        public String toString() {
            return getDisplayName().getString();
        }
    }

    public static void init(Path configDir) {
        configFile = configDir.resolve("pua.properties").toFile();
        load();
        if (!configFile.exists()) {
            save();
        }
    }

    private static void load() {
        if (configFile.exists()) {
            Properties props = new Properties();
            try (InputStreamReader reader = new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8)) {
                props.load(reader);
                showBorder = Boolean.parseBoolean(props.getProperty("showBorder", "true"));
                try {
                    colorScheme = ColorScheme.valueOf(props.getProperty("colorScheme", "NATURE"));
                } catch (IllegalArgumentException e) {
                    colorScheme = ColorScheme.NATURE;
                }
                useGradient = Boolean.parseBoolean(props.getProperty("useGradient", "true"));
                try {
                    int cacheTime = Integer.parseInt(props.getProperty("markerCacheTime", "300"));
                    markerCacheTime = Math.min(Math.max(cacheTime, 10), MAX_CACHE_TIME);
                } catch (NumberFormatException e) {
                    markerCacheTime = 300;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void save() {
        try {
            if (!configFile.getParentFile().exists()) {
                configFile.getParentFile().mkdirs();
            }
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8)) {
                writer.write("# Punctuation Mod Configuration\n");
                writer.write("# 标点模组配置文件\n");
                writer.write("#\n");
                writer.write("# showBorder - 是否显示标点高亮边框\n");
                writer.write("#\n");
                writer.write("# colorScheme - 边框颜色方案：\n");
                writer.write("#   NATURE   - 自然绿\n");
                writer.write("#   ICE      - 寒冰蓝\n");
                writer.write("#   FIRE     - 火焰红\n");
                writer.write("#   PURPLE   - 神秘紫\n");
                writer.write("#   GOLD     - 金色\n");
                writer.write("#   RED      - 红色\n");
                writer.write("#   BLUE     - 蓝色\n");
                writer.write("#   GREEN    - 绿色\n");
                writer.write("#   PINK     - 粉色\n");
                writer.write("#   ORANGE   - 橙色\n");
                writer.write("#   CYAN     - 青色\n");
                writer.write("#   MAGENTA  - 品红\n");
                writer.write("#   WHITE    - 白色\n");
                writer.write("#   BLACK    - 黑色\n");
                writer.write("#\n");
                writer.write("# useGradient - 是否启用边框颜色渐变\n");
                writer.write("#\n");
                writer.write("# markerCacheTime - 标点缓存时间（秒），范围 10-600，默认 300（5分钟）\n");
                writer.write("#\n");
                writer.write("showBorder=" + showBorder + "\n");
                writer.write("colorScheme=" + colorScheme.name() + "\n");
                writer.write("useGradient=" + useGradient + "\n");
                writer.write("markerCacheTime=" + markerCacheTime + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("config.pua.title"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        ConfigCategory visualCategory = builder.getOrCreateCategory(Text.translatable("config.pua.category.visual"));

        visualCategory.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.pua.show_border"), showBorder)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("config.pua.show_border.tooltip"))
                .setSaveConsumer(newValue -> showBorder = newValue)
                .build());

        visualCategory.addEntry(entryBuilder.startEnumSelector(Text.translatable("config.pua.color_scheme"), ColorScheme.class, colorScheme)
                .setDefaultValue(ColorScheme.NATURE)
                .setTooltip(Text.translatable("config.pua.color_scheme.tooltip"))
                .setSaveConsumer(newValue -> colorScheme = newValue)
                .build());

        visualCategory.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.pua.use_gradient"), useGradient)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("config.pua.use_gradient.tooltip"))
                .setSaveConsumer(newValue -> useGradient = newValue)
                .build());

        visualCategory.addEntry(entryBuilder.startIntField(Text.translatable("config.pua.marker_cache_time"), markerCacheTime)
                .setDefaultValue(300)
                .setMin(10)
                .setMax(MAX_CACHE_TIME)
                .setTooltip(Text.translatable("config.pua.marker_cache_time.tooltip"))
                .setSaveConsumer(newValue -> markerCacheTime = newValue)
                .build());

        builder.setSavingRunnable(PunctuationConfig::save);

        return builder.build();
    }

    public static float[] getBorderColor() {
        return intToRgb(colorScheme.getStartColor());
    }

    public static float[] getBorderColorEnd() {
        return intToRgb(colorScheme.getEndColor());
    }

    private static float[] intToRgb(int color) {
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        return new float[]{r, g, b};
    }
}