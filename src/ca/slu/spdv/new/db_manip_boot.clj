(ns ca.slu.spdv.new.db-manip-boot
  (:use lobos.connectivity
        lobos.backends.h2
        :reload))

(defn open-slu-db
  [db-spec]
  (open-global db-spec))

(defn close-slu-db
  []
  (close-global))
