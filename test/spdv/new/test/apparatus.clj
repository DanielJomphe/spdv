(ns spdv.new.test.apparatus
  (:use clojure.test
        spdv.new.apparatus
        [apparatus config cluster]))

;;; START Temp stuff until clojure contrib settles down (from contrib.with-ns)
(defmacro with-ns
  "Evaluates body in another namespace.  ns is either a namespace
  object or a symbol.  This makes it possible to define functions in
  namespaces other than the current one."
  [ns & body]
  `(binding [*ns* (the-ns ~ns)]
     ~@(map (fn [form] `(eval '~form)) body)))

(defmacro with-temp-ns
  "Evaluates body in an anonymous namespace, which is then immediately
  removed.  The temporary namespace will 'refer' clojure.core."
  [& body]
  `(try
     (create-ns 'sym#)
     (let [result# (with-ns 'sym#
                     (clojure.core/refer-clojure)
                     ~@body)]
       result#)
     (finally (remove-ns 'sym#))))
;;;   END Temp stuff until clojure contrib settles down (from contrib.with-ns)

;;; Utilities
(defmacro with-test-temp-ns [& body]
  `(with-temp-ns
     (refer 'clojure.test)
     (refer 'spdv.new.apparatus)
     (refer 'apparatus.cluster)
     ~@body))

;;; Global state
(def ^:dynamic
  *hz-instance*)

;;; State fixtures
(defn fix-hz-instance [f]
  (binding [*hz-instance* (get-default-hz-instance)]
    (f)
    (shutdown-instance *hz-instance*)))

(use-fixtures :once fix-hz-instance)

(deftest make-sure-test-fixtures-are-ok-before-doing-real-tests
  (is (not (= nil *hz-instance*))))

;;; Test the application
(deftest test-the-apparatus-library
  (is (= 2 (-> (eval-any '(+ 1 1)) (.get)))))

(deftest test-make-symbol
  (are [exp act] (and (symbol? act)
                      (= exp (str act)))
       "-"   (make-symbol ""  "")
       "---" (make-symbol "-" "-")
       "a-b" (make-symbol "a" "b")
       "a-b" (make-symbol "a" "b")
       "b-c" (make-symbol "b" "c")))

(deftest test-defcrud
  (with-test-temp-ns
    (defcrud get-map "test-defcrud-get-map")
    (is (empty?        (test-defcrud-get-map-list)))
    (is (= nil         (test-defcrud-get-map-get :k)))
    (is (= nil         (test-defcrud-get-map-put :k :v)))
    (is (not (empty?   (test-defcrud-get-map-list))))
    (is (= :v   (first (test-defcrud-get-map-list))))
    (is (= :v          (test-defcrud-get-map-get :k)))
    (is (= nil         (test-defcrud-get-map-get :kkk)))
    (is (= :v          (test-defcrud-get-map-put :k :v2)))
    (is (= :v2         (test-defcrud-get-map-get :k)))
    (is (= :v2         (test-defcrud-get-map-delete :k)))
    (is (empty?        (test-defcrud-get-map-list)))
    (is (= nil         (test-defcrud-get-map-get :k)))))

(deftest test-defcrud-map
  (with-test-temp-ns
    (defcrud-map "test-defcrud-map")
    (is (empty?        (test-defcrud-map-list)))
    (is (= nil         (test-defcrud-map-get :k)))
    (is (= nil         (test-defcrud-map-put :k :v)))
    (is (not (empty?   (test-defcrud-map-list))))
    (is (= :v   (first (test-defcrud-map-list))))
    (is (= :v          (test-defcrud-map-get :k)))
    (is (= nil         (test-defcrud-map-get :kkk)))
    (is (= :v          (test-defcrud-map-put :k :v2)))
    (is (= :v2         (test-defcrud-map-get :k)))
    (is (= :v2         (test-defcrud-map-delete :k)))
    (is (empty?        (test-defcrud-map-list)))
    (is (= nil         (test-defcrud-map-get :k)))))

;;; Dev utilities
(comment
  (use 'clojure.pprint)
  (defn macprint [form]
    (do
      (println "[==============")
      (pprint (macroexpand-1 form))
      (println " ==============]")))

  (macprint '(defcrudop (get-map "my-map") "my-map" "get" [k#] (.get k#)))
  (macprint '(defcrud get-map "my-map")))
