(ns spdv.new.views
  (:use [hiccup core form-helpers page-helpers]
        spdv.new.closure-templates)
  (:import [com.google.template.soy.data SoyMapData SoyListData]))

(defn main-layout [& content]
  (html5
   [:head
    [:title "SPDV"]
    (include-js  "/js/soyutils.js")
    (include-js  "/js/generated/closure_templates_1.js")
    (include-js  "/js/modernizr-2.0.4.js")
    (include-css "/css/style.css")]
   [:body content]))

(defn view-global-status [data]
  (main-layout
     [:h2 "État global du système"]
     [:div#instances 
      (let [os (data :others)
            s  (data :self)]
        (.render tofu "spdv.templates.statusInstanceSelf"
                 (SoyMapData. {"instanceId" (s :instance-id)
                               "memberHost" (s :member-host)
                               "memberName" (s :member-name)}) nil)
        (for [o os]
          (.render tofu "spdv.templates.statusInstanceOther"
                   (SoyMapData. {"instanceId" (o :instance-id)
                                 "memberHost" (o :member-host)
                                 "memberName" (o :member-name)}) nil)))]))

(comment ;for reference until I implement new forms hiccup-wise
         (form-to [:put "/status"]
                (label        :new-name
                              (str (s :instance-id) " "
                                   (s :member-host) " "))
                (text-field   :new-name (s :member-name))
                (hidden-field :cur-name (s :member-name))
                (submit-button "Changer le nom")))

;;; Deprecated learning stuff to be removed soon
(defn hello-server []
  (str "<h2>closure templates on the server side</h2>"
       (.render tofu-ns ".helloName"
                {"name" "Daniel"} nil)
       "<hr/>"
       (.render tofu-ns ".helloName" {"name"         "Daniel",
                                      "greetingWord" "Bonjour"} nil)
       "<hr/>"
       (.render tofu-ns ".helloNames" {"name"            "Daniel",
                                       "additionalNames" ["Bob" "Cid"]} nil)))

(defn hello-client []
  (main-layout
   [:h2 "closure templates on the client side"]
   [:script {:type "text/javascript"}
     (str "document.write(spdv.templates.helloName({'name':'Daniel'}));")]
   [:hr]
   [:script {:type "text/javascript"}
     (str "document.write(spdv.templates.helloName({'name':'Daniel',
                                            'greetingWord':'Bonjour'}));")]
   [:hr]
   [:script {:type "text/javascript"}
     (str "document.write(spdv.templates.helloNames({'name':'Daniel',
                                          'additionalNames':['Bob', 'Cid']}));")]))

;;; Deprecated learning stuff, but for now, let's keep some for the unit tests.
(defn parse-input [a b]
  [(Integer/parseInt a) (Integer/parseInt b)])
