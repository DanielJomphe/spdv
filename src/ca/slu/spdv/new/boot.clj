(ns spdv.new.boot
  (:refer-clojure :exclude [distinct conj! disj! compile drop take sort])
  (:use clojureql.core
        (spdv.new
         db-schema-boot
         db-manip-boot)
        :reload))

(def db-credentials {:user "sa" :password ""})

(def db-spec (:driver-manager-spec
              {:driver-manager-spec
               (conj db-credentials {:classname "org.h2.Driver"
                                     :subprotocol "h2"
                                     :subname (:serv-prst
                                               {:serv-prst      "tcp://localhost/~/test"
                                                :embd-prst      "jdbc:h2:~/test"
                                                :embd-mmry      "jdbc:h2:mem:test"
                                                :embd-mmry-priv "jdbc:h2:mem:"})})
               :data-source-spec (conj db-credentials {:datasource "optional, a javax.sql.DataSource"})
               :jndi-spec {:name "optional, a String or a javax.naming.Name"
                           :environment "optional, a java.util.Map"}}))

(boot-db-schema db-spec)
(boot-db-manip db-spec)

(let [u (table db-spec "\"slu-global\".\"users\"")] ; for now, can't use keywords
  @(conj! u {"\"name\"" "Daniel"})
  )

(unboot-db-manip)
(unboot-db-schema)

