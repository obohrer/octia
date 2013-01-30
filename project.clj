(defproject octia "0.0.1-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [compojure           "1.1.1"]]
  :profiles {:dev
             {:dependencies [[ring-mock       "0.1.1"]
                             [midje           "1.3.0" :exclusions [org.clojure/clojure]]]}})
