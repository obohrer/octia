(defproject octia "0.0.4-SNAPSHOT"
  :description "A clojure library on top of compojure/ring to easily define api endpoints.
  Supports iodocs generation."
  :url "http://github.com/obohrer/octia"
  :dependencies [[org.clojure/clojure       "1.5.1"]
                 [compojure                 "1.1.6"
                  :exclusions [joda-time]]
                 [cheshire                  "5.3.1"]
                 [com.palletops/thread-expr "1.3.0"]]
  :warn-on-reflection true
  :profiles {:dev
             {:dependencies [[ring-mock "0.1.5"]
                             [midje     "1.6.3"
                              :exclusions [org.clojure/clojure org.clojure/tools.macro joda-time]]]}})
