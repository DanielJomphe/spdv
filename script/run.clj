(use 'ring.adapter.jetty)
(require 'spdv.new.routes)

;;No more needed for development thanks to ring-serve.
;;If I want swank in prod, let's use the following instead:
;;https://github.com/weavejester/ring-serve/blob/master/src/ring/middleware/swank.clj
(defn wrap-swank [handler]
  (let [conn swank.core.connection/*current-connection*]
    (fn [req]
      (binding [swank.core.connection/*current-connection* conn]
        (handler req)))))

(def swanked-app
  (-> #'spdv.new.routes/app
      (wrap-swank)))

(let [port (Integer/parseInt (get (System/getenv) "PORT" "8080"))]
  (run-jetty #'swanked-app {:port port}))
