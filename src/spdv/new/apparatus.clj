(ns spdv.new.apparatus
  (:use [apparatus config cluster])
  (:import [spdv MacAddress]
           [com.hazelcast.core
            Hazelcast
            LifecycleService LifecycleListener LifecycleEvent
            MembershipEvent MembershipListener]
           [com.hazelcast.query Predicate]))

;;; Hazelcast Core: HazelcastInstance
(defn get-default-hz-instance []
  "Sometimes, a default instance is good enough.
   Side effect: starts an instance if none exists yet."
  (Hazelcast/getDefaultInstance))

(defn get-name [hz-instance]
  (.getName hz-instance))

;;; lifecycle
(defn get-lifecycle-service
  ([]            (-> (Hazelcast/getLifecycleService)))
  ([hz-instance] (-> hz-instance (.getLifecycleService))))

(defn add-lifecycle-listener [lifecycle-service on-event]
  (.addLifecycleListener
   lifecycle-service
   (reify LifecycleListener
     (^void stateChanged [_ ^LifecycleEvent e]
       (let [state (.getState e)]
         (on-event state))))))

(defn shutdown-instance [hz-instance]
  (.shutdown (get-lifecycle-service hz-instance)))

;;; Hazelcast Core: Cluster
(defn get-cluster []
  "Side effect: starts an instance if none exists yet."
  (Hazelcast/getCluster))

(defn get-member-local []
  "Side effect: starts an instance if none exists yet."
  (-> (get-cluster) (.getLocalMember)))

(defn get-members []
  "Side effect: starts an instance if none exists yet."
  (-> (get-cluster) (.getMembers)))

(defn add-membership-listener [on-member-added
                               on-member-removed]
  (-> (get-cluster)
      (.addMembershipListener
       (reify MembershipListener
         (^void memberAdded   [_ ^MembershipEvent e] (on-member-added   e))
         (^void memberRemoved [_ ^MembershipEvent e] (on-member-removed e))))))

;;; Hazelcast Core: Member
(defn get-host [member]
  (-> member (.getInetSocketAddress) (.getAddress) (.getHostAddress)))

(defn get-port [member]
  (str (-> member (.getInetSocketAddress) (.getPort))))

;;; Utility. Not sure I shouldn't instead use Hazelcast/getIdGenerator
;;; but I would prefer to have instance ids that mean something to me.
(defn make-id [member & full]
  (let [mac  (MacAddress/get)
        host (get-host member)
        port (get-port member)]
    (str (if full mac  (last (.split mac "-")))    "["
         (if full host (last (.split host "[.]"))) ":"
         port                                      "]")))

(defn make-id-generated []
  (-> (Hazelcast/getIdGenerator "hz-instance-ids") (.newId)))

;;; Hazelcast Query: Predicate
(def pred-all
  (reify Predicate
    (apply [_ _] true)))

;;; CRUD for Hazelcast data structures
(defn symbol-crud [name op]
  (symbol (str name "-" op)))

(comment
  (use 'clojure.pprint))

(defmacro defcrudop                     ;support diff arities?
                                        ;might also not -> auto
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

;; alternatively, could map on ops & bindings & forms, with default
;; ops being the usual crud : ["list" "put" "get" "delete"]
;; or better?: doseq on [{:op "list" :bindings [] :forms ...}...]
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
  (do (...only showing below the R of CRUD...)
    (defcrudop (get-map "services") "services" "get"
      [k#] (.get k#))))
