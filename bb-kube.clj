#!/usr/bin/env bb

(require '[clojure.java.shell :as sh])

(defn clever-split [keys out]
  (for
   [l (rest (str/split out #"\n"))]
    (zipmap
     keys
     (str/split l #"\s+"))))

; "kubectl get pods --all-namespaces -o wide --sort-by=" {.spec.nodeName} "

(defn get-pods []
  (println "fetching pods...")
  (let [{:keys [out]}
        (->>
         ["/usr/local/bin/kubectl" "get" "pods"
          "--all-namespaces"
          "-o" "wide"]
         (apply sh/sh))]
    
    (clever-split
     [:namespace :name :ready :status :restarts :age :ip :node :nominated-node :readiness-gates]
     out)))

; git ls-remote --heads origin

(defn get-branches []
  (println "fetching branches...")
  (let [{:keys [out]}
        (->>
         ["git" "ls-remote" "--heads" "origin"]
         (apply sh/sh))]
    
    (clever-split
     [:id :name]
     out)))

(defn in-branch [kube-namespace branch]
  (let
   [matches
    (re-matches
     (re-pattern (str ".*" kube-namespace))
     (get branch :name))]
    
    ;(println "Matches? " (str ".*" kube-namespace) (get branch :name) (count matches) matches)

    (if (>= (count matches) 1)
      {:matched true
       :namespace kube-namespace
       :branch-id (:id branch)
       :branch-name (:name branch)}
      
      {:matched false
       :namespace kube-namespace})))

(defn namespace-in-branch [branches kube-namespace]
  (set
   (map
    (partial in-branch kube-namespace)
    branches)))

(defn add-pod-info [pod-keys pods pod-branch]
  (merge
   pod-branch
   {:pod-info
    (map
     #(select-keys % pod-keys)
     (get pods (:namespace pod-branch)))}))

(defn with-pod-info [pod-keys grouped-pods matched-namespaces]
  (map
   (partial add-pod-info pod-keys grouped-pods)
   matched-namespaces))

(defn print-pods [pods message & [sort-fn]]
  (println "")
  (println "---------------")
  (println message)
  (println "---------------")

  (doseq
   [{:keys [namespace pod-info branch-name]}
    (sort-by (if sort-fn sort-fn :namespace) pods)]

    (println
     namespace
     "-" (count pod-info)
     "-" (set (map #(get % :age) pod-info))
     (if branch-name (str "- " branch-name) ""))
    
    (doseq [p (sort-by :name pod-info)]
      (println "  " (:name p) "-" (:age p)))))

(defn -main []
  (let [pods
        (get-pods)

        grouped-pods
        (group-by :namespace pods)

        kube-namespaces
        (->>
         pods
         (map (fn [pod] (get pod :namespace)))
         set)

        branches
        (->>
         (get-branches))



        create-list
        (->>
         kube-namespaces
         (mapcat (partial namespace-in-branch branches))
         (with-pod-info [:name :ip :age :node] grouped-pods))

        [with-branches without-branches]
        [(filter #(get % :matched) create-list) (remove #(get % :matched) create-list)]]

    (println "namespaces: " (count kube-namespaces))
    (println "branches: " (count branches))

    (print-pods with-branches "PODs with existing branches")
    (print-pods without-branches "PODs without existing branches")))

(-main)