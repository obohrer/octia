(ns octia.doc-test
  (:require [octia.doc :as doc])
  (:use clojure.test
        octia.core
        midje.sweet))

(def stub "stub")

(deftest group-test
  (let [r (endpoints->handler
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
               stub)
               )]
    (doc/generate r)))