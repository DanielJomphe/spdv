(ns spdv.new.test.apparatus
  (:use clojure.test
        spdv.new.apparatus))

(comment
  (use 'clojure.pprint)
  (defn macprint [form]
    (do
      (println "==============")
      (pprint (macroexpand-1 form))))

  (macprint '(defcrudop (get-map "my-map") "my-map" "get" [k#] (.get k#)))
  (macprint '(defcrud get-map "my-map"))

  (comment --> (defn my-map-get [k#]
                 (-> (get-map "my-map") (.get k#)))
           --> (do ...
                   (defcrudop (ap-ds-getter name) name "get" [k#] (.get k#))
                   ...))

  (defcrud get-map "blah")
  (blah-list)
  (blah-put :k :v)
  (blah-get :k)
  (blah-delete :k))
