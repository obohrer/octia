(ns octia.core
  (:require [octia.compojure-adapter :as compojure-adapter]
            [octia.endpoint          :as endpoint]
            [octia.wrapper           :as wrapper]
            [octia.doc               :as doc]))

(def default-group
  {:path ""
   :opts {}})

(def ^{:dynamic true} *group* default-group)

(defmacro group
  [path {:keys [doc wrappers] :as opts} & body]
  `(reify
     endpoint/Endpoint
       (doc [this#] ~doc)
       (path [this#] ~path)
       (sub-endpoints [this#]
         (binding [*group* {:path ~path
                             :opts ~opts}] (vector ~@body)))
     clojure.lang.IFn
       (invoke [this# request#]
         (binding [*group* {:path ~path
                             :opts ~opts}]
            (some #(% request#) (vector ~@body))))))

(defn endpoints->handler
  "Combine several endpoints into one handler"
  [& endpoints]
  (reify
     endpoint/Endpoint
       (sub-endpoints [this] endpoints)
     clojure.lang.IFn
       (invoke [this request]
         (some #(% request) endpoints))))

(defn merge-paths
  [grp-path endpoint-path]
  (cond
    (string? endpoint-path)
      (str grp-path endpoint-path)
    (vector? endpoint-path)
      (-> (str grp-path (first endpoint-path)) vector (concat (rest endpoint-path)) vec)))

(defmacro endpoint
  "Generate an endpoint"
  [method path {:keys [doc wrappers] :as opts} args & body]
  `(let [path# (-> *group* :path (merge-paths ~path))
         all-wrappers-def# (-> *group* :opts :wrappers (or []) (concat ~wrappers))
         wrappers-factories# (->> all-wrappers-def# (map wrapper/->wrapper-factory))
         endpoint-def# (reify endpoint/Endpoint
                         (doc [this#] ~doc)
                         (path [this#] path#)
                         (method [this#] ~method)
                         (sub-endpoints [this#] nil))
         wrappers# (->> wrappers-factories# (map #(wrapper/build % endpoint-def#)))
         route-fn# (compojure-adapter/m-compile-route ~method path# wrappers# ~args ~body)]
     (reify
       endpoint/Endpoint
         (doc [this#] ~doc)
         (path [this#] path#)
         (method [this#] ~method)
         (sub-endpoints [this#] nil)
       clojure.lang.IFn
         (invoke [this# request#] (route-fn# request#)))))

(defmacro GET
  [path {:keys [doc wrappers] :as opts} args & body]
  `(endpoint :get ~path ~opts ~args ~@body))

(defmacro POST
  [path {:keys [doc wrappers] :as opts} args & body]
  `(endpoint :post ~path ~opts ~args ~@body))

(defmacro PUT
  [path {:keys [doc wrappers] :as opts} args & body]
  `(endpoint :put ~path ~opts ~args ~@body))

(defmacro DELETE
  [path {:keys [doc wrappers] :as opts} args & body]
  `(endpoint :delete ~path ~opts ~args ~@body))

(defmacro ANY
  [path {:keys [doc wrappers] :as opts} args & body]
  `(endpoint :any ~path ~opts ~args ~@body))
