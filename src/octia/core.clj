(ns octia.core
  (:require [octia.compojure-adapter :as compojure-adapter]
            [octia.endpoint          :as endpoint]
            [octia.wrapper           :as wrapper]
            [octia.doc               :as doc]
            [clojure.string          :as string]))

(def default-group
  {:path ""
   :opts {}})

(def ^{:dynamic true} *group* default-group)

(declare merge-groups)

(defmacro group
  [path {:keys [doc wrappers] :as opts} & body]
  `(binding [*group* (merge-groups *group* {:path ~path :opts ~opts})]
     (let [endpoints# (vector ~@body)]
       (reify
         endpoint/Endpoint
           (doc [this#] ~doc)
           (path [this#] ~path)
           (sub-endpoints [this#]
             endpoints#)
         clojure.lang.IFn
           (invoke [this# request#]
             (some #(% request#) endpoints#))))))

(defn merge-paths
  [grp-path endpoint-path]
  (cond
    (string? endpoint-path)
      (str grp-path endpoint-path)
    (vector? endpoint-path)
      (-> (str grp-path (first endpoint-path)) vector (concat (rest endpoint-path)) vec)))

(defn merge-groups
  [g1 g2]
  {:path (->> [g1 g2] (map :path) string/join)
   :opts (merge (:opts g2)
                {:wrappers (concat (-> g1 :opts :wrappers)
                                   (-> g2 :opts :wrappers))
                 :doc (-> g2 :opts :doc)})})

(defmacro endpoint
  "Generate an endpoint"
  [method path {:keys [doc wrappers] :as opts} args & body]
  `(let [path# (-> *group* :path (merge-paths ~path))
         all-wrappers-def# (-> *group* :opts :wrappers (or []) (concat ~wrappers))
         compiled-route# (compojure-adapter/prepare-route path#)
         wrappers-factories# (->> all-wrappers-def# (map wrapper/->wrapper-factory))
         endpoint-def# (reify endpoint/Endpoint
                         (doc [this#] ~doc)
                         (path [this#] path#)
                         (route [this#] compiled-route#)
                         (method [this#] ~method)
                         (sub-endpoints [this#] nil))
         wrappers# (->> wrappers-factories# (map #(wrapper/build % endpoint-def#)))
         route-fn# (compojure-adapter/m-compile-route ~method compiled-route# wrappers# ~args ~body)]
     (reify
       endpoint/Endpoint
         (doc [this#] ~doc)
         (path [this#] path#)
         (route [this#] compiled-route#)
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

(defmacro HEAD
  [path {:keys [doc wrappers] :as opts} args & body]
  `(endpoint :head ~path ~opts ~args ~@body))

(defmacro OPTIONS
  [path {:keys [doc wrappers] :as opts} args & body]
  `(endpoint :options ~path ~opts ~args ~@body))

(defmacro PATCH
  [path {:keys [doc wrappers] :as opts} args & body]
  `(endpoint :patch ~path ~opts ~args ~@body))

(defmacro ANY
  [path {:keys [doc wrappers] :as opts} args & body]
  `(endpoint nil ~path ~opts ~args ~@body))
