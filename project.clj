(defproject spdv "0.1.0-SNAPSHOT"
  :description "SPDV: FIXME..."
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [apparatus "1.0.0-SNAPSHOT"]
                 [compojure "0.6.2"]
                 [hiccup "0.3.4"]
                 [ring/ring-core "0.3.7"]
                 [ring/ring-devel "0.3.7"]
                 [ring/ring-jetty-adapter "0.3.7"]
                 [lobos "0.7.0-SNAPSHOT"]
                 [clojureql "1.0.0"]
                 [com.h2database/h2 "1.3.153"]]
  :dev-dependencies [[swank-clojure "1.2.1"]
                     [lein-ring "0.4.0"]
                     [ring-serve "0.1.0"]
                     ;[marginalia "0.5.0"]
                     ]
  :disable-implicit-clean true
  ;;:aot [apparatus.eval]
  ;;:main apparatus.main
  :ring {:handler spdv.new.routes/app}
  :javac-options {:destdir "classes/"}
  :java-source-path "src/main/java")
