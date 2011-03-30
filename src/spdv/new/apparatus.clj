(ns spdv.new.apparatus
  (:import [com.hazelcast.core Hazelcast]))

(defn get-local-member []
  (-> (Hazelcast/getCluster) (.getLocalMember)))

(defn get-host [member]
  (-> member (.getInetSocketAddress) (.getAddress) (.getHostAddress)))

(defn get-port [member]
  (str (-> member (.getInetSocketAddress) (.getPort))))
