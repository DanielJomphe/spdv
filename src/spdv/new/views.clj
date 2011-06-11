(ns spdv.new.views
  (:use [hiccup core form-helpers page-helpers]
        spdv.new.closure-templates)
  (:import [com.google.template.soy.data SoyMapData SoyListData]))

(defn main-layout [& content]
  (html5
   {:lang "fr"}
   [:head
    [:meta {:charset "utf-8"}] ;TODO make sure server and ring middleware don't override this, especially on Windows. If present in HTTP headers, the value should be exactly 'Content-Type: text/html; charset="utf-8"'
    [:title "SPDV"]
    (include-js  "/js/soyutils.js")
    (include-js  "/js/generated/closure_templates_1.js")
    (include-js  "/js/modernizr-2.0.4.js")
    (include-js  "/js/html5.js")        ;won't probably need this one but anyway for now
    (include-css "/css/html5reset-1.6.1.css")
    (include-css "/css/style.css")]
   [:body
    [:header ""]
    content
    [:footer ""]]))

(defn view-global-status [data]
  (let [os (data :others)
            s  (data :self)]
       (main-layout
        [:section#instances
         [:header
          [:h1 "État global du système"]] 
         (.render tofu "spdv.templates.statusInstanceSelf"
                  (SoyMapData. {"instanceId" (s :instance-id)
                                "memberHost" (s :member-host)
                                "memberName" (s :member-name)}) nil)
         (for [o os]
           (.render tofu "spdv.templates.statusInstanceOther"
                    (SoyMapData. {"instanceId" (o :instance-id)
                                  "memberHost" (o :member-host)
                                  "memberName" (o :member-name)}) nil))])))

(comment        ;for reference until I implement new forms hiccup-wise
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
   [:section
    [:header
     [:h1 "closure templates on the client side"]]
    [:script {:type "text/javascript"}
     (str "document.write(spdv.templates.helloName({'name':'Daniel'}));")]]
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
