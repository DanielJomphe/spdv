(ns spdv.new.test.routes
  (:use clojure.test
        spdv.new.routes))

(deftest handle-input-valid
  (let [resp (main-routes {:uri "/adder" :request-method :get})]
    (is (= 200 (:status resp)))
    (is (re-find #"add two numbers" (:body resp)))))

(deftest handle-add-valid
  (let [resp (main-routes {:uri "/adder" :request-method :post
                       :params {"a" "1" "b" "2"}})]
    (is (= 200 (:status resp)))
    (is (re-find #"1 \+ 2 = 3" (:body resp)))))

(deftest handle-add-invalid
  (let [resp (main-routes {:uri "/adder" :request-method :post
                       :params {"a" "foo" "b" "bar"}})]
    (is (= 200 (:status resp)))
    (is (re-find #"those are not both numbers" (:body resp)))))

(deftest handle-catchall
  (let [resp (main-routes {:uri "/foo" :request-method :get})]
    (is (= 302 (:status resp)))
    (is (= "/" (get-in resp [:headers "Location"])))))

(deftest not-found-route
  (let [resp (main-routes {:uri "/foo" :request-method :get})]
    (is (= (:status resp) 404))
    (is (= (:body resp) "Page not found"))))
