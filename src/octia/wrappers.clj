(ns octia.wrappers
  (:require [cheshire.core :as cheshire]
            [pallet.thread-expr :refer [when-not->]]
            [ring.util.request :as request])
  (:import (java.io InputStream)))

(defn wrap-json-output
  "convert the body of a response into json and add appropriate headers"
  [handler]
  (fn [request]
    (let [resp (handler request)]
      (-> resp
          (when-not-> (instance? InputStream (:body resp))
                      (update-in [:body] cheshire/generate-string)
                      (update-in [:headers] #(merge % {"Content-Type" "application/json; charset=UTF-8"})))))))

(defn- json-request?
  [req]
  (if-let [type (request/content-type req)]
    (not (empty? (re-find #"^application/(vnd.+)?json" type)))))

(defn wrap-json-body
  [handler]
  (fn [req]
    (if-let [body (and (json-request? req) (request/body-string req))]
      (let [json-body (cheshire/parse-string body true)
            req* (assoc req :json-body json-body)]
        (handler req*))
      (handler req))))
