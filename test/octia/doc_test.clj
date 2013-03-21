(ns octia.doc-test
  (:require [octia.doc      :as doc]
            [octia.endpoint :as endpoint])
  (:use clojure.test
        octia.core
        midje.sweet))

(def stub "stub")

(deftest group-test
  (let [r (group "" {}
            (group "~api/users"
                   {:doc
                    {:description "A group for users routes"
                     :name "Users routes"}}
              (GET "/:id"
                   {:doc
                    {:description "Get a user"
                     :name "get-user"
                     :params {:id
                              {:type "string"
                               :description "The user id"}}}}
                 {{:keys [id] :as user} :params}
                 stub)
              (PUT "/:id"
                   {:doc
                     {:description "Update a user"
                      :name "update-user"
                      :params {:id
                                {:type "string"
                                 :description "The user id"}}}}
                 {{:keys [id] :as user} :params}
                 stub))
            
            (group "~api/posts"
                   {:doc
                    {:description "A group for posts routes"
                     :name "Posts routes"}}
              (GET "/:id"
                   {:doc
                    {:description "Get a post"
                     :name "get-post"
                     :params {:id
                               {:type "string"
                                :description "The post id"}}}}
                 {{:keys [id] :as post} :params}
                 stub))
            
            (GET "~api/ping"
                 {:doc
                  {:description "ping"
                   :name "ping"}}
               {:as request}
               stub))]
    (expect (->> r doc/generate :endpoints (map :name) vec)
     => ["Users routes" "Posts routes" "ping"])))

(deftest param-string-test
  (facts
    (doc/parameter ["X" {:type "string" :description "A string"}])
    =>
    {:Name        "X"
     :Type        "string"
     :Required    "Y"
     :Default     nil
     :Description "A string"}

    (doc/parameter ["Y" {:type "string" :description "A string" :optional true}])
    =>
    {:Name        "Y"
     :Type        "string"
     :Required    "N"
     :Default     nil
     :Description "A string"}

    (doc/parameter ["Y" {:type "string" :description "A string" :default "Toto"}])
    =>
    {:Name        "Y"
     :Type        "string"
     :Required    "Y"
     :Default     "Toto"
     :Description "A string"}))

(deftest param-bool-test
  (facts
    (doc/parameter ["X" {:type "boolean" :description "A bool" :default true}])
    =>
    {:Name        "X"
     :Type        "boolean"
     :Required    "Y"
     :Default     true
     :Description "A bool"}))

(deftest param-enumerated-test
  (facts
    (doc/parameter ["X" {:type "enumerated" :description "A string" :choices ["A" "B" "C"]}])
    =>
    {:Name            "X"
     :Type            "enumerated"
     :Required        "Y"
     :EnumeratedList  ["A" "B" "C"]
     :Default         nil
     :Description     "A string"}))


(deftest param-location-test
  (let [r (PUT "/:id"
               {:doc
                {:description "Update a post"
                 :name "update-post"
                 :params {:id {:type "string" :description "The post id"}
                          :m {:type "string" :descrption "message" :location :json}}}}
               {{:keys [id] :as post} :params}
               stub)]
  (facts
    (doc/json-params (-> r endpoint/doc :params vec))
    =>
    [[:m {:location :json, :type "string", :descrption "message"}]]
    
    (doc/query-params (-> r endpoint/doc :params vec))
    =>
    [[:id {:type "string", :description "The post id"}]])))
