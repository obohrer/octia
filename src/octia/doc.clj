(ns octia.doc
  (:require [octia.endpoint :as endpoint]
            [cheshire.core  :as cheshire]
            [clojure.string :as string]
            [clojure.pprint :as pprint]))

(def default-type "string")

(defn bool->s
  [b]
  (if b
    "Y"
    "N"))

(defn parameter
  [[name {:keys [type description choices optional default]}]]
  (case (or type default-type)
    "enumerated"
    {:Name        name
     :Type        type
     :Description description
     :Required    (-> optional not bool->s)
     :Default     default
     :EnumeratedList choices}
    {:Name        name
     :Type        type
     :Required    (-> optional not bool->s)
     :Default     default
     :Description description}))

(defn json-input?
  [input]
  (= input :json))

(def json-content-type
  {"Name"           "Content-Type",
   "Required"       (bool->s true)
   "Default"        "application/json"
   "Type"           "enumerated"
   "Description"    "response mime type"
   "EnumeratedList" ["application/json"]})

(def accept-json
  {"Name"           "Accept",
   "Required"       (bool->s true)
   "Default"        "application/json"
   "Type"           "enumerated"
   "Description"    "response mime type"
   "EnumeratedList" ["application/json"]})

(defn json-params
  [params]
  (filter (comp (partial = :json) :location second) params))

(defn query-params
  [params]
  (filter (comp #(or (nil? %) (= :query %)) :location second) params))

(defn endpoint
  [endpoint]
  (let [{:keys [input description params] :as doc} (endpoint/doc endpoint)
        method       (endpoint/method endpoint)
        path         (endpoint/path endpoint)
        json-params  (json-params params)
        query-params (query-params params)]
    {:MethodName    (:name doc)
     :Synopsis      description
     :HTTPMethod    (-> method name string/upper-case)
     :URI           path
     :elements      (map parameter json-params)
     :parameters    (map parameter query-params)
     :headers       (if (json-input? input)
                      [json-content-type accept-json]
                      [accept-json])
     :RequiresOAuth (bool->s false)}))

(defn endpoints-group
  [group]
  (let [sub-endpoints (endpoint/sub-endpoints group)
        endpoints (or sub-endpoints [group])]
    {:name (-> group endpoint/doc :name)
     :methods (map endpoint endpoints)}))

(defn generate-iodocs
  [main-handler]
  (let [groups (->> main-handler endpoint/sub-endpoints (map endpoints-group))]
    {:endpoints groups}))

(defn generate
  [main-handler]
  (let [docs (generate-iodocs main-handler)]
    (count docs)
    (pprint/pprint docs)
    (spit "doc.json" (cheshire/generate-string docs))))
