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
            [compojure.response :as c-response]))

(comment
  (def hz-instance (instance (config))))

;;; Instance & Member: two angles of the same thing
(def hz-instance  (get-default-hz-instance)) ;auto-startup happens here
(def hz-member    (get-local-member))

;;; This CRUD is to be used as an history of current and previous
;;; hz-instances, along with their attached data (see below).
(defcrud defcrudop get-map "instances")

;;; The current hz-instance
(def instance-id (make-id hz-member :full))

(defn instance-data []
  (instances-get instance-id))

(defn instance-data-set! [v]
  (instances-put instance-id v))

;;; The current hz-instance's attached data
(instance-data-set! {:instance-id   instance-id
                     :instance-name (get-name hz-instance)
                     :member-host   (get-host hz-member)
                     :member-name   (make-id  hz-member)})

;;; The structured history of past and current hz-instances' attached data
(defn instances-data []
  "Builds a structured map of attached data of the hz-instances,
   out of the app's persistent memory.
   For now, this thing contains no optimizations whatsoever."
  (let [xs         (sort-by #(:member-name %)
                            (instances-list))
        name-discr #(= (:member-name %)
                       ((instance-data) :member-name))]
    {:self   (first
              (filter name-discr xs))
     :others  (filter #(not (name-discr %)) xs)}))

;;; Dev utilities
(comment
  (use 'spdv.new.routes)
  (use 'ring.util.serve)
  (serve app)
  (instances-put "instance-id" {:instance-id   "instance-id"
                                :instance-name "instance-name"
                                :member-host   "member-host"
                                :member-name   "member-name"})
  (use 'clojure.pprint)
  (defn pr-line [] (println "=============="))
  (do (pr-line) (pprint (instance-data)))
  (do (pr-line) (pprint (instances-data)))
  (do (pr-line) (doseq [s (instances-list)] (pprint s)))
  (stop-server)
  (swank.core/break)
  (-> (eval-any '(+ 1 1)) (.get)))

;;; UI controller
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
                  (instance-data-set!
                   (merge (instance-data)
                          {:member-name (.trim new-name)})))
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

;;; Server utilities
(def production?
  (= "production" (get (System/getenv) "APP_ENV")))

(def development?
  (not production?))

;;; Server configuration
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
