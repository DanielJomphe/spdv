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
  (:require [compojure.route    :as route]
            [compojure.handler  :as handler]
            [compojure.response :as response]
            [clj-json.core      :as json]))

(boot-server)

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
           (GET "/"    [] (view-status   (servers-props-data)))
           (GET "/api" [] (json-response (servers-props-data)))
           (PUT "/" [cur-name new-name]
                (when-not (or
                           (= new-name cur-name)
                           (.isEmpty (.trim new-name)))
                  (server-props-merge! {:member-name (.trim new-name)}))
                (view-status (servers-props-data))))
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

;;; Web server configuration
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

;;; TODO Create a dev/prod lifecycle that makes sense instead of the following stuff
;;; vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
(use 'ring.util.serve)
(defn boot-web-server []
  (serve app))

(when production?
  (boot-web-server))

;;; Dev utilities
(comment
  (boot-web-server)
  (stop-server))
