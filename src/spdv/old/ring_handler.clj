(ns spdv.new.ring-handler
  (:use (spdv.new closure-templates)))

;;; No more used. Was useful before using Compojure.

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
      (assoc orig-resp :body (str "Wrapping Hello..."
                                  (:body orig-resp)
                                  "Wrapped Hello!")))))

(defn wrap-error [app]
  (fn [req]
    (if (= "/error" (:uri req))
      (throw (Exception. "Demonstrating ring.middleware.stacktrace"))
      (app req))))
