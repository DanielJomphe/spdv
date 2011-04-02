(ns spdv.new.apparatus
  (:use [apparatus config cluster])
  (:import [com.hazelcast.core Hazelcast]
           [com.hazelcast.query Predicate]))

(defn get-local-member []
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
(defmacro defcrud                       ;stop supporting diff crudop-deffers?
  [crudop-deffer ap-ds-getter name]
  `(do
     (~crudop-deffer (~ap-ds-getter ~name) ~name "list"
                     []          (.values pred-all))
     (~crudop-deffer (~ap-ds-getter ~name) ~name "put"
                     [id# name#] (.put id# {:name name#}))
     (~crudop-deffer (~ap-ds-getter ~name) ~name "get"
                     [id#]       (.get id#))
     (~crudop-deffer (~ap-ds-getter ~name) ~name "delete"
                     [id#]       (.remove id#))))

(comment
  (pprint (macroexpand-1 '(defcrud defcrudop get-map "services")))
  -->
  (do (...only showing the R in CRUD below...)
    (defcrudop (get-map "services") "services" "get"
      [id#] (.get id#))))


(defcrud defcrudop get-map "services")

(comment
  (services-put :1 :name1)
  (services-put :2 :name2)
  (services-put :3 :name3)
  (services-put :4 :name4)
  (do (doseq [s (services-list)] (println s)) (println "=============="))

  (-> (eval-any '(+ 1 1)) (.get)))
