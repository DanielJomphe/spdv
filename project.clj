(defproject spdv "0.1.0-SNAPSHOT"
  :description "SPDV: FIXME..."
  :dependencies [[org.clojure/clojure "1.3.0-beta3"]
                 ;[org.clojure.contrib/java-utils  "1.3.0-beta1"]
                 [apparatus "1.0.0-SNAPSHOT"]
                 ;[noir "1.1.1-SNAPSHOT"]
                 ;[noir-cljs "0.1.0-SNAPSHOT"]
                 ;[compojure "0.6.5"]
                 ;[hiccup "0.3.6"]
                 [aleph "0.2.0-beta1"]
                 [ring/ring-core          "0.3.11"]
                 [ring/ring-devel         "0.3.11"]
                 [ring/ring-jetty-adapter "0.3.11"]
                 [ring-json-params "0.1.3"]
                 [status-codes "0.3"]
                 ;[clj-json "0.3.2"]
                 ;[lobos "0.7.0"]
                 ;[clojureql "1.0.1"]
                 ;[com.h2database/h2 "1.3.158"]
                 [robert/hooke "1.1.2"]]
  :dev-dependencies [
                     ;;[clojure-source "1.3.0-beta3"]
                     [slamhound "1.2.0"]
                     [ring-serve "0.1.1"]
                     [lein-ring "0.4.5"]
                     [lein-retest "1.0.1"]
                     [lein-difftest "1.3.3"]
                     [lein-play "1.0.0-SNAPSHOT"]
                     [lein-namespace-depends "0.1.0-SNAPSHOT"]
                     [lein-diagnostics "0.0.1"]
                     [lein-search "0.3.4"]
                     [lein-notes "0.0.1"]]
  :jvm-opts ["-agentlib:jdwp=transport=dt_socket,server=y,suspend=n"]
  :disable-implicit-clean true
;;:aot [apparatus.eval]
;;:main apparatus.main
  :ring {:handler spdv.new.routes/app}
  :javac-options {:destdir "classes/"}
  :java-source-path "src/main/java"
  :hooks [leiningen.hooks.retest
          leiningen.hooks.difftest])
