(ns octia.wrapper)

(defprotocol WrapperFactory
  "A wrapper factory allow you to create wrappers
   which are sensible to endpoint definitions"
  (build [this endpoint]))

(defn ->wrapper-factory
  "Transform fn to a dummy wrapper factory
   or return wrapper factory if supplied"
  [f]
  (if (fn? f)
    (reify WrapperFactory
      (build [this# endpoint#]
        f))
    f))
