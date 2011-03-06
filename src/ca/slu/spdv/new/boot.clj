(ns ca.slu.spdv.new.boot
  (:refer-clojure :exclude [alter drop
                            bigint boolean char double float time])
  (:use (lobos core schema)
        (ca.slu.spdv.new slu-db slu-schema)
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

;;; For now let's just use this thing globally like in the examples...

(open-slu-db db-spec)

(create-schema      slu-schema)
(set-default-schema slu-schema)



(drop-schema        slu-schema)

(close-slu-db)


