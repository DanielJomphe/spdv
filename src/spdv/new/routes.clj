(ns spdv.new.routes
  (:use compojure.core
        spdv.new.middleware
        spdv.new.views
        spdv.new.apparatus
        [apparatus config cluster]
        [hiccup.middleware :only (wrap-base-url)]
        ring.handler.dump
        [ring.middleware file file-info lint reload stacktrace]
        ring.util.response)
  (:require [compojure.route    :as c-route]
            [compojure.handler  :as c-handler]
            [compojure.response :as c-response])
  (:import spdv.MacAddress))

(defcrud defcrudop get-map "instances")

(comment
  (def instance-hz (instance (config))))

(def cluster-local-member (get-local-member))

(defn make-id [& full]
  (let [mac  (MacAddress/get)
        host (get-host cluster-local-member)
        port (get-port cluster-local-member)]
    (str (if full mac  (last (.split mac "-")))    "["
         (if full host (last (.split host "[.]"))) ":"
         port                                      "]")))

(def instance-id (make-id :full))

(defn instance-config []
  (instances-get instance-id))

(defn instance-config-set! [v]
  (instances-put instance-id v))

(instance-config-set! {:id   instance-id
                       :name (make-id)})

(comment                                ;dev
  (use 'spdv.new.routes)
  (use 'ring.util.serve)
  (serve app)
  (stop-server)
  (swank.core/break)
  (instances-put :2 {:id "id2" :name "name2"})
  (do (doseq [s (instances-list)] (println s)) (println "=============="))
  (-> (eval-any '(+ 1 1)) (.get)))

(defn instances-data []
  (let [xs         (sort-by #(:name %) (instances-list))
        name-discr #(= (:name %) ((instance-config) :name))]
    {:self   (first
              (filter name-discr xs))
     :others  (filter #(not (name-discr %)) xs)}))

(defroutes main-routes
  (context "/" []
           (GET  "/" []
                 (view-global-status (instances-data)))
           (GET  "/" [name]
                 (view-global-status (instances-data)))
           (PUT "/" [cur-name new-name]
                (when-not (or
                           (= new-name cur-name)
                           (.isEmpty (.trim new-name)))
                  (instance-config-set!
                   (merge (instance-config)
                          {:name (.trim new-name)})))
                (view-global-status (instances-data))))
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

(def production?
  (= "production" (get (System/getenv) "APP_ENV")))

(def development?
  (not production?))

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
