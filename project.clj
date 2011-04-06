(defproject spdv "0.1.0-SNAPSHOT"
  :description "SPDV: FIXME..."
  :dependencies [[org.clojure/clojure             "1.2.1"]
                 [org.clojure/clojure-contrib     "1.2.0"]
               ;;[org.clojure.contrib/core        "1.3.0-alpha4"]
               ;;[org.clojure.contrib/java-utils  "1.3.0-alpha4"]
                 [apparatus "1.0.0-SNAPSHOT"]
                 [compojure "0.6.2"]
                 [hiccup "0.3.4"]
                 [ring/ring-core          "0.3.7"]
                 [ring/ring-devel         "0.3.7"]
                 [ring/ring-jetty-adapter "0.3.7"]
                 [ring-json-params "0.1.3"]
                 [clj-json "0.3.2"]
                 [lobos "0.7.0-SNAPSHOT"]
                 [clojureql "1.0.1"]
                 [com.h2database/h2 "1.3.153"]
                 [robert/hooke "1.1.0"]]
  :dev-dependencies [[swank-clojure "1.3.0"]
                     [lein-ring "0.4.0"]
                     [lein-retest "1.0.1"]
                     [lein-difftest "1.3.1"]
                     [lein-play "1.0.0-SNAPSHOT"]
                     [ring-serve "0.1.0"]
                     [lein-namespace-depends "0.1.0-SNAPSHOT"]
                     [lein-diagnostics "0.0.1"]
                     [lein-search "0.3.4"]
                     [lein-notes "0.0.1"]]
  :disable-implicit-clean true
;;:aot [apparatus.eval]
;;:main apparatus.main
  :ring {:handler spdv.new.routes/app}
  :javac-options {:destdir "classes/"}
  :java-source-path "src/main/java"
  :hooks [leiningen.hooks.retest
          leiningen.hooks.difftest])
