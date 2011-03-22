(ns spdv.new.closure-templates
  (:import [java.io File]
           [com.google.template.soy SoyFileSet$Builder]
           [com.google.template.soy.tofu SoyTofu]
           [com.google.template.soy.data SoyMapData SoyListData]))

(defn html-paragraph [html]
  (str "<p>"
       html
       "</p>"))

(defn hello-world []
  (let [tofu (-> (SoyFileSet$Builder.)
                 (.add (File. "src/spdv/new/closure_templates.soy"))
                 (.build)
                 (.compileToJavaObj))
        my-tofu (.forNamespace tofu "spdv.templates")]
    (html-paragraph
     (str
      (html-paragraph (.render tofu "spdv.templates.helloWorld" {} nil))
      (html-paragraph (.render my-tofu ".helloName" (SoyMapData. {"name" "Daniel"}) nil))
      (html-paragraph (.render my-tofu ".helloName" (SoyMapData. {"name" "Daniel" "greetingWord" "Bonjour"}) nil))
      (html-paragraph (.render my-tofu ".helloNames" (SoyMapData. {"name" "Daniel"
                                                           "additionalNames" (SoyListData. ["Bob" "Cid" "Lee"])})
                       nil))))))
