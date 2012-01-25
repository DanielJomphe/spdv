(ns spdv.new.routes
  (:use compojure.core
        spdv.new.middleware
        spdv.new.views
        spdv.new.apparatus
        [apparatus config cluster]
        [hiccup.middleware :only (wrap-base-url)]
        ;[lamina.core :only (permanent-channel enqueue receive siphon map*)]
        ;[aleph.http :only (start-http-server)]
        ring.handler.dump
        [ring.middleware file file-info json-params lint reload stacktrace]
        ring.util.response)
  (:require [compojure.route    :as route]
            [compojure.handler  :as handler]
            [compojure.response :as response]
            [clj-json.core      :as json]))

(boot-server)

;;; JSON usage is being deprecated thanks to ClojureScript's advent.
(defn json-response [data & [status]]
  {:status  (or status 200)
   :headers {"Content-Type" "application/json"}
   :body    (json/generate-string data)})

;;; WebSockets
;;; For simple server-sent events, see SSE, which is, FWIR, easier and
;;; more reliable. Anyway I'm yet to make the following work.
;;; That said, I'll probably end up wanting a persistent,
;;; bidirectional connection anyway, so WebSockets is the way to go.
(comment
  (def out-ch (permanent-channel))

  (defn server-status-handler [ch handshake]
    (receive ch (fn [name]
                  (siphon (map* #(str name %) ch) out-ch)
                  (siphon out-ch ch))))

  (start-http-server server-status-handler {:port 8080 :websocket true}))

(comment
  (enqueue out-ch "yo!!!")
  (use 'lamina.core/close-connection)
)

;;; UI controller using Compojure is being deprecated in favor of Noir.
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
      (wrap-json-params)
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
