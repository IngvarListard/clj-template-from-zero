(ns hello-clj.middleware
  (:require [ring.middleware.defaults :as ring-defaults]))

(defn- add-app-component
  "Middleware to add your application component into the request. Use
  the same qualified keyword in your controller to retrieve it."
  [handler application]
  (fn [req]
    (handler (assoc req :application/component application))))

(defn my-middleware
  "This middleware runs for every request and can execute before/after logic.

  If the handler returns an HTTP response (like a redirect), we're done.
  Else we use the result of the handler to render an HTML page."
  [handler]
  (fn [req]
    (let [resp (handler req)]
      resp)))

(defn middleware-stack
  "Given the application component and middleware, return a standard stack of
  Ring middleware for a web application."
  [app-component app-middleware]
  (fn [handler]
    (-> handler
        (app-middleware)
        (add-app-component app-component)
        (ring-defaults/wrap-defaults (-> ring-defaults/site-defaults
                                         ;; disable XSRF for now
                                         (assoc-in [:security :anti-forgery] false)
                                         ;; support load balancers
                                         (assoc-in [:proxy] true))))))
