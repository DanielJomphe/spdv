(ns spdv.new.views
  (:use [hiccup core form-helpers page-helpers]
        spdv.new.closure-templates)
  (:import [com.google.template.soy.data SoyMapData SoyListData]))

(defn main-layout [& content]
  (html5
   [:head
    [:title "SPDV"]
    (include-js  "/js/soyutils.js")
    (include-js  "/js/closure_templates_1.js")
    (include-css "/css/style.css")]
   [:body content]))

(defn view-global-status [data]
  (let [os (data :others)
        s  (data :self)]
    (main-layout
     [:h2 "État global du système"]
     [:instances
      (.render tofu "spdv.templates.statusInstanceSelf"
               (SoyMapData. {"instanceId" (s :instance-id)
                             "memberHost" (s :member-host)
                             "memberName" (s :member-name)}) nil)
      (for [o os]
        (.render tofu "spdv.templates.statusInstanceOther"
                 (SoyMapData. {"instanceId" (o :instance-id)
                               "memberHost" (o :member-host)
                               "memberName" (o :member-name)}) nil))])))

(comment ;for reference until I implement new forms hiccup-wise
         (form-to [:put "/"]
                (label        :new-name
                              (str (s :instance-id) " "
                                   (s :member-host) " "))
                (text-field   :new-name (s :member-name))
                (hidden-field :cur-name (s :member-name))
                (submit-button "Changer le nom")))

(defn view-input [& [a b]]
  (main-layout
   [:h2 "add two numbers"]
   [:form {:method "post" :action "/adder"}
    (if (and a b) [:p "those are not both numbers!"])
    [:input.math {:type "text" :member-name "a" :value a}] [:span.math " + "]
    [:input.math {:type "text" :member-name "b" :value b}] [:br]
    [:input.action {:type "submit" :value "add"}]]))

(defn view-output [a b sum]
  (main-layout
   [:h2 "two numbers added"]
   [:p.math a " + " b " = " sum]
   [:a.action {:href "/adder"} "add more numbers"]))

(defn parse-input [a b]
  [(Integer/parseInt a) (Integer/parseInt b)])

(defn hello-server []
  (str "<h2>closure templates on the server side</h2>"
       (.render tofu "spdv.templates.helloWorld" {} nil)
       (.render tofu-ns ".helloName"
                (SoyMapData. {"name" "Daniel"}) nil)
       (.render tofu-ns ".helloName"
                (SoyMapData. {"name" "Daniel" "greetingWord" "Bonjour"}) nil)
       (.render tofu-ns ".helloNames"
                (SoyMapData. {"name" "Daniel"
                              "additionalNames" (SoyListData. ["Bob" "Cid" "Lee"])})
                nil)))

(defn hello-client []
  (main-layout
   [:h2 "closure templates on the client side"]
   [:script {:type "text/javascript"}
    (str "document.write(spdv.templates.helloWorld());"
         "document.write(spdv.templates.helloName({'name':'Daniel'}));"
         "document.write(spdv.templates.helloName({'name':'Daniel', 'greetingWord':'Bonjour'}));"
         "document.write(spdv.templates.helloNames({'name':'Daniel', 'additionalNames':['Bob', 'Cid', 'Lee']}));")]))
