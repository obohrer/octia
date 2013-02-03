(ns octia.doc
  (:require [octia.endpoint :as endpoint]
            [cheshire.core  :as cheshire]
            [clojure.pprint :as pprint]))

(defn parameter
  [[name {:keys [type description]}]]
  {:Name        name
   :Type        type
   :Description description})

(defn endpoint
  [endpoint]
  (let [{:keys [name description params]} (endpoint/doc endpoint)
        method (endpoint/method endpoint)
        path   (endpoint/path endpoint)]
    {:MethodName  name
     :Synopsis    description
     :HTTPMethod  method
     :URI         path
     :parameters  (map parameter params)}))

(defn endpoints-group
  [group]
  (if-let [endpoints (endpoint/sub-endpoints group)]
    {:name (-> group endpoint/doc :name)
     :methods (map endpoint endpoints)}
    {:name (-> group endpoint/doc :name)
     :methods (endpoint group)}))

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
  
;
; {
;    "endpoints":[
;       {
;          "name":"Score Methods",
;          "methods":[
;             {
;                "MethodName":"Klout Score",
;                "Synopsis":"This method allows you to retrieve a Klout score",
;                "HTTPMethod":"GET",
;                "URI":"/klout.:format",
;                "RequiresOAuth":"N",
;                "parameters":[
;                   {
;                      "Name":"users",
;                      "Required":"Y",
;                      "Default":"",
;                      "Type":"string",
;                      "Description":"One or more (comma-separated) Twitter usernames"
;                   },
;                   {
;                      "Name":"format",
;                      "Required":"Y",
;                      "Default":"json",
;                      "Type":"enumerated",
;                      "Description":"Output format as JSON or XML",
;                      "EnumeratedList":[
;                         "json",
;                         "xml"
;                      ]
;                   }
;                ]
;             }
;          ]
;       },