# GEO IP

The 'geoip' module maps IP adresses to country and location (latitude, longitude)
that can be visualized on a world map. The 'geoip' module uses the free 
[MaxMind](https://www.maxmind.com/) location databases.


## MaxMind GeoLite2 databases

MindMax offers free GEO IP databases. Starting with December 30, 2019 a license key
is required to download the free GeoLite2 databases. The license key is free, but 
requires a registration at MindMax.

Venice supports both the country as well as the city GEO database. The city database is
pretty large and lookups are therefore much slower compared to the country database. 

To sign up for a GeoLite2 account visit [GeoLite2](https://dev.maxmind.com/geoip/geoip2/geolite2/). 
If you have already a key the databases can be manually downloaded. Just insert your 
key in these URLs:

**Manually download GeoLite2 Country database** 

https://download.maxmind.com/app/geoip_download?edition_id=GeoLite2-Country-CSV&license_key=YOUR_KEY&suffix=zip


**Manually download GeoLite2 City database**

https://download.maxmind.com/app/geoip_download?edition_id=GeoLite2-City-CSV&license_key=YOUR_KEY&suffix=zip


**Download GeoLite2 databases with Venice**

Alternatively the databases can be downloaded using the Venice GeoIP module. You just run it from a REPL.

```clojure
(do
  (load-module :geoip)
  
  (defn download-maxmind-country-db [lic-key dest-zip]
    (when (some? (io/file-parent dest-zip))
      (io/mkdirs (io/file-parent dest-zip)))
    (geoip/download-maxmind-db-to-zipfile
      (io/file dest-zip) 
      :country 
      lic-key))

  (download-maxmind-country-db YOUR-MAXMIND-LIC-KEY "resources/geoip-country.zip"))
```

**Note**

The MindMax country database does not include GPS location data. Venice uses Google's free
country database to map the countries' ISO codes to latitude, longitude coordinates.

https://raw.githubusercontent.com/google/dspl/master/samples/google/canonical/countries.csv


## Example: Visualize IP addresses on a map

```clojure
(do
  (load-module :mercator)
  (load-module :geoip)


  ;; The MaxMind country database. 
  ;; Please make sure this file exists. It can be downloaded by just copy/paste 
  ;; the 'download-maxmind-db' function below to a Venice REPL and run it with 
  ;; your license key. A free MaxMind GeoLite2 license key can be obtained from
  ;; 'https://www.maxmind.com/en/home'
  (def maxmind-country-zip "resources/geoip-country.zip")
  
  ;; the png map created
  (def map-out-file "./ip-world-map.png")

  (defn download-maxmind-db [lic-key]
    (when (some? (io/file-parent maxmind-country-zip))
      (io/mkdirs (io/file-parent maxmind-country-zip)))
    (geoip/download-maxmind-db-to-zipfile
      (io/file maxmind-country-zip) :country lic-key))

  (defn draw [format file locations]
    (-> (mercator/load-mercator-image)
        (mercator/draw-locations locations)
        (mercator/crop-image 400 600)
        (mercator/save-image format file)))

  (def resolver (geoip/ip-to-country-loc-resolver
                   maxmind-country-zip
                   (geoip/download-google-country-db)))

  (defn map-ip-to-location [ip ip-loc-resolver]
    (let [data (ip-loc-resolver ip)]
      { :loc (geoip/map-location-to-numerics (:loc data))
        :ip ip
        :country (:country-name data)
        :country-iso (:country-iso data) } ))

  (if (io/exists-file? maxmind-country-zip)
    (do
      (->> ["91.223.55.1" "220.100.34.45" "167.120.90.10"]
      
           ; retrieve the location data for the IP addresses
           (map #(map-ip-to-location % resolver))
           
           ; map to locations, enrich the data with with a label and define
           ; optional colors and font
           (map #(let [[lat lon] (:loc %)
                       country (:country-iso %)]
                   [lat
                    lon
                    { :label country
                      :fill-color [255 128 128 255]
                      :border-color [255 0 0 255]
                      :label-color [255 255 255 255]
                      :radius 10
                      :font-size-px 14}]))
                           
           ; draw the data to a PNG
           (draw :png map-out-file)))
    (do
      (println "The MaxMind country file" maxmind-country-zip "does not exist!")
      (println "Please download it:")
      (println "    (download-maxmind-db YOUR-MAXMIND-LIC-KEY)"))))
```
<img src="https://github.com/jlangch/venice/blob/master/doc/charts/geoip-example.png">


## Example: Visualize Tomcat IP addresses on a map

The script `tomcat-geoip.venice` parses Tomcat access log files, maps IP addresses
to locations and visualize them on a map.

```clojure
(do
  (load-module :tomcat-util)
  (load-module :mercator)
  (load-module :geoip)


  ;; The MaxMind country database.
  ;; Please make sure this file exists. It can be downloaded by running
  ;; the (download-maxmind-db YOUR-MAXMIND-LIC-KEY) function below.
  ;; A free MaxMind GeoLite2 license key can be obtained from
  ;; 'https://www.maxmind.com/en/home'
  (def maxmind-country-zip "resources/geoip-country.zip")

  (def private-ip-addresses
        [ (cidr/parse "10.0.0.0/8")
          (cidr/parse "172.16.0.0/12")
          (cidr/parse "192.168.0.0/16") ])

  (defn private-ip? [ip]
    (any? #(cidr/in-range? ip %) private-ip-addresses))

  (defn format-label [country-iso freq]
    (cond
      (> freq 1000000) (str/format "%s %.1fm" country-iso (/ freq 1000000.0))
      (> freq 1000)    (str/format "%s %.1fk" country-iso (/ freq 1000.0))
      :else            (str/format "%s %d" country-iso freq)))

  (defn merge-freq-maps [freq-maps]
    (apply (partial merge-with +) freq-maps))

  (defn merge-ip-locations-by-country [ip-locations]
    ;; ip-locations: list of map with keys :loc :ip :freq :country :country-iso
    ;; group by :country-iso and sum up :freq
    (->> (vals (group-by :country-iso ip-locations))
         (map #(let [sum (apply + (map :freq %))
                     location (dissoc (first %) :ip)]
                 (assoc location :freq sum)))))

  (defn get-mercator-img [mercator-img]
    (if (some? mercator-img)
      mercator-img
      (mercator/load-mercator-image)))

  (defn draw [styles mercator-img format file locations]
    (let [img (get-mercator-img mercator-img)]
      (-> img
          (mercator/draw-locations locations styles)
          (mercator/crop-image 400 600)
          (mercator/save-image format file))))

  (defn parse-ip [log]
    (->> (tc-util/simple-ipaddr-access-log-entry-parser)
         (tc-util/parse-access-log log)
         (map :ip)
         (filter #(not (private-ip? %)))
         (frequencies)))

  (defn parse-zip-logs [log-file]
    (->> (io/zip-list-entry-names log-file)
         (filter #(io/file-ext? % "log"))
         (map #(parse-ip (io/unzip log-file %)))))

  (defn parse-log-file [log-file]
    (println "Parsing" log-file "...")
    (if (io/file-ext? log-file "zip")
      (parse-zip-logs log-file)
      (parse-ip log-file)))

  (defn parse-log-files [log-files]
    ;; returns an aggregated map with IP frequencies:
    ;;    { "196.52.43.56" 3 "178.197.226.244" 8 }
    (merge-freq-maps (flatten (map parse-log-file log-files))))

  (defn map-to-location [ip-freq ip-loc-resolver]
    (let [ip (key ip-freq)
          data (ip-loc-resolver ip)]
      { :loc (geoip/map-location-to-numerics (:loc data))
        :ip ip
        :freq (val ip-freq)
        :country (:country-name data)
        :country-iso (:country-iso data) } ))

  (defn create-map [styles mercator-img ip-freq-map ip-loc-resolver out-file]
    (->> (entries ip-freq-map)
         (map #(map-to-location % ip-loc-resolver))
         (merge-ip-locations-by-country)
         (map #(let [[lat lon] (:loc %)
                     country (:country-iso %)
                     frequency (:freq %)
                     label (format-label country frequency)]
                 [lat lon {:label label :font-size-px 14}]))
         (draw styles mercator-img :png out-file)))

  (defn create-ip-loc-resolver []
    ; this may take some time
    (when (io/exists-file? maxmind-country-zip)
      (println "Parsing MaxMind country DB...")
      (geoip/ip-to-country-loc-resolver
                     maxmind-country-zip
                     (geoip/download-google-country-db))))

  (def ip-loc-rv nil)

  (defn process [styles mercator-img out-file log-files]
    (if (io/exists-file? maxmind-country-zip)
      (do
        (when (nil? ip-loc-rv)
          (def ip-loc-rv (create-ip-loc-resolver)))
        (println "Processing log files...")
        (-<> (map io/file log-files)
             (parse-log-files <>)
             (create-map styles mercator-img <> ip-loc-rv out-file)))
      (do
        (println "The MaxMind country file" maxmind-country-zip " does not exist!")
        (println "Please download it:")
        (println "    (download-maxmind-db -your-maxmind-lic-key-)"))))

  (defn load-image [file]
    (mercator/load-image file))

  (defn download-maxmind-db [lic-key]
    (io/mkdirs (io/file-parent maxmind-country-zip))
    (geoip/download-maxmind-db-to-zipfile
      (io/file maxmind-country-zip) :country lic-key))

  (defn run-custom [styles mercator-img out-file & log-files]
    (process styles mercator-img out-file log-files))

  (defn run [out-file & log-files]
    (process nil nil out-file log-files))

  (println """
           Actions:
              [1] (run "./ip-map.png"
                       "resources/localhost_access_log.2019-12.zip")
              [2] (run "./ip-map.png"
                       "resources/localhost_access_log.2019-12-01.log")
              [3] (apply (partial run "./ip-map.png")
                         (io/list-files-glob "resources"
                                             "localhost_access_log.2020-*"))
              [4] (run-custom
                        ;; colors are specified as RGBA vectors
                        { :marker { :fill-color [255 128 128 255]
                                    :border-color [255 0 0 255]
                                    :label-color [131 52 235 255]
                                    :radius 10
                                    :font-size-px 14 } }
                        (load-image "resources/mercator-2.png")
                        "./ip-map.png"
                        "resources/localhost_access_log.2019-12.zip")
              [5] (download-maxmind-db -your-maxmind-lic-key-)
           """)

  (when-not *macroexpand-on-load*
    (println """

             -------------------------------------------------------------------
             Warning: macroexpand-on-load is not activated. To get a much better
                      performance activate macroexpand-on-load before loading
                      this script.

                      From the REPL run: !macroexpand
             -------------------------------------------------------------------
             """)))
```

Run the script from a REPL:

```text
venice> !macroexpand
venice> (load-file "tomcat-geoip.venice")
venice> (run "./ip-map.png" "localhost_access_log.2019-12.zip")
```

<img src="https://github.com/jlangch/venice/blob/master/doc/charts/geoip-tomcat.png">
