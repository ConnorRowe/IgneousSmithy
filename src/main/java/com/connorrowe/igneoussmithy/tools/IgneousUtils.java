package com.connorrowe.igneoussmithy.tools;

import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponent;

public final class IgneousUtils
{
    public static String CapitaliseString(String str)
    {
        if (str == null || str.isEmpty())
        {
            return str;
        }

        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static class BasicTextComponent extends TextComponent
    {
        private final String text;
        private final Style style;

        public BasicTextComponent(String text, Style style)
        {
            this.text = text;
            this.style = style;
        }

        @Override
        public String getUnformattedComponentText()
        {
            return text;
        }

        @Override
        public Style getStyle()
        {
            return style;
        }

        @Override
        public TextComponent copyRaw()
        {
            return this;
        }
    }
}
