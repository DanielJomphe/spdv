(ns spdv.new.ring-handler
  (:use (ring.handler dump)
        (ring.middleware reload lint)
        (ring.adapter jetty)
        spdv.new.closure-templates
        :reload))

(defn hello [req]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    (hello-world)})

(defn wrap-hello [app]
  (fn [req]
    (hello req)))

(defn wrap-everything [app]
  (fn [req]
    (let [orig-resp (app req)]
      (assoc orig-resp :body (str "Wrapping Hello..." (:body orig-resp) "Wrapped Hello!")))))

(defn wrap-error [app]
  (fn [req]
    (if (= "/error" (:uri req))
      (throw (Exception. "Demonstrating ring.middleware.stacktrace"))
      (app req))))

(def app
  (-> handle-dump
      wrap-lint
      wrap-hello
      wrap-lint
      wrap-error
      wrap-lint
      wrap-everything
      wrap-lint
      (wrap-reload '(ring.handler.dump))
      wrap-lint))

