# GEO IP

The 'geoip' module maps IPv4 and IPv6 adresses to country and location (latitude, longitude)
that can be visualized on a world map. The 'geoip' module uses the free 
[MaxMind](https://www.maxmind.com/) location databases.

GEOIP lookups are lightning-fast. A country lookup for an IPv4 address
on a 2017 MacBook Pro (Mac OSX, Core i7 2.8 GHz) takes ~1.1us 
based on actual MaxMind data. Venice uses an ultra-fast trie concurrent 
data structure to store CIDR / country / city relations and do IP lookups.

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

Alternatively the databases can be downloaded using the Venice GeoIP module. You just 
run this snippet from a REPL.

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

[Google Country Database](https://raw.githubusercontent.com/google/dspl/master/samples/google/canonical/countries.csv)



## Example 1: Lookup IP location

The **geoip** module returns location information for IPv4 or IPv6 addresses.

E.g.:

```text
IP address:     "41.216.186.131"

Location info:  { :ip            "41.216.186.131" 
                  :loc           ["17.357822" "-62.782998"] 
                  :country-iso   "KN"
                  :country-name  "St Kitts and Nevis" }
```


IP lookup:

```clojure
(do
  (load-module :geoip)

  ;; The MaxMind country database. 
  (def maxmind-country-zip "resources/geoip-country.zip")
  
  (def resolver (delay (create-resolver)))
  
  (defn create-resolver [] 
    ; this may take some time
    (println "Loading Google country DB ...")
    (let [country-db (geoip/download-google-country-db)]
      (println "Parsing MaxMind DB ...")
      (geoip/ip-to-country-loc-resolver maxmind-country-zip country-db)))
     
  (defn lookup-ip [ip] (@resolver ip))

  (when-not (io/exists-file? maxmind-country-zip)
    (throw (. :VncException :new 
              (str "The MaxMind country file" maxmind-country-zip "does not exist!"))))
  
  (when-not *macroexpand-on-load*
    (println """

             -------------------------------------------------------------------
             Warning: macroexpand-on-load is not activated. To get a much better
                      performance activate macroexpand-on-load before loading
                      this script.

                      From the REPL run: !macroexpand
             -------------------------------------------------------------------
             """))

  ; example:
  ; (lookup-ip "41.216.186.131")
)
```



## Example 2: Visualize IP addresses on a map

To visualize IPv4 and IPv6 addresses on a world map Venice' **mercator** 
and **geoip** modules can be combined to draw the locations. 

The **geoip** module returns the IP locations (country, latitude, and 
longitude) that are passed to the **mercator** module to draw corresponding
markers on a world map.


```clojure
(do
  (load-module :mercator)
  (load-module :geoip)


  ;; The MaxMind country database. 
  (def maxmind-country-zip "resources/geoip-country.zip")
  
  ;; the png map created
  (def map-out-file "./ip-world-map.png")

  (def resolver (delay (create-resolver)))
  
  (defn create-resolver[] 
    ; this may take some time
    (println "Loading Google country DB ...")
    (let [country-db (geoip/download-google-country-db)]
      (println "Parsing MaxMind DB ...")
      (geoip/ip-to-country-loc-resolver maxmind-country-zip country-db)))

  (defn draw [format file locations]
    (-> (mercator/load-mercator-image)
        (mercator/draw-locations locations)
        (mercator/crop-image 400 600)
        (mercator/save-image format file)))

  (defn map-ip-to-location [ip ip-loc-resolver]
    (let [data (ip-loc-resolver ip)]
      { :loc         (geoip/map-location-to-numerics (:loc data))
        :ip          ip
        :country     (:country-name data)
        :country-iso (:country-iso data) } ))

  (defn visualize [& ip]
    (->> ; retrieve the location data for the IP addresses
         (map #(map-ip-to-location % @resolver) ip)
         
         ; map to locations, enrich the data with with a label and define
         ; optional colors and font
         (map #(let [[lat lon] (:loc %)
                     country   (:country-iso %)]
                 [lat
                  lon
                  { :label        country
                    :fill-color   [255 128 128 255]
                    :border-color [255   0   0 255]
                    :label-color  [255 255 255 255]
                    :radius       10
                    :font-size-px 14}]))

         ; draw the data to a PNG
         (draw :png map-out-file)))
  
  (when-not (io/exists-file? maxmind-country-zip)
    (throw (. :VncException :new 
              (str "The MaxMind country file" maxmind-country-zip "does not exist!"))))
    
  (visualize "91.223.55.1" "220.100.34.45" "167.120.90.10"))
```
<img src="https://github.com/jlangch/venice/blob/master/doc/assets/geoip/geoip-example.png">



## Example 3: Visualize Tomcat IP addresses on a map

The script `tomcat-geoip.venice` 
  - parses IP addresses from Tomcat access log files
  - aggregates the IP address (computes frequency)
  - maps the IP addresses to locations
  - visualizes them on a world map.


Script  _tomcat-geoip.venice_ :

```clojure
(do
  (load-module :tomcat-util)
  (load-module :mercator)
  (load-module :geoip)


  ;; The MaxMind country database.
  (def maxmind-country-zip "resources/geoip-country.zip")

  (def private-ip-addresses
        [ (cidr/parse "10.0.0.0/8")
          (cidr/parse "172.16.0.0/12")
          (cidr/parse "192.168.0.0/16") ])

  (def resolver (delay (create-resolver)))

  (defn create-resolver[]
    ; this may take some time
    (println "Loading Google country DB ...")
    (let [country-db (geoip/download-google-country-db)]
      (println "Parsing MaxMind DB ...")
      (geoip/ip-to-country-loc-resolver maxmind-country-zip country-db)))

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
      (do
        (println "Downloading Mercator image...")
        (mercator/load-mercator-image))))

  (defn draw [styles mercator-img format file locations]
    (println "Drawing Mercator map ...")
    (-> (get-mercator-img mercator-img)
        (mercator/draw-locations locations styles)
        (mercator/crop-image 400 600)
        (mercator/save-image format file)))

  (defn parse-ip [log]
    (->> (tc-util/simple-ipaddr-access-log-entry-parser)
         (tc-util/parse-access-log log)
         (map :ip)
         (filter #(not (private-ip? %)))
         (frequencies)))

  (defn parse-zip-logs [zip-log-file]
    (->> (vals (io/unzip-all zip-log-file))
         (map #(parse-ip %))))

  (defn parse-log-file [log-file]
    (println "Parsing" log-file "...")
    (if (io/file-ext? log-file "zip")
      (parse-zip-logs log-file)
      (parse-ip log-file)))

  (defn parse-log-files [log-files]
    ;; returns an aggregated map with IP frequencies:
    ;;    { "196.52.43.56" 3 "178.197.226.244" 8 }
    (merge-freq-maps (flatten (map parse-log-file log-files))))

  (defn map-ip-to-location [ip-freq ip-loc-resolver]
    (let [ip (key ip-freq)
          data (ip-loc-resolver ip)]
      { :loc         (geoip/map-location-to-numerics (:loc data))
        :ip          ip
        :freq        (val ip-freq)
        :country     (:country-name data)
        :country-iso (:country-iso data) } ))

  (defn create-map [styles mercator-img ip-freq-map ip-loc-resolver out-file]
    (printf "Mapping %d IP addresses ...%n" (count ip-freq-map))
    (->> (entries ip-freq-map)
         (map #(map-ip-to-location % ip-loc-resolver)) ;; this is very slow
         (merge-ip-locations-by-country)
         (map #(let [[lat lon] (:loc %)
                     country   (:country-iso %)
                     frequency (:freq %)
                     label     (format-label country frequency)]
                 [lat lon {:label label :font-size-px 14}]))
         (draw styles mercator-img :png out-file)))

  (defn process [styles mercator-img out-file log-files]
     (println "Processing log files...")
     (-<> (map io/file log-files)
          (parse-log-files <>)
          (create-map styles mercator-img <> @resolver out-file)))

  (defn load-image [file]
    (println "Loading Mercator image...")
    (mercator/load-image file))

  (defn download-maxmind-db [lic-key]
    (->> (geoip/download-maxmind-db :country lic-key)
         (io/spit (io/file maxmind-country-zip))))

  (defn lookup-ip [ip] (@resolver ip))

  (defn run-custom [styles mercator-img out-file & log-files]
    (process styles mercator-img out-file log-files))

  (defn run [out-file & log-files]
    (process nil nil out-file log-files))

  (println """
           Actions:
              [1] (lookup-ip "41.216.186.131")
              [2] (download-maxmind-db "YOUR-MAXMIND-KEY")
              [3] (run "./ip-map.png"
                       "resources/localhost_access_log.2019-12.zip")
              [4] (run "./ip-map.png"
                       "resources/localhost_access_log.2019-12-01.log")
              [5] (apply run "./ip-map.png"
                             (io/list-files-glob "resources"
                                                 "localhost_access_log.2020-*"))
           """)

  (when-not *macroexpand-on-load*
    (println)
    (println *err*
             """
             -------------------------------------------------------------------
             Warning: macroexpand-on-load is not activated. To get a much better
                      performance activate macroexpand-on-load before loading
                      this script.

                      From the REPL run: !macroexpand
             -------------------------------------------------------------------
             """))

  (when-not (io/exists-file? maxmind-country-zip)
    (println)
    (println *err*
             """
             -------------------------------------------------------------------
             Error: The MaxMind country file does not exist!

                    ~{maxmind-country-zip}
             -------------------------------------------------------------------
             """)))
```

Run the script from a REPL:

```text
venice> !macroexpand
venice> (load-file "tomcat-geoip.venice")
venice> (lookup-ip "41.216.186.131")
venice> (run "./ip-map.png" "localhost_access_log.2019-12.zip")
```

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/geoip/geoip-tomcat.png">




## See also: IP Risk Level

See: [Auth0 IP Risk Level](https://auth0.com/signals/ip)

Blog: [Auth0 Blog](https://auth0.com/blog/introducing-auth0-signals-threat-intelligence-to-protect-customers-from-widespread-identity-cyberattacks/)
