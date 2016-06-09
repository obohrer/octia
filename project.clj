(defproject octia "0.0.5"
  :description "A clojure library on top of compojure/ring to easily define api endpoints.
  Supports iodocs generation."
  :url "http://github.com/obohrer/octia"
  :dependencies [[org.clojure/clojure       "1.8.0"]
                 [compojure                 "1.4.0"
                  :exclusions [joda-time]]
                 [cheshire                  "5.5.0"]
                 [com.palletops/thread-expr "1.3.0"]]
  :pedantic? :abort
  :global-vars {*warn-on-reflection* true}
  :profiles {:dev
             {:dependencies [[ring-mock                 "0.1.5"]
                             [javax.servlet/servlet-api "2.5"]
                             [midje                     "1.8.3"
                              :exclusions [[commons-codec]]]]}})
