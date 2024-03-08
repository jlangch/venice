package com.github.jlangch.venice.support;

import java.util.ArrayList;
import java.util.List;


public class Apple {

    public Apple() {
    }

    public Color getColorDefault() {
        return Color.green;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(final Color color) {
        this.color = color;
    }

    public List<Color> getShades() {
        return shades;
    }

    public void setShades(final List<Color> shades) {
        this.shades.clear();
        if (shades != null) {
        	for(Object s : shades) {
        		if (!(s instanceof Color)) {
        			throw new IllegalArgumentException("args must be of type " + Color.class.getName());
        		}
        	}
            this.shades.addAll(shades);
        }
    }

    private Color color;
    private List<Color> shades = new ArrayList<>();
}
