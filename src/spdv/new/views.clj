(ns spdv.new.views
  (:use [hiccup core form-helpers page-helpers]
        spdv.new.closure-templates)
  (:import [com.google.template.soy.data SoyMapData SoyListData]))

(defn status-header []
  [:section#status
   [:div#debug]
   [:div#wsMessages]])

(defn menu-header []
  [:nav [:ul
         [:li [:a {:href "/"} "home"]]
         [:li [:a {:href "/status"} "status"]]]])

(defn menu-index []
  [:nav [:ul]])

(defn menu-footer [])

(defn main-layout [& content]
  (html5 {:lang "fr"}
   [:head
    [:meta {:charset "utf-8"}] ;TODO make sure server and ring middleware don't override this, especially on Windows. If present in HTTP headers, the value should be exactly 'Content-Type: text/html; charset="utf-8"'
    [:title "SPDV"]
    (include-js  "/js/soyutils.js")
    (include-js  "/js/generated/closure_templates_1.js")
    (include-js  "/js/modernizr-2.0.4.js")
    (include-js  "/js/html5.js") ;won't probably need this one but anyway for now
    (include-js  "https://ajax.googleapis.com/ajax/libs/jquery/1.6.2/jquery.min.js")
    (include-css "/css/html5reset-1.6.1.css")
    (include-css "/css/style.css")]
   [:body
    [:header [:h1 "HEADER"] (status-header) (menu-header) [:hr]]
    content
    [:footer [:hr] (menu-footer) [:h1 "FOOTER"]]]))

(defn view-index []
  (main-layout
   [:section#index
    [:header [:h1 "Index"]
     (menu-index)]]))

(defn view-status [data]
  (let [os (data :others)
        s  (data :self)]
    (main-layout
     [:section#instances
      [:header [:h1 "État global du système"]]
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

