(ns spdv.new.apparatus
  (:use [apparatus config cluster])
  (:import [spdv MacAddress]
           [com.hazelcast.core
            Hazelcast
            LifecycleService LifecycleListener LifecycleEvent LifecycleEvent$LifecycleState
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
(defn make-id
  ([]
     (make-id (get-member-local) :full))
  ([member & full]
     (let [mac  (MacAddress/get)
           host (get-host member)
           port (get-port member)]
       (str (if full mac  (last (.split mac "-")))    "["
            (if full host (last (.split host "[.]"))) ":"
            port                                      "]"))))

(defn make-id-generated []
  (-> (Hazelcast/getIdGenerator "hz-instance-ids") (.newId)))

;;; Hazelcast Query: Predicate
;;; I stopped using it. For some reason, (.values pred-all) became long-running!?, so I instead now use (.values).
(def pred-all
  (reify Predicate
    (apply [_ _] true)))

;;; CRUD DSL for Hazelcast data structures
(defn make-symbol [before-dash after-dash]
  (symbol (str before-dash "-" after-dash)))

(defmacro defcrudop [hz-ds name op bindings & forms]
  `(defn ~(make-symbol name op) [~@bindings]
     (-> ~hz-ds ~@forms)))

(defmacro defcrud [ap-ds-getter name]
  `(do (defcrudop (~ap-ds-getter ~name) ~name "put"    [k# v#] (.put    k# v#))
       (defcrudop (~ap-ds-getter ~name) ~name "list"   [     ] (.values      ))
       (defcrudop (~ap-ds-getter ~name) ~name "get"    [k#   ] (.get    k#   ))
       (defcrudop (~ap-ds-getter ~name) ~name "delete" [k#   ] (.remove k#   ))))

(comment
  (defcrudop (get-map "my-map") "my-map" "get" [k#] (.get k#))
  -->
  (defn my-map-get [k#]
    (-> (get-map "my-map") (.get k#)))

  (defcrud get-map "my-map")
  -->
  (do ...
      (defcrudop (ap-ds-getter name) name "get" [k#] (.get k#))
      ...))

;;; Constructors for Hazelcast data structures
(defmacro defcrud-map [name]
  `(defcrud get-map ~name))

;;; BOOT - TODO move this elsewhere
(defn boot-apparatus []
  (comment
    (def hz-instance (instance (config))))

  ;; The following are exported globals (ThreadLocals for now).
  ;; TODO Might want to make them locals to the server below, or ref them instead.
  ;; vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv

  ;; Instance & Member: two angles of the same thing
  (def hz-instance  (get-default-hz-instance)) ;auto-startup happens here
  (def hz-member    (get-member-local)))

(defn boot-server []
  (boot-apparatus)
  ;; The following are exported globals (ThreadLocals for now).
  ;; TODO Might want to make them locals here, and/or ref some of them instead.
  ;; vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
  (def server-id   (make-id))
  (def server-name (get-name hz-instance))
  (def server-host (get-host hz-member))

  ;; The following vars definitely need to be exported.
  ;; TODO Might want to ref them instead of using ThreadLocals.
  ;; vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv

  ;; This CRUD is to be used as an history of the properties of the past and current
  ;; server instances, along with their attached data (see below).
  (defcrud-map "servers-props")

  (defn server-props []
    (servers-props-get server-id))

  (defn server-props-set! [v]
    (servers-props-put server-id v))

  (defn server-props-merge! [v]
    (server-props-set!
     (merge (server-props) v)))

  ;; The current server's properties
  (server-props-set! {:instance-id   server-id
                      :instance-name server-name
                      :member-host   server-host
                      :member-name   server-id
                      :state         "ACTIVE"})

  ;; This should probably not be exported.
  ;; Hookup into the LifecycleService
  (add-lifecycle-listener
   (get-lifecycle-service hz-instance)
   #(if (= LifecycleEvent$LifecycleState/STARTED %)
      (server-props-merge! {:state "ACTIVE"})
      (server-props-merge! {:state "INACTIVE"})))

  ;; The structured history of properties of the past and current server instances
  (defn servers-props-data []
    "Builds a structured map of attached data of the hz-instances,
   out of the app's persistent memory.
   For now, this thing contains no optimizations whatsoever."
    (let [xs         (sort-by #(:member-name %)
                              (servers-props-list))
          name-discr #(= (:member-name %)
                         ((server-props) :member-name))]
      {:self   (first
                (filter name-discr xs))
       :others  (remove name-discr xs)})))

;;; Dev utilities
(comment
  (use 'spdv.new.routes)
  (use 'clojure.pprint)
  (defn pr-line [] (println "=============="))
  (def               ins-0 (instance (config)))
  (def               ins-1 (instance (config)))
  (add-lifecycle-listener
   (get-lifecycle-service ins-0)
   #(do (pr-line) (println (str "!!! Instance state changed in the cluster: " %))))
  (add-membership-listener
   #(do (pr-line) (println (str "!!! Member added to the cluster: " %)))
   #(do (pr-line) (println (str "!!! Member removed from the cluster: " %))))
  (shutdown-instance ins-0)
  (shutdown-instance ins-1)
  (servers-props-put "server-id0"
                     {:instance-id   "00-00-00-00-00-00[000.000.000.000:0000]"
                      :instance-name "some_hazelcast_0_name"
                      :member-host   "000.000.000.000"
                      :member-name   "00[000:0000]"})
  (servers-props-put "server-id1"
                     {:instance-id   "11-11-11-11-11-11[111.111.111.111:1111]"
                      :instance-name "some_hazelcast_1_name"
                      :member-host   "111.111.111.111"
                      :member-name   "11[111:1111]"})
  (do (pr-line) (pprint (server-props)))
  (do (pr-line) (pprint (servers-props-data)))
  (do (pr-line) (doseq [s (servers-props-list)] (pprint s)))
  (do (pr-line) (doseq [s (get-members)] (pprint s)))
  (swank.core/break)
  (defn shred-user []
    (doseq [s (filter (complement #{'shred-user}) (map first (ns-interns 'user)))]
      (ns-unmap 'user s)))
  (shred-user))
