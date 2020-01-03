# CIDR

CIDR [(Classless Inter-Domain Routing)](https://en.wikipedia.org/wiki/Classless_Inter-Domain_Routing) is a method for allocating IP addresses and IP routing. 

Venice's CIDR module parses CIDR IP notations to IP address ranges. It supports both IPv4 and IPv6.

GEO IP database provider like _MaxMind_ publish their data with CIDR IP notations.


```clojure
(do
  (import :java.net.InetAddress)

  (load-module :cidr)
  
  ;; parse a CIDR notation into an IP address range (start/end IP address)
  (cidr/parse "222.192.0.0/11") 
  
  (cidr/parse "2001:0db8:85a3:08d3:1319:8a2e:0370:7347/64")


  ;; test if an IP4 address is within a CIDR address block
  (let [cidr (cidr/parse "222.192.0.0/11")
        ip (. :InetAddress :getByName "222.220.0.0")]
     (cidr/in-range? "222.220.0.0" "222.220.0.0/11")
     (cidr/in-range? ip "222.220.0.0/11")
     (cidr/in-range? "222.220.0.0" cidr)
     (cidr/in-range? ip cidr))
```
