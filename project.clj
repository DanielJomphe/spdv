(defproject ca.slu.spdv "0.1.0-SNAPSHOT"
  :description "SPDV: FIXME..."
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [apparatus "1.0.0-SNAPSHOT"]
                 [com.h2database/h2 "1.3.153"]
                 [lobos "0.7.0-SNAPSHOT"]
                 [clojureql "1.0.0"]]
  :dev-dependencies [[swank-clojure "1.2.1"]
                     [marginalia "0.5.0-alpha"]]
  :aot [apparatus.eval]
  :main apparatus.main)
