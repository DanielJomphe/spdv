(ns spdv.new.ring-handler
  (:use ring.adapter.jetty
        :reload))

(defn app [req]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "Hello World from Ring"})
