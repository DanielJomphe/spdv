(ns ca.slu.spdv.new.lobos-utils
  (:refer-clojure :exclude [alter compile drop
                            bigint boolean char double float time])
  (:use (lobos core schema)
        :reload))

(defn surrogate-key [table]
  (integer table :id :auto-inc :primary-key))

(defn datetime-tracked [table]
  (-> table
      (timestamp :updated_on)
      (timestamp :created_on (default (now)))))

(defn refer-to [table ptable]
  (let [cname (-> (->> ptable name butlast (apply str))
                  (str "_id")
                  keyword)]
    (integer table cname [:refer ptable :id :on-delete :set-null])))

(defmacro tabl [name & elements]
  `(-> (table ~name
              (surrogate-key)
              (datetime-tracked))
       ~@elements))

