(ns octia.core-test
  (:require [ring.mock.request :as request]
            [octia.endpoint    :as endpoint]
            [octia.wrapper     :as wrapper])
  (:use clojure.test
        octia.core
        midje.sweet))

(unfinished wrapper-called wrapper2-called group-wrapper-called)

(defn group-wrapper
  [handler]
  (fn [req]
    (group-wrapper-called)
    (handler req)))

(defn wrapper
  [handler]
  (fn [req]
    (wrapper-called)
    (handler req)))

(defn wrapper2
  [handler]
  (fn [req]
    (wrapper2-called)
    (handler req)))

(unfinished handle-update handle-get ping handle-get-post)

(def success "success")

(deftest simple-route
  (let [r (endpoint :put
                    "/:id"
                    {:doc "XXXX"
                     :wrappers [wrapper]}
                    {{:keys [id] :as user} :params}
                    (handle-update id))]
    (expect
      (-> (request/request :put "/123") r :body)
      => success
      (fake (handle-update "123") => success)
      (fake (wrapper-called) => nil))))

(deftest method-route-put
  (let [r (PUT "/:id"
                   {:doc "XXXX"
                    :wrappers [wrapper]}
                   {{:keys [id] :as user} :params}
                   (handle-update id))]
    (expect
      (-> (request/request :put "/123") r :body)
      => success
      (fake (handle-update "123") => success)
      (fake (wrapper-called) => nil))))

(deftest method-route-get
  (let [r (GET "/:id"
               {:doc "XXXX"
                :wrappers [wrapper]}
               {{:keys [id] :as user} :params}
               (handle-get id))]
    (expect
      (-> (request/request :get "/123") r :body)
      => success
      (fake (handle-get "123") => success)
      (fake (wrapper-called) => nil))))

(deftest wrapper-test
  (let [r (GET "/:id"
               {:doc "XXXX"
                :wrappers [wrapper]}
               {{:keys [id] :as user} :params}
               (handle-get id))]
    (expect
      (-> (request/request :get "/123") r :body)
      => success
      (fake (handle-get "123") => success)
      (fake (wrapper-called) => anything))))

(deftest group-test
  (let [r (endpoints->handler
            (group "~api/users/"
                   {:doc "A group for users routes"
                    :wrappers [group-wrapper]}
              (GET ":id"
                   {:doc "XXXX"
                    :wrappers [wrapper]}
                 {{:keys [id] :as user} :params}
                 (handle-get id))
              (PUT ":id"
                   {:doc "YYYY"
                    :wrappers [wrapper]}
                 {{:keys [id] :as user} :params}
                 (handle-update id)))
            (group "~api/posts/"
                   {:doc "A group for posts routes"
                    :wrappers [group-wrapper]}
              (GET ":id"
                   {:doc "ZZZZ"
                    :wrappers [wrapper2]}
                 {{:keys [id] :as post} :params}
                 (handle-get-post id)))
            (GET "~api/ping"
                 {:doc "AAAA"}
               {:as request}
               (ping)))]
    (expect
      (-> (request/request :get "~api/users/123") r :body)
      => success
      (fake (handle-get "123") => success)
      (fake (wrapper-called) => anything)
      (fake (group-wrapper-called) => anything))
    (expect
      (-> (request/request :put "~api/users/123") r :body)
      => success
      (fake (handle-update "123") => success)
      (fake (wrapper-called) => anything)
      (fake (group-wrapper-called) => anything))
    (expect
      (-> (request/request :get "~api/posts/123") r :body)
      => success
      (fake (handle-get-post "123") => success)
      (fake (wrapper2-called) => anything)
      (fake (group-wrapper-called) => anything))
    (expect
      (-> (request/request :get "~api/ping") r :body)
      => success
      (fake (ping) => success))))

(unfinished inc-req-count)

(deftest wrapper-factory-test
  "Simple wrapper factory which produces wrappers
   which use endpoint information (path & method) to collect stats"
  (let [stat-wrapper (reify wrapper/WrapperFactory
                       (build [this endpoint]
                         (fn [handler]
                           (fn [req]
                             (inc-req-count (endpoint/method endpoint) (endpoint/path endpoint))
                             (handler req)))))
        r (GET "/:id"
               {:doc "XXXX"
                :wrappers [stat-wrapper]}
               {{:keys [id] :as user} :params}
               (handle-get id))]
    (expect
      (-> (request/request :get "/123") r :body)
      => success
      (fake (handle-get "123") => success)
      (fake (inc-req-count :get "/:id") => anything))))
