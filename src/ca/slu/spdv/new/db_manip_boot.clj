(ns spdv.new.db-manip-boot
  (:refer-clojure :exclude [distinct conj! disj! compile drop take sort])
  (:use clojureql.core
        :reload))

(defn boot-db-manip
  [db-spec])

(defn unboot-db-manip
  [])
