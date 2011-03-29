(ns spdv.new.test.views
  (:use clojure.test
        spdv.new.views))

(deftest parse-input-valid
  (is (= [1 2] (parse-input "1" "2"))))

(deftest parse-input-invalid
  (is (thrown? NumberFormatException
               (parse-input "foo" "bar"))))

(deftest view-output-valid
  (let [html (view-output 1 2 3)]
    (is (re-find #"two numbers added" html))))
