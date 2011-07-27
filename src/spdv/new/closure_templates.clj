(ns spdv.new.closure-templates
  (:import [java.io File]
           [com.google.template.soy SoyFileSet$Builder]
           [com.google.template.soy.tofu SoyTofu]
           [com.google.template.soy.data SoyMapData SoyListData]
           [com.google.template.soy.jssrc SoyJsSrcOptions]))

(def soyfs   (-> (SoyFileSet$Builder.)
                 (.add (File. "src/spdv/new/closure_templates.soy"))
                 (.build)))
(def tofu    (.compileToJavaObj soyfs))
(def tofu-ns (.forNamespace tofu "spdv.templates"))

(def js-opt  (doto (SoyJsSrcOptions.)
               (.setShouldGenerateJsdoc true)
               (.setShouldProvideRequireSoyNamespaces true)))

(dorun (let [files (.compileToJsSrc soyfs js-opt nil)]
         (for [i (range (.size files)) ]
           (spit (str "resources/public/js/generated/closure_templates_" (+ 1 i) ".js")
                 (nth files i)))))
