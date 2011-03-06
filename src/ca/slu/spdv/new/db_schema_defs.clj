(ns ca.slu.spdv.new.slu-schema
  (:refer-clojure :exclude [alter compile drop
                            bigint boolean char double float time])
  (:use lobos.core
        lobos.schema
        ca.slu.spdv.new.lobos-utils
        :reload))

(defschema slu-schema :slu-global
  (tabl :users
        (varchar :name 100 :unique)
        (check :name (> (length :name) 1)))
  (tabl :posts
        (varchar :title 200 :unique)
        (text :content)
        (refer-to :users))
  (tabl :comments
        (text :content)
        (refer-to :users)
        (refer-to :posts)))

