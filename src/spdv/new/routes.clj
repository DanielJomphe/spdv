(ns spdv.new.routes
  (:use compojure.core
        spdv.new.middleware
        spdv.new.views
        spdv.new.apparatus
        [apparatus config cluster]
        [hiccup.middleware :only (wrap-base-url)]
        ring.handler.dump
        [ring.middleware file file-info json-params lint reload stacktrace]
        ring.util.response)
  (:import [com.hazelcast.core
            LifecycleEvent$LifecycleState])
  (:require [compojure.route    :as route]
            [compojure.handler  :as handler]
            [compojure.response :as response]
            [clj-json.core      :as json]))

(comment
  (def hz-instance (instance (config))))

;;; Instance & Member: two angles of the same thing
(def hz-instance  (get-default-hz-instance)) ;auto-startup happens here
(def hz-member    (get-member-local))

;;; This CRUD is to be used as an history of current and previous
;;; hz-instances, along with their attached data (see below).
(defcrud get-map "instances")

;;; The current hz-instance
(def instance-id (make-id hz-member :full))

(defn instance-data []
  (instances-get instance-id))

(defn instance-data-set! [v]
  (instances-put instance-id v))

(defn instance-data-merge! [v]
  (instance-data-set!
   (merge (instance-data) v)))

;;; The current hz-instance's attached data
(instance-data-set! {:instance-id   instance-id
                     :instance-name (get-name hz-instance)
                     :member-host   (get-host hz-member)
                     :member-name   (make-id  hz-member)
                     :state         "ACTIVE"})

;;; Hookup into the LifecycleService
(add-lifecycle-listener
 (get-lifecycle-service hz-instance)
 #(if (= LifecycleEvent$LifecycleState/STARTED %)
    (instance-data-merge! {:state "ACTIVE"})
    (instance-data-merge! {:state "INACTIVE"})))

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
     :others  (remove name-discr xs)}))

;;; Dev utilities
(comment
  (use 'spdv.new.routes)
  (use 'clojure.pprint)
  (defn pr-line [] (println "=============="))
  (def               ins-0 (instance (config)))
  (def               ins-1 (instance (config)))
  (add-lifecycle-listener
   (get-lifecycle-service ins-0)
   #(do (pr-line) (println (str "!!! Instance state changed in the cluster: " %))))
  (add-membership-listener
   #(do (pr-line) (println (str "!!! Member added to the cluster: " %)))
   #(do (pr-line) (println (str "!!! Member removed from the cluster: " %))))
  (shutdown-instance ins-0)
  (shutdown-instance ins-1)
  (instances-put "instance-id0"
                 {:instance-id   "00-00-00-00-00-00[000.000.000.000:0000]"
                  :instance-name "some_hazelcast_0_name"
                  :member-host   "000.000.000.000"
                  :member-name   "00[000:0000]"})
  (instances-put "instance-id1"
                 {:instance-id   "11-11-11-11-11-11[111.111.111.111:1111]"
                  :instance-name "some_hazelcast_1_name"
                  :member-host   "111.111.111.111"
                  :member-name   "11[111:1111]"})
  (do (pr-line) (pprint (instance-data)))
  (do (pr-line) (pprint (instances-data)))
  (do (pr-line) (doseq [s (instances-list)] (pprint s)))
  (do (pr-line) (doseq [s (get-members)] (pprint s)))
  (use 'ring.util.serve)
  (serve app)
  (stop-server)
  (swank.core/break)
  (defn shred-user []
    (doseq [s (filter (complement #{'shred-user}) (map first (ns-interns 'user)))]
      (ns-unmap 'user s)))
  (shred-user))

;;; JSON
(defn json-response [data & [status]]
  {:status  (or status 200)
   :headers {"Content-Type" "application/json"}
   :body    (json/generate-string data)})

;;; UI controller
(defroutes main-routes
  (context "/" []
           (GET "/" [] (view-index)))
  (context "/status" []
           (GET "/"    [] (view-status (instances-data)))
           (GET "/api" [] (json-response (instances-data)))
           (PUT "/" [cur-name new-name]
                (when-not (or
                           (= new-name cur-name)
                           (.isEmpty (.trim new-name)))
                  (instance-data-merge! {:member-name (.trim new-name)}))
                (view-status (instances-data))))
  (context "/closure-server" []
           (GET "/" [] (hello-server)))
  (context "/closure-client" []
           (GET "/" [] (hello-client)))
  (route/resources "/")
  (route/not-found "Page not found")
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
  (-> (handler/site main-routes)
      wrap-lint
      (wrap-json-params)
      wrap-lint
      ;;handle-dump
      ;;wrap-lint
      (wrap-base-url)
      (wrap-utf)
      (wrap-file "resources")
      (wrap-file-info)
      (wrap-request-logging)
      (wrap-if development? wrap-reload '[spdv.new.middleware
                                          spdv.new.closure-templates
                                          spdv.new.views])
      (wrap-bounce-favicon)
      (wrap-exception-logging)
      (wrap-if production?  wrap-failsafe)
      ;;(wrap-if development? wrap-stacktrace) ;lein-ring/ring-serve
      ))
