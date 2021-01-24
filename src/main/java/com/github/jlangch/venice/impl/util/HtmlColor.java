/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2021 Venice
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jlangch.venice.impl.util;

import java.awt.Color;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * HTML color utils.
 * 
 * @author juerg
 */
public class HtmlColor {
	
	public static String getHexColor(final String name) {
		if (name == null) {
			return null;
		}
		else {
			return (String)colors.get(name.toLowerCase(Locale.getDefault()));
		}
	}
	
	public static boolean isValidName(final String name) {
		return getColor(name) != null;
	}
	
	/**
	 * Parses a stringified color.
	 * 
	 * <p>Colors are defined using a hexadecimal notation for the 
	 * combination of Red, Green, and Blue color values (RGB). The lowest 
	 * value that can be given to one light source is 0 (hex #00). The 
	 * highest value is 255 (hex #FF).
	 * 
	 * <p>E.g:
	 * <p>Hex notation:</p><ul>
	 *   <li> #000000 =&gt; BLACK
	 *   <li> #FF0000 =&gt; RED
	 *   <li> #00FF00 =&gt; GREEN
	 *   <li> #0000FF =&gt; BLUE
	 *   <li> #FFFFFF =&gt; WHITE   
	 * </ul>
	 *  <p>Symbolic color names:</p><ul>
	 *   <li> black   =&gt; BLACK
	 *   <li> red     =&gt; RED
	 *   <li> green   =&gt; GREEN
	 *   <li> blue    =&gt; BLUE
	 *   <li> white   =&gt; WHITE   
	 * </ul>
	 * <p>Hex notation without leading hash:</p><ul>
	 *   <li> 000000 =&gt; BLACK
	 *   <li> FF0000 =&gt; RED
	 *   <li> 00FF00 =&gt; GREEN
	 *   <li> 0000FF =&gt; BLUE
	 *   <li> FFFFFF =&gt; WHITE
	 * </ul>
	 * 
	 * @param name a symbolic color name or a 6-digit hex color
	 * @return The parsed color or null on parsing errors
	 */
	public static Color getColor(final String name) {
		if (name == null || name.trim().length() == 0) {
			return null;
		}
		
		String hexColor = (String)colors.get(name.trim().toLowerCase(Locale.getDefault()));
		if (hexColor == null) {
			hexColor = name.trim(); // Try to parse name as hex color 
		}
		
		hexColor = hexColor.startsWith("#") ? hexColor.substring(1) : hexColor;
		
		if ((hexColor.length()) == 6) {
			try {
				return new Color(
						Integer.parseInt(hexColor.substring(0,2), 16),
						Integer.parseInt(hexColor.substring(2,4), 16),
						Integer.parseInt(hexColor.substring(4,6), 16));
			}
			catch(RuntimeException ex) {
				return null;
			}
		}
		else {
			return null;
		}
	}
	
	private static Map<String,String> colors = new ConcurrentHashMap<String,String>();

	static {
		colors.put("AliceBlue", 		"#F0F8FF");
		colors.put("AntiqueWhite", 		"#FAEBD7");
		colors.put("Aqua", 				"#00FFFF");
		colors.put("Aquamarine", 		"#7FFFD4");
		colors.put("Azure", 			"#F0FFFF");
		colors.put("Beige", 			"#F5F5DC");
		colors.put("Bisque", 			"#FFE4C4");
		colors.put("Black", 			"#000000");
		colors.put("BlanchedAlmond", 	"#FFEBCD");
		colors.put("Blue", 				"#0000FF");
		colors.put("BlueViolet", 		"#8A2BE2");
		colors.put("Brown", 			"#A52A2A");
		colors.put("BurlyWood", 		"#DEB887");
		colors.put("CadetBlue", 		"#5F9EA0");
		colors.put("Chartreuse", 		"#7FFF00");
		colors.put("Chocolate", 		"#D2691E");
		colors.put("Coral", 			"#FF7F50");
		colors.put("CornflowerBlue", 	"#6495ED");
		colors.put("Cornsilk", 			"#FFF8DC");
		colors.put("Crimson", 			"#DC143C");
		colors.put("Cyan", 				"#00FFFF");
		colors.put("DarkBlue", 			"#00008B");
		colors.put("DarkCyan", 			"#008B8B");
		colors.put("DarkGoldenRod", 	"#B8860B");
		colors.put("DarkGray", 			"#A9A9A9");
		colors.put("DarkGrey", 			"#A9A9A9");
		colors.put("DarkGreen", 		"#006400");
		colors.put("DarkKhaki", 		"#BDB76B");
		colors.put("DarkMagenta", 		"#8B008B");
		colors.put("DarkOliveGreen", 	"#556B2F");
		colors.put("Darkorange", 		"#FF8C00");
		colors.put("DarkOrchid", 		"#9932CC");
		colors.put("DarkRed", 			"#8B0000");
		colors.put("DarkSalmon",	 	"#E9967A");
		colors.put("DarkSeaGreen",	 	"#8FBC8F");
		colors.put("DarkSlateBlue", 	"#483D8B");
		colors.put("DarkSlateGray", 	"#2F4F4F");
		colors.put("DarkSlateGrey", 	"#2F4F4F");
		colors.put("DarkTurquoise", 	"#00CED1");
		colors.put("DarkViolet", 		"#9400D3");
		colors.put("DeepPink", 			"#FF1493");
		colors.put("DeepSkyBlue", 		"#00BFFF");
		colors.put("DimGray", 			"#696969");
		colors.put("DimGrey", 			"#696969");
		colors.put("DodgerBlue", 		"#1E90FF");
		colors.put("FireBrick", 		"#B22222");
		colors.put("FloralWhite",	 	"#FFFAF0");
		colors.put("ForestGreen", 		"#228B22");
		colors.put("Fuchsia", 			"#FF00FF");
		colors.put("Gainsboro", 		"#DCDCDC");
		colors.put("GhostWhite", 		"#F8F8FF");
		colors.put("Gold", 				"#FFD700");
		colors.put("GoldenRod", 		"#DAA520");
		colors.put("Gray", 				"#808080");
		colors.put("Grey", 				"#808080");
		colors.put("Green", 			"#008000");
		colors.put("GreenYellow", 		"#ADFF2F");
		colors.put("HoneyDew", 			"#F0FFF0");
		colors.put("HotPink", 			"#FF69B4");
		colors.put("IndianRed",  		"#CD5C5C");
		colors.put("Indigo",  			"#4B0082");
		colors.put("Ivory", 			"#FFFFF0");
		colors.put("Khaki", 			"#F0E68C");
		colors.put("Lavender", 			"#E6E6FA");
		colors.put("LavenderBlush", 	"#FFF0F5");
		colors.put("LawnGreen", 		"#7CFC00");
		colors.put("LemonChiffon", 		"#FFFACD");
		colors.put("LightBlue", 		"#ADD8E6");
		colors.put("LightCoral", 		"#F08080");
		colors.put("LightCyan", 		"#E0FFFF");
		colors.put("LightGoldenRodYellow", "#FAFAD2");
		colors.put("LightGray", 		"#D3D3D3");
		colors.put("LightGrey", 		"#D3D3D3");
		colors.put("LightGreen", 		"#90EE90");
		colors.put("LightPink", 		"#FFB6C1");
		colors.put("LightSalmon", 		"#FFA07A");
		colors.put("LightSeaGreen", 	"#20B2AA");
		colors.put("LightSkyBlue", 		"#87CEFA");
		colors.put("LightSlateGray", 	"#778899");
		colors.put("LightSlateGrey", 	"#778899");
		colors.put("LightSteelBlue", 	"#B0C4DE");
		colors.put("LightYellow", 		"#FFFFE0");
		colors.put("Lime", 				"#00FF00");
		colors.put("LimeGreen", 		"#32CD32");
		colors.put("Linen", 			"#FAF0E6");
		colors.put("Magenta", 			"#FF00FF");
		colors.put("Maroon", 			"#800000");
		colors.put("MediumAquaMarine", 	"#66CDAA");
		colors.put("MediumBlue", 		"#0000CD");
		colors.put("MediumOrchid", 		"#BA55D3");
		colors.put("MediumPurple", 		"#9370D8");
		colors.put("MediumSeaGreen", 	"#3CB371");
		colors.put("MediumSlateBlue",	"#7B68EE");
		colors.put("MediumSpringGreen", "#00FA9A");
		colors.put("MediumTurquoise", 	"#48D1CC");
		colors.put("MediumVioletRed", 	"#C71585");
		colors.put("MidnightBlue", 		"#191970");
		colors.put("MintCream", 		"#F5FFFA");
		colors.put("MistyRose", 		"#FFE4E1");
		colors.put("Moccasin", 			"#FFE4B5");
		colors.put("NavajoWhite", 		"#FFDEAD");
		colors.put("Navy", 				"#000080");
		colors.put("OldLace", 			"#FDF5E6");
		colors.put("Olive", 			"#808000");
		colors.put("OliveDrab", 		"#6B8E23");
		colors.put("Orange", 			"#FFA500");
		colors.put("OrangeRed", 		"#FF4500");
		colors.put("Orchid", 			"#DA70D6");
		colors.put("PaleGoldenRod", 	"#EEE8AA");
		colors.put("PaleGreen", 		"#98FB98");
		colors.put("PaleTurquoise", 	"#AFEEEE");
		colors.put("PaleVioletRed", 	"#D87093");
		colors.put("PapayaWhip", 		"#FFEFD5");
		colors.put("PeachPuff", 		"#FFDAB9");
		colors.put("Peru", 				"#CD853F");
		colors.put("Pink", 				"#FFC0CB");
		colors.put("Plum", 				"#DDA0DD");
		colors.put("PowderBlue", 		"#B0E0E6");
		colors.put("Purple", 			"#800080");
		colors.put("Red", 				"#FF0000");
		colors.put("RosyBrown", 		"#BC8F8F");
		colors.put("RoyalBlue", 		"#4169E1");
		colors.put("SaddleBrown", 		"#8B4513");
		colors.put("Salmon", 			"#FA8072");
		colors.put("SandyBrown", 		"#F4A460");
		colors.put("SeaGreen", 			"#2E8B57");
		colors.put("SeaShell", 			"#FFF5EE");
		colors.put("Sienna", 			"#A0522D");
		colors.put("Silver", 			"#C0C0C0");
		colors.put("SkyBlue", 			"#87CEEB");
		colors.put("SlateBlue", 		"#6A5ACD");
		colors.put("SlateGray", 		"#708090");
		colors.put("SlateGrey", 		"#708090");
		colors.put("Snow", 				"#FFFAFA");
		colors.put("SpringGreen", 		"#00FF7F");
		colors.put("SteelBlue", 		"#4682B4");
		colors.put("Tan", 				"#D2B48C");
		colors.put("Teal", 				"#008080");
		colors.put("Thistle", 			"#D8BFD8");
		colors.put("Tomato", 			"#FF6347");
		colors.put("Turquoise", 		"#40E0D0");
		colors.put("Violet", 			"#EE82EE");
		colors.put("Wheat", 			"#F5DEB3");
		colors.put("White", 			"#FFFFFF");
		colors.put("WhiteSmoke", 		"#F5F5F5");
		colors.put("Yellow", 			"#FFFF00");
		colors.put("YellowGreen", 		"#9ACD32");
		
		// Add the lowercase color names
		Map<String,String> lowercase = new HashMap<String,String>();
		for (Map.Entry<String, String> entry : colors.entrySet()) {
			lowercase.put(entry.getKey().toLowerCase(Locale.getDefault()), entry.getValue());
		}
		colors.putAll(lowercase);
	}
}
