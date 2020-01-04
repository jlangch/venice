# MERCATOR Maps

The [(Mercator Projection)](https://en.wikipedia.org/wiki/Mercator_projection)
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


Show a few cities given its coordinates on a world map:

```clojure
(do
  (load-module :mercator)

  (-> (mercator/load-mercator-image)
      (mercator/draw-locations (vals mercator/cities))
      (mercator/crop-image 400 600)
      (mercator/save-image :png "./test-map.png")))
```

<img src="https://github.com/jlangch/venice/blob/master/doc/charts/mercator.png" width="600">
