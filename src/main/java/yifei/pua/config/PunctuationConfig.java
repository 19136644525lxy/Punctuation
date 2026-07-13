package yifei.pua.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class PunctuationConfig {
    public static boolean showBorder = true;
    public static ColorScheme colorScheme = ColorScheme.NATURE;
    public static boolean useGradient = true;

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

        builder.setSavingRunnable(() -> {
        });

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