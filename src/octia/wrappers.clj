(ns octia.wrappers
  (:require [cheshire.core :as cheshire]
            [pallet.thread-expr :refer [when-not->]]))

(defn wrap-json-output
  "convert the body of a response into json and add appropriate headers"
  [handler]
  (fn [request]
    (let [resp (handler request)]
      (-> resp
          (when-not-> (instance? java.io.InputStream (:body resp))
                      (update-in [:body] cheshire/generate-string)
                      (dissoc :raw-body) ;; Remove the raw body from the response
                      (update-in [:headers] #(merge % {"Content-Type" "application/json; charset=UTF-8"})))))))

(defn- json-request?
  [req]
  (if-let [#^String type (:content-type req)]
    (not (empty? (re-find #"^application/(vnd.+)?json" type)))))

(defn wrap-json-body
  [handler]
  (fn [req]
    (if-let [body (and (json-request? req) (:body req))]
      (let [bstr (slurp body)
            json-body (cheshire/parse-string bstr true)
            req* (assoc req :json-body json-body)]
        (handler req*))
      (handler req))))
