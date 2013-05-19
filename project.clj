(defproject feedbackasaurus "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.5"]
                 [hiccup "1.0.3"]
                 [com.novemberain/monger "1.5.0"]
                 [dieter "0.4.0"]
                 [org.clojure/tools.logging "0.2.6"]
                 [cheshire "5.1.1"]]
  :plugins [[lein-ring "0.8.3"]]
  :ring {:handler feedbackasaurus.handler/app}
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.3"]]}})
