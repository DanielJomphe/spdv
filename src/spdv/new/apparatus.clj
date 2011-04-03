(ns spdv.new.apparatus
  (:use [apparatus config cluster])
  (:import [com.hazelcast.core  Hazelcast]
           [com.hazelcast.query Predicate]))

(defn get-members []
  "Has the side effect of starting an instance if none exists yet."
  (-> (Hazelcast/getCluster) (.getMembers)))

(defn get-local-member []
  "Has the side effect of starting an instance if none exists yet."
  (-> (Hazelcast/getCluster) (.getLocalMember)))

(defn get-host [member]
  (-> member (.getInetSocketAddress) (.getAddress) (.getHostAddress)))

(defn get-port [member]
  (str (-> member (.getInetSocketAddress) (.getPort))))

(def pred-all
  (reify Predicate
    (apply [_ _] true)))

(defn symbol-crud [name op]
  (symbol (str name "-" op)))

(comment (use 'clojure.pprint))

(defmacro defcrudop                     ;support diff arities?
  [hz-ds name op bindings & forms]
  `(defn ~(symbol-crud name op)
     [~@bindings]
     (-> ~hz-ds ~@forms)))

(comment
  (pprint (macroexpand-1 '(defcrudop (get-map "services") "services" "list"
                            [] (.values pred-all))))
  -->
  (defn services-list
    [] (-> (get-map "services") (.values pred-all))))

;;; alternatively, could map on ops & bindings & forms, with default
;;; ops being the usual crud : ["list" "put" "get" "delete"]
;;; or better?: doseq on [{:op "list" :bindings [] :forms ...}...]
(defmacro defcrud                ;stop supporting diff crudop-deffers?
  [crudop-deffer ap-ds-getter name]
  `(do
     (~crudop-deffer (~ap-ds-getter ~name) ~name "put"
                     [k# v#] (.put    k# v#))
     (~crudop-deffer (~ap-ds-getter ~name) ~name "list"
                     []      (.values pred-all))
     (~crudop-deffer (~ap-ds-getter ~name) ~name "get"
                     [k#]    (.get    k#))
     (~crudop-deffer (~ap-ds-getter ~name) ~name "delete"
                     [k#]    (.remove k#))))

(comment
  (pprint (macroexpand-1 '(defcrud defcrudop get-map "services")))
  -->
  (do (...only showing the R in CRUD below...)
    (defcrudop (get-map "services") "services" "get"
      [k#] (.get k#))))
