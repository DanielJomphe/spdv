(ns ca.slu.spdv.new.db-schema-boot
  (:refer-clojure :exclude [alter drop
                            bigint boolean char double float time])
  (:use (lobos
         core
         connectivity
         schema)
        lobos.backends.h2
        (ca.slu.spdv.new
         db-manip-boot
         db-schema-defs)
        :reload))

(declare no-schema?)

;;; For now let's just use this thing globally like in the examples...

(defn boot-db-schema
  [db-spec]
  (open-global db-spec)
  (when (no-schema?)
    (create-schema schema-slu)
    (set-default-schema schema-slu)))

(defn unboot-db-schema
  []
  (drop-schema schema-slu) ; for now, let's drop it
  (close-global))

(defn- no-schema?
  []
  true)

(comment
  (drop-schema schema-slu)
  (close-global)
  )
