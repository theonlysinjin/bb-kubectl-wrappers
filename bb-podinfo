#!/usr/bin/env bb

(ns bb-kube
  (:require
   [clojure.string :as str]
   [clojure.java.io :as io]
   [clojure.java.shell :as sh]
   [clojure.tools.cli :as tc]))

(defn clever-split [keys out]
  (for
   [l (rest (str/split out #"\n"))]
    (zipmap
     keys
     (str/split l #"\s+"))))

;; ------- ;;
;; kubectl ;;
;; ------- ;;

(defn add-pod-info [pods pod-key]
  {:namespace pod-key
   :pod-info (get pods pod-key)})

; "kubectl get pods --all-namespaces -o wide --sort-by=" {.spec.nodeName} "

(defn list-pods []
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

(defn get-pods []
  (let [pods
        (list-pods)

        grouped-pods
        (group-by :namespace pods)]

    (map
     (partial add-pod-info grouped-pods)
     (keys grouped-pods))))

;; ------- ;;
;;   git   ;;
;; ------- ;;

(defn branch-matches?
  [{:keys [namespace] :as pod}
   {:keys [] :as branch}]

  (re-matches
   (re-pattern (str ".*" namespace))
   (get branch :name)))

(defn add-branch-info [branches pod]
  (let
   [matched-branches
    (filter (partial branch-matches? pod) branches)]

    (if (pos? (count matched-branches))
      (assoc pod :branches matched-branches)
      pod)))

; git ls-remote --heads origin

(defn get-branches [repo-path]
  (println "fetching branches...")
  (let [{:keys [out]}
        (sh/with-sh-dir repo-path
          (->>
           ["git" "ls-remote" "--heads" "origin"]
           (apply sh/sh)))]

    (clever-split
     [:id :name]
     out)))


;; ------- ;;
;;  print  ;;
;; ------- ;;


(defn print-pods [{:keys [verbosity]} pods message & [sort-fn]]
  (println "")
  (println "---------------")
  (println message)
  (println "---------------")
  
  (doseq
   [{:keys [namespace pod-info branches]}
    (sort-by (if sort-fn sort-fn :namespace) pods)]

    (let
     [fn-print
      (fn []
        (println
         namespace
         "-" (count pod-info)
         "-" (set (map #(get % :age) pod-info))
         (if branches (str/join ", " (map :name branches)) "")))]

      (cond
        (> verbosity 0)
        (do
          (fn-print)
          (doseq [p (sort-by :name pod-info)]
            (println "  " (:name p) "-" (:age p) "-" (:node p))))

        :else
        (fn-print)))))

(defn single-node [{:keys [verbosity]} node pods sort-fn]
  (let
   [fn-print
    (fn []
      (println
       node
       "-" (count pods)))]

    (cond
      (> verbosity 0)
      (do
        (fn-print)
        (doseq [{:keys [namespace name age]}
                (sort-by (if sort-fn sort-fn (juxt :namespace :name :age)) pods)]
          (println "  " namespace "-" name "-" age)))

      :else
      (fn-print))))

(defn print-nodes [{:keys [verbosity] :as options} nodes message & [sort-fn]]
  (when message
    (println "")
    (println "---------------")
    (println message)
    (println "---------------"))
  
  (cond
    (map? nodes)
    (doseq
     [[node pods] nodes]
      (single-node options node pods sort-fn))

    :else
    (single-node options (get-in nodes [0 :node]) nodes sort-fn)))

;; ------- ;;
;;  -main  ;;
;; ------- ;;

(def cli-options [["-r" "--repo" ""]])

(def cli-options
  ;; An option with a required argument
  [["-r" "--repo REPO" "Pick a repo to compare it to"
    :parse-fn #(.getAbsolutePath (io/file %))]

   ["-n" "--nodes NODE" "Filter by nodes"
    :default true
    :parse-fn #(str %)]

   ["-v" nil "Verbosity level"
    :id :verbosity
    :default 0
    :update-fn inc]

   ;; A boolean option defaulting to nil
   ["-h" "--help"]])

(defn -main [& args]
  (let [{:keys [summary options arguments]}
        (tc/parse-opts args cli-options)]

    (cond
      (:help options)
      (do
        (println "Help")
        (println summary))

      :else
      (let [pods (get-pods)]
        (cond

          (:repo options)
          (let
           [branches
            (get-branches (:repo options))

            pods
            (->>
             pods
             (map (partial add-branch-info branches)))

            [with-branches
             without-branches]
            [(filter #(get % :branches) pods)
             (remove #(get % :branches) pods)]]

            (println "namespaces: " (count pods))
            (println "branches: " (count branches))

            (print-pods options with-branches "PODs with existing branches")
            (print-pods options without-branches "PODs without existing branches"))

          (:nodes options)
          (let
           [node-name (:nodes options)
            actual-pods
            (mapcat
             #(get % :pod-info)
             pods)

            by-node
            (group-by :node actual-pods)]

            (if (true? (:nodes options))
              (print-nodes options by-node "Nodes")
              (print-nodes options (get by-node node-name) (str "Node: " node-name))))


          :else
          (do
            (println "namespaces: " (count pods))
            (print-pods options pods "Running PODs")))))))


(apply -main *command-line-args*)

;;;; Scratch

(comment
  (-main "-r repo" "-n namespace"))