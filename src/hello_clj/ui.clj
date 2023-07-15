(ns hello-clj.ui
  (:require [clojure.java.io :as io]))

(defn css-path []
  (if-some [f (io/file (io/resource "public/css/main.css"))]
    (str "/css/main.css?t=" (.lastModified f))
    "/css/main.css"))

(defn g-fonts
  [families]
  [:link {:href (apply str "https://fonts.googleapis.com/css2?display=swap"
                       (for [f families]
                         (str "&family=" f)))
          :rel "stylesheet"}])

(defn base-html
  [{:base/keys [title
                description
                lang
                image
                icon
                url
                canonical
                font-families
                head]}
   & contents]
  [:html
   {:lang lang
    :style {:min-height "100%"
            :height "auto"}}
   [:head
    [:title title]
    [:meta {:name "description" :content description}]
    [:meta {:content title :property "og:title"}]
    [:meta {:content description :property "og:description"}]
    (when image
      [:<>
       [:meta {:content image :property "og:image"}]
       [:meta {:content "summary_large_image" :name "twitter:card"}]])
    (when-some [url (or url canonical)]
      [:meta {:content url :property "og:url"}])
    (when-some [url (or canonical url)]
      [:link {:ref "canonical" :href url}])
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
    (when icon
      [:link {:rel "icon"
              :type "image/png"
              :sizes "16x16"
              :href icon}])
    [:meta {:charset "utf-8"}]
    (when (not-empty font-families)
      [:<>
       [:link {:href "https://fonts.googleapis.com", :rel "preconnect"}]
       [:link {:crossorigin "crossorigin",
               :href "https://fonts.gstatic.com",
               :rel "preconnect"}]
       (g-fonts font-families)])
    (into [:<>] head)]
   [:body
    {:style {:position "absolute"
             :width "100%"
             :min-height "100%"
             :display "flex"
             :flex-direction "column"}}
    contents]])

(defn base-html*
  "Wraps contents in an :html and :body element with various metadata set.

  font-families:  A collection of families to request from Google fonts (see g-fonts).
  head:           Additional Rum elements to include inside the head."
  [{:base/keys [title
                description
                lang
                image
                icon
                url
                canonical
                font-families
                head]
    :as opts}
   & contents]
  (apply base-html opts contents))

(defn base [{:keys [::recaptcha] :as ctx} & body]
  (apply
    base-html*
    (-> ctx
        (merge #:base{:title "hello world"
                      :lang "en-US"
                      :icon "/img/glider.png"
                      :description (str "hello world" " Description")
                      :image "https://clojure.org/images/clojure-logo-120b.png"})
        (update :base/head (fn [head]
                             (concat [[:link {:rel "stylesheet" :href (css-path)}]
                                      [:script {:src "https://unpkg.com/htmx.org@1.9.0"}]
                                      [:script {:src "https://unpkg.com/htmx.org/dist/ext/ws.js"}]
                                      [:script {:src "https://unpkg.com/hyperscript.org@0.9.8"}]
                                      (when recaptcha
                                        [:script {:src "https://www.google.com/recaptcha/api.js"
                                                  :async "async" :defer "defer"}])]
                                     head))))
    body))

(defn page [ctx & body]
  (base
    ctx
    [:.flex-grow]
    [:.p-3.mx-auto.max-w-screen-sm.w-full
     ;(when (bound? #'csrf/*anti-forgery-token*)
     ;  {:hx-headers (cheshire/generate-string
     ;                 {:x-csrf-token csrf/*anti-forgery-token*})})
     body]
    [:.flex-grow]
    [:.flex-grow]))