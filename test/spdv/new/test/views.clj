(ns spdv.new.test.views
  (:use clojure.test
        spdv.new.views))

(deftest view-status-valid
  (let [html (view-status {:self {:instance-id   "instance-id"
                                  :instance-name "instance-name"
                                  :member-host   "member-host"
                                  :member-name   "member-name"}
                           :others '({:instance-id   "instance-id-other"
                                      :instance-name "instance-name-other"
                                      :member-host   "member-host-other"
                                      :member-name   "member-name-other"})})]
    (is (re-find #"instance-id-other" html))))
