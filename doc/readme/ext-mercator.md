# MERCATOR Maps

The [Mercator Projection](https://en.wikipedia.org/wiki/Mercator_projection)
is a cylindrical map projection. 

Venice's **geoip** module uses the Mercator map projection to show IP 
locations on a world map.

World coordinates are given by a latitude and a longitude.

**Latitude** specifies the north-south position of a point on the Earth's
surface. Latitude is an angle which ranges from 0° at the equator to +90°
at north and -90° at south pole.

**Longitude** specifies the east-west position of a point on the Earth's
surface. Longitude is an angle which ranges from 0° at the prime meridian
to +180° eastward and -180° westward.

### Example

Show a few cities (Zurich, New York, Tokyo, ...) given by its coordinates on 
a world map:

```clojure
(do
  (load-module :mercator)

  (def cities
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
