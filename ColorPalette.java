package util;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * ColorPalette inspired by Solo Leveling's aesthetic
 */
public class ColorPalette {
    // Solo Leveling inspired theme colors
    public static final Color SL_PRIMARY = new Color(64, 80, 141);      // Blue-purple
    public static final Color SL_PRIMARY_DARK = new Color(32, 40, 70);  // Darker blue-purple
    public static final Color SL_PRIMARY_LIGHT = new Color(107, 126, 197); // Lighter blue-purple
    public static final Color SL_ACCENT = new Color(109, 198, 236);     // Cyan/blue glow
    public static final Color SL_ACCENT_BRIGHT = new Color(0, 255, 255); // Bright cyan for highlights
    public static final Color SL_BACKGROUND = new Color(22, 25, 37);    // Dark background
    public static final Color SL_CARD = new Color(35, 40, 60);          // Slightly lighter background
    public static final Color SL_TEXT_PRIMARY = new Color(240, 245, 255); // Almost white
    public static final Color SL_TEXT_SECONDARY = new Color(170, 185, 220); // Light blue-gray
    public static final Color SL_DIVIDER = new Color(75, 85, 125);      // Medium blue-purple

    // Status colors with Solo Leveling aesthetic
    public static final Color SL_SUCCESS = new Color(0, 255, 180);      // Mint green glow
    public static final Color SL_ERROR = new Color(255, 60, 110);       // Red-pink
    public static final Color SL_WARNING = new Color(255, 200, 70);     // Amber yellow
    public static final Color SL_INFO = new Color(109, 198, 236);       // Cyan

    // Gamification elements with Solo Leveling aesthetic
    public static final Color SL_XP = new Color(255, 215, 80);          // Gold with higher saturation
    public static final Color SL_COIN = new Color(255, 180, 0);         // Golden yellow
    public static final Color SL_LEVEL = new Color(109, 198, 236);      // Glowing blue
    public static final Color SL_MANA = new Color(130, 60, 255);        // Purple

    // Task difficulty colors
    public static final Color SL_EASY = new Color(0, 255, 180);         // Mint green
    public static final Color SL_MEDIUM = new Color(255, 200, 70);      // Amber yellow
    public static final Color SL_HARD = new Color(255, 60, 110);        // Red-pink

    // Medal colors
    public static final Color SL_GOLD = new Color(255, 215, 80);        // Gold
    public static final Color SL_SILVER = new Color(200, 210, 225);     // Silver
    public static final Color SL_BRONZE = new Color(205, 127, 50);      // Bronze

    // Pomodoro timer colors
    public static final Color SL_STUDY = new Color(0, 255, 180);        // Mint green for study time
    public static final Color SL_BREAK = new Color(135, 206, 250);      // Light sky blue for break time

    // Regular colors (Light theme)
    public static final Color LIGHT_PRIMARY = new Color(63, 81, 181);
    public static final Color LIGHT_PRIMARY_DARK = new Color(48, 63, 159);
    public static final Color LIGHT_PRIMARY_LIGHT = new Color(121, 134, 203);
    public static final Color LIGHT_ACCENT = new Color(255, 64, 129);
    public static final Color LIGHT_BACKGROUND = new Color(245, 245, 245);
    public static final Color LIGHT_CARD = new Color(255, 255, 255);
    public static final Color LIGHT_TEXT_PRIMARY = new Color(33, 33, 33);
    public static final Color LIGHT_TEXT_SECONDARY = new Color(117, 117, 117);
    public static final Color LIGHT_DIVIDER = new Color(189, 189, 189);

    public static final Color LIGHT_SUCCESS = new Color(76, 175, 80);
    public static final Color LIGHT_ERROR = new Color(244, 67, 54);
    public static final Color LIGHT_WARNING = new Color(255, 152, 0);
    public static final Color LIGHT_INFO = new Color(33, 150, 243);

    private static final Map<String, Color> slColorMap = new HashMap<>();
    private static final Map<String, Color> lightColorMap = new HashMap<>();

    static {
        // Initialize Solo Leveling theme map (used for both dark and light mode in this app)
        slColorMap.put("primary", SL_PRIMARY);
        slColorMap.put("primaryDark", SL_PRIMARY_DARK);
        slColorMap.put("primaryLight", SL_PRIMARY_LIGHT);
        slColorMap.put("accent", SL_ACCENT);
        slColorMap.put("accentBright", SL_ACCENT_BRIGHT);
        slColorMap.put("background", SL_BACKGROUND);
        slColorMap.put("panelBackground", SL_BACKGROUND);
        slColorMap.put("cardBackground", SL_CARD);
        slColorMap.put("text", SL_TEXT_PRIMARY);
        slColorMap.put("textSecondary", SL_TEXT_SECONDARY);
        slColorMap.put("divider", SL_DIVIDER);
        slColorMap.put("success", SL_SUCCESS);
        slColorMap.put("error", SL_ERROR);
        slColorMap.put("warning", SL_WARNING);
        slColorMap.put("info", SL_INFO);
        slColorMap.put("buttonBackground", SL_PRIMARY);
        slColorMap.put("buttonText", SL_TEXT_PRIMARY);
        slColorMap.put("xp", SL_XP);
        slColorMap.put("coin", SL_COIN);
        slColorMap.put("level", SL_LEVEL);
        slColorMap.put("mana", SL_MANA);

        // Initialize light theme map (fallback, not used in Solo Leveling theme)
        lightColorMap.put("primary", LIGHT_PRIMARY);
        lightColorMap.put("primaryDark", LIGHT_PRIMARY_DARK);
        lightColorMap.put("primaryLight", LIGHT_PRIMARY_LIGHT);
        lightColorMap.put("accent", LIGHT_ACCENT);
        lightColorMap.put("background", LIGHT_BACKGROUND);
        lightColorMap.put("panelBackground", LIGHT_BACKGROUND);
        lightColorMap.put("cardBackground", LIGHT_CARD);
        lightColorMap.put("text", LIGHT_TEXT_PRIMARY);
        lightColorMap.put("textSecondary", LIGHT_TEXT_SECONDARY);
        lightColorMap.put("divider", LIGHT_DIVIDER);
        lightColorMap.put("success", LIGHT_SUCCESS);
        lightColorMap.put("error", LIGHT_ERROR);
        lightColorMap.put("warning", LIGHT_WARNING);
        lightColorMap.put("info", LIGHT_INFO);
        lightColorMap.put("buttonBackground", LIGHT_PRIMARY);
        lightColorMap.put("buttonText", Color.WHITE);
    }

    /**
     * Get the color map for the specified theme
     */
    public static Map<String, Color> getColorMap(boolean isDarkTheme) {
        // Always return Solo Leveling theme regardless of dark/light setting
        return slColorMap;
    }

    /**
     * Get a specific color for the current theme
     */
    public static Color getColor(String colorName, boolean isDarkTheme) {
        Map<String, Color> colorMap = getColorMap(isDarkTheme);
        return colorMap.getOrDefault(colorName, slColorMap.get("text"));
    }
}
