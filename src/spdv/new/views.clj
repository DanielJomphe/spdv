(ns spdv.new.views
  (:use [hiccup core form-helpers page-helpers]
        spdv.new.closure-templates)
  (:import [com.google.template.soy.data SoyMapData SoyListData]))

(defn main-layout [& content]
  (html5
   [:head
    [:title "adder"]
    (include-js  "/js/soyutils.js")
    (include-js  "/js/closure_templates_1.js")
    (include-css "/css/style.css")]
   [:body content]))

(defn view-global-status [name & [all]]
  (main-layout
   [:h2 "État global du système"]
   (when all
     [:ul#all
      (for [x (sort-by #(str (% :name)) all)] ;unwrap from str if guaranteed
        [:li (x :name)])])
   (form-to [:put "/"]
            (label        :new-name "Nom de cette instance : ")
            (text-field   :new-name name)
            (hidden-field :cur-name name)
            (submit-button "Changer le nom"))))

(defn view-input [& [a b]]
  (main-layout
   [:h2 "add two numbers"]
   [:form {:method "post" :action "/adder"}
    (if (and a b) [:p "those are not both numbers!"])
    [:input.math {:type "text" :name "a" :value a}] [:span.math " + "]
    [:input.math {:type "text" :name "b" :value b}] [:br]
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
