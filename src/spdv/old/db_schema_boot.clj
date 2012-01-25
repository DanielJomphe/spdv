(ns spdv.new.db-schema-boot
  (:refer-clojure :exclude [alter drop
                            bigint boolean char double float time])
  (:use (lobos
         core
         connectivity
         schema)
        lobos.backends.h2
        (spdv.new
         db-schema-defs)
        :reload))

;;; For now let's just use this thing globally like in the examples...

(defn- no-schema?
  []
  true)

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

(comment
  (drop-schema schema-slu)
  (close-global)
  )
