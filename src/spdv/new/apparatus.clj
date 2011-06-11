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
;;; I stopped using it. For some reason, (.values pred-all) became long-running!?, so I instead now use (.values).
(def pred-all
  (reify Predicate
    (apply [_ _] true)))

;;; CRUD for Hazelcast data structures
(defn make-symbol [before-dash after-dash]
  (symbol (str before-dash "-" after-dash)))

(defmacro defcrudop [hz-ds name op bindings & forms]
  `(defn ~(make-symbol name op) [~@bindings]
     (-> ~hz-ds ~@forms)))

(defmacro make-crudop [& rest]
  "This macro depends on its local context to provide all vars it references except 'rest'."
  `(defcrudop (~'ap-ds-getter ~'name) ~'name ~@rest))

(defmacro defcrud [ap-ds-getter name]
  `(do ~(make-crudop "put"    [k# v#] (.put    k# v#))
       ~(make-crudop "list"   [     ] (.values      ))
       ~(make-crudop "get"    [k#   ] (.get    k#   ))
       ~(make-crudop "delete" [k#   ] (.remove k#   ))))

(comment
  (defn macprint [form]
    (do
      (println "==============")
      (use 'clojure.pprint)
      (pprint (macroexpand-1 form))))

  (macprint '(defcrudop (get-map "my-map") "my-map" "get" [k#] (.get k#)))
  (macprint '(make-crudop "get" [k#] (.get k#)))
  (macprint '(defcrud get-map "my-map"))

  (comment --> (defn my-map-get [k#]
                 (-> (get-map "my-map") (.get k#)))
           --> (defcrudop (get-map "my-map") "my-map" "get" [k#] (.get k#))
           --> (do ...
                   (make-crudop "get" [k#] (.get k#))
                   ...))

  (defcrud get-map "blah")
  (blah-list)
  (blah-put :k :v)
  (blah-get :k)
  (blah-delete :k))
