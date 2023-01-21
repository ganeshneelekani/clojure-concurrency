(ns clojure-concurrency.core
  (:require [clojure.core.async :refer [chan <!! >!! put! take!
                                        sliding-buffer
                                        dropping-buffer
                                        close!
                                        thread
                                        go
                                        <!
                                        >!]])
  (:gen-class))

#_(let [c (chan)]
  (future (>!! c 42))
  (future (println (<!! c))))

#_(let [c (chan)]
  (future (dotimes [x 10]
            (>!! c x)))
  (future (dotimes [x 10]
            (>!! c x)))
  (future (dotimes [x 20]
            (println (<!! c)))))

#_(let [c (chan)]
  (put! c 42 (fn [v]
               (println "sent "v)))
  (take! c (fn [v]
             (println "recieved "v))))


#_(let [c (chan 3)]
  @(future
    (dotimes [x 3]
      (>!! c x)
      (println " Sent "x)))
  (future
    (dotimes [x 3] 
      (println " Got "  (<!! c )))))

#_(let [c (chan (dropping-buffer 2))]
  @(future
    (dotimes [x 3]
      (>!! c x)
      (println " Sent " x))
    (println " done sent"))
  (future
    (dotimes [x 3]
      (println " Got "  (<!! c)))
    (println "done Got")))

;; Sliding Buffer
#_(let [c (chan (sliding-buffer 2))]
  @(future
     (dotimes [x 3]
       (>!! c x)
       (println " Sent " x))
     (println " done sent"))
  (future
    (dotimes [x 3]
      (println " Got "  (<!! c)))
    (println "done Got")))


#_(let [c (chan)]
  (future
    (dotimes [x 2]
      (>!! c x)
      (println " sent "x))
    (close! c))
  (println " Sent ")
  (future 
    (loop []
      (when-some [v (<!! c)]
        (println "Got "v)
        (recur)))
    (println " exiting ")))

#_(thread 42)

#_(<!! (thread 
       (let [t1 (thread "thread 1")
             t2 (thread "thread 2")]
         [(<!! t1) (<!! t2)])))

#_(let [c (chan)]
  (thread 
    (dotimes [x 3]
      (>!! c x)
      (println "Put : "x)))
  (thread 
    (dotimes [x 3] 
      (println "Take : " (<!! c)))))

(go 42)

(let [c (chan)]
  (go
     (dotimes [x 3]
       (>! c x)
       (println " Put " x)))
  (go
    (dotimes [x 3]
      (println " Take "  (<! c)))))

