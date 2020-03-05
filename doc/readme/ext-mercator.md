# MERCATOR Maps

The [Mercator Projection](https://en.wikipedia.org/wiki/Mercator_projection)
is a cylindrical map projection. 

Venice's **mercator** module draws locations given by world coordinates 
on a mercator map. A location is drawn as round marker with a label. 

World coordinates are given by a latitude and a longitude:

**Latitude** specifies the north-south position of a point on the Earth's
surface. Latitude is an angle which ranges from 0° at the equator to +90°
at north and -90° at south pole.

**Longitude** specifies the east-west position of a point on the Earth's
surface. Longitude is an angle which ranges from 0° at the prime meridian
to +180° eastward and -180° westward.


### API

Basic rendering flow:

```clojure
  (-> (mercator/load-mercator-image)
      (mercator/draw-locations '([47.3717400 8.5422600]))
      (mercator/crop-image 400 600)
      (mercator/save-image :png "./test-map.png")))
```

##### `load-mercator-image`

Usage: (load-mercator-image)

Loads the mercator image (returns an image of type `:java.awt.image.BufferedImage`)


##### `draw-locations`

Usage: (draw-locations image locations) (draw-locations image locations default-styles)

Draws the locations. Expects a list of locations and a :java.awt.image.BufferedImage. 
A location is a vector composed of a latitude, a longitude, and an optional marker properties 
map.

Location examples:

- `[47.3717400 8.5422600]`
- `[47.3717400 8.5422600 { :label "Zurich" }]`


##### `crop-image`

Usage: (crop-image image crop-top crop-bottom)

Crops the map image north and south to remove uninteresting image parts


##### `save-image`

Usage: (save-image image format file)

Saves the image to a file in the given format (:png, :jpg, :gif)



### Example

Show a few cities (Zurich, New York, Tokyo, ...) given by its coordinates on 
a world map:

```clojure
(do
  (load-module :mercator)

  (def cities
                  ; latitude       longitude   marker
    { :zurich     [ 47.3717400     8.5422600 { :label "Zurich"
                                               :radius 10
                                               :fill-color [128 128 255 255]
                                               :border-color [0 0 255 255]
                                               :label-color [255 255 255 255]
                                               :font-size-px 14 } ]
      :new-york   [ 40.7127780   -74.0058330 { :label "New York" } ]
      :tokyo      [ 35.6894875   139.6917064 { :label "Tokyo" } ]
      :perth      [-31.9535130   115.8570470 { :label "Perth" } ]
      :honolulu   [ 21.3069444  -157.8583333 { :label "Honolulu" } ]
      :montevideo [-34.9011127   -56.1645314 { :label "Montevideo" } ]
      :greenwich  [ 51.4825770     0.0000000 { :label "Greenwich" } ]
      :reykjavik  [ 64.1354800   -21.8954100 { :label "Reykjavik" } ]
      :kapstadt   [-33.9248690    18.4240550 { :label "Kapstadt" } ]
      :zero       [  0.0           0.0 ] })

  (-> (mercator/load-mercator-image)
      (mercator/draw-locations (vals cities))
      (mercator/crop-image 400 600)
      (mercator/save-image :png "./test-map.png")))
```

<img src="https://github.com/jlangch/venice/blob/master/doc/charts/mercator.png">


### Customizing

#### Mercator World Map

Venice uses a square **Mercator**  map with a latitude range -85.0° to +85.0°.

The function `(mercator/load-mercator-image)` loads it from [Wikipedia Mercator Map](https://upload.wikimedia.org/wikipedia/commons/7/73/Mercator_projection_Square.JPG)

You can provide your own map though, but it must:

- follow the [Web Mercator projection](https://en.wikipedia.org/wiki/Web_Mercator_projection) standard
- be a square image
- have a latitude range -85.0° to +85.0°
- have a longitude range -180° to +180°


#### Markers

The markers can be customized with a label, size and color passed as an optional map to each location. E.g.:

```clojure
{ :label "Zurich"
  :radius 10
  :fill-color [128 128 255 255]
  :border-color [0 0 255 255]
  :label-color [255 255 255 255]
  :font-size-px 14 }
```

Label:

| Property          | Type   | Example           | Description                    |
| :---              | :---   | :---              | :---                           |
| :label            | string | "Zurich"          | A label                        |
| :label-color      | vector | [255 255 255 255] | The label's color as RGBA      |
| :font-size-px     | long   | 14                | The label's font size in pixel |

Marker:

| Property          | Type   | Example           | Description                       |
| :---              | :---   | :---              | :---                              |
| :radius           | long   | 10                | The marker's radius in pixel      |
| :border-color     | vector | [0 0 255 255]     | The marker's border color as RGBA |
| :fill-color       | vector | [128 128 255 255] | The marker's fill color as RGBA   |

