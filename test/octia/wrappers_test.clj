(ns octia.wrappers-test
  (:require [midje.sweet :refer :all]
            [octia.wrappers :refer :all]
            [ring.mock.request :as ring]
            [octia.core :refer [POST group]]
            [cheshire.core :as cheshire]
            [compojure.handler :as handler]
            [clojure.test :refer [deftest]]))

(defn map-map
  "map a function over a clojure map"
  [f m]
  (zipmap (keys m) (map f (vals m))))

(def simple-route
  (group "/json" {}
         (POST "/in"
               {:wrappers [wrap-json-body]}
               {urls :json-body}
               {:body urls})
         (POST "/out"
               {:wrappers [wrap-json-output]}
               {}
               {:body {:a "b"}})
         (POST "/both"
               {:wrappers [wrap-json-body wrap-json-output]}
               {urls :json-body}
               {:body (map-map clojure.string/upper-case urls)})))

(defn request
  [uri body content-type]
  ((handler/api simple-route)
   (-> (ring/request :post uri)
       (ring/body body)
       (ring/content-type content-type))))

(deftest json-body
  (fact "json string request body is deserialized into clojure map"
        (let [resp (request "/json/in" (cheshire/generate-string {:a "b" :c "d"}) "application/json")]
          (:body resp) => {:a "b" :c "d"}))
  (fact "json string is not deserialized if content type is missing"
        (let [resp (request "/json/in" (cheshire/generate-string {:a "b" :c "d"}) nil)]
          (:body resp) => nil))
  (fact "json string is not deserialized if content type is wrong"
        (let [resp (request "/json/in" (cheshire/generate-string {:a "b" :c "d"}) "application/xml")]
          (:body resp) => nil)))

(deftest json-output
  (fact "clojure map is serialized into json response"
        (let [resp (request "/json/out" "" "")]
          (:body resp) => "{\"a\":\"b\"}"
          (:headers resp) => {"Content-Type" "application/json; charset=UTF-8"})))

(deftest json-both
  (fact "json is deserialized and serialized"
        (let [resp (request "/json/both" (cheshire/generate-string {:a "b" :c "d"}) "application/json")]
          (:body resp) => "{\"c\":\"D\",\"a\":\"B\"}"
          (:headers resp) => {"Content-Type" "application/json; charset=UTF-8"})))

