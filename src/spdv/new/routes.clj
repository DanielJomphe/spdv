(ns spdv.new.routes
  (:use compojure.core
        spdv.new.middleware
        spdv.new.views
        spdv.new.apparatus
        [hiccup.middleware :only (wrap-base-url)]
        ring.handler.dump
        [ring.middleware file file-info lint reload stacktrace]
        ring.util.response)
  (:require [compojure.route    :as c-route]
            [compojure.handler  :as c-handler]
            [compojure.response :as c-response])
  (:import spdv.MacAddress))

(comment                                ;dev
  (do
    (use 'spdv.new.routes)
    (use 'ring.util.serve)
    (serve app)
    (use '[apparatus config cluster])
    (instance (config)))
  (stop-server)
  (swank.core/break)
  )

(def production?
  (= "production" (get (System/getenv) "APP_ENV")))

(def development?
  (not production?))

(def *startup-instance-name* (let [mac  (MacAddress/get)
                                   mbr  (get-local-member)
                                   host (get-host mbr)
                                   port (get-port mbr)]
                               (str mac ":" host ":" port)))

(def instance-name (atom *startup-instance-name*
                         :validator #(not (empty? %))))

(defroutes main-routes
  (context "/" []
           (GET  "/" []
                 (view-global-status-input @instance-name))
           (POST "/" [cur-name new-name]
                 (try
                   (reset! instance-name (.trim new-name))
                   (catch Exception e))
                 (view-global-status-input @instance-name)))
  (context "/adder" []
           (GET  "/" [] (view-input))
           (POST "/" [a b]
                 (try
                   (let [[a b] (parse-input a b)]
                     (view-output a b (+ a b)))
                   (catch NumberFormatException e
                     (view-input a b)))))
  (context "/closure-server" []
           (GET "/" [] (hello-server)))
  (context "/closure-client" []
           (GET "/" [] (hello-client)))
  (c-route/resources "/")
  (c-route/not-found "Page not found")
  (comment
    (ANY "/*" [_]
         (redirect "/"))))

(def app
  (-> (c-handler/site main-routes)
      wrap-lint
      ;;handle-dump
      ;;wrap-lint
      (wrap-base-url)
      (wrap-utf)
      (wrap-file "resources")
      (wrap-file-info)
      (wrap-request-logging)
      (wrap-if development? wrap-reload '[spdv.new.middleware
                                          spdv.new.views])
      (wrap-bounce-favicon)
      (wrap-exception-logging)
      (wrap-if production?  wrap-failsafe)
      ;;(wrap-if development? wrap-stacktrace) ;lein-ring/ring-serve
      ))
