(defproject uima-clj "0.1.0-SNAPSHOT"
  :description "UIMA Library for Clojure"
  :license {:name "Apache License 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [com.taoensso/timbre "4.1.4"]
                 [org.apache.uima/uimaj-core "2.8.1"]
                 [org.apache.uima/uimafit-core "2.1.0"]]
  :profiles {:dev {:dependencies [[midje "1.6.0"]]}})
