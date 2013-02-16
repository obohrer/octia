(ns octia.compojure-adapter
  (:require [compojure.core :as c]
            [clout.core     :as clout]))

;; From compojure
(defn prepare-route
  "Pre-compile the route."
  [route]
  (cond
    (string? route)
      (clout/route-compile route)
    (vector? route)
      (clout/route-compile
        (first route)
        (apply hash-map (rest route)))
    :else
      (if (string? route)
         (clout/route-compile route)
         route)))

(defn wrap-with
  "Takes a function and a vector of decorators, returns
  a new function decored."
  [func decorators]
  (reduce #(%2 %1) func decorators))

(defmacro m-compile-route
  "Generate a function from a route definition"
  [method route decorators bindings body]
  `(c/make-route
    ~method (prepare-route ~route)
    (wrap-with
      (fn [request#]
        (c/let-request [~bindings request#] ~@body))
      ~decorators)))
