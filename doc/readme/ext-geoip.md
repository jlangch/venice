# GEO IP

The 'geoip' module maps IP adresses to country and location (latitude, longitude)
that can be visualized on a world map. The 'geoip' module uses the free 
[MaxMind](https://www.maxmind.com/) location databases.


## Example: Visualize Tomcat IP addresses on a map

The script `tomcat-geoip.venice` parses Tomcat access log files, maps IP addresses
to locations and visualize them on a map.

```clojure
(do
  (load-module :tomcat-util)
  (load-module :mercator)
  (load-module :geoip)


  (def maxmind-country-zip "resources/geoip-country.zip")

  (def private-ip-addresses
        [ (cidr/parse "10.0.0.0/8")
          (cidr/parse "172.16.0.0/12")
          (cidr/parse "192.168.0.0/16") ])


  (defn private-ip? [ip]
    (any? #(cidr/in-range? ip %) private-ip-addresses))

  (defn format-label [country-iso freq]
    (cond
      (> freq 1000) (str country-iso " " (/ freq 1000) "k")
      :else         (str country-iso " " freq)))

  (defn merge-freq-maps [freq-maps]
    (apply (partial merge-with +) freq-maps))

  (defn merge-ip-locations-by-country [ip-locations]
    (vals (reduce (fn [x y]
                   (let [country (:country-iso y)
                         r (get x country)]
                     (if (nil? r)
                       (assoc x country y)
                       (let [freq (+ (:freq r) (:freq y))]
                         (assoc x country (assoc r :freq freq))))))
                  {}
                  ip-locations)))

  (defn draw [format file locations]
    (-> (mercator/load-mercator-image)
        (mercator/draw-locations locations)
        (mercator/crop-image 400 600)
        (mercator/save-image format file)))

  (defn parse-ip [log]
    (->> (tc-util/simple-ipaddr-access-log-entry-parser)
         (tc-util/parse-access-log log)
         (map :ip)
         (frequencies)))

  (defn parse-zip-logs [log-file]
    (->> (io/zip-list-entry-names log-file)
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
    (let [ip (key ip-freq) data (ip-loc-resolver ip)]
      { :loc (geoip/map-location-to-numerics (:loc data))
        :ip ip
        :freq (val ip-freq)
        :country (:country-name data)
        :country-iso (:country-iso data) } ))

  (defn create-map [ip-freq-map ip-loc-resolver out-file]
    (->> (entries ip-freq-map)
         (map #(map-to-location % ip-loc-resolver))
         (filter #(not (private-ip? (:ip %))))
         (merge-ip-locations-by-country)
         (map (fn [x] [ (first (:loc x))
                        (second (:loc x))
                        {:label (format-label (:country-iso x) (:freq x))
                         :font-size-px 14}]))
         (draw :png out-file)))

  (defn create-ip-loc-resolver []
    ; this may take some time
    (when (io/exists-file? maxmind-country-zip)
      (println "Parsing MaxMind country DB...")
      (geoip/ip-to-country-loc-resolver
                     maxmind-country-zip
                     (geoip/download-google-country-db))))

  (def ip-loc-rv nil)

  (defn download-maxmind-db [lic-key]
    (io/mkdirs (io/file-parent maxmind-country-zip))
    (geoip/download-maxmind-db-to-zipfile
      (io/file maxmind-country-zip) :country lic-key))

  (defn run [out-file & log-files]
    (if (io/exists-file? maxmind-country-zip)
      (do
        (when (nil? ip-loc-rv)
          (def ip-loc-rv (create-ip-loc-resolver)))
        (println "Processing log files...")
        (-<> (map io/file log-files)
             (parse-log-files <>)
             (create-map <> ip-loc-rv out-file)))
      (do
        (println "The MaxMind country file" maxmind-country-zip " does not exist!")
        (println "Please download it:")
        (println "    (download-maxmind-db -your-maxmind-lic-key-)"))))


  (println """
           Actions:
              [1] (run "./ip-map.png"
                       "resources/localhost_access_log.2019-12.zip")
              [2] (run "./ip-map.png"
                       "resources/localhost_access_log.2019-12-01.log")
              [3] (apply (partial run "./ip-map.png")
                         (io/list-files-glob "resources"
                                             "localhost_access_log.*.log"))
              [4] (download-maxmind-db -your-maxmind-lic-key-)
           """)

  (when (false? *macroexpand-on-load*)
    (println """

             Warning: macroexpand-on-load is not activated. To get a much better
                      performance activate macroexpand-on-load before loading
                      this script.

                      From the REPL run: !macroexpand
             """)))
```

Run the script from a REPL:

```text
venice> !macroexpand
venice> (load-file "tomcat-geoip.venice")
venice> (run "localhost_access_log.2019-12.zip" "./ip-map.png")
```

<img src="https://github.com/jlangch/venice/blob/master/doc/charts/geoip-tomcat.png">
