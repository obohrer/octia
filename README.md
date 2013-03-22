# octia

A clojure library on top of compojure/ring to easily define api endpoints.

Supports iodocs generation.

Why octia ?
* enable specific wrappers per endpoint
* wrappers can be generated using endpoint definition
* documentation can be included in endpoint definition
* iodocs can be automatically generated from your request handler

## Installation

With leiningen :
```clojure
[octia "0.0.2"]
```

## Usage

```clojure
(use 'octia.core)

(group "api/" {:wrappers [json-output-wrapper]}
  (group "users"
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
       {{:keys [id]} :params}
       stub)
    (PUT "/:id"
         {:wrappers [authenticated]
          :doc
           {:description "Update a user"
            :name "update-user"
            :params {:id
                      {:type "string"
                       :description "The user id"}}}}
       {{:keys [id]} :params}
       stub))

  (group "posts"
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
       {{:keys [id]} :params}
       stub)))
```

## Leiningen plugin
lein-octia can be used to generate documentation using a leiningen command :

```clojure
[lein-octia "0.0.1"]
```
In project.clj :
```clojure
:octia {:routes my-ns/routes-symbol}
```

```
lein octia doc
```
