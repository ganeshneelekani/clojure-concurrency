(ns clojure-concurrency.async
  (:require [org.httpkit.client :as http]
            [clojure.core.async :refer [chan <!! >!! put! take!
                                        sliding-buffer dropping-buffer
                                        close! thread go <! >!
                                        alts! alts!! merge mult tap
                                        pub sub] :as async]
            [cheshire.core :as cheshire]))

(comment 
  
  (defn http-get [url]
    (let [c (chan)]
      (println url)
      (http/get url
                (fn [r]
                  (put! c r)))
      c))

  (defn request-and-process [nm]
    (go 
      (-> (str "http://imdbapi.poromenos.org/js/?name=%25" nm "%25")
          http-get
          <!
          :body
          (cheshire/parse-string true))))


  (<!! (request-and-process "Matrix"))

(def logging-chan (chan 24))


(defn log [& args]
  (>!! logging-chan (apply str args)))

(do (future
      (dotimes [x 100]
        (log "(...."x"....)")))
    (future
      (dotimes [x 100]
        (log "(...." x "....)"))))
(future
  (loop []
    (when-some [v (<!! logging-chan)]
      (println v)
      (recur))))
  
  ;; alts
  
  (let [c1 (chan 1)
        c2 (chan 1)]
    (>!! c1 42)
    (thread 
      (let [[v c] (alts!! [c1 c2])]
        (println "Value v " v "Value c" c))))
  
  (let [c1 (chan 1)
        c2 (chan 1)]
    (>!! c1 42)
    (>!! c2 43)
    (thread
      (let [[v c] (alts!! [c1 c2]
                          :priority true)]
        (println "Value v " v "Value c" c)
        (println "Chan 1? "(= c1 c))
        (println "Chan 2?" (= c2 c))))) 

  (let [c1 (chan 10)
        c2 (chan 10)
        cm (merge [c1 c2] 1)]
    (>!! c1 1 )
    (>!! c1 2)
    (>!! c2 3)
    (>!! c2 4)
    
    (dotimes [x 4]
      (println (<!! cm)))) 
  
  ;; Single value to multiple channel can be done by mult
  
  
  (let [c (chan 10)
        m (mult c)
        t1 (chan 10)
        t2 (chan 10)]
    (tap m t1)
    (tap m t2)
    
    (>!! c 42)
    (>!! c 43)
    
    (thread
      (dotimes [x 10]
        (println "Got from T1" (<!! t1))))
    
    (thread
      (dotimes [x 10]
        (println "Got from T2" (<!! t2)))))
  
  ;; pub sub is same as mult but provide fn to filter data
  
  (let [c (chan)
        p (pub c pos?)
        s1 (chan 10)
        s2 (chan 10)]
    
    (sub p true s1)
    (sub p false s2)
    
    (>!! c 42)
    (>!! c -42)
    (>!! c -2)
    (>!! c 2)
    
    (close! c)
    
    (thread
      (loop [] 
        (when-some [v (<!! s1)]
          (println " S1 " (<!! s1))
          (recur))))
    
    (thread
      (loop []
        (when-some [v (<!! s2)]
          (println " S2 " (<!! s2))
          (recur)))))

(let [c (chan)]
  (async/onto-chan! c (range 10))
  (<!! (async/into #{} c)))

(let [c (chan)]
  (async/onto-chan! c (range 10))
  (<!! (async/reduce + 0 c)))

 ;; simple parallel operation to channel

(let [c (chan)
      out (chan)]

  (async/onto-chan! c (range 10))
  (async/pipeline 5 out (map (fn [x]
                               (Thread/sleep 500)
                               (inc x))) c)

  (<!! (async/into [] out)))    
)

  
