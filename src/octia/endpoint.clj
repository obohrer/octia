(ns octia.endpoint)

(defprotocol Endpoint
  "Definition of an endpoint"
  (doc [this])
  (path ^String [this])
  (route [this])
  (method [this])
  (sub-endpoints [this]))
