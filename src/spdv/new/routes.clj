(ns spdv.new.routes
  (:use compojure.core
        spdv.new.middleware
        spdv.new.views
        spdv.new.apparatus
        [apparatus config cluster]
        [hiccup.middleware :only (wrap-base-url)]
        [ring.middleware file file-info lint reload stacktrace]
        ring.util.response)
  (:require [compojure.route    :as route]
            [compojure.handler  :as handler]
            [compojure.response :as response]))

(boot-server)

;;; UI controller using Compojure is being deprecated in favor of Noir.
(defroutes main-routes
  (context "/" []
           (GET "/" [] (view-index)))
  (context "/status" []
           (GET "/"    [] (view-status   (servers-props-data)))
           (PUT "/" [cur-name new-name]
                (when-not (or
                           (= new-name cur-name)
                           (.isEmpty (.trim new-name)))
                  (server-props-merge! {:member-name (.trim new-name)}))
                (view-status (servers-props-data))))
  (route/not-found "Page not found")
  (comment
    (ANY "/*" [_]
         (redirect "/"))))

;;; Server utilities TODO unify with the rest of the config in NoirCast.
(def production?
  (= "production" (get (System/getenv) "APP_ENV")))

(def development?
  (not production?))

;;; Web server configuration is probably deprecated thanks to most of
;;; Noir's defaults.
(def app
  (-> (handler/site main-routes)
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
      ))

;;; TODO Temporary dev/prod lifecycle
;;; vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
(use 'ring.util.serve)
(defn boot-web-server []
  (serve app))

(when production?
  (boot-web-server))

;;; Dev utilities
(comment
  (boot-web-server)
  (stop-server))
