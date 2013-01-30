(ns octia.endpoint)

(defprotocol Endpoint
  "Definition of an endpoint"
  (doc [this])
  (path ^String [this])
  (method [this])
  (sub-endpoints [this]))
